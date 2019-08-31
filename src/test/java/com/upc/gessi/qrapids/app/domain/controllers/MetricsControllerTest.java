package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.domain.repositories.MetricCategory.MetricCategoryRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetric;
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
    public void getSingleMetricHistoricalEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.SingleHistoricalData(dtoMetric.getId(), from, to, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getSingleMetricHistoricalEvaluation(dtoMetric.getId(), projectExternalId, from, to);

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

    @Test
    public void getMetricsForQualityFactorHistoricalEvaluation() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        LocalDate from = LocalDate.parse("2019-08-01");
        LocalDate to = LocalDate.parse("2019-08-31");
        when(qmaMetrics.HistoricalData(factorId, from, to, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, from, to);

        // Then
        assertEquals(dtoMetricList.size(), dtoMetricListFound.size());
        assertEquals(dtoMetric, dtoMetricListFound.get(0));
    }

    @Test
    public void getMetricsPrediction() throws IOException {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        dtoMetric.setDatasource("Forecast");
        dtoMetric.setRationale("Forecast");
        float first80 = 0.97473043f;
        float second80 = 0.9745246f;
        Pair<Float, Float> confidence80 = Pair.of(first80, second80);
        dtoMetric.setConfidence80(confidence80);
        float first95 = 0.9747849f;
        float second95 = 0.97447014f;
        Pair<Float, Float> confidence95 = Pair.of(first95, second95);
        dtoMetric.setConfidence95(confidence95);
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String projectExternalId = "test";
        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(qmaForecast.ForecastMetric(dtoMetricList, technique, freq, horizon, projectExternalId)).thenReturn(dtoMetricList);

        // When
        List<DTOMetric> dtoMetricListFound = metricsController.getMetricsPrediction(dtoMetricList, projectExternalId, technique, freq, horizon);

        // Then
        assertEquals(dtoMetricList.size(), dtoMetricListFound.size());
        assertEquals(dtoMetric, dtoMetricListFound.get(0));
    }
}