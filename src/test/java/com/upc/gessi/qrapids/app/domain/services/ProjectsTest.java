package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectsTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private QMAProjects qmaProjects;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private Projects projectsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(projectsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
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
        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        when(projectRepository.findByExternalId(any(String.class))).thenReturn(null);

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
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(projectRepository, times(3)).findByExternalId(any(String.class));
        verify(projectRepository, times(3)).save(any(Project.class));
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    public void getProjectsCategoriesConflict() throws Exception {
        when(qmaProjects.getAssessedProjects()).thenThrow(new CategoriesException());

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
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);
    }

    @Test
    public void getProjectsWithReadError() throws Exception {
        when(qmaProjects.getAssessedProjects()).thenThrow(new IOException());

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
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);
    }
}