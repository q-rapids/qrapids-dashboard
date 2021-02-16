package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.domain.repositories.MetricCategory.MetricCategoryRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetricEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetricsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAMetrics qmaMetrics;

    @Mock
    private Forecast qmaForecast;

    @Mock
    private MetricCategoryRepository metricCategoryRepository;

    @InjectMocks
    private MetricsController metricsController;

    @Before
    public void setUp () {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getMetricCategories() {
        // Given
        List<MetricCategory> metricCategoryList = domainObjectsBuilder.buildMetricCategoryList();
        when(metricCategoryRepository.findAll()).thenReturn(metricCategoryList);

        // When
        List<MetricCategory> metricCategoryListFound = metricsController.getMetricCategories();

        // Then
        assertEquals(metricCategoryList.size(), metricCategoryListFound.size());
        assertEquals(metricCategoryList.get(0), metricCategoryListFound.get(0));
        assertEquals(metricCategoryList.get(1), metricCategoryListFound.get(1));
        assertEquals(metricCategoryList.get(2), metricCategoryListFound.get(2));
    }

    @Test
    public void newMetricCategories() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawMetricCategoryList();

        // When
        metricsController.newMetricCategories(categories);

        // Then
        verify(metricCategoryRepository, times(1)).deleteAll();

        ArgumentCaptor<MetricCategory> metricCategoryArgumentCaptor = ArgumentCaptor.forClass(MetricCategory.class);
        verify(metricCategoryRepository, times(3)).save(metricCategoryArgumentCaptor.capture());
        List<MetricCategory> metricCategoryListSaved = metricCategoryArgumentCaptor.getAllValues();
        assertEquals(categories.get(0).get("name"), metricCategoryListSaved.get(0).getName());
        assertEquals(categories.get(0).get("color"), metricCategoryListSaved.get(0).getColor());
        assertEquals(Float.parseFloat(categories.get(0).get("upperThreshold")) / 100f, metricCategoryListSaved.get(0).getUpperThreshold(), 0f);
        assertEquals(categories.get(1).get("name"), metricCategoryListSaved.get(1).getName());
        assertEquals(categories.get(1).get("color"), metricCategoryListSaved.get(1).getColor());
        assertEquals(Float.parseFloat(categories.get(1).get("upperThreshold")) / 100f, metricCategoryListSaved.get(1).getUpperThreshold(), 0f);
        assertEquals(categories.get(2).get("name"), metricCategoryListSaved.get(2).getName());
        assertEquals(categories.get(2).get("color"), metricCategoryListSaved.get(2).getColor());
        assertEquals(Float.parseFloat(categories.get(2).get("upperThreshold")) / 100f, metricCategoryListSaved.get(2).getUpperThreshold(), 0f);
    }

    @Test(expected = CategoriesException.class)
    public void newMetricCategoriesNotEnough() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawMetricCategoryList();
        categories.remove(2);
        categories.remove(1);

        // Throw
        metricsController.newMetricCategories(categories);
    }

    @Test
    public void getAllMetricsCurrentEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String projectExternalId = "test";
        when(qmaMetrics.CurrentEvaluation(null, projectExternalId, null)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getAllMetricsCurrentEvaluation(projectExternalId, null);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }

    @Test
    public void getSingleMetricCurrentEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        String projectExternalId = "test";
        when(qmaMetrics.SingleCurrentEvaluation(dtoMetricEvaluation.getId(), projectExternalId)).thenReturn(dtoMetricEvaluation);

        // When
        DTOMetricEvaluation dtoMetricEvaluationFound = metricsController.getSingleMetricCurrentEvaluation(dtoMetricEvaluation.getId(), projectExternalId);

        // Then
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationFound);
    }

    @Test
    public void getMetricsForQualityFactorCurrentEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String projectExternalId = "test";
        String factorId = "testingperformance";
        when(qmaMetrics.CurrentEvaluation(factorId, projectExternalId, null)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }

    @Test
    public void getSingleMetricHistoricalEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.SingleHistoricalData(dtoMetricEvaluation.getId(), from, to, projectExternalId, null)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getSingleMetricHistoricalEvaluation(dtoMetricEvaluation.getId(), projectExternalId, null, from, to);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }

    @Test
    public void getAllMetricsHistoricalEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.HistoricalData(null, from, to, projectExternalId, null)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getAllMetricsHistoricalEvaluation(projectExternalId,null, from, to);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }

    @Test
    public void getMetricsForQualityFactorHistoricalEvaluation() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.HistoricalData(factorId, from, to, projectExternalId, null)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, from, to);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }

    @Test
    public void getMetricsPrediction() throws IOException {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        dtoMetricEvaluation.setDatasource("Forecast");
        dtoMetricEvaluation.setRationale("Forecast");
        float first80 = 0.97473043f;
        float second80 = 0.9745246f;
        Pair<Float, Float> confidence80 = Pair.of(first80, second80);
        dtoMetricEvaluation.setConfidence80(confidence80);
        float first95 = 0.9747849f;
        float second95 = 0.97447014f;
        Pair<Float, Float> confidence95 = Pair.of(first95, second95);
        dtoMetricEvaluation.setConfidence95(confidence95);
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);

        String projectExternalId = "test";
        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(qmaForecast.ForecastMetric(dtoMetricEvaluationList, technique, freq, horizon, projectExternalId)).thenReturn(dtoMetricEvaluationList);

        // When
        List<DTOMetricEvaluation> dtoMetricEvaluationListFound = metricsController.getMetricsPrediction(dtoMetricEvaluationList, projectExternalId, technique, freq, horizon);

        // Then
        assertEquals(dtoMetricEvaluationList.size(), dtoMetricEvaluationListFound.size());
        assertEquals(dtoMetricEvaluation, dtoMetricEvaluationListFound.get(0));
    }
}