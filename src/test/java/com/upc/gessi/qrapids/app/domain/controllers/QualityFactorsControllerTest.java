package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QualityFactorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @Mock
    private Forecast qmaForecast;

    @Mock
    private QMASimulation qmaSimulation;

    @InjectMocks
    private QualityFactorsController qualityFactorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getSingleFactorEvaluation() throws IOException {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        String projectExternalId = "test";
        when(qmaQualityFactors.SingleCurrentEvaluation(dtoFactor.getId(), projectExternalId)).thenReturn(dtoFactor);

        // When
        DTOFactor dtoFactorFound = qualityFactorsController.getSingleFactorEvaluation(dtoFactor.getId(), projectExternalId);

        // Then
        assertEquals(dtoFactor, dtoFactorFound);
    }

    @Test
    public void getAllFactorsEvaluation() throws IOException {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        String projectExternalId = "test";
        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // When
        List<DTOFactor> dtoFactorListFound = qualityFactorsController.getAllFactorsEvaluation(projectExternalId);

        // Then
        assertEquals(dtoFactorList.size(), dtoFactorListFound.size());
        assertEquals(dtoFactor, dtoFactorListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsCurrentEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId);

        // Then
        assertEquals(dtoQualityFactorList.size(), dtoQualityFactorListFound.size());
        assertEquals(dtoQualityFactor, dtoQualityFactorListFound.get(0));
    }

    @Test
    public void getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(strategicIndicatorId, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId);

        // Then
        assertEquals(dtoQualityFactorList.size(), dtoQualityFactorListFound.size());
        assertEquals(dtoQualityFactor, dtoQualityFactorListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsHistoricalEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String projectExternalId = "test";
        LocalDate from = dtoQualityFactor.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoQualityFactor.getMetrics().get(0).getDate();
        when(qmaQualityFactors.HistoricalData(null, from, to, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, from, to);

        // Then
        assertEquals(dtoQualityFactorList.size(), dtoQualityFactorListFound.size());
        assertEquals(dtoQualityFactor, dtoQualityFactorListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsPrediction() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactorCurrentEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> currentEvaluation = new ArrayList<>();
        currentEvaluation.add(dtoQualityFactorCurrentEvaluation);

        DTOQualityFactor dtoQualityFactorPrediction = domainObjectsBuilder.buildDTOQualityFactorForPrediction();
        List<DTOQualityFactor> prediction = new ArrayList<>();
        prediction.add(dtoQualityFactorPrediction);
        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";
        String projectExternalId = "test";
        when(qmaForecast.ForecastFactor(currentEvaluation, technique, freq, horizon, projectExternalId)).thenReturn(prediction);

        // When
        List<DTOQualityFactor> predictionFound = qualityFactorsController.getAllFactorsWithMetricsPrediction(currentEvaluation, technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(prediction.size(), predictionFound.size());
        assertEquals(dtoQualityFactorPrediction, predictionFound.get(0));
    }

    @Test
    public void simulate() throws IOException {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        String projectExternalId = "test";
        String metricId = "fasttests";
        Float metricValue = 0.7f;
        LocalDate date = LocalDate.parse("2019-07-07");
        Map<String, String> metric = new HashMap<>();
        metric.put("id", metricId);
        metric.put("value", metricValue.toString());
        List<Map<String, String>> metricList = new ArrayList<>();
        metricList.add(metric);

        Map<String, Float> metricsMap = new HashMap<>();
        metricsMap.put(metricId, metricValue);

        when(qmaSimulation.simulateQualityFactors(metricsMap, projectExternalId, date)).thenReturn(dtoFactorList);

        // When
        List<DTOFactor> factorsSimulationList = qualityFactorsController.simulate(metricsMap, projectExternalId, date);

        // Then
        assertEquals(dtoFactorList.size(), factorsSimulationList.size());
        assertEquals(dtoFactor, factorsSimulationList.get(0));
    }
}