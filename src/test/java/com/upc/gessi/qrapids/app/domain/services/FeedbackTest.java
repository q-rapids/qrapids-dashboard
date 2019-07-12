package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.database.repositories.Feedback.FeedFactorRepositoryImpl;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeedbackTest {

    private MockMvc mockMvc;

    @Mock
    FeedbackRepository feedbackRepository;

    @Mock
    FeedbackValueRepository feedbackValueRepository;

    @Mock
    FeedFactorRepositoryImpl feedFactorRepository;

    @InjectMocks
    private Feedback feedbackController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(feedbackController)
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

        when(feedbackRepository.save(any(com.upc.gessi.qrapids.app.domain.models.Feedback.class))).thenReturn(null);

        when(feedbackValueRepository.save(any(FeedbackValues.class))).thenReturn(null);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/feedback")
                .param("id", strategicIndicatorId.toString())
                .param("newvalue", value.toString())
                .param("oldvalue", oldValue.toString())
                .param("factorIds", gson.toJson(factorIds))
                .param("factorNames", gson.toJson(factorNames))
                .param("factorValues", gson.toJson(factorValues))
                .param("factorEvaluationDates", gson.toJson(factorEvaluationDates));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        // Verify mock interactions
        verify(feedbackRepository, times(1)).save(any(com.upc.gessi.qrapids.app.domain.models.Feedback.class));
        verifyNoMoreInteractions(feedbackRepository);

        verify(feedbackValueRepository, times(3)).save(any(FeedbackValues.class));
        verifyNoMoreInteractions(feedbackValueRepository);
    }

    @Test
    public void addNewFeedbackMissingParams() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/feedback")
                .param("id", "1");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isMethodNotAllowed());
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
        when(feedbackRepository.getFeedback(strategicIndicatorId)).thenReturn(feedbackList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/Feedback/" + strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].siId", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$[0].date", is(date.getTime())))
                .andExpect(jsonPath("$[0].author", is(nullValue())))
                .andExpect(jsonPath("$[0].appUser", is(nullValue())))
                .andExpect(jsonPath("$[0].newvalue", is(newValue)))
                .andExpect(jsonPath("$[0].oldvalue", is(oldValue)));

        // Verify mock interactions
        verify(feedbackRepository, times(1)).getFeedback(strategicIndicatorId);
        verifyNoMoreInteractions(feedbackRepository);
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

        when(feedFactorRepository.getFeedbackReport(strategicIndicatorId, projectId)).thenReturn(feedbackFactorsList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/FeedbackReport/" + strategicIndicatorId)
                .param("prj", projectId);

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
                .andExpect(jsonPath("$[0].newCategoryColor", is(newCategoryColor)));

        // Verify mock interactions
        verify(feedFactorRepository, times(1)).getFeedbackReport(strategicIndicatorId, projectId);
        verifyNoMoreInteractions(feedFactorRepository);
    }
}