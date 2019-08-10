package com.upc.gessi.qrapids.app.domain.repositories.Project;

import com.upc.gessi.qrapids.app.domain.models.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ComponentScan("com.upc.gessi.qrapids.app.database.repositories")
@DataJpaTest
public class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    public void findByExternalId () {
        // Given
        String project1ExternalId = "project1";
        String project1Name = "Project 1";
        String project1Description = "test project";
        Project project1 = new Project(project1ExternalId, project1Name, project1Description, null, true);
        entityManager.persist(project1);

        String project2ExternalId = "project2";
        String project2Name = "Project 2";
        String project2Description = "test project";
        Project project2 = new Project(project2ExternalId, project2Name, project2Description, null, true);
        entityManager.persistAndFlush(project2);

        // When
        Project projectFound = projectRepository.findByExternalId(project1ExternalId);

        // Then
        assertEquals(project1, projectFound);
    }

    @Test
    public void findByName () {
        // Given
        String project1ExternalId = "project1";
        String project1Name = "Project 1";
        String project1Description = "test project";
        Project project1 = new Project(project1ExternalId, project1Name, project1Description, null, true);
        entityManager.persist(project1);

        String project2ExternalId = "project2";
        String project2Name = "Project 2";
        String project2Description = "test project";
        Project project2 = new Project(project2ExternalId, project2Name, project2Description, null, true);
        entityManager.persistAndFlush(project2);

        // When
        Project projectFound = projectRepository.findByName(project1Name);

        // Then
        assertEquals(project1, projectFound);
    }

}