package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackValueRepository feedbackValueRepository;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private StrategicIndicatorsController strategicIndicatorsController;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @InjectMocks
    private FeedbackController feedbackController;

    @Before
    public void setUp () {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void saveFeedbackForStrategicIndicator() throws ParseException {
        // Given
        Long strategicIndicatorId = 1L;
        float value = 0.75f;
        float oldValue = 0.6f;
        Feedback feedback = new Feedback(strategicIndicatorId, Date.valueOf("2019-08-01"), "-1", null, value, oldValue);

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

        List<Float> factorValues = new ArrayList<>();
        Float factor1Value = 0.8f;
        factorValues.add(factor1Value);
        Float factor2Value = 0.7f;
        factorValues.add(factor2Value);
        Float factor3Value = 0.6f;
        factorValues.add(factor3Value);

        List<String> factorEvaluationDates = new ArrayList<>();
        String factor1Date = "2019-07-07";
        factorEvaluationDates.add(factor1Date);
        String factor2Date = "2019-07-06";
        factorEvaluationDates.add(factor2Date);
        String factor3Date = "2019-07-05";
        factorEvaluationDates.add(factor3Date);

        // When
        feedbackController.saveFeedbackForStrategicIndicator(feedback, factorIds, factorNames, factorValues, factorEvaluationDates);

        // Then
        verify(feedbackRepository, times(1)).save(feedback);

        ArgumentCaptor<FeedbackValues> feedbackValuesArgumentCaptor = ArgumentCaptor.forClass(FeedbackValues.class);
        verify(feedbackValueRepository, times(3)).save(feedbackValuesArgumentCaptor.capture());

        FeedbackValues feedbackValues1 = feedbackValuesArgumentCaptor.getAllValues().get(0);
        assertEquals(factor1Id, feedbackValues1.getFactorId());
        assertEquals(factor1Name, feedbackValues1.getFactorName());
        assertEquals(factor1Value, feedbackValues1.getFactorValue(), 0f);
        assertEquals(factor1Date, feedbackValues1.getEvaluationDate().toString());

        FeedbackValues feedbackValues2 = feedbackValuesArgumentCaptor.getAllValues().get(1);
        assertEquals(factor2Id, feedbackValues2.getFactorId());
        assertEquals(factor2Name, feedbackValues2.getFactorName());
        assertEquals(factor2Value, feedbackValues2.getFactorValue(), 0f);
        assertEquals(factor2Date, feedbackValues2.getEvaluationDate().toString());

        FeedbackValues feedbackValues3 = feedbackValuesArgumentCaptor.getAllValues().get(2);
        assertEquals(factor3Id, feedbackValues3.getFactorId());
        assertEquals(factor3Name, feedbackValues3.getFactorName());
        assertEquals(factor3Value, feedbackValues3.getFactorValue(), 0f);
        assertEquals(factor3Date, feedbackValues3.getEvaluationDate().toString());
    }

    @Test
    public void getFeedbackReport() throws IOException, CategoriesException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        Long strategicIndicatorId = strategicIndicator.getId();
        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));

        Date feedbackDate = Date.valueOf("2019-08-01");
        float newValue = 0.75f;
        float oldValue = 0.7f;
        Feedback feedback = new Feedback(strategicIndicatorId, feedbackDate, null, null, newValue, oldValue);
        List<Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(feedback);

        when(feedbackRepository.findAllBySiId(strategicIndicatorId)).thenReturn(feedbackList);

        String newValueCategory = "Good";
        when(strategicIndicatorsController.getLabel(newValue)).thenReturn(newValueCategory);

        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDtoStrategicIndicatorEvaluation(strategicIndicator);
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        when(qmaStrategicIndicators.CurrentEvaluation(project.getExternalId())).thenReturn(dtoStrategicIndicatorEvaluationList);

        FeedbackValues codeQualityFeedbackValues = new FeedbackValues("codequality", "Code Quality", 0.8f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        FeedbackValues softwareStabilityFeedbackValues = new FeedbackValues("softwarestability", "Software Stability", 0.7f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        FeedbackValues testingStatusFeedbackValues = new FeedbackValues("testingstatus", "Testing Status", 0.6f, Date.valueOf("2019-07-31"), strategicIndicatorId, feedbackDate);
        List<FeedbackValues> feedbackValuesList = new ArrayList<>();
        feedbackValuesList.add(codeQualityFeedbackValues);
        feedbackValuesList.add(softwareStabilityFeedbackValues);
        feedbackValuesList.add(testingStatusFeedbackValues);

        when(feedbackValueRepository.findAllBySiIdAndFeedbackDate(strategicIndicatorId, feedbackDate)).thenReturn(feedbackValuesList);

        // When
        List<FeedbackFactors> feedbackFactorsList = feedbackController.getFeedbackReport(strategicIndicatorId);

        // Then
        int expectedNumberOfFeedbackFactors = 1;
        assertEquals(expectedNumberOfFeedbackFactors, feedbackFactorsList.size());

        FeedbackFactors feedbackFactors = feedbackFactorsList.get(0);
        assertEquals(strategicIndicatorId, feedbackFactors.getSiId());
        assertEquals(strategicIndicator.getName(), feedbackFactors.getSiName());
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
        assertEquals(dtoStrategicIndicatorEvaluationList.get(0).getProbabilities().get(0).getLabel(), feedbackFactors.getOldCategory());
        assertEquals(dtoStrategicIndicatorEvaluationList.get(0).getProbabilities().get(0).getColor(), feedbackFactors.getOldCategoryColor());

        assertEquals(newValue, feedbackFactors.getNewvalue(), 0f);
        assertEquals(newValueCategory, feedbackFactors.getNewCategory());
        assertEquals(dtoStrategicIndicatorEvaluationList.get(0).getProbabilities().get(0).getColor(), feedbackFactors.getNewCategoryColor());
    }
}