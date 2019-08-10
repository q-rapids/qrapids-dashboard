package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.services.Util;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedFactorControllerTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackValueRepository feedbackValueRepository;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private Util util;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @InjectMocks
    private FeedFactorController feedFactorController;

    @Test
    public void getFeedbackReport() throws IOException, CategoriesException {
        // Given
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "prj-1";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        project.setId(projectId);
        project.setBacklogId(projectBacklogId);

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

        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));

        Date feedbackDate = Date.valueOf("2019-08-01");
        float newValue = 0.75f;
        float oldValue = 0.7f;
        Feedback feedback = new Feedback(strategicIndicatorId, feedbackDate, null, null, newValue, oldValue);
        List<Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(feedback);

        when(feedbackRepository.findAllBySiId(strategicIndicatorId)).thenReturn(feedbackList);

        String newValueCategory = "Good";
        when(util.getLabel(newValue)).thenReturn(newValueCategory);

        List<DTOSIAssesment> dtoSIAssessmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssessment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssessment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment3);

        Float strategicIndicatorValue = 0.7f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicatorExternalId, strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, dtoSIAssessmentList, LocalDate.now(), datasource, strategicIndicatorId, categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);

        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        FeedbackValues codeQualityFeedbackValues = new FeedbackValues("codequality", "Code Quality", 0.8f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        FeedbackValues softwareStabilityFeedbackValues = new FeedbackValues("softwarestability", "Software Stability", 0.7f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        FeedbackValues testingStatusFeedbackValues = new FeedbackValues("testingstatus", "Testing Status", 0.6f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        List<FeedbackValues> feedbackValuesList = new ArrayList<>();
        feedbackValuesList.add(codeQualityFeedbackValues);
        feedbackValuesList.add(softwareStabilityFeedbackValues);
        feedbackValuesList.add(testingStatusFeedbackValues);

        when(feedbackValueRepository.findAllBySiIdAndFeedbackDate(strategicIndicatorId, feedbackDate)).thenReturn(feedbackValuesList);

        // When
        List<FeedbackFactors> feedbackFactorsList = feedFactorController.getFeedbackReport(strategicIndicatorId);

        // Then
        int expectedNumberOfFeedbackFactors = 1;
        assertEquals(expectedNumberOfFeedbackFactors, feedbackFactorsList.size());

        FeedbackFactors feedbackFactors = feedbackFactorsList.get(0);
        assertEquals(strategicIndicatorId, feedbackFactors.getSiId());
        assertEquals(strategicIndicatorName, feedbackFactors.getSiName());
        assertEquals(feedbackDate.toString(), feedbackFactors.getDate());

        List<String> factorNames = feedbackFactors.getFact();
        assertEquals(3, factorNames.size());
        assertEquals(codeQualityFeedbackValues.getFactorName(), factorNames.get(0));
        assertEquals(softwareStabilityFeedbackValues.getFactorName(), factorNames.get(1));
        assertEquals(testingStatusFeedbackValues.getFactorName(), factorNames.get(2));

        List<Float> factorValues = feedbackFactors.getFactVal();
        assertEquals(3, factorValues.size());
        assertEquals(codeQualityFeedbackValues.getFactorValue(), factorValues.get(0), 0f);
        assertEquals(softwareStabilityFeedbackValues.getFactorValue(), factorValues.get(1), 0f);
        assertEquals(testingStatusFeedbackValues.getFactorValue(), factorValues.get(2), 0f);

        assertNull(feedbackFactors.getAuthor());
        assertEquals(oldValue, feedbackFactors.getOldvalue(), 0f);
        assertEquals(dtoSIAssesment1.getLabel(), feedbackFactors.getOldCategory());
        assertEquals(dtoSIAssesment1.getColor(), feedbackFactors.getOldCategoryColor());

        assertEquals(newValue, feedbackFactors.getNewvalue(), 0f);
        assertEquals(newValueCategory, feedbackFactors.getNewCategory());
        assertEquals(dtoSIAssesment1.getColor(), feedbackFactors.getNewCategoryColor());
    }
}