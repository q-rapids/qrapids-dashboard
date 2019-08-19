package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.controllers.FeedbackController;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeedbackTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    FeedbackRepository feedbackRepository;

    @Mock
    FeedbackValueRepository feedbackValueRepository;

    @Mock
    FeedbackController feedbackDomainController;

    @InjectMocks
    private Feedback feedbackController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(feedbackController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void addNewFeedback() throws Exception {
        Long strategicIndicatorId = 1L;
        Float value = 0.75f;
        Float oldValue = 0.6f;

        List<String> factorIds = new ArrayList<>();
        String factor1Id = "factor1";
        factorIds.add(factor1Id);
        String factor2Id = "factor2";
        factorIds.add(factor2Id);
        String factor3Id = "factor3";
        factorIds.add(factor3Id);

        List<String> factorNames = new ArrayList<>();
        String factor1Name = "Factor 1";
        factorNames.add(factor1Name);
        String factor2Name = "Factor 2";
        factorNames.add(factor2Name);
        String factor3Name = "Factor 3";
        factorNames.add(factor3Name);

        List<String> factorValues = new ArrayList<>();
        Float factor1Value = 0.8f;
        factorValues.add(factor1Value.toString());
        Float factor2Value = 0.7f;
        factorValues.add(factor2Value.toString());
        Float factor3Value = 0.6f;
        factorValues.add(factor3Value.toString());

        List<String> factorEvaluationDates = new ArrayList<>();
        String factor1Date = "2019-07-07";
        factorEvaluationDates.add(factor1Date);
        String factor2Date = "2019-07-06";
        factorEvaluationDates.add(factor2Date);
        String factor3Date = "2019-07-05";
        factorEvaluationDates.add(factor3Date);

        // Perform request
        Gson gson = new Gson();
        Map<String, String> feedback = new HashMap<>();
        feedback.put("newvalue", value.toString());
        feedback.put("oldvalue", oldValue.toString());
        feedback.put("factorIds", gson.toJson(factorIds));
        feedback.put("factorNames", gson.toJson(factorNames));
        feedback.put("factorValues", gson.toJson(factorValues));
        feedback.put("factorEvaluationDates", gson.toJson(factorEvaluationDates));

        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/strategicIndicators/{id}/feedback", strategicIndicatorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(feedback));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andDo(document("feedback/add-feedback",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")
                        ),
                        requestFields(
                                fieldWithPath("newvalue")
                                        .description("New strategic indicator value"),
                                fieldWithPath("oldvalue")
                                        .description("Old strategic indicator value"),
                                fieldWithPath("factorIds")
                                        .description("List of the factors identifiers"),
                                fieldWithPath("factorNames")
                                        .description("List of the factors names"),
                                fieldWithPath("factorValues")
                                        .description("List of the factors values"),
                                fieldWithPath("factorEvaluationDates")
                                        .description("List of the factors evaluation dates")
                        )
                ));

        // Verify mock interactions
        ArgumentCaptor<com.upc.gessi.qrapids.app.domain.models.Feedback> feedbackArgumentCaptor = ArgumentCaptor.forClass(com.upc.gessi.qrapids.app.domain.models.Feedback.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> factorIdsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> factorNamesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Float>> factorValuesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> factorEvaluationDatesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(feedbackDomainController, times(1)).saveFeedbackForStrategicIndicator(feedbackArgumentCaptor.capture(), factorIdsArgumentCaptor.capture(), factorNamesArgumentCaptor.capture(), factorValuesArgumentCaptor.capture(), factorEvaluationDatesArgumentCaptor.capture());

        assertEquals(factor1Id, factorIdsArgumentCaptor.getValue().get(0));
        assertEquals(factor2Id, factorIdsArgumentCaptor.getValue().get(1));
        assertEquals(factor3Id, factorIdsArgumentCaptor.getValue().get(2));

        assertEquals(factor1Name, factorNamesArgumentCaptor.getValue().get(0));
        assertEquals(factor2Name, factorNamesArgumentCaptor.getValue().get(1));
        assertEquals(factor3Name, factorNamesArgumentCaptor.getValue().get(2));

        assertEquals(factor1Value, factorValuesArgumentCaptor.getValue().get(0));
        assertEquals(factor2Value, factorValuesArgumentCaptor.getValue().get(1));
        assertEquals(factor3Value, factorValuesArgumentCaptor.getValue().get(2));

        assertEquals(factor1Date, factorEvaluationDatesArgumentCaptor.getValue().get(0));
        assertEquals(factor2Date, factorEvaluationDatesArgumentCaptor.getValue().get(1));
        assertEquals(factor3Date, factorEvaluationDatesArgumentCaptor.getValue().get(2));
    }

    @Test
    public void addNewFeedbackMissingParams() throws Exception {
        Long strategicIndicatorId = 1L;
        Float value = 0.75f;

        Gson gson = new Gson();
        Map<String, String> feedback = new HashMap<>();
        feedback.put("newvalue", value.toString());

        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/strategicIndicators/{id}/feedback", strategicIndicatorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(feedback));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("feedback/add-feedback-missing-param",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verifyZeroInteractions(feedbackDomainController);
    }

    @Test
    public void getFeedback() throws Exception {
        Long strategicIndicatorId = 1L;
        java.util.Date dateAux = new java.util.Date();
        Date date = new Date(dateAux.getTime());
        Double newValue = 0.75;
        Double oldValue = 0.6;

        com.upc.gessi.qrapids.app.domain.models.Feedback feedback = new com.upc.gessi.qrapids.app.domain.models.Feedback(strategicIndicatorId, date, null, null, newValue.floatValue(), oldValue.floatValue());
        List<com.upc.gessi.qrapids.app.domain.models.Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(feedback);
        when(feedbackDomainController.getFeedbackForStrategicIndicator(strategicIndicatorId)).thenReturn(feedbackList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/feedback", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].strategicIndicatorId", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$[0].date", is(date.toString())))
                .andExpect(jsonPath("$[0].author", is(nullValue())))
                .andExpect(jsonPath("$[0].newValue", is(newValue)))
                .andExpect(jsonPath("$[0].oldValue", is(oldValue)))
                .andDo(document("feedback/get-feedback",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")
                        ),
                        responseFields(
                                fieldWithPath("[].strategicIndicatorId")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].date")
                                        .description("Feedback creation date"),
                                fieldWithPath("[].author")
                                        .description("Feedback creator name"),
                                fieldWithPath("[].newValue")
                                        .description("New strategic indicator value"),
                                fieldWithPath("[].oldValue")
                                        .description("Old strategic indicator value")
                        )
                ));

        // Verify mock interactions
        verify(feedbackDomainController, times(1)).getFeedbackForStrategicIndicator(strategicIndicatorId);
        verifyNoMoreInteractions(feedbackDomainController);
    }

    @Test
    public void getFeedbackReport() throws Exception {
        String projectId = "test";
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Blocking";
        String date = "2019-07-07";

        List<String> factorNames = new ArrayList<>();
        String factor1Name = "Factor 1";
        factorNames.add(factor1Name);
        String factor2Name = "Factor 2";
        factorNames.add(factor2Name);
        String factor3Name = "Factor 3";
        factorNames.add(factor3Name);

        List<Float> factorValues = new ArrayList<>();
        Double factor1Value = 0.8;
        factorValues.add(factor1Value.floatValue());
        Double factor2Value = 0.7;
        factorValues.add(factor2Value.floatValue());
        Double factor3Value = 0.6;
        factorValues.add(factor3Value.floatValue());

        Double oldValue = 0.6;
        String oldCategory = "Medium";
        String oldCategoryColor = "Orange";

        Double newValue = 0.75;
        String newCategory = "High";
        String newCategoryColor = "Green";

        FeedbackFactors feedbackFactors = new FeedbackFactors(strategicIndicatorId, strategicIndicatorName, date, factorNames, factorValues, null, oldValue.floatValue(), oldCategory, oldCategoryColor, newValue.floatValue(), newCategory, newCategoryColor);
        List<FeedbackFactors> feedbackFactorsList = new ArrayList<>();
        feedbackFactorsList.add(feedbackFactors);

        when(feedbackDomainController.getFeedbackReport(strategicIndicatorId)).thenReturn(feedbackFactorsList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}/feedbackReport", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].siId", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$[0].siName", is(strategicIndicatorName)))
                .andExpect(jsonPath("$[0].date", is(date)))
                .andExpect(jsonPath("$[0].fact[0]", is(factor1Name)))
                .andExpect(jsonPath("$[0].fact[1]", is(factor2Name)))
                .andExpect(jsonPath("$[0].fact[2]", is(factor3Name)))
                .andExpect(jsonPath("$[0].factVal[0]", is(factor1Value)))
                .andExpect(jsonPath("$[0].factVal[1]", is(factor2Value)))
                .andExpect(jsonPath("$[0].factVal[2]", is(factor3Value)))
                .andExpect(jsonPath("$[0].author", is(nullValue())))
                .andExpect(jsonPath("$[0].oldvalue", is(oldValue)))
                .andExpect(jsonPath("$[0].oldCategory", is(oldCategory)))
                .andExpect(jsonPath("$[0].oldCategoryColor", is(oldCategoryColor)))
                .andExpect(jsonPath("$[0].newvalue", is(newValue)))
                .andExpect(jsonPath("$[0].newCategory", is(newCategory)))
                .andExpect(jsonPath("$[0].newCategoryColor", is(newCategoryColor)))
                .andDo(document("feedback/get-feedback-report",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")
                        ),
                        responseFields(
                                fieldWithPath("[].siId")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].siName")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].date")
                                        .description("Feedback creation date"),
                                fieldWithPath("[].fact")
                                        .description("List with factor names"),
                                fieldWithPath("[].factVal")
                                        .description("List with factor values"),
                                fieldWithPath("[].author")
                                        .description("Feedback creator name"),
                                fieldWithPath("[].oldvalue")
                                        .description("Old strategic indicator value"),
                                fieldWithPath("[].oldCategory")
                                        .description("Old strategic indicator category name"),
                                fieldWithPath("[].oldCategoryColor")
                                        .description("Old strategic indicator category color"),
                                fieldWithPath("[].newvalue")
                                        .description("New strategic indicator value"),
                                fieldWithPath("[].newCategory")
                                        .description("New strategic indicator category name"),
                                fieldWithPath("[].newCategoryColor")
                                        .description("New strategic indicator category color")
                        )
                ));

        // Verify mock interactions
        verify(feedbackDomainController, times(1)).getFeedbackReport(strategicIndicatorId);
        verifyNoMoreInteractions(feedbackDomainController);
    }
}