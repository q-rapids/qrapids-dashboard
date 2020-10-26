package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedFactorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
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
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FactorEvaluationControllerTest {

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
    private FactorsController factorsController;

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
        List<QFCategory> factorCategoryListFound = factorsController.getFactorCategories();

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
        factorsController.newFactorCategories(categories);

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
        factorsController.newFactorCategories(categories);
    }

    @Test
    public void getSingleFactorEvaluation() throws IOException {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        String projectExternalId = "test";
        when(qmaQualityFactors.SingleCurrentEvaluation(dtoFactorEvaluation.getId(), projectExternalId)).thenReturn(dtoFactorEvaluation);

        // When
        DTOFactorEvaluation dtoFactorEvaluationFound = factorsController.getSingleFactorEvaluation(dtoFactorEvaluation.getId(), projectExternalId);

        // Then
        assertEquals(dtoFactorEvaluation, dtoFactorEvaluationFound);
    }

    @Test
    public void getAllFactorsEvaluation() throws IOException {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        String projectExternalId = "test";
        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorEvaluationList);

        // When
        List<DTOFactorEvaluation> dtoFactorEvaluationListFound = factorsController.getAllFactorsEvaluation(projectExternalId);

        // Then
        assertEquals(dtoFactorEvaluationList.size(), dtoFactorEvaluationListFound.size());
        assertEquals(dtoFactorEvaluation, dtoFactorEvaluationListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsCurrentEvaluation() throws IOException {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);
        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(null, projectExternalId, true)).thenReturn(dtoDetailedFactorEvaluationList);

        // When
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationListFound = factorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, true);

        // Then
        assertEquals(dtoDetailedFactorEvaluationList.size(), dtoDetailedFactorEvaluationListFound.size());
        assertEquals(dtoDetailedFactorEvaluation, dtoDetailedFactorEvaluationListFound.get(0));
    }

    @Test
    public void getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation() throws IOException {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);
        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(strategicIndicatorId, projectExternalId, true)).thenReturn(dtoDetailedFactorEvaluationList);

        // When
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationListFound = factorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId, true);

        // Then
        assertEquals(dtoDetailedFactorEvaluationList.size(), dtoDetailedFactorEvaluationListFound.size());
        assertEquals(dtoDetailedFactorEvaluation, dtoDetailedFactorEvaluationListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsHistoricalEvaluation() throws IOException {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);
        String projectExternalId = "test";
        LocalDate from = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate();
        when(qmaQualityFactors.HistoricalData(null, from, to, projectExternalId)).thenReturn(dtoDetailedFactorEvaluationList);

        // When
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationListFound = factorsController.getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, from, to);

        // Then
        assertEquals(dtoDetailedFactorEvaluationList.size(), dtoDetailedFactorEvaluationListFound.size());
        assertEquals(dtoDetailedFactorEvaluation, dtoDetailedFactorEvaluationListFound.get(0));
    }

    @Test
    public void getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation() throws IOException {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);
        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        LocalDate from = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate();
        when(qmaQualityFactors.HistoricalData(strategicIndicatorId, from, to, projectExternalId)).thenReturn(dtoDetailedFactorEvaluationList);

        // When
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationListFound = factorsController.getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(strategicIndicatorId, projectExternalId, from, to);

        // Then
        assertEquals(dtoDetailedFactorEvaluationList.size(), dtoDetailedFactorEvaluationListFound.size());
        assertEquals(dtoDetailedFactorEvaluation, dtoDetailedFactorEvaluationListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsPrediction() throws IOException {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluationCurrentEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> currentEvaluation = new ArrayList<>();
        currentEvaluation.add(dtoDetailedFactorEvaluationCurrentEvaluation);

        DTODetailedFactorEvaluation dtoDetailedFactorEvaluationPrediction = domainObjectsBuilder.buildDTOQualityFactorForPrediction();
        List<DTODetailedFactorEvaluation> prediction = new ArrayList<>();
        prediction.add(dtoDetailedFactorEvaluationPrediction);
        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";
        String projectExternalId = "test";
        when(qmaForecast.ForecastDetailedFactor(currentEvaluation, technique, freq, horizon, projectExternalId)).thenReturn(prediction);

        // When
        List<DTODetailedFactorEvaluation> predictionFound = factorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(prediction.size(), predictionFound.size());
        assertEquals(dtoDetailedFactorEvaluationPrediction, predictionFound.get(0));
    }

    @Test
    public void simulate() throws IOException {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);

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

        when(qmaSimulation.simulateQualityFactors(metricsMap, projectExternalId, date)).thenReturn(dtoFactorEvaluationList);

        // When
        List<DTOFactorEvaluation> factorsSimulationList = factorsController.simulate(metricsMap, projectExternalId, date);

        // Then
        assertEquals(dtoFactorEvaluationList.size(), factorsSimulationList.size());
        assertEquals(dtoFactorEvaluation, factorsSimulationList.get(0));
    }

    @Test
    public void getFactorLabelFromValueGood() {
        // Given
        List<QFCategory> qfCategoryList = domainObjectsBuilder.buildFactorCategoryList();
        Collections.reverse(qfCategoryList);
        when(factorCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(qfCategoryList);
        float value = 0.8f;

        // When
        String label = factorsController.getFactorLabelFromValue(value);

        // Then
        String expectedLabel = "Good";
        assertEquals(expectedLabel, label);
    }

    @Test
    public void getFactorLabelFromValueNeutral() {
        // Given
        List<QFCategory> qfCategoryList = domainObjectsBuilder.buildFactorCategoryList();
        Collections.reverse(qfCategoryList);
        when(factorCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(qfCategoryList);
        float value = 0.5f;

        // When
        String label = factorsController.getFactorLabelFromValue(value);

        // Then
        String expectedLabel = "Neutral";
        assertEquals(expectedLabel, label);
    }

    @Test
    public void getFactorLabelFromValueBad() {
        // Given
        List<QFCategory> qfCategoryList = domainObjectsBuilder.buildFactorCategoryList();
        Collections.reverse(qfCategoryList);
        when(factorCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(qfCategoryList);
        float value = 0.2f;

        // When
        String label = factorsController.getFactorLabelFromValue(value);

        // Then
        String expectedLabel = "Bad";
        assertEquals(expectedLabel, label);
    }
}