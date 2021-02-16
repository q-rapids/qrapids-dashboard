package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.controllers.FactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.exceptions.QualityFactorNotFoundException;
import com.upc.gessi.qrapids.app.domain.exceptions.StrategicIndicatorQualityFactorNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetricEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedFactorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import com.upc.gessi.qrapids.app.testHelpers.HelperFunctions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
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

public class FactorEvaluationTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private FactorsController qualityFactorsDomainController;

    @Mock
    private MetricsController metricsDomainController;

    @Mock
    private ProjectsController projectsController;

    @InjectMocks
    private Factors factorsController;

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
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);

        String projectExternalId = "test";
        when(qualityFactorsDomainController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId,null,true)).thenReturn(dtoDetailedFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].date", is(dtoDetailedFactorEvaluation.getDate())))
                .andExpect(jsonPath("$[0].value", is(dtoDetailedFactorEvaluation.getValue())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getQualityFactors())))
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
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, null,true);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getSingleFactorEvaluation() throws Exception {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        String projectExternalId = "test";
        when(qualityFactorsDomainController.getSingleFactorEvaluation(dtoFactorEvaluation.getId(), projectExternalId)).thenReturn(dtoFactorEvaluation);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/current", dtoFactorEvaluation.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoFactorEvaluation.getId())))
                .andExpect(jsonPath("$.name", is(dtoFactorEvaluation.getName())))
                .andExpect(jsonPath("$.description", is(dtoFactorEvaluation.getDescription())))
                .andExpect(jsonPath("$.value.first", is(HelperFunctions.getFloatAsDouble(dtoFactorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$.value.second", is(dtoFactorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$.value_description", is(dtoFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$.date[0]", is(dtoFactorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$.date[1]", is(dtoFactorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(dtoFactorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(nullValue())))
                .andExpect(jsonPath("$.rationale", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$.confidence80", is(dtoFactorEvaluation.getConfidence80())))
                .andExpect(jsonPath("$.confidence95", is(dtoFactorEvaluation.getConfidence95())))
                .andExpect(jsonPath("$.forecastingError", is(nullValue())))
                .andExpect(jsonPath("$.mismatchDays", is(0)))
                .andExpect(jsonPath("$.missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$.strategicIndicators[0]", is(dtoFactorEvaluation.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$.formattedDate", is(dtoFactorEvaluation.getDate().toString())))
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
                                fieldWithPath("value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("confidence80")
                                        .description("Quality factor forecasting 80% confidence interval"),
                                fieldWithPath("confidence95")
                                        .description("Quality factor forecasting 95% confidence interval"),
                                fieldWithPath("forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("missingMetrics")
                                        .description("Metrics without assessment"),
                                fieldWithPath("strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));


        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getSingleFactorEvaluation(dtoFactorEvaluation.getId(), projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getDetailedQualityFactorsHistoricalData() throws Exception {
        // Given
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);

        String projectExternalId = "test";
        LocalDate from = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoDetailedFactorEvaluation.getMetrics().get(0).getDate();
        when(qualityFactorsDomainController.getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, null, from, to)).thenReturn(dtoDetailedFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/historical")
                .param("prj", projectExternalId)
                .param("from", from.toString())
                .param("to", to.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].date", is(dtoDetailedFactorEvaluation.getDate())))
                .andExpect(jsonPath("$[0].value", is(dtoDetailedFactorEvaluation.getValue())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getQualityFactors())))
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
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId,null, from, to);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getAllDetailedQualityFactors() throws Exception {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        String projectExternalId = "test";
        when(qualityFactorsDomainController.getAllFactorsEvaluation(projectExternalId, null,true)).thenReturn(dtoFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(HelperFunctions.getFloatAsDouble(dtoFactorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoFactorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(dtoFactorEvaluation.getConfidence80())))
                .andExpect(jsonPath("$[0].confidence95", is(dtoFactorEvaluation.getConfidence95())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactorEvaluation.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactorEvaluation.getDate().toString())))
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
                                fieldWithPath("[].value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Quality factor forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Quality factor forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsEvaluation(projectExternalId, null,true);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsPrediction() throws Exception {
        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactorForPrediction();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);
        String projectExternalId = "test";
        String freq = "7";
        String horizon = "7";
        String technique = "PROPHET";
        when(qualityFactorsDomainController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, null,true)).thenReturn(dtoDetailedFactorEvaluationList);
        when(qualityFactorsDomainController.getFactorsWithMetricsPrediction(dtoDetailedFactorEvaluationList, technique, freq, horizon, projectExternalId)).thenReturn(dtoDetailedFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/metrics/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].date", is(dtoDetailedFactorEvaluation.getDate())))
                .andExpect(jsonPath("$[0].value", is(dtoDetailedFactorEvaluation.getValue())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoDetailedFactorEvaluation.getMetrics().get(0).getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getDatasource())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.first", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getConfidence80().getFirst()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.second", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getConfidence80().getSecond()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.first", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getConfidence95().getFirst()))))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.second", is(HelperFunctions.getFloatAsDouble(dtoDetailedFactorEvaluation.getMetrics().get(0).getConfidence95().getSecond()))))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoDetailedFactorEvaluation.getMetrics().get(0).getQualityFactors())))
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
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, null,true);
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsPrediction(dtoDetailedFactorEvaluationList, technique, freq, horizon, projectExternalId);
    }

    @Test
    public void simulate() throws Exception {
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);

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

        when(qualityFactorsDomainController.simulate(metricsMap, projectExternalId, null, LocalDate.parse(date))).thenReturn(dtoFactorEvaluationList);

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
                .andExpect(jsonPath("$[0].id", is(dtoFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(HelperFunctions.getFloatAsDouble(dtoFactorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoFactorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(dtoFactorEvaluation.getConfidence80())))
                .andExpect(jsonPath("$[0].confidence95", is(dtoFactorEvaluation.getConfidence95())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactorEvaluation.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactorEvaluation.getDate().toString())))
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
                                fieldWithPath("[].value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Quality factor forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Quality factor forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).simulate(metricsMap, projectExternalId, null, LocalDate.parse(date));
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getMetricsEvaluationForQF() throws Exception {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String factorId = "testingperformance";
        String projectExternalId = "test";
        DTOFactorEvaluation f = domainObjectsBuilder.buildDTOFactor();
        when(metricsDomainController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricEvaluationList);
        when(qualityFactorsDomainController.getSingleFactorEvaluation(factorId, projectExternalId)).thenReturn(f);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/current", factorId)
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(f.getId())))
                .andExpect(jsonPath("$[0].name", is(f.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(f.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(f.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(f.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(HelperFunctions.getFloatAsDouble(f.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(f.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(f.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoMetricEvaluation.getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoMetricEvaluation.getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoMetricEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoMetricEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoMetricEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoMetricEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoMetricEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoMetricEvaluation.getQualityFactors())))
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
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);
        verifyNoMoreInteractions(metricsDomainController);
    }

    @Test
    public void getMetricsHistoricalDataForQF() throws Exception {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String projectExternalId = "test";
        String dateFrom = "2019-07-07";
        String dateTo = "2019-07-15";
        DTOFactorEvaluation factorEvaluation = domainObjectsBuilder.buildDTOFactor();

        when(metricsDomainController.getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))).thenReturn(dtoMetricEvaluationList);
        when(qualityFactorsDomainController.getSingleFactorEvaluation(factorId,projectExternalId)).thenReturn(factorEvaluation);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/historical", factorId)
                .param("prj", projectExternalId)
                .param("from", dateFrom)
                .param("to", dateTo);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].date", is(nullValue())))
                .andExpect(jsonPath("$[0].value", is(nullValue())))
                .andExpect(jsonPath("$[0].value_description", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoMetricEvaluation.getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoMetricEvaluation.getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoMetricEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoMetricEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoMetricEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoMetricEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoMetricEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].confidence95", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoMetricEvaluation.getQualityFactors())))
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
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorHistoricalEvaluation(factorId, projectExternalId, LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
        verifyNoMoreInteractions(metricsDomainController);
        verify(qualityFactorsDomainController, times(1)).getSingleFactorEvaluation(factorId,projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getMetricsPredictionDataForQF() throws Exception {
        // Given
        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String projectExternalId = "test";
        DTOFactorEvaluation factorEvaluation = domainObjectsBuilder.buildDTOFactor();

        when(metricsDomainController.getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId)).thenReturn(dtoMetricEvaluationList);

        dtoMetricEvaluation.setDatasource("Forecast");
        dtoMetricEvaluation.setRationale("Forecast");
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetricEvaluation.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetricEvaluation.setConfidence95(confidence95);

        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(metricsDomainController.getMetricsPrediction(dtoMetricEvaluationList, projectExternalId, technique, freq, horizon)).thenReturn(dtoMetricEvaluationList);
        when(qualityFactorsDomainController.getSingleFactorEvaluation(factorId,projectExternalId)).thenReturn(factorEvaluation);


        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}/metrics/prediction", factorId)
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].name", is(factorName)))
                .andExpect(jsonPath("$[0].date", is(nullValue())))
                .andExpect(jsonPath("$[0].value", is(nullValue())))
                .andExpect(jsonPath("$[0].value_description", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].id", is(dtoMetricEvaluation.getId())))
                .andExpect(jsonPath("$[0].metrics[0].name", is(dtoMetricEvaluation.getName())))
                .andExpect(jsonPath("$[0].metrics[0].description", is(dtoMetricEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].metrics[0].value", is(HelperFunctions.getFloatAsDouble(dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].value_description", is(String.format("%.2f", dtoMetricEvaluation.getValue()))))
                .andExpect(jsonPath("$[0].metrics[0].date[0]", is(dtoMetricEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].metrics[0].date[1]", is(dtoMetricEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].metrics[0].date[2]", is(dtoMetricEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].metrics[0].datasource", is(dtoMetricEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].rationale", is(dtoMetricEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].metrics[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].metrics[0].qualityFactors", is(dtoMetricEvaluation.getQualityFactors())))
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
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
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
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].metrics[].qualityFactors")
                                        .description("List of the quality factors that use this metric")
                        )
                ));

        // Verify mock interactions
        verify(metricsDomainController, times(1)).getMetricsForQualityFactorCurrentEvaluation(factorId, projectExternalId);
        verify(metricsDomainController, times(1)).getMetricsPrediction(dtoMetricEvaluationList, projectExternalId, technique, freq, horizon);
        verifyNoMoreInteractions(metricsDomainController);
        verify(qualityFactorsDomainController, times(1)).getSingleFactorEvaluation(factorId,projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    // NEW TESTS
    @Test
    public void importFactorsAndUpdateDatabase() throws Exception {
        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/import");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qualityFactors/import",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).importFactorsAndUpdateDatabase();
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsHistoricalData() throws Exception {
        // Given
        String projectExternalId = "test";
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qualityFactorsDomainController.getAllFactorsHistoricalEvaluation(projectExternalId,null, fromDate, toDate)).thenReturn(dtoFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(HelperFunctions.getFloatAsDouble(dtoFactorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoFactorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoFactorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].confidence80", is(dtoFactorEvaluation.getConfidence80())))
                .andExpect(jsonPath("$[0].confidence95", is(dtoFactorEvaluation.getConfidence95())))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactorEvaluation.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactorEvaluation.getDate().toString())))
                .andDo(document("qualityFactors/historical",
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
                                fieldWithPath("[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Quality factor forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence95")
                                        .description("Quality factor forecasting 95% confidence interval"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));


        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsHistoricalEvaluation(projectExternalId, null, fromDate, toDate);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsHistoricalDataReadError() throws Exception {
        String projectExternalId = "test";
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qualityFactorsDomainController.getAllFactorsHistoricalEvaluation(projectExternalId,null, fromDate, toDate)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("qualityFactors/historical-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getQualityFactorsPredictionData() throws Exception {
        // Given
        String projectExternalId = "test";
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation.setDatasource("Forecast");
        dtoFactorEvaluation.setRationale("Forecast");
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoFactorEvaluation.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoFactorEvaluation.setConfidence95(confidence95);

        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);

        String technique = "PROPHET";
        String freq = "7";
        String horizon = "7";

        when(qualityFactorsDomainController.getAllFactorsEvaluation(projectExternalId, null,true)).thenReturn(dtoFactorEvaluationList);
        when(qualityFactorsDomainController.getFactorsPrediction(dtoFactorEvaluationList, projectExternalId, technique, freq, horizon)).thenReturn(dtoFactorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(dtoFactorEvaluation.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoFactorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoFactorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(HelperFunctions.getFloatAsDouble(dtoFactorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoFactorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoFactorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoFactorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoFactorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoFactorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].rationale", is(dtoFactorEvaluation.getRationale())))
                .andExpect(jsonPath("$[0].confidence80.first", is(first80)))
                .andExpect(jsonPath("$[0].confidence80.second", is(second80)))
                .andExpect(jsonPath("$[0].confidence95.first", is(first95)))
                .andExpect(jsonPath("$[0].confidence95.second", is(second95)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingMetrics", is(nullValue())))
                .andExpect(jsonPath("$[0].strategicIndicators[0]", is(dtoFactorEvaluation.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].formattedDate", is(dtoFactorEvaluation.getDate().toString())))
                .andDo(document("qualityFactors/prediction",
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
                                fieldWithPath("[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].value.first")
                                        .description("Quality factor numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Quality factor category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].confidence80")
                                        .description("Quality factor forecasting 80% confidence interval"),
                                fieldWithPath("[].confidence80.first")
                                        .description("Quality factor forecasting 80% confidence interval higher values"),
                                fieldWithPath("[].confidence80.second")
                                        .description("Quality factor forecasting 80% confidence interval lower values"),
                                fieldWithPath("[].confidence95")
                                        .description("Quality factor forecasting 95% confidence interval"),
                                fieldWithPath("[].confidence95.first")
                                        .description("Quality factor forecasting 95% confidence interval higher values"),
                                fieldWithPath("[].confidence95.second")
                                        .description("Quality factor forecasting 95% confidence interval lower values"),
                                fieldWithPath("[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the quality factor and some metrics"),
                                fieldWithPath("[].missingMetrics")
                                        .description("Metrics without assessment"),
                                fieldWithPath("[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].formattedDate")
                                        .description("Readable quality factor evaluation date")
                        )
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getAllFactorsEvaluation(projectExternalId, null,true);
        verify(qualityFactorsDomainController, times(1)).getFactorsPrediction(dtoFactorEvaluationList, projectExternalId, technique, freq, horizon);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getAllQualityFactors () throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "prj-1";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        project.setId(projectId);
        project.setBacklogId(projectBacklogId);

        // define factor with its metric composition
        List<QualityFactorMetrics> qualityMetrics = new ArrayList<>();

        Factor factor =  new Factor("codequality", "Quality of the implemented code", project);
        factor.setId(1L);
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code",project);
        metric1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor);
        qfm1.setId(1L);
        qualityMetrics.add(qfm1);
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", project);
        metric2.setId(2L);
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor);
        qfm1.setId(2L);
        qualityMetrics.add(qfm2);
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold",project);
        metric3.setId(3L);
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor);
        qfm1.setId(3L);
        qualityMetrics.add(qfm3);
        factor.setQualityFactorMetricsList(qualityMetrics);
        factor.setWeighted(false);

        List<Factor> qualityFactorList = new ArrayList<>();
        qualityFactorList.add(factor);

        when(qualityFactorsDomainController.getQualityFactorsByProjectAndProfile(projectExternalId, null)).thenReturn(qualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(factor.getId().intValue())))
                .andExpect(jsonPath("$[0].externalId", is(factor.getExternalId())))
                .andExpect(jsonPath("$[0].name", is(factor.getName())))
                .andExpect(jsonPath("$[0].description", is(factor.getDescription())))
                .andExpect(jsonPath("$[0].threshold", is(factor.getThreshold())))
                .andExpect(jsonPath("$[0].metrics", hasSize(3)))
                .andExpect(jsonPath("$[0].metrics[0]", is("1")))
                .andExpect(jsonPath("$[0].metrics[1]", is("2")))
                .andExpect(jsonPath("$[0].metrics[2]", is("3")))
                .andExpect(jsonPath("$[0].weighted", is(factor.isWeighted())))
                .andExpect(jsonPath("$[0].metricsWeights", is(factor.getWeights())))
                .andDo(document("qualityFactors/get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].externalId")
                                        .description("Quality factor external identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].threshold")
                                        .description("Quality factor minimum acceptable value"),
                                fieldWithPath("[].metrics")
                                        .description("List of the metrics composing the quality factor"),
                                fieldWithPath("[].metrics[]")
                                        .description("Metric identifier"),
                                fieldWithPath("[].weighted")
                                        .description("Quality factor is weighted or not"),
                                fieldWithPath("[].metricsWeights")
                                        .description("List of the metrics composing the quality factor with their corresponding weights"))
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorsByProjectAndProfile(projectExternalId, null);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactor() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Factor factor = domainObjectsBuilder.buildFactor(project);
        when(qualityFactorsDomainController.getQualityFactorById(factor.getId())).thenReturn(factor);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}", factor.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(factor.getId().intValue())))
                .andExpect(jsonPath("$.externalId", is(factor.getExternalId())))
                .andExpect(jsonPath("$.name", is(factor.getName())))
                .andExpect(jsonPath("$.description", is(factor.getDescription())))
                .andExpect(jsonPath("$.threshold", is(Double.valueOf(factor.getThreshold().toString()))))
                .andExpect(jsonPath("$.metrics", hasSize(3)))
                .andExpect(jsonPath("$.metrics[0]", is(factor.getMetricsIds().get(0))))
                .andExpect(jsonPath("$.metrics[1]", is(factor.getMetricsIds().get(1))))
                .andExpect(jsonPath("$.metrics[2]", is(factor.getMetricsIds().get(2))))
                .andExpect(jsonPath("$.weighted", is(factor.isWeighted())))
                .andExpect(jsonPath("$.metricsWeights", is(factor.getWeights())))
                .andDo(document("qualityFactors/get-one",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier")),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("externalId")
                                        .description("Quality factor external identifier"),
                                fieldWithPath("name")
                                        .description("Quality factor name"),
                                fieldWithPath("description")
                                        .description("Quality factor description"),
                                fieldWithPath("threshold")
                                        .description("Quality factor minimum acceptable value"),
                                fieldWithPath("metrics")
                                        .description("List of the metrics composing the quality factor"),
                                fieldWithPath("metrics[]")
                                        .description("Metric identifier"),
                                fieldWithPath("weighted")
                                        .description("Quality factor is weighted or not"),
                                fieldWithPath("metricsWeights[]")
                                        .description("List of the metrics composing the quality factor with their corresponding weights"))
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorById(factor.getId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getMissingQualityFactor() throws Exception {
        Long qualityFactorId = 2L;
        when(qualityFactorsDomainController.getQualityFactorById(qualityFactorId)).thenThrow(new QualityFactorNotFoundException());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/{id}", qualityFactorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andDo(document("qualityFactors/get-one-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void newQualityFactor() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Factor factor = domainObjectsBuilder.buildFactor(project);
        when(qualityFactorsDomainController.assessQualityFactor(factor.getName(),factor.getProject().getExternalId())).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors")
                .param("prj", project.getExternalId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getMetrics()));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qualityFactors/new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("name")
                                        .description("Quality factor name"),
                                parameterWithName("description")
                                        .description("Quality factor description"),
                                parameterWithName("threshold")
                                        .description("Quality factor minimum acceptable value"),
                                parameterWithName("metrics")
                                        .description("Comma separated values of the metrics identifiers which belong to the quality factor"))
                ));

        // Verify mock interactions
        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsController);

        verify(qualityFactorsDomainController, times(1)).saveQualityFactor(eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()), eq(factor.getMetrics()), eq(project));
        verify(qualityFactorsDomainController, times(1)).assessQualityFactor(factor.getName(), factor.getProject().getExternalId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void newQualityFactorAssessmentError() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Factor factor = domainObjectsBuilder.buildFactor(project);
        when(qualityFactorsDomainController.assessQualityFactor(factor.getName(),factor.getProject().getExternalId())).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors")
                .param("prj", project.getExternalId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getMetrics()));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("qualityFactors/new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsController);

        verify(qualityFactorsDomainController, times(1)).saveQualityFactor(eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()) , eq(factor.getMetrics()), eq(project));
        verify(qualityFactorsDomainController, times(1)).assessQualityFactor(factor.getName(), factor.getProject().getExternalId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void editQualityFactor() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Factor factor = domainObjectsBuilder.buildFactor(project);

        when(qualityFactorsDomainController.getQualityFactorById(factor.getId())).thenReturn(factor);
        when(qualityFactorsDomainController.assessQualityFactor(factor.getName(), project.getExternalId())).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors/{id}", factor.getId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param ("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getWeights()))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qualityFactors/update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Quality factor name"),
                                parameterWithName("description")
                                        .description("Quality factor description"),
                                parameterWithName("threshold")
                                        .description("Quality factor minimum acceptable value"),
                                parameterWithName("metrics")
                                        .description("Comma separated values of the metrics identifiers which belong to the quality factor and their corresponding weights (-1 if no weighted)"))
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorById(factor.getId());
        verify(qualityFactorsDomainController, times(1)).editQualityFactor(eq(factor.getId()), eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()) , eq(factor.getWeights()));
        verify(qualityFactorsDomainController, times(1)).assessQualityFactor(factor.getName(), factor.getProject().getExternalId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void editQualityFactorAssessment() throws Exception, StrategicIndicatorQualityFactorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Factor factor = domainObjectsBuilder.buildFactor(project);

        when(qualityFactorsDomainController.getQualityFactorById(factor.getId())).thenReturn(factor);
        when(qualityFactorsDomainController.assessQualityFactor(factor.getName(), project.getExternalId())).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors/{id}", factor.getId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param ("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getWeights()))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qualityFactors/update-assessment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorById(factor.getId());
        verify(qualityFactorsDomainController, times(1)).editQualityFactor(eq(factor.getId()), eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()), eq(factor.getWeights()));
        verify(qualityFactorsDomainController, times(1)).assessQualityFactor(factor.getName(), project.getExternalId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void editQualityFactorAssessmentError() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Factor factor = domainObjectsBuilder.buildFactor(project);

        when(qualityFactorsDomainController.getQualityFactorById(factor.getId())).thenReturn(factor);
        when(qualityFactorsDomainController.assessQualityFactor(factor.getName(), project.getExternalId())).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors/{id}", factor.getId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param ("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getMetrics()))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("qualityFactors/update-assessment-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorById(factor.getId());
        verify(qualityFactorsDomainController, times(1)).editQualityFactor(eq(factor.getId()), eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()), eq(factor.getMetrics()));
        verify(qualityFactorsDomainController, times(1)).assessQualityFactor(factor.getName(), project.getExternalId());
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void editQualityFactorMissingParam() throws Exception {
        Long qualityFactorID = 1L;
        String qualityFactorName = "Code Quality";
        String qualityFactorDescription = "Quality of the implemented code";
        String qualityFactorThreshold = "";

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors/{id}", qualityFactorID)
                .param("name", qualityFactorName)
                .param("description", qualityFactorDescription)
                .param ("threshold", qualityFactorThreshold)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("qualityFactors/update-missing-params",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void editQualityFactorIntegrityViolation() throws Exception, StrategicIndicatorQualityFactorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Factor factor = domainObjectsBuilder.buildFactor(project);

        when(qualityFactorsDomainController.getQualityFactorById(factor.getId())).thenReturn(factor);
        when(qualityFactorsDomainController.editQualityFactor(eq(factor.getId()), eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()), eq(factor.getMetrics()))).thenThrow(new DataIntegrityViolationException(""));

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/qualityFactors/{id}", factor.getId())
                .param("name", factor.getName())
                .param("description", factor.getDescription())
                .param ("threshold", factor.getThreshold().toString())
                .param("metrics", String.join(",", factor.getMetrics()))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("qualityFactors/update-data-integrity-violation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).getQualityFactorById(factor.getId());
        verify(qualityFactorsDomainController, times(1)).editQualityFactor(eq(factor.getId()), eq(factor.getName()), eq(factor.getDescription()), eq(factor.getThreshold().toString()), eq(factor.getMetrics()));
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void deleteOneQualityFactor() throws Exception {
        Long factorId = 1L;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/qualityFactors/{id}", factorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qualityFactors/delete-one",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality factor identifier"))
                ));

        // Verify mock interactions
        verify(qualityFactorsDomainController, times(1)).deleteFactor(factorId);
    }
}