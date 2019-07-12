package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectsTest {

    private MockMvc mockMvc;

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
                .get("/api/assessedProjects");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is(project1)))
                .andExpect(jsonPath("$[1]", is(project2)))
                .andExpect(jsonPath("$[2]", is(project3)));

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
                .get("/api/assessedProjects");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());

        // Verify mock interactions
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);
    }

    @Test
    public void getProjectsWithReadError() throws Exception {
        when(qmaProjects.getAssessedProjects()).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/assessedProjects");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());

        // Verify mock interactions
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);
    }
}