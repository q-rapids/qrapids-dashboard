package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAFakedata;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StrategicIndicatorsTest {

    private MockMvc mockMvc;

    @Mock
    private QMAFakedata qmaFakedata;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private Forecast forecast;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @InjectMocks
    private StrategicIndicators strategicIndicatorsController;

    private String projectExternalId;

    private DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation;
    private List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();

    private DTOFactor dtoFactor;
    private DTODetailedStrategicIndicator dtoDetailedStrategicIndicator;
    private List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();

    private Double getFloatAsDouble(Float fValue) {
        return Double.valueOf(fValue.toString());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(strategicIndicatorsController)
                .build();

        projectExternalId = "test";

        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        String strategicIndicatorId = "blocking";
        Long strategicIndicatorDbId = 1L;
        String strategicIndicatorName = "Blocking";
        String strategicIndicatorDescription = "Blocking elements";
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicatorId, strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, dtoSIAssesmentList, date, datasource, strategicIndicatorDbId, categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);

        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        String factorId = "blockingcode";
        String factorName = "Blocking code";
        String factorDescription = "Technical debt in software code in terms of rule violations";
        Float factorValue = 0.8f;
        LocalDate evaluationDate = LocalDate.parse(dateString);
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "blocking";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue, evaluationDate, null, factorRationale, strategicIndicatorsList);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(strategicIndicatorId, strategicIndicatorName, dtoFactorList);
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);
    }

    @After
    public void tearDown() {
        dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList = new ArrayList<>();
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluation() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/CurrentEvaluation")
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
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));


        // Verify mock interactions
        verify(qmaStrategicIndicators, times(1)).CurrentEvaluation(projectExternalId);
        verifyNoMoreInteractions(qmaStrategicIndicators);
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluationCategoriesConflict() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId)).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());
    }

    @Test
    public void getStrategicIndicatorsCurrentEvaluationReadError() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluation() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, dtoStrategicIndicatorEvaluation.getId())).thenReturn(dtoStrategicIndicatorEvaluation);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/" + dtoStrategicIndicatorEvaluation.getId() + "/CurrentEvaluation")
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
                .andExpect(jsonPath("$.forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));


        // Verify mock interactions
        verify(qmaStrategicIndicators, times(1)).SingleCurrentEvaluation(projectExternalId, dtoStrategicIndicatorEvaluation.getId());
        verifyNoMoreInteractions(qmaStrategicIndicators);
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluationCategoriesConflict() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, dtoStrategicIndicatorEvaluation.getId())).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/" + dtoStrategicIndicatorEvaluation.getId() + "/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());
    }

    @Test
    public void getSingleStrategicIndicatorCurrentEvaluationReadError() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        when(qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, dtoStrategicIndicatorEvaluation.getId())).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/" + dtoStrategicIndicatorEvaluation.getId() + "/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getStrategicIndicatorsHistoricalData() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaStrategicIndicators.HistoricalData(fromDate, toDate, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/HistoricalData")
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
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));


        // Verify mock interactions
        verify(qmaStrategicIndicators, times(1)).HistoricalData(fromDate, toDate, projectExternalId);
        verifyNoMoreInteractions(qmaStrategicIndicators);
    }

    @Test
    public void getStrategicIndicatorsHistoricalDataCategoriesConflict() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaStrategicIndicators.HistoricalData(fromDate, toDate, projectExternalId)).thenThrow(new CategoriesException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());
    }

    @Test
    public void getStrategicIndicatorsHistoricalDataReadError() throws Exception {
        when(qmaFakedata.usingFakeData()).thenReturn(false);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaStrategicIndicators.HistoricalData(fromDate, toDate, projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDetailedStrategicIndicatorsCurrentEvaluation() throws Exception {
        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(qmaDetailedStrategicIndicators, times(1)).CurrentEvaluation(null, projectExternalId);
        verifyNoMoreInteractions(qmaDetailedStrategicIndicators);
    }

    @Test
    public void getDetailedStrategicIndicatorsCurrentEvaluationReadError() throws Exception {
        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/CurrentEvaluation")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDetailedSingleStrategicIndicator() throws Exception {
        when(qmaDetailedStrategicIndicators.CurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/CurrentEvaluation/" + dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(qmaDetailedStrategicIndicators, times(1)).CurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId);
        verifyNoMoreInteractions(qmaDetailedStrategicIndicators);
    }

    @Test
    public void getDetailedSingleStrategicIndicatorReadError() throws Exception {
        when(qmaDetailedStrategicIndicators.CurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/CurrentEvaluation/" + dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDetailedStrategicIndicatorsHistoricalData() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(null, fromDate, toDate, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(qmaDetailedStrategicIndicators, times(1)).HistoricalData(null, fromDate, toDate, projectExternalId);
        verifyNoMoreInteractions(qmaDetailedStrategicIndicators);
    }

    @Test
    public void getDetailedStrategicIndicatorsHistoricalDataReadError() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(null, fromDate, toDate, projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/HistoricalData")
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDetailedSingleStrategicIndicatorHistoricalData() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(dtoDetailedStrategicIndicator.getId(), fromDate, toDate, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/HistoricalData/" + dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(qmaDetailedStrategicIndicators, times(1)).HistoricalData(dtoDetailedStrategicIndicator.getId(), fromDate, toDate, projectExternalId);
        verifyNoMoreInteractions(qmaDetailedStrategicIndicators);
    }

    @Test
    public void getDetailedSingleStrategicIndicatorHistoricalDataReadError() throws Exception {
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(dtoDetailedStrategicIndicator.getId(), fromDate, toDate, projectExternalId)).thenThrow(new IOException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/HistoricalData/" + dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("from", from)
                .param("to", to);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getQualityFactorsPredicitionData() throws Exception {
        dtoFactor.setDatasource("Forecast");
        dtoFactor.setRationale("Forecast");

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(forecast.ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/PredictionData")
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getQualityFactorsPredicitionData1() throws Exception {
        dtoFactor.setDatasource("Forecast");
        dtoFactor.setRationale("Forecast");

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(forecast.ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/DetailedStrategicIndicators/PredictionData/" + dtoDetailedStrategicIndicator.getId())
                .param("prj", projectExternalId)
                .param("technique", technique)
                .param("horizon", horizon);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoDetailedStrategicIndicator.getId())))
                .andExpect(jsonPath("$[0].name", is(dtoDetailedStrategicIndicator.getName())))
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
                .andExpect(jsonPath("$[0].factors[0].formattedDate", is(dtoFactor.getDate().toString())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId));
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getStrategicIndicatorsPrediction() throws Exception {
        dtoStrategicIndicatorEvaluation.getProbabilities().get(0).setValue(0.8f);
        dtoStrategicIndicatorEvaluation.getProbabilities().get(1).setValue(0.2f);
        dtoStrategicIndicatorEvaluation.getProbabilities().get(2).setValue(0f);

        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        when(forecast.ForecastSI(technique, freq, horizon, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/StrategicIndicators/PredictionData")
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
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));

        // Verify mock interactions
        verify(forecast, times(1)).ForecastSI(technique, freq, horizon, projectExternalId);
        verifyNoMoreInteractions(forecast);
    }
}