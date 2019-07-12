package com.upc.gessi.qrapids.app.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;

import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FactorsServiceTest {

    private MockMvc mockMvc;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @Mock
    private QMASimulation qmaSimulation;

    @Mock
    private Forecast forecast;

    @Mock
    private QFCategoryRepository qfCategoryRepository;

    @InjectMocks
    private FactorsService factorsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(factorsController)
                .build();
    }

    @Test
    public void getQualityFactorsEvaluations() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, null, metricRationale, evaluationDate, metricValue.floatValue());
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).CurrentEvaluation(null, projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getSingleFactorEvaluation() throws Exception {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String factorDescription = "Performance of the tests";
        Double factorValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        DTOFactor dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String projectExternalId = "test";
        when(qmaQualityFactors.SingleCurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoFactor);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/" + factorId + "/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(factorId)))
                .andExpect(jsonPath("$.name", is(factorName)))
                .andExpect(jsonPath("$.description", is(factorDescription)))
                .andExpect(jsonPath("$.value", is(factorValue)))
                .andExpect(jsonPath("$.value_description", is(String.format("%.2f", factorValue))))
                .andExpect(jsonPath("$.date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$.date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(nullValue())))
                .andExpect(jsonPath("$.rationale", is(factorRationale)))
                .andExpect(jsonPath("$.forecastingError", is(nullValue())))
                .andExpect(jsonPath("$.strategicIndicators[0]", is(strategicIndicatorsList.get(0))))
                .andExpect(jsonPath("$.formattedDate", is(evaluationDate.toString())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).SingleCurrentEvaluation(factorId, projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getQualityFactorsEvaluationsForOneStrategicIndicator() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, null, metricRationale, evaluationDate, metricValue.floatValue());
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        String strategicIndicatorId = "processperformance";
        when(qmaQualityFactors.CurrentEvaluation(strategicIndicatorId, projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/CurrentEvaluation/" + strategicIndicatorId)
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).CurrentEvaluation(strategicIndicatorId, projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getQualityFactorsHistoricalData() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, null, metricRationale, evaluationDate, metricValue.floatValue());
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        String from = evaluationDate.minusDays(7).toString();
        String to = evaluationDate.toString();
        when(qmaQualityFactors.HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getQualityFactorsHistoricalDataForOneStrategicIndicator() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, null, metricRationale, evaluationDate, metricValue.floatValue());
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        String from = evaluationDate.minusDays(7).toString();
        String to = evaluationDate.toString();
        when(qmaQualityFactors.HistoricalData(strategicIndicatorId, LocalDate.parse(from), LocalDate.parse(to), projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/HistoricalData/" + strategicIndicatorId)
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).HistoricalData(strategicIndicatorId, LocalDate.parse(from), LocalDate.parse(to), projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getAllQualityFactors() throws Exception {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String factorDescription = "Performance of the tests";
        Double factorValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        DTOFactor dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        String projectExternalId = "test";
        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/getAll")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].description", is(factorDescription)))
                .andExpect(jsonPath("$[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", factorValue))))
                .andExpect(jsonPath("$[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(factorRationale)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(strategicIndicatorsList.get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(evaluationDate.toString())));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getQualityFactorsCategories() {

    }

    @Test
    public void getQualityFactorsPredicitionData() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        String metricDataSource = "Forecast";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "Forecast";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, metricDataSource, metricRationale, evaluationDate, metricValue.floatValue());
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetric.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetric.setConfidence95(confidence95);
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        String horizon = "7";
        String technique = "PROPHET";
        when(forecast.ForecastFactor(anyList(), eq(technique), eq("7"), eq(horizon), eq(projectExternalId))).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/PredictionData")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(metricDataSource)))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastFactor(anyList(), eq(technique), eq("7"), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getQualityFactorsPredicitionDataForOneStrategicIndicator() throws Exception {
        // Factor setup
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        String metricDataSource = "Forecast";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "Forecast";
        DTOMetric dtoMetric = new DTOMetric(metricId, metricName, metricDescription, metricDataSource, metricRationale, evaluationDate, metricValue.floatValue());
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetric.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetric.setConfidence95(confidence95);
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        DTOQualityFactor dtoQualityFactor = new DTOQualityFactor(factorId, factorName, dtoMetricList);
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        String horizon = "7";
        String technique = "PROPHET";
        when(forecast.ForecastFactor(anyList(), eq(technique), eq("7"), eq(horizon), eq(projectExternalId))).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/QualityFactors/PredictionData/" + strategicIndicatorId)
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].metrics[0].name", is(metricName)))
                .andExpect(jsonPath("$[0].metrics[0].description", is(metricDescription)))
                .andExpect(jsonPath("$[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", metricValue))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(metricDataSource)))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(metricRationale)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastFactor(anyList(), eq(technique), eq("7"), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void simulate() throws Exception {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String factorDescription = "Performance of the tests";
        Double factorValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        DTOFactor dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        String projectExternalId = "test";
        String metricId = "fasttests";
        Float metricValue = 0.7f;
        String date = "2019-07-07";
        Map<String, String> metric = new HashMap<>();
        metric.put("id", metricId);
        metric.put("value", metricValue.toString());
        List<Map<String, String>> metricList = new ArrayList<>();
        metricList.add(metric);

        Map<String, Float> metricsMap = new HashMap<>();
        metricsMap.put(metricId, metricValue);

        when(qmaSimulation.simulateQualityFactors(metricsMap, projectExternalId, LocalDate.parse(date))).thenReturn(dtoFactorList);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(metricList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/QualityFactors/Simulate")
                .param("prj", projectExternalId)
                .param("date", date)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].description", is(factorDescription)))
                .andExpect(jsonPath("$[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", factorValue))))
                .andExpect(jsonPath("$[0].date[0]", is(evaluationDate.getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(evaluationDate.getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(evaluationDate.getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(factorRationale)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(strategicIndicatorsList.get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(evaluationDate.toString())));

        // Verify mock interactions
        verify(qmaSimulation, times(1)).simulateQualityFactors(metricsMap, projectExternalId, LocalDate.parse(date));
        verifyNoMoreInteractions(qmaSimulation);
    }
}