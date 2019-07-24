package com.upc.gessi.qrapids.app.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;

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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FactorsServiceTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

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
                .apply(documentationConfiguration(this.restDocumentation))
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
                .get("/api/qualityFactors/metrics/current")
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
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/{id}", factorId)
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
                .andExpect(jsonPath("$.formattedDate", is(evaluationDate.toString())))
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
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/current", strategicIndicatorId)
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
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/current-si",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")),
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
                .get("/api/qualityFactors/metrics/historical")
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
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/historical", strategicIndicatorId)
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
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/historical-si",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")),
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
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors")
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
                .andExpect(jsonPath("$[0].formattedDate", is(evaluationDate.toString())))
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
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void getQualityFactorsCategories() throws Exception {
        // Factors categories setup
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorGoodCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorNeutralCategory);

        when(qfCategoryRepository.findAll()).thenReturn(factorCategoryList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qualityFactors/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(factorGoodCategoryId.intValue())))
                .andExpect(jsonPath("$[0].name", is(factorGoodCategoryName)))
                .andExpect(jsonPath("$[0].color", is(factorGoodCategoryColor)))
                .andExpect(jsonPath("$[0].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorGoodCategoryUpperThreshold))))
                .andDo(document("qf/categories",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality factor category identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality factor category name"),
                                fieldWithPath("[].color")
                                        .description("Quality factor category hexadecimal color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Quality factor category upper threshold")
                        )
                ));

        // Verify mock interactions
        verify(qfCategoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(qfCategoryRepository);
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
                .get("/api/qualityFactors/metrics/prediction")
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
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/prediction", strategicIndicatorId)
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
                .andExpect(jsonPath("$[0].metrics[0].forecastingError", is(nullValue())))
                .andDo(document("qf/prediction-si",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")),
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
                .post("/api/qualityFactors/simulate")
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
                .andExpect(jsonPath("$[0].formattedDate", is(evaluationDate.toString())))
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
        verify(qmaSimulation, times(1)).simulateQualityFactors(metricsMap, projectExternalId, LocalDate.parse(date));
        verifyNoMoreInteractions(qmaSimulation);
    }
}