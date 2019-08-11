package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.AlertType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AlertsControllerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertsController alertsController;

    @Test
    public void getAlerts() throws ProjectNotFoundException {
        // Given
        // Project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Alerts setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert = new Alert(idElement, name, alertType, value, threshold, category, date, alertStatus, true, project);
        alert.setId(alertId);

        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);
        when(alertRepository.findByProject_IdOrderByDateDesc(projectId)).thenReturn(alertList);

        // When
        List<Alert> alertsFound = alertsController.getAlerts(projectExternalId);

        // Then
        int expectedNumberOfAlertsFound = 1;
        assertEquals(expectedNumberOfAlertsFound, alertsFound.size());
        assertEquals(alert, alertsFound.get(0));
    }

    @Test(expected = ProjectNotFoundException.class)
    public void getAlertsProjectNotFound() throws ProjectNotFoundException {
        // Given
        String projectExternalId = "missingProject";
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(null);

        // Throws
        alertsController.getAlerts(projectExternalId);
    }

    @Test
    public void setViewedStatusForAlerts() {
        // Given
        // Project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        // Alerts setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert = new Alert(idElement, name, alertType, value, threshold, category, date, alertStatus, true, project);
        alert.setId(alertId);

        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);

        // When
        alertsController.setViewedStatusForAlerts(alertList);

        // Then
        List<Long> alertIds = new ArrayList<>();
        alertIds.add(alertId);
        verify(alertRepository, times(1)).setViewedStatusFor(alertIds);
    }

    @Test
    public void countNewAlerts() throws ProjectNotFoundException {
        // Given
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // alerts setup
        Long newAlerts = 2L;
        Long newAlertWithQR = 1L;
        when(alertRepository.countByProject_IdAndStatus(projectId, AlertStatus.NEW)).thenReturn(newAlerts);
        when(alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW)).thenReturn(newAlertWithQR);

        // When
        Pair<Long, Long> newAlertsFound = alertsController.countNewAlerts(projectExternalId);

        // Then
        assertEquals(newAlerts, newAlertsFound.getFirst());
        assertEquals(newAlertWithQR, newAlertsFound.getSecond());

        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(alertRepository, times(1)).countByProject_IdAndStatus(projectId, AlertStatus.NEW);
        verify(alertRepository, times(1)).countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW);
        verifyNoMoreInteractions(alertRepository);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void countNewAlertsProjectNotFound () throws ProjectNotFoundException {
        // Given
        String projectExternalId = "missingProject";
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(null);

        // Throws
        alertsController.countNewAlerts(projectExternalId);
    }
}