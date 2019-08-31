package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetric;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import com.upc.gessi.qrapids.app.testHelpers.HelperFunctions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetricsTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private MetricsController metricsDomainController;

    @InjectMocks
    private Metrics metricsController;

    private String projectExternalId;
    private DTOMetric dtoMetric;
    private List<DTOMetric> dtoMetricList = new ArrayList<>();
    private List<MetricCategory> metricCategoryList = new ArrayList<>();
    private List<Map<String, String>> metricRawCategoriesList = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(metricsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();

        DomainObjectsBuilder domainObjectsBuilder = new DomainObjectsBuilder();

        projectExternalId = "test";
        dtoMetric = domainObjectsBuilder.buildDTOMetric();
        dtoMetricList.add(dtoMetric);
        metricCategoryList = domainObjectsBuilder.buildMetricCategoryList();
        metricRawCategoriesList = domainObjectsBuilder.buildRawMetricCategoryList();
    }

    @After
    public void tearDown() {
        dtoMetricList = new ArrayList<>();
    }

    @Test
    public void getMetricsCategories () throws Exception {
        // Given
        when(metricsDomainController.getMetricCategories()).thenReturn(metricCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/metrics/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(metricCategoryList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(metricCategoryList.get(0).getName())))
                .andExpect(jsonPath("$[0].color", is(metricCategoryList.get(0).getColor())))
                .andExpect(jsonPath("$[0].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricCategoryList.get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[1].id", is(metricCategoryList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(metricCategoryList.get(1).getName())))
                .andExpect(jsonPath("$[1].color", is(metricCategoryList.get(1).getColor())))
                .andExpect(jsonPath("$[1].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricCategoryList.get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[2].id", is(metricCategoryList.get(2).getId().intValue())))
                .andExpect(jsonPath("$[2].name", is(metricCategoryList.get(2).getName())))
                .andExpect(jsonPath("$[2].color", is(metricCategoryList.get(2).getColor())))
                .andExpect(jsonPath("$[2].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricCategoryList.get(2).getUpperThreshold()))))
                .andDo(document("metrics/categories",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Category identifier"),
                                fieldWithPath("[].name")
                                        .description("Category name"),
                                fieldWithPath("[].color")
                                        .description("Category hexadecimal color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Category upper threshold")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getMetricCategories();
    }

    @Test
    public void newMetricsCategories () throws Exception {
        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/metrics/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(metricRawCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("metrics/categories-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].name")
                                        .description("Metrics category name"),
                                fieldWithPath("[].color")
                                        .description("Metrics category color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Metrics category upper threshold"))
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).newMetricCategories(metricRawCategoriesList);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void newMetricsCategoriesNotEnough () throws Exception {
        // Give
        metricRawCategoriesList.remove(2);
        metricRawCategoriesList.remove(1);
        doThrow(new CategoriesException()).when(metricsDomainController).newMetricCategories(metricRawCategoriesList);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/metrics/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(metricRawCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("Not enough categories")))
                .andDo(document("metrics/categories-new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).newMetricCategories(metricRawCategoriesList);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsEvaluations() throws Exception {
        // Given
        when(metricsDomainController.getAllMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/metrics/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andDo(document("metrics/current",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getAllMetricsCurrentEvaluation(projectExternalId);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getSingleMetricEvaluation() throws Exception {
        // Given
        when(metricsDomainController.getSingleMetricCurrentEvaluation(dtoMetric.getId(), projectExternalId)).thenReturn(dtoMetric);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/metrics/{id}/current", dtoMetric.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$.name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$.description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$.value", is(HelperFunctions.getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$.value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$.date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$.date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(nullValue())))
                .andExpect(jsonPath("$.rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$.confidence80", is(nullValue())))
                .andExpect(jsonPath("$.confidence95", is(nullValue())))
                .andExpect(jsonPath("$.forecastingError", is(nullValue())))
                .andDo(document("metrics/single",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Metric identifier")),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Metric identifier"),
                                fieldWithPath("name")
                                        .description("Metric name"),
                                fieldWithPath("description")
                                        .description("Metric description"),
                                fieldWithPath("value")
                                        .description("Metric value"),
                                fieldWithPath("value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getSingleMetricCurrentEvaluation(dtoMetric.getId(), projectExternalId);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsHistoricalData() throws Exception {
        // Given
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(metricsDomainController.getAllMetricsHistoricalEvaluation(projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/metrics/historical")
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andDo(document("metrics/historical",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("from")
                                        .description("Starting date (yyyy-mm-dd) for the requested the period"),
                                parameterWithName("to")
                                        .description("Ending date (yyyy-mm-dd) for the requested the period")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getAllMetricsHistoricalEvaluation(projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getHistoricalDataForMetric() throws Exception {
        // Given
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(metricsDomainController.getSingleMetricHistoricalEvaluation(dtoMetric.getId(), projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/metrics/{id}/historical", dtoMetric.getId())
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoMetric.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoMetric.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoMetric.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoMetric.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoMetric.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andDo(document("metrics/single-historical",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Metric identifier")),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("from")
                                        .description("Starting date (yyyy-mm-dd) for the requested the period"),
                                parameterWithName("to")
                                        .description("Ending date (yyyy-mm-dd) for the requested the period")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getSingleMetricHistoricalEvaluation(dtoMetric.getId(), projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsPredictionData() throws Exception {
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

        when(metricsDomainController.getAllMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoMetricList);
        when(metricsDomainController.getMetricsPrediction(dtoMetricList, projectExternalId, technique, freq, horizon)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/metrics/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoMetric.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoMetric.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoMetric.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetric.getValue()))))
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
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andDo(document("metrics/prediction",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("technique")
                                        .description("Forecasting technique"),
                                parameterWithName("horizon")
                                        .description("Amount of days that the prediction will cover")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence80.first")
                                        .description("Metric forecasting 80% confidence interval higher values"),
                                fieldWithPath("[].confidence80.second")
                                        .description("Metric forecasting 80% confidence interval lower values"),
                                fieldWithPath("[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].confidence95.first")
                                        .description("Metric forecasting 95% confidence interval higher values"),
                                fieldWithPath("[].confidence95.second")
                                        .description("Metric forecasting 95% confidence interval lower values"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getAllMetricsCurrentEvaluation(projectExternalId);
        verify(metricsDomainController, times(1)).getMetricsPrediction(dtoMetricList, projectExternalId, technique, freq, horizon);
        verifyNoMoreInteractions(metricsDomainController);
    }
}