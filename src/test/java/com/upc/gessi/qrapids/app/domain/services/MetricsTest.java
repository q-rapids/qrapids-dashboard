package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetricsTest {

    private MockMvc mockMvc;

    @Mock
    QMAMetrics qmaMetrics;

    @Mock
    Forecast forecast;

    @InjectMocks
    private Metrics metricsController;

    private String projectExternalId;
    DTOMetric dtoMetric;
    private List<DTOMetric> dtoMetricList = new ArrayList<>();

    private Double getFloatAsDouble(Float fValue) {
        return Double.valueOf(fValue.toString());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(metricsController)
                .build();

        projectExternalId = "test";

        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        dtoMetric = new DTOMetric(metricId, metricName, metricDescription, null, metricRationale, evaluationDate, metricValue.floatValue());
        dtoMetricList.add(dtoMetric);
    }

    @After
    public void tearDown() {
        dtoMetricList = new ArrayList<>();
    }


    @Test
    public void getMetricsEvaluations() throws Exception {
        when(qmaMetrics.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).CurrentEvaluation(null, projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getSingleMetricEvaluation() throws Exception {
        when(qmaMetrics.SingleCurrentEvaluation(dtoMetric.getId(), projectExternalId)).thenReturn(dtoMetric);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/" + dtoMetric.getId() + "/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$.name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$.description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$.value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$.value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$.date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$.date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(nullValue())))
                .andExpect(jsonPath("$.rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$.confidence80", is(nullValue())))
                .andExpect(jsonPath("$.confidence95", is(nullValue())))
                .andExpect(jsonPath("$.forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).SingleCurrentEvaluation(dtoMetric.getId(), projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getMetricsEvaluation() throws Exception {
        String factorId = "testingperformance";
        when(qmaMetrics.CurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/CurrentEvaluation/" + factorId)
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).CurrentEvaluation(factorId, projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getMetricsHistoricalData() throws Exception {
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(qmaMetrics.HistoricalData(null, LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).HistoricalData(null, LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getMetricsHistoricalData1() throws Exception {
        String factorId = "testingperformance";
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(qmaMetrics.HistoricalData(factorId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/HistoricalData/" + factorId)
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).HistoricalData(factorId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getHistoricalDataForMetric() throws Exception {
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(qmaMetrics.SingleHistoricalData(dtoMetric.getId(), LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/" + dtoMetric.getId() + "/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(qmaMetrics, times(1)).SingleHistoricalData(dtoMetric.getId(), LocalDate.parse(dateFrom), LocalDate.parse(dateTo), projectExternalId);
        verifyNoMoreInteractions(qmaMetrics);
    }

    @Test
    public void getMetricsPredicitionData() throws Exception {
        dtoMetric.setDatasource("Forecast");
        dtoMetric.setRationale("Forecast");
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetric.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetric.setConfidence95(confidence95);

        String factorId = "testingperformance";
        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(forecast.ForecastMetric(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/PredictionData/" + factorId)
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastMetric(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getMetricsPredicitionData1() throws Exception {
        dtoMetric.setDatasource("Forecast");
        dtoMetric.setRationale("Forecast");
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetric.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetric.setConfidence95(confidence95);

        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(forecast.ForecastMetric(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Metrics/PredictionData/")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastMetric(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }
}