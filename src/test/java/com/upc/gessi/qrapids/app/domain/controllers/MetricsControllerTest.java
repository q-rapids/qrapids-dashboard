package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAMetrics qmaMetrics;

    @InjectMocks
    private MetricsController metricsController;

    @Before
    public void setUp () {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getAllMetricsCurrentEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String projectExternalId = "test";
        when(qmaMetrics.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getAllMetricsCurrentEvaluation(projectExternalId);

        // Then
        assertEquals(dtoMetricList.size(), dtoMetricListFound.size());
        assertEquals(dtoMetric, dtoMetricListFound.get(0));
    }

    @Test
    public void getSingleMetricCurrentEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        String projectExternalId = "test";
        when(qmaMetrics.SingleCurrentEvaluation(dtoMetric.getId(), projectExternalId)).thenReturn(dtoMetric);

        // When
        DTOMetric dtoMetricFound = metricsController.getSingleMetricCurrentEvaluation(dtoMetric.getId(), projectExternalId);

        // Then
        assertEquals(dtoMetric, dtoMetricFound);
    }

    @Test
    public void getMetricsForQualityFactorCurrentEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String projectExternalId = "test";
        String factorId = "testingperformance";
        when(qmaMetrics.CurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);

        // Then
        assertEquals(dtoMetricList.size(), dtoMetricListFound.size());
        assertEquals(dtoMetric, dtoMetricListFound.get(0));
    }

    @Test
    public void getAllMetricsHistoricalEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.HistoricalData(null, from, to, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getAllMetricsHistoricalEvaluation(projectExternalId, from, to);

        // Then
        assertEquals(dtoMetricList.size(), dtoMetricListFound.size());
        assertEquals(dtoMetric, dtoMetricListFound.get(0));
    }
}