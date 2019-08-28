package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
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
}