package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private QMAProjects qmaProjects;

    @Mock
    private Backlog backlog;

    @InjectMocks
    private ProjectsController projectsController;

    @Before
    public void setUp () {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void findProjectByExternalId() throws ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectRepository.findByExternalId(project.getExternalId())).thenReturn(project);

        // When
        Project projectFound = projectsController.findProjectByExternalId(project.getExternalId());

        // Then
        assertEquals(project, projectFound);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void findProjectByExternalIdNotFound() throws ProjectNotFoundException {
        String projectExternalId = "missingProject";
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(null);

        // Throw
        projectsController.findProjectByExternalId(projectExternalId);
    }


    @Test
    public void importProjectsAndUpdateDatabase() throws IOException, CategoriesException {
        // Given
        List<String> projectsList = new ArrayList<>();
        String project1 = "project1";
        projectsList.add(project1);
        String project2 = "project2";
        projectsList.add(project2);
        String project3 = "project3";
        projectsList.add(project3);
        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);
        when(projectRepository.findByExternalId(any(String.class))).thenReturn(null);

        // When
        List<String> projectsListFound = projectsController.importProjectsAndUpdateDatabase();

        // Then
        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(projectRepository, times(3)).findByExternalId(any(String.class));
        verify(projectRepository, times(3)).save(any(Project.class));
        verifyNoMoreInteractions(projectRepository);
    }

    @Test(expected = CategoriesException.class)
    public void importProjectsCategoriesException() throws IOException, CategoriesException {
        // Given
        when(qmaProjects.getAssessedProjects()).thenThrow(new CategoriesException());

        // Throw
        projectsController.importProjectsAndUpdateDatabase();
    }

    @Test(expected = IOException.class)
    public void importProjectsReadError() throws IOException, CategoriesException {
        // Given
        when(qmaProjects.getAssessedProjects()).thenThrow(new IOException());

        // Throw
        projectsController.importProjectsAndUpdateDatabase();
    }

    @Test
    public void getMilestonesForProject() throws ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectRepository.findByExternalId(project.getExternalId())).thenReturn(project);
        LocalDate now = LocalDate.now();

        List<DTOMilestone> dtoMilestoneList = domainObjectsBuilder.buildDTOMilestoneList();
        when(backlog.getMilestones(project.getBacklogId(), now)).thenReturn(dtoMilestoneList);

        // When
        List<DTOMilestone> dtoMilestoneListFound = projectsController.getMilestonesForProject(project.getExternalId(), now);
        assertEquals(dtoMilestoneList, dtoMilestoneListFound);
    }
}