package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.StrategicIndicatorNotFoundException;
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
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.upc.gessi.qrapids.app.testHelpers.HelperFunctions.getFloatAsDouble;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StrategicIndicatorsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private Forecast forecast;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private QualityFactorsController qualityFactorsDomainController;

    @Mock
    private StrategicIndicatorsController strategicIndicatorsDomainController;

    @Mock
    private ProjectsController projectsController;

    @InjectMocks
    private StrategicIndicators strategicIndicatorsController;

    private String projectExternalId;

    private DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation;
    private List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();

    private DTOFactor dtoFactor;
    private DTODetailedStrategicIndicator dtoDetailedStrategicIndicator;
    private List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(strategicIndicatorsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();

        domainObjectsBuilder = new DomainObjectsBuilder();

        projectExternalId = "test";

        dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);
    }

    @After
    public void tearDown() {
        dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList = new ArrayList<>();
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluation() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$[0].dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$[0].name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[0].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue())))
                .andExpect(jsonPath("$[0].probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$[0].probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[1].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue())))
                .andExpect(jsonPath("$[0].probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$[0].probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[2].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue())))
                .andExpect(jsonPath("$[0].probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$[0].probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$[0].hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$[0].hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andDo(document("si/current",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].dbId")
                                        .description("Strategic indicator database identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].probabilities")
                                        .description("Strategic indicator categories list"),
                                fieldWithPath("[].probabilities[].id")
                                        .description("Strategic indicator category identifier"),
                                fieldWithPath("[].probabilities[].label")
                                        .description("Strategic indicator category label"),
                                fieldWithPath("[].probabilities[].value")
                                        .description("Strategic indicator category probability"),
                                fieldWithPath("[].probabilities[].color")
                                        .description("Strategic indicator category hexadecimal color"),
                                fieldWithPath("[].probabilities[].upperThreshold")
                                        .description("Strategic indicator category upper threshold"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].datasource")
                                        .description("Strategic indicator source of data"),
                                fieldWithPath("[].categories_description")
                                        .description("Array with the strategic indicator categories and thresholds"),
                                fieldWithPath("[].hasBN")
                                        .description("Does the strategic indicator have a Bayesian Network?"),
                                fieldWithPath("[].hasFeedback")
                                        .description("Does the strategic indicator have any feedback"),
                                fieldWithPath("[].forecastingError")
                                        .description("Errors in the forecasting"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"))
                ));


        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getAllStrategicIndicatorsCurrentEvaluation(projectExternalId);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluationCategoriesConflict() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId)).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("si/current-conflict",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluationReadError() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/current-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluation() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluation);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/current", dtoStrategicIndicatorEvaluation.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$.dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$.name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$.description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$.value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$.value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$.value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$.probabilities", hasSize(3)))
                .andExpect(jsonPath("$.probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$.probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$.probabilities[0].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue())))
                .andExpect(jsonPath("$.probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$.probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$.probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$.probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$.probabilities[1].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue())))
                .andExpect(jsonPath("$.probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$.probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$.probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$.probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$.probabilities[2].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue())))
                .andExpect(jsonPath("$.probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$.probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$.date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$.date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$.date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$.datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$.categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$.hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$.hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$.forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())))
                .andExpect(jsonPath("$.mismatchDays", is(0)))
                .andExpect(jsonPath("$.missingFactors", is(nullValue())))
                .andDo(document("si/single-current",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic Indicator identifier")),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("dbId")
                                        .description("Strategic indicator database identifier"),
                                fieldWithPath("name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("probabilities")
                                        .description("Strategic indicator categories list"),
                                fieldWithPath("probabilities[].id")
                                        .description("Strategic indicator category identifier"),
                                fieldWithPath("probabilities[].label")
                                        .description("Strategic indicator category label"),
                                fieldWithPath("probabilities[].value")
                                        .description("Strategic indicator category probability"),
                                fieldWithPath("probabilities[].color")
                                        .description("Strategic indicator category hexadecimal color"),
                                fieldWithPath("probabilities[].upperThreshold")
                                        .description("Strategic indicator category upper threshold"),
                                fieldWithPath("date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("datasource")
                                        .description("Strategic indicator source of data"),
                                fieldWithPath("categories_description")
                                        .description("Array with the strategic indicator categories and thresholds"),
                                fieldWithPath("hasBN")
                                        .description("Does the strategic indicator have a Bayesian Network?"),
                                fieldWithPath("hasFeedback")
                                        .description("Does the strategic indicator have any feedback"),
                                fieldWithPath("forecastingError")
                                        .description("Errors in the forecasting"),
                                fieldWithPath("mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("missingFactors")
                                        .description("Factors without assessment"))
                ));


        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluationCategoriesConflict() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId)).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/{id}/current",dtoStrategicIndicatorEvaluation.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("si/single-current-conflict",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluationReadError() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/{id}/current",dtoStrategicIndicatorEvaluation.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/single-current-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getStrategicIndicatorsHistoricalData() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$[0].dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$[0].name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[0].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue())))
                .andExpect(jsonPath("$[0].probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$[0].probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[1].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue())))
                .andExpect(jsonPath("$[0].probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$[0].probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[2].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue())))
                .andExpect(jsonPath("$[0].probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$[0].probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$[0].hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$[0].hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andDo(document("si/historical",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].dbId")
                                        .description("Strategic indicator database identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].probabilities")
                                        .description("Strategic indicator categories list"),
                                fieldWithPath("[].probabilities[].id")
                                        .description("Strategic indicator category identifier"),
                                fieldWithPath("[].probabilities[].label")
                                        .description("Strategic indicator category label"),
                                fieldWithPath("[].probabilities[].value")
                                        .description("Strategic indicator category probability"),
                                fieldWithPath("[].probabilities[].color")
                                        .description("Strategic indicator category hexadecimal color"),
                                fieldWithPath("[].probabilities[].upperThreshold")
                                        .description("Strategic indicator category upper threshold"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].datasource")
                                        .description("Strategic indicator source of data"),
                                fieldWithPath("[].categories_description")
                                        .description("Array with the strategic indicator categories and thresholds"),
                                fieldWithPath("[].hasBN")
                                        .description("Does the strategic indicator have a Bayesian Network?"),
                                fieldWithPath("[].hasFeedback")
                                        .description("Does the strategic indicator have any feedback"),
                                fieldWithPath("[].forecastingError")
                                        .description("Errors in the forecasting"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"))
                ));


        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getStrategicIndicatorsHistoricalDataCategoriesConflict() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate)).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("si/historical-conflict",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getStrategicIndicatorsHistoricalDataReadError() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/historical-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getDetailedStrategicIndicatorsCurrentEvaluation() throws Exception {
        // Given
        when(strategicIndicatorsDomainController.getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityFactors/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-current",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getDetailedStrategicIndicatorsCurrentEvaluationReadError() throws Exception {
        when(strategicIndicatorsDomainController.getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityFactors/current")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/detailed-current-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getSingleDetailedStrategicIndicator() throws Exception {
        when(strategicIndicatorsDomainController.getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/current", dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-single-current",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getDetailedSingleStrategicIndicatorReadError() throws Exception {
        when(strategicIndicatorsDomainController.getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/current", dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/detailed-single-current-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getDetailedStrategicIndicatorsHistoricalData() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityFactors/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-historical",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getAllDetailedStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getDetailedStrategicIndicatorsHistoricalDataReadError() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityFactors/historical")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/detailed-historical-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getDetailedSingleStrategicIndicatorHistoricalData() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, fromDate, toDate)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/historical", dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-single-historical",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getSingleDetailedStrategicIndicatorsHistoricalEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, fromDate, toDate);
        verifyNoMoreInteractions(qmaDetailedStrategicIndicators);
    }

    @Test
    public void getDetailedSingleStrategicIndicatorHistoricalDataReadError() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(strategicIndicatorsDomainController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, fromDate, toDate)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/historical", dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/detailed-single-historical-read-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getDetailedStrategicIndicatorPredictionData() throws Exception {
        dtoFactor.setDatasource("Forecast");
        dtoFactor.setRationale("Forecast");

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(strategicIndicatorsDomainController.getDetailedStrategicIndicatorsPrediction(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityFactors/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-prediction",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId);
        verify(strategicIndicatorsDomainController, times(1)).getDetailedStrategicIndicatorsPrediction(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getSingleDetailedStrategicIndicatorPredictionData() throws Exception {
        dtoFactor.setDatasource("Forecast");
        dtoFactor.setRationale("Forecast");

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(strategicIndicatorsDomainController.getDetailedStrategicIndicatorsPrediction(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/prediction", dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
                .andExpect(jsonPath("$[0].date[0]", is(dtoDetailedStrategicIndicator.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoDetailedStrategicIndicator.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoDetailedStrategicIndicator.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoDetailedStrategicIndicator.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoDetailedStrategicIndicator.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoDetailedStrategicIndicator.getValue_description())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andExpect(jsonPath("$[0].factors", hasSize(dtoDetailedStrategicIndicator.getFactors().size())))
                .andExpect(jsonPath("$[0].factors[0].id", is(dtoFactor.getId())))
                .andExpect(jsonPath("$[0].factors[0].name", is(dtoFactor.getName())))
                .andExpect(jsonPath("$[0].factors[0].description", is(dtoFactor.getDescription())))
                .andExpect(jsonPath("$[0].factors[0].value", is(getFloatAsDouble(dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].value_description", is(String.format("%.2f", dtoFactor.getValue()))))
                .andExpect(jsonPath("$[0].factors[0].date[0]", is(dtoFactor.getDate().getYear())))
                .andExpect(jsonPath("$[0].factors[0].date[1]", is(dtoFactor.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].factors[0].date[2]", is(dtoFactor.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].factors[0].datasource", is(dtoFactor.getDatasource())))
                .andExpect(jsonPath("$[0].factors[0].rationale", is(dtoFactor.getRationale())))
                .andExpect(jsonPath("$[0].factors[0].forecastingError", is(dtoFactor.getForecastingError())))
                .andExpect(jsonPath("$[0].factors[0].strategicIndicators[0]", is(dtoFactor.getStrategicIndicators().get(0))))
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())))
                .andDo(document("si/detailed-single-prediction",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"),
                                fieldWithPath("[].factors")
                                        .description("Quality factors that compose the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].name")
                                        .description("Quality factor name"),
                                fieldWithPath("[].factors[].description")
                                        .description("Quality factor description"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].value_description")
                                        .description("Readable quality factor value"),
                                fieldWithPath("[].factors[].date")
                                        .description("Quality factor evaluation date"),
                                fieldWithPath("[].factors[].datasource")
                                        .description("Quality factor source of data"),
                                fieldWithPath("[].factors[].rationale")
                                        .description("Quality factor evaluation rationale"),
                                fieldWithPath("[].factors[].forecastingError")
                                        .description("Description of forecasting errors"),
                                fieldWithPath("[].factors[].strategicIndicators")
                                        .description("List of the strategic indicators that use this quality factor"),
                                fieldWithPath("[].factors[].formattedDate")
                                        .description("Readable quality factor evaluation date"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId);
        verify(strategicIndicatorsDomainController, times(1)).getDetailedStrategicIndicatorsPrediction(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getStrategicIndicatorsPrediction() throws Exception {
        dtoStrategicIndicatorEvaluation.getProbabilities().get(0).setValue(0.8f);
        dtoStrategicIndicatorEvaluation.getProbabilities().get(1).setValue(0.2f);
        dtoStrategicIndicatorEvaluation.getProbabilities().get(2).setValue(0f);

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(strategicIndicatorsDomainController.getStrategicIndicatorsPrediction(technique, freq, horizon, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/prediction")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$[0].dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$[0].name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[0].value", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue()))))
                .andExpect(jsonPath("$[0].probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$[0].probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[1].value", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue()))))
                .andExpect(jsonPath("$[0].probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$[0].probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[2].value", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue()))))
                .andExpect(jsonPath("$[0].probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$[0].probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$[0].hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$[0].hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andDo(document("si/prediction",
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
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].dbId")
                                        .description("Strategic indicator database identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].probabilities")
                                        .description("Strategic indicator categories list"),
                                fieldWithPath("[].probabilities[].id")
                                        .description("Strategic indicator category identifier"),
                                fieldWithPath("[].probabilities[].label")
                                        .description("Strategic indicator category label"),
                                fieldWithPath("[].probabilities[].value")
                                        .description("Strategic indicator category probability"),
                                fieldWithPath("[].probabilities[].color")
                                        .description("Strategic indicator category hexadecimal color"),
                                fieldWithPath("[].probabilities[].upperThreshold")
                                        .description("Strategic indicator category upper threshold"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].datasource")
                                        .description("Strategic indicator source of data"),
                                fieldWithPath("[].categories_description")
                                        .description("Array with the strategic indicator categories and thresholds"),
                                fieldWithPath("[].hasBN")
                                        .description("Does the strategic indicator have a Bayesian Network?"),
                                fieldWithPath("[].hasFeedback")
                                        .description("Does the strategic indicator have any feedback"),
                                fieldWithPath("[].forecastingError")
                                        .description("Errors in the forecasting"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).getStrategicIndicatorsPrediction(technique, freq, horizon, projectExternalId);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void getQualityFactorsEvaluationsForOneStrategicIndicator() throws Exception {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String projectExternalId = "test";
        String strategicIndicatorId = "processperformance";
        when(qualityFactorsDomainController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/current", strategicIndicatorId)
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
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsHistoricalDataForOneStrategicIndicator() throws Exception {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        LocalDate from = dtoQualityFactor.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoQualityFactor.getMetrics().get(0).getDate();
        when(qualityFactorsDomainController.getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(strategicIndicatorId, projectExternalId, from, to)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/historical", strategicIndicatorId)
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
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(strategicIndicatorId, projectExternalId, from, to);
        verifyNoMoreInteractions(qualityFactorsDomainController);
    }

    @Test
    public void getQualityFactorsPredictionDataForOneStrategicIndicator() throws Exception {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactorForPrediction();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String strategicIndicatorId = "processperformance";
        String projectExternalId = "test";
        String freq = "7";
        String horizon = "7";
        String technique = "PROPHET";
        when(qualityFactorsDomainController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId)).thenReturn(dtoQualityFactorList);
        when(qualityFactorsDomainController.getFactorsWithMetricsPrediction(dtoQualityFactorList, technique, freq, horizon, projectExternalId)).thenReturn(dtoQualityFactorList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/qualityFactors/metrics/prediction", strategicIndicatorId)
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
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(strategicIndicatorId, projectExternalId);
        verify(qualityFactorsDomainController, times(1)).getFactorsWithMetricsPrediction(dtoQualityFactorList, technique, freq, horizon, projectExternalId);
    }

    @Test
    public void getAllStrategicIndicators () throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "prj-1";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        project.setId(projectId);
        project.setBacklogId(projectBacklogId);

        when(projectsController.findProjectByExternalId(projectExternalId)).thenReturn(project);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorExternalId = "productquality";
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "codequality";
        qualityFactors.add(factor1);
        String factor2 = "softwarestability";
        qualityFactors.add(factor2);
        String factor3 = "testingstatus";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<Strategic_Indicator> strategicIndicatorList = new ArrayList<>();
        strategicIndicatorList.add(strategicIndicator);

        when(strategicIndicatorsDomainController.getStrategicIndicatorsByProject(project)).thenReturn(strategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$[0].externalId", is(strategicIndicatorExternalId)))
                .andExpect(jsonPath("$[0].name", is(strategicIndicatorName)))
                .andExpect(jsonPath("$[0].description", is(strategicIndicatorDescription)))
                .andExpect(jsonPath("$[0].network", is(nullValue())))
                .andExpect(jsonPath("$[0].qualityFactors", hasSize(3)))
                .andExpect(jsonPath("$[0].qualityFactors[0]", is(factor1)))
                .andExpect(jsonPath("$[0].qualityFactors[1]", is(factor2)))
                .andExpect(jsonPath("$[0].qualityFactors[2]", is(factor3)))
                .andDo(document("si/get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].externalId")
                                        .description("Strategic indicator external identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("[].network")
                                        .description("Strategic indicator bayesian network"),
                                fieldWithPath("[].qualityFactors")
                                        .description("List of the quality factors composing the strategic indicator"),
                                fieldWithPath("[].qualityFactors[]")
                                        .description("Quality factor name"))
                ));

        // Verify mock interactions
        verify(projectsController, times(1)).findProjectByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectsController);

        verify(strategicIndicatorsDomainController, times(1)).getStrategicIndicatorsByProject(project);
        verifyNoMoreInteractions(strategicIndicatorsDomainController);
    }

    @Test
    public void deleteOneStrategicIndicator() throws Exception {
        Long strategicIndicatorId = 1L;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/strategicIndicators/{id}", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("si/delete-one",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).deleteStrategicIndicator(strategicIndicatorId);
    }

    @Test
    public void deleteOneStrategicIndicatorNotFound() throws Exception {
        Long strategicIndicatorId = 1L;
        doThrow(new StrategicIndicatorNotFoundException()).when(strategicIndicatorsDomainController).deleteStrategicIndicator(strategicIndicatorId);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/strategicIndicators/{id}", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());

        // Verify mock interactions
        verify(strategicIndicatorsDomainController, times(1)).deleteStrategicIndicator(strategicIndicatorId);
    }

}