package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AlertsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertsController alertsController;

    @Before
    public void setUp () {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getAlertById() throws AlertNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();

        // Alerts setup
        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertRepository.findById(alert.getId())).thenReturn(Optional.of(alert));

        // When
        Alert alertFound = alertsController.getAlertById(alert.getId());

        // Then
        assertEquals(alert, alertFound);
    }

    @Test(expected = AlertNotFoundException.class)
    public void getAlertByIdNotFound () throws AlertNotFoundException {
        // Given
        long alertId = 1L;
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // Throw
        Alert alertFound = alertsController.getAlertById(alertId);
    }

    @Test
    public void getAlerts() throws ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectRepository.findByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);
        when(alertRepository.findByProject_IdOrderByDateDesc(project.getId())).thenReturn(alertList);

        // When
        List<Alert> alertsFound = alertsController.getAlerts(project.getExternalId());

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
        Project project = domainObjectsBuilder.buildProject();

        // Alerts setup
        Alert alert = domainObjectsBuilder.buildAlert(project);
        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);

        // When
        alertsController.setViewedStatusForAlerts(alertList);

        // Then
        List<Long> alertIds = new ArrayList<>();
        alertIds.add(alert.getId());
        verify(alertRepository, times(1)).setViewedStatusFor(alertIds);
    }

    @Test
    public void countNewAlerts() throws ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        String projectExternalId = project.getExternalId();
        Long projectId = project.getId();
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