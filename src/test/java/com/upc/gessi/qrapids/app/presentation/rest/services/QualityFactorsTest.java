package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactor;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetric;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import com.upc.gessi.qrapids.app.testHelpers.HelperFunctions;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

public class QualityFactorsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private QualityFactorsController qualityFactorsDomainController;

    @Mock
    private MetricsController metricsDomainController;

    @InjectMocks
    private QualityFactors factorsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(factorsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getFactorsCategories () throws Exception {
        // Given
        List<QFCategory> factorCategoryList = domainObjectsBuilder.buildFactorCategoryList();
        when(qualityFactorsDomainController.getFactorCategories()).thenReturn(factorCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(factorCategoryList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(factorCategoryList.get(0).getName())))
                .andExpect(jsonPath("$[0].color", is(factorCategoryList.get(0).getColor())))
                .andExpect(jsonPath("$[0].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorCategoryList.get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[1].id", is(factorCategoryList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(factorCategoryList.get(1).getName())))
                .andExpect(jsonPath("$[1].color", is(factorCategoryList.get(1).getColor())))
                .andExpect(jsonPath("$[1].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorCategoryList.get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[2].id", is(factorCategoryList.get(2).getId().intValue())))
                .andExpect(jsonPath("$[2].name", is(factorCategoryList.get(2).getName())))
                .andExpect(jsonPath("$[2].color", is(factorCategoryList.get(2).getColor())))
                .andExpect(jsonPath("$[2].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorCategoryList.get(2).getUpperThreshold()))))
                .andDo(document("qf/categories",
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
        verify(qualityFactorsDomainController, times(1)).getFactorCategories();
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void newFactorsCategories () throws Exception {
        // Given
        List<Map<String, String>> factorCategoriesList = domainObjectsBuilder.buildRawFactorCategoryList();

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qualityFactors/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(factorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qf/categories-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].name")
                                        .description("Quality factors category name"),
                                fieldWithPath("[].color")
                                        .description("Quality factors category color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Quality factors category upper threshold"))
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).newFactorCategories(factorCategoriesList);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void newFactorsCategoriesNotEnough () throws Exception {
        // Given
        List<Map<String, String>> factorCategoriesList = domainObjectsBuilder.buildRawSICategoryList();
        factorCategoriesList.remove(2);
        factorCategoriesList.remove(1);
        doThrow(new CategoriesException()).when(qualityFactorsDomainController).newFactorCategories(factorCategoriesList);

        //Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qualityFactors/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(factorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Not enough categories"))
                .andDo(document("qf/categories-new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getQualityFactorsEvaluations() throws Exception {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        when(qualityFactorsDomainController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoQualityFactor.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoQualityFactor.getName())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoQualityFactor.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoQualityFactor.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoQualityFactor.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoQualityFactor.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoQualityFactor.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoQualityFactor.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoQualityFactor.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/current",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].metrics")
                                        .description("List with all the quality factor metrics"),
                                fieldWithPath("[].metrics[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].metrics[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].metrics[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].metrics[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].metrics[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].metrics[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].metrics[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].metrics[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].metrics[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].metrics[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].metrics[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsCurrentEvaluation(projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getSingleFactorEvaluation() throws Exception {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        String projectExternalId = "test";
        when(qualityFactorsDomainController.getSingleFactorEvaluation(dtoFactor.getId(), projectExternalId)).thenReturn(dtoFactor);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}", dtoFactor.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$.name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$.description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$.value", is(HelperFunctions.getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$.value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$.date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$.date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(nullValue())))
                .andExpect(jsonPath("$.rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$.forecastingError", is(nullValue())))
                .andExpect(jsonPath("$.strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$.formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("qf/single",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier")),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("name")
                                        .description("Quality factor name"),
                                fieldWithPath("description")
                                        .description("Quality factor description"),
                                fieldWithPath("value")
                                        .description("Quality factor value"),
                                fieldWithPath("value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));


        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getSingleFactorEvaluation(dtoFactor.getId(), projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsHistoricalData() throws Exception {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        LocalDate from = dtoQualityFactor.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoQualityFactor.getMetrics().get(0).getDate();
        when(qualityFactorsDomainController.getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, from, to)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/historical")
                .param("prj", projectExternalId)
                .param("from", from.toString())
                .param("to", to.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoQualityFactor.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoQualityFactor.getName())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoQualityFactor.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoQualityFactor.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoQualityFactor.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoQualityFactor.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoQualityFactor.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoQualityFactor.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoQualityFactor.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/historical",
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
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].metrics")
                                        .description("List with all the quality factor metrics"),
                                fieldWithPath("[].metrics[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].metrics[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].metrics[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].metrics[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].metrics[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].metrics[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].metrics[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].metrics[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].metrics[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].metrics[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].metrics[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, from, to);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getAllQualityFactors() throws Exception {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        String projectExternalId = "test";
        when(qualityFactorsDomainController.getAllFactorsEvaluation(projectExternalId)).thenReturn(dtoFactorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("qf/all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsEvaluation(projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsPrediction() throws Exception {
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactorForPrediction();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String projectExternalId = "test";
        String freq = "7";
        String horizon = "7";
        String technique = "PROPHET";
        when(qualityFactorsDomainController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoQualityFactorList);
        when(qualityFactorsDomainController.getFactorsWithMetricsPrediction(dtoQualityFactorList, technique, freq, horizon, projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoQualityFactor.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoQualityFactor.getName())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoQualityFactor.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoQualityFactor.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoQualityFactor.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoQualityFactor.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoQualityFactor.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoQualityFactor.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoQualityFactor.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(dtoQualityFactor.getMetrics().get(0).getDatasource())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoQualityFactor.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.first", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getConfidence80().getFirst()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.second", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getConfidence80().getSecond()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.first", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getConfidence95().getFirst()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.second", is(HelperFunctions.getFloatAsDouble(dtoQualityFactor.getMetrics().get(0).getConfidence95().getSecond()))))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/prediction",
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
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].metrics")
                                        .description("List with all the quality factor metrics"),
                                fieldWithPath("[].metrics[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].metrics[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].metrics[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].metrics[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].metrics[].value_description")
                                        .description("Metric readable value"),
                                fieldWithPath("[].metrics[].date")
                                        .description("Metric evaluation date"),
                                fieldWithPath("[].metrics[].datasource")
                                        .description("Metric source of data"),
                                fieldWithPath("[].metrics[].rationale")
                                        .description("Metric evaluation rationale"),
                                fieldWithPath("[].metrics[].confidence80")
                                        .description("Metric forecasting 80% confidence interval"),
                                fieldWithPath("[].metrics[].confidence80.first")
                                        .description("Metric forecasting 80% confidence interval higher values"),
                                fieldWithPath("[].metrics[].confidence80.second")
                                        .description("Metric forecasting 80% confidence interval lower values"),
                                fieldWithPath("[].metrics[].confidence95")
                                        .description("Metric forecasting 95% confidence interval"),
                                fieldWithPath("[].metrics[].confidence95.first")
                                        .description("Metric forecasting 95% confidence interval higher values"),
                                fieldWithPath("[].metrics[].confidence95.second")
                                        .description("Metric forecasting 95% confidence interval lower values"),
                                fieldWithPath("[].metrics[].forecastingError")
                                        .description("Description of forecasting errors")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsCurrentEvaluation(projectExternalId);
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsPrediction(dtoQualityFactorList, technique, freq, horizon, projectExternalId);
    }

    @Test
    public void simulate() throws Exception {
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
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

        when(qualityFactorsDomainController.simulate(metricsMap, projectExternalId, LocalDate.parse(date))).thenReturn(dtoFactorList);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(metricList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qualityFactors/simulate")
                .param("prj", projectExternalId)
                .param("date", date)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("qf/simulation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .description("Date of the quality factors evaluation simulation model base")),
                        requestFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].value")
                                        .description("Metric value")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).simulate(metricsMap, projectExternalId, LocalDate.parse(date));
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getMetricsEvaluationForQF() throws Exception {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        when(metricsDomainController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/current", factorId)
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
                .andDo(document("metrics/current-qf",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier")),
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
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsHistoricalDataForQF() throws Exception {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        when(metricsDomainController.getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/historical", factorId)
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
                .andDo(document("metrics/historical-qf",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier")),
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
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsPredictionDataForQF() throws Exception {
        // Given
        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        when(metricsDomainController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricList);

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

        when(metricsDomainController.getMetricsPrediction(dtoMetricList, projectExternalId, technique, freq, horizon)).thenReturn(dtoMetricList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/prediction", factorId)
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
                .andDo(document("metrics/prediction-qf",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier")),
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
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);
        verify(metricsDomainController, times(1)).getMetricsPrediction(dtoMetricList, projectExternalId, technique, freq, horizon);
        verifyNoMoreInteractions(metricsDomainController);
    }
}