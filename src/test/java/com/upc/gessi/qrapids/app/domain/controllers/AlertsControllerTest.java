package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.domain.repositories.Metric.MetricRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AlertsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private QualityFactorRepository factorRepository;

    @Mock
    private QRPatternsController qrPatternsController;

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
    public void getAlerts() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);
        when(alertRepository.findByProject_IdOrderByDateDesc(project.getId())).thenReturn(alertList);

        // When
        List<Alert> alertsFound = alertsController.getAlerts(project);

        // Then
        int expectedNumberOfAlertsFound = 1;
        assertEquals(expectedNumberOfAlertsFound, alertsFound.size());
        assertEquals(alert, alertsFound.get(0));
    }

    @Test
    public void setViewedStatusForAlerts() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
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
    public void countNewAlerts() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Long projectId = project.getId();

        // alerts setup
        Long newAlerts = 2L;
        Long newAlertWithQR = 1L;
        when(alertRepository.countByProject_IdAndStatus(projectId, AlertStatus.NEW)).thenReturn(newAlerts);
        when(alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW)).thenReturn(newAlertWithQR);

        // When
        Pair<Long, Long> newAlertsFound = alertsController.countNewAlerts(project);

        // Then
        assertEquals(newAlerts, newAlertsFound.getFirst());
        assertEquals(newAlertWithQR, newAlertsFound.getSecond());

        verify(alertRepository, times(1)).countByProject_IdAndStatus(projectId, AlertStatus.NEW);
        verify(alertRepository, times(1)).countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW);
        verifyNoMoreInteractions(alertRepository);
    }

    @Test
    public void createAlert() {
        // Given
        String id = "duplication";
        String name = "Duplication";
        AlertType type = AlertType.METRIC;
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "duplication";
        Project project = domainObjectsBuilder.buildProject();
        when(qrPatternsController.existsPatternForAlert(any(Alert.class))).thenReturn(true);

        // When
        alertsController.createAlert(id, name, type, value, threshold, category, project);

        // Then
        verify(qrPatternsController, times(1)).existsPatternForAlert(any(Alert.class));

        ArgumentCaptor<Alert> alertArgumentCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(alertArgumentCaptor.capture());
        Alert alertSaved = alertArgumentCaptor.getValue();
        assertEquals(id, alertSaved.getId_element());
        assertEquals(name, alertSaved.getName());
        assertEquals(type, alertSaved.getType());
        assertEquals(value, alertSaved.getValue(), 0f);
        assertEquals(threshold, alertSaved.getThreshold(), 0f);
        assertEquals(category, alertSaved.getCategory());
        assertEquals(AlertStatus.NEW, alertSaved.getStatus());
        assertTrue(alertSaved.isReqAssociat());
        assertEquals(project, alertSaved.getProject());
    }

    @Test
    public void checkMetricAlert(){
        // Given
        String externalId = "duplication";
        float value = 0.4f;
        String projectExternalId = "test";

        Project project = domainObjectsBuilder.buildProject();
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        Metric metric = domainObjectsBuilder.buildMetric(project);
        when(metricRepository.findByExternalIdAndProjectId(externalId, project.getId())).thenReturn(metric);

        // When
        alertsController.checkMetricAlert(externalId, value, projectExternalId);

        // Then
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verify(metricRepository, times(1)).findByExternalIdAndProjectId(externalId, project.getId());

        ArgumentCaptor<Alert> alertArgumentCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(alertArgumentCaptor.capture());
        Alert alertSaved = alertArgumentCaptor.getValue();
        assertEquals(externalId, alertSaved.getId_element());
        assertEquals(metric.getName(), alertSaved.getName());
        assertEquals(AlertType.METRIC, alertSaved.getType());
        assertEquals(value, alertSaved.getValue(), 0f);
        assertEquals(metric.getThreshold(), alertSaved.getThreshold(), 0f);
        assertEquals(externalId, alertSaved.getCategory());
        assertEquals(AlertStatus.NEW, alertSaved.getStatus());
        assertFalse(alertSaved.isReqAssociat());
        assertEquals(project, alertSaved.getProject());
    }

    @Test
    public void checkFactorAlert(){
        // Given
        String externalId = "codequality";
        float value = 0.15f;
        String projectExternalId = "test";

        Project project = domainObjectsBuilder.buildProject();
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        Factor factor = domainObjectsBuilder.buildFactor(project);
        when(factorRepository.findByExternalIdAndProjectId(externalId, project.getId())).thenReturn(factor);

        // When
        alertsController.checkFactorAlert(externalId, value, projectExternalId);

        // Then
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verify(factorRepository, times(1)).findByExternalIdAndProjectId(externalId, project.getId());

        ArgumentCaptor<Alert> alertArgumentCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(alertArgumentCaptor.capture());
        Alert alertSaved = alertArgumentCaptor.getValue();
        assertEquals(externalId, alertSaved.getId_element());
        assertEquals(factor.getName(), alertSaved.getName());
        assertEquals(AlertType.FACTOR, alertSaved.getType());
        assertEquals(value, alertSaved.getValue(), 0f);
        assertEquals(factor.getThreshold(), alertSaved.getThreshold(), 0f);
        assertEquals(externalId, alertSaved.getCategory());
        assertEquals(AlertStatus.NEW, alertSaved.getStatus());
        assertFalse(alertSaved.isReqAssociat());
        assertEquals(project, alertSaved.getProject());
    }

}