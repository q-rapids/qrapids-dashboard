package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectsControllerTest {

    DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private ProjectRepository projectRepository;

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
}