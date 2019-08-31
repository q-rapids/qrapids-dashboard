package com.upc.gessi.qrapids.app.domain.repositories.Alert;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.AlertType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AlertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    public void findByProject_IdOrderByDateDesc() {
    }

    @Test
    public void findAlertById() {
        // Given
        Alert alert = new Alert();
        entityManager.persistAndFlush(alert);

        // When
        Optional<Alert> alertFound = alertRepository.findById(alert.getId());

        // Then
        assertTrue(alertFound.isPresent());
        assertEquals(alert.getId(), alertFound.get().getId());
    }

    @Test
    public void setViewedStatusFor() {
        // Given
        Alert alertNew = new Alert();
        alertNew.setStatus(AlertStatus.NEW);
        entityManager.persist(alertNew);
        Alert alertResolved = new Alert();
        alertResolved.setStatus(AlertStatus.RESOLVED);
        entityManager.persistAndFlush(alertResolved);

        List<Long> alertIds = new ArrayList<>();
        alertIds.add(alertNew.getId());

        // When
        alertRepository.setViewedStatusFor(alertIds);

        // Then
        alertNew = entityManager.find(Alert.class, alertNew.getId());
        assertEquals(alertNew.getStatus(), AlertStatus.VIEWED);

        alertResolved = entityManager.find(Alert.class, alertResolved.getId());
        assertEquals(alertResolved.getStatus(), AlertStatus.RESOLVED);
    }

    @Test
    public void countByProject_IdAndStatus() {
        // Given
        Project project1 = new Project("test", "Test", null, null, true);
        entityManager.persist(project1);

        Alert alert1 = new Alert();
        alert1.setProject(project1);
        alert1.setStatus(AlertStatus.NEW);
        entityManager.persist(alert1);

        Alert alert2 = new Alert();
        alert2.setProject(project1);
        alert2.setStatus(AlertStatus.VIEWED);
        entityManager.persist(alert2);

        Project project2 = new Project("test2", "Test 2", null, null, true);
        entityManager.persist(project2);

        Alert alert3 = new Alert();
        alert3.setProject(project2);
        alert3.setStatus(AlertStatus.RESOLVED);
        entityManager.persistAndFlush(alert3);

        // When
        Long numberOfAlerts = alertRepository.countByProject_IdAndStatus(project1.getId(), AlertStatus.NEW);

        // Then
        Long expectedNumberOfAlerts = 1L;
        assertEquals(expectedNumberOfAlerts, numberOfAlerts);
    }

    @Test
    public void countByProject_IdAndReqAssociatIsTrueAndStatusEquals() {
        // Given
        Project project1 = new Project("test", "Test", null, null, true);
        entityManager.persist(project1);

        Alert alert1 = new Alert();
        alert1.setProject(project1);
        alert1.setStatus(AlertStatus.NEW);
        alert1.setReqAssociat(false);
        entityManager.persist(alert1);

        Alert alert2 = new Alert();
        alert2.setProject(project1);
        alert2.setStatus(AlertStatus.NEW);
        alert2.setReqAssociat(true);
        entityManager.persist(alert2);

        Alert alert3 = new Alert();
        alert3.setProject(project1);
        alert3.setStatus(AlertStatus.VIEWED);
        alert3.setReqAssociat(true);
        entityManager.persist(alert3);

        Project project2 = new Project("test2", "Test 2", null, null, true);
        entityManager.persist(project2);

        Alert alert4 = new Alert();
        alert4.setProject(project2);
        alert4.setStatus(AlertStatus.NEW);
        alert4.setReqAssociat(true);
        entityManager.persistAndFlush(alert4);

        // When
        Long numberOfAlerts = alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project1.getId(), AlertStatus.NEW);

        // Then
        Long expectedNumberOfAlerts = 1L;
        assertEquals(expectedNumberOfAlerts, numberOfAlerts);
    }

    @Test
    public void getAlerts () {
        Project project1 = new Project("test", "Test", null, null, true);
        project1 = entityManager.persist(project1);

        Alert alert1 = new Alert("duplication", "Duplication", AlertType.METRIC, 0.4f, 0.5f, "duplication", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persist(alert1);

        Alert alert2 = new Alert("testingperformance", "Testing Performance", AlertType.METRIC, 0.6f, 0.8f, "testingperformance", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persist(alert2);

        Alert alert3 = new Alert("comments", "Comments", AlertType.METRIC, 0.3f, 0.5f, "comments", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persistAndFlush(alert3);

        List<Alert> alertList = alertRepository.findAll();

        int expectedNumberOfAlerts = 3;
        assertEquals(expectedNumberOfAlerts, alertList.size());
    }

    @Test
    public void getAlertsByName () {
        Project project1 = new Project("test", "Test", null, null, true);
        entityManager.persist(project1);

        Alert alert1 = new Alert("duplication", "Duplication", AlertType.METRIC, 0.4f, 0.5f, "duplication", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persist(alert1);

        Alert alert2 = new Alert("testingperformance", "Testing Performance", AlertType.METRIC, 0.6f, 0.8f, "testingperformance", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persist(alert2);

        Alert alert3 = new Alert("comments", "Comments", AlertType.METRIC, 0.3f, 0.5f, "comments", new Date(), AlertStatus.NEW, true, project1);
        entityManager.persistAndFlush(alert3);

        List<Alert> alertList = alertRepository.findAlertByName("Duplication");

        int expectedNumberOfAlerts = 1;
        assertEquals(expectedNumberOfAlerts, alertList.size());

        Alert alertFound = alertList.get(0);
        assertEquals(alert1, alertFound);
    }
}