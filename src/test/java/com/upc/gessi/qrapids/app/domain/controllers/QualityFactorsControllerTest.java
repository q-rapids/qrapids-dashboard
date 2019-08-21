package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QualityFactorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @Mock
    private Forecast qmaForecast;

    @Mock
    private QMASimulation qmaSimulation;

    @Mock
    private QFCategoryRepository factorCategoryRepository;

    @InjectMocks
    private QualityFactorsController qualityFactorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getFactorCategories() {
        // Given
        List<QFCategory> factorCategoryList = domainObjectsBuilder.buildFactorCategoryList();
        when(factorCategoryRepository.findAll()).thenReturn(factorCategoryList);

        // When
        List<QFCategory> factorCategoryListFound = qualityFactorsController.getFactorCategories();

        // Then
        assertEquals(factorCategoryList.size(), factorCategoryListFound.size());
        assertEquals(factorCategoryList.get(0), factorCategoryListFound.get(0));
        assertEquals(factorCategoryList.get(1), factorCategoryListFound.get(1));
        assertEquals(factorCategoryList.get(2), factorCategoryListFound.get(2));
    }

    @Test
    public void newFactorCategories() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawFactorCategoryList();

        // When
        qualityFactorsController.newFactorCategories(categories);

        // Then
        verify(factorCategoryRepository, times(1)).deleteAll();

        ArgumentCaptor<QFCategory> factorCategoryArgumentCaptor = ArgumentCaptor.forClass(QFCategory.class);
        verify(factorCategoryRepository, times(3)).save(factorCategoryArgumentCaptor.capture());
        List<QFCategory> factorCategoryListSaved = factorCategoryArgumentCaptor.getAllValues();
        assertEquals(categories.get(0).get("name"), factorCategoryListSaved.get(0).getName());
        assertEquals(categories.get(0).get("color"), factorCategoryListSaved.get(0).getColor());
        assertEquals(Float.parseFloat(categories.get(0).get("upperThreshold")) / 100f, factorCategoryListSaved.get(0).getUpperThreshold(), 0f);
        assertEquals(categories.get(1).get("name"), factorCategoryListSaved.get(1).getName());
        assertEquals(categories.get(1).get("color"), factorCategoryListSaved.get(1).getColor());
        assertEquals(Float.parseFloat(categories.get(1).get("upperThreshold")) / 100f, factorCategoryListSaved.get(1).getUpperThreshold(), 0f);
        assertEquals(categories.get(2).get("name"), factorCategoryListSaved.get(2).getName());
        assertEquals(categories.get(2).get("color"), factorCategoryListSaved.get(2).getColor());
        assertEquals(Float.parseFloat(categories.get(2).get("upperThreshold")) / 100f, factorCategoryListSaved.get(2).getUpperThreshold(), 0f);
    }

    @Test(expected = CategoriesException.class)
    public void newFactorCategoriesNotEnough() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawSICategoryList();
        categories.remove(2);
        categories.remove(1);

        // Throw
        qualityFactorsController.newFactorCategories(categories);
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
    public void getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        LocalDate from = dtoQualityFactor.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoQualityFactor.getMetrics().get(0).getDate();
        when(qmaQualityFactors.HistoricalData(strategicIndicatorId, from, to, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(strategicIndicatorId, projectExternalId, from, to);

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
        List<DTOQualityFactor> predictionFound = qualityFactorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, freq, horizon, projectExternalId);

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