package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.QrapidsApplication;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ProjectsController projectsDomainController;

    @InjectMocks
    private Projects projectsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(projectsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getProjectsAndUpdateDB() throws Exception {
        List<String> projectsList = new ArrayList<>();
        String project1 = "project1";
        projectsList.add(project1);
        String project2 = "project2";
        projectsList.add(project2);
        String project3 = "project3";
        projectsList.add(project3);
        when(projectsController.importProjects()).thenReturn(projectsList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects/import");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is(project1)))
                .andExpect(jsonPath("$[1]", is(project2)))
                .andExpect(jsonPath("$[2]", is(project3)))
                .andDo(document("projects/import",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]")
                                        .description("List with the external identifiers of all the assessed projects"))
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).importProjectsAndUpdateDatabase();
    }

    @Test
    public void getProjectsCategoriesConflict() throws Exception {
        when(projectsDomainController.importProjectsAndUpdateDatabase()).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects/import");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(status().reason(is("The categories do not match")))
                .andDo(document("projects/import-conflict",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).importProjectsAndUpdateDatabase();
    }

    @Test
    public void getProjectsWithReadError() throws Exception {
        when(projectsDomainController.importProjectsAndUpdateDatabase()).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects/import");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason(is("Error on ElasticSearch connection")))
                .andDo(document("projects/import-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).importProjectsAndUpdateDatabase();
    }

    @Test
    public void getProjects() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        when(projectsDomainController.getProjects(null)).thenReturn(dtoProjectList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$[0].name", is(projectName)))
                .andExpect(jsonPath("$[0].description", is(projectDescription)))
                .andExpect(jsonPath("$[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].active", is(active)))
                .andExpect(jsonPath("$[0].backlogId", is(projectBacklogId)))
                .andDo(document("projects/all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Project identifier"),
                                fieldWithPath("[].externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("[].name")
                                        .description("Project name"),
                                fieldWithPath("[].description")
                                        .description("Project description"),
                                fieldWithPath("[].logo")
                                        .description("Project logo file"),
                                fieldWithPath("[].active")
                                        .description("Is an active project?"),
                                fieldWithPath("[].backlogId")
                                        .description("Project identifier in the backlog"))
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).getProjects(null);
        verifyNoMoreInteractions(projectsDomainController);
    }

    @Test
    public void getProjectsByProfile() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);
        Long profileID = 1L;

        when(projectsDomainController.getProjects(profileID)).thenReturn(dtoProjectList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects")
                .param("profile_id", String.valueOf(profileID));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$[0].name", is(projectName)))
                .andExpect(jsonPath("$[0].description", is(projectDescription)))
                .andExpect(jsonPath("$[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].active", is(active)))
                .andExpect(jsonPath("$[0].backlogId", is(projectBacklogId)))
                .andDo(document("profile/projects/all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Project identifier"),
                                fieldWithPath("[].externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("[].name")
                                        .description("Project name"),
                                fieldWithPath("[].description")
                                        .description("Project description"),
                                fieldWithPath("[].logo")
                                        .description("Project logo file"),
                                fieldWithPath("[].active")
                                        .description("Is an active project?"),
                                fieldWithPath("[].backlogId")
                                        .description("Project identifier in the backlog"))
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).getProjects(profileID);
        verifyNoMoreInteractions(projectsDomainController);
    }

    @Test
    public void updateProject() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "999";
        // getResource() : The name of a resource is a '/'-separated path name that identifies the resource.
        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + "/" + "icons" + "/" + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, logoMultipartFile.getBytes(), true, projectBacklogId);

        when(projectsDomainController.checkProjectByName(projectId, projectName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/projects/{id}", projectId)
                .file(logoMultipartFile)
                .param("externalId", projectExternalId)
                .param("name", projectName)
                .param("description", projectDescription)
                .param("backlogId", projectBacklogId)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("projects/update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("externalId")
                                        .description("Project external identifier"),
                                parameterWithName("name")
                                        .description("Project name"),
                                parameterWithName("description")
                                        .description("Project description"),
                                parameterWithName("backlogId")
                                        .description("Project identifier in the backlog")),
                        requestParts(
                                partWithName("logo")
                                        .description("Project logo file")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).checkProjectByName(projectId, projectName);

        ArgumentCaptor<DTOProject> argument = ArgumentCaptor.forClass(DTOProject.class);
        verify(projectsDomainController, times(1)).updateProject(argument.capture());
        assertEquals(dtoProject.getId(), argument.getValue().getId());
        assertEquals(dtoProject.getExternalId(), argument.getValue().getExternalId());
        assertEquals(dtoProject.getName(), argument.getValue().getName());
        assertEquals(dtoProject.getDescription(), argument.getValue().getDescription());
        assertEquals(dtoProject.getActive(), argument.getValue().getActive());
        assertEquals(dtoProject.getExternalId(), argument.getValue().getExternalId());

        verifyNoMoreInteractions(projectsDomainController);
    }

    @Test
    public void updateProjectNameAlreadyExists() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "999";
// getResource() : The name of a resource is a '/'-separated path name that identifies the resource.
        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + "/" + "icons" + "/" + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(projectsDomainController.checkProjectByName(projectId, projectName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/projects/{id}", projectId)
                .file(logoMultipartFile)
                .param("externalId", projectExternalId)
                .param("name", projectName)
                .param("description", projectDescription)
                .param("backlogId", projectBacklogId)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("projects/update-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).checkProjectByName(projectId, projectName);

        verifyNoMoreInteractions(projectsDomainController);
    }

    @Test
    public void getProjectById() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);

        when(projectsDomainController.getProjectById(projectId.toString())).thenReturn(dtoProject);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/projects/{id}", projectId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(projectId.intValue())))
                .andExpect(jsonPath("$.externalId", is(projectExternalId)))
                .andExpect(jsonPath("$.name", is(projectName)))
                .andExpect(jsonPath("$.description", is(projectDescription)))
                .andExpect(jsonPath("$.logo", is(nullValue())))
                .andExpect(jsonPath("$.active", is(active)))
                .andExpect(jsonPath("$.backlogId", is(projectBacklogId)))
                .andDo(document("projects/single",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Project identifier")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Project identifier"),
                                fieldWithPath("externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("name")
                                        .description("Project name"),
                                fieldWithPath("description")
                                        .description("Project description"),
                                fieldWithPath("logo")
                                        .description("Project logo file"),
                                fieldWithPath("active")
                                        .description("Is an active project?"),
                                fieldWithPath("backlogId")
                                        .description("Project identifier in the backlog"))
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).getProjectById(projectId.toString());
        verifyNoMoreInteractions(projectsDomainController);
    }

    @Test
    public void getNextMilestones () throws Exception {
        // Given
        String projectExternalId = "test";
        List<DTOMilestone> milestoneList = domainObjectsBuilder.buildDTOMilestoneList();
        LocalDate now = LocalDate.now();

        when(projectsDomainController.getMilestonesForProject(eq(projectExternalId), eq(now))).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId)
                .param("date", now.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(milestoneList.get(0).getDate())))
                .andExpect(jsonPath("$[0].name", is(milestoneList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(milestoneList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].type", is(milestoneList.get(0).getType())))
                .andDo(document("milestones/get-from-date",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Minimum milestone date (yyyy-mm-dd)")
                        ),
                        responseFields(
                                fieldWithPath("[].date")
                                        .description("Milestone date"),
                                fieldWithPath("[].name")
                                        .description("Milestone name"),
                                fieldWithPath("[].description")
                                        .description("Milestone description"),
                                fieldWithPath("[].type")
                                        .description("Milestone type"))
                ));
    }

    @Test
    public void getAllMilestones () throws Exception {
        // Given
        String projectExternalId = "test";
        List<DTOMilestone> milestoneList = domainObjectsBuilder.buildDTOMilestoneList();

        when(projectsDomainController.getMilestonesForProject(projectExternalId, null)).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(milestoneList.get(0).getDate())))
                .andExpect(jsonPath("$[0].name", is(milestoneList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(milestoneList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].type", is(milestoneList.get(0).getType())));
    }

    @Test
    public void getAllPhases () throws Exception {
        // Given
        String projectExternalId = "test";
        List<DTOPhase> phaseList = domainObjectsBuilder.buildDTOPhaseList();

        when(projectsDomainController.getPhasesForProject(projectExternalId, null)).thenReturn(phaseList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/phases")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dateFrom", is(phaseList.get(0).getDateFrom())))
                .andExpect(jsonPath("$[0].name", is(phaseList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(phaseList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].dateTo", is(phaseList.get(0).getDateTo())))
                .andDo(document("phases/get-from-date",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Minimum phase date (yyyy-mm-dd)")
                        ),
                        responseFields(
                                fieldWithPath("[].dateFrom")
                                        .description("Phase from date"),
                                fieldWithPath("[].name")
                                        .description("Phase name"),
                                fieldWithPath("[].description")
                                        .description("Phase description"),
                                fieldWithPath("[].dateTo")
                                        .description("Phase to date"))
                ));
    }
}