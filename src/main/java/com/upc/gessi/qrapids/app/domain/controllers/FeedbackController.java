package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOSIAssessment;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackValueRepository feedbackValueRepository;

    @Autowired
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Autowired
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    public Feedback buildFeedback (Long strategicIndicatorId, Date date, String author, AppUser user, float value, float oldValue) {
        return new Feedback(strategicIndicatorId, date, author, user, value, oldValue);
    }

    public void saveFeedbackForStrategicIndicator (Feedback feedback, List<String> factorIds, List<String> factorNames, List<Float> factorValues, List<String> factorEvaluationDates) throws ParseException {
        feedbackRepository.save(feedback);
        for (int i = 0; i < factorIds.size(); i++) {
            java.util.Date evaluationDateAux = new SimpleDateFormat("yyyy-MM-dd").parse(factorEvaluationDates.get(i));
            Date evaluationDate = new Date(evaluationDateAux.getTime());
            FeedbackValues feedbackValue = new FeedbackValues(factorIds.get(i), factorNames.get(i), factorValues.get(i), evaluationDate, feedback.getSiId(), feedback.getDate());
            feedbackValueRepository.save(feedbackValue);
        }
    }

    public List<Feedback> getFeedbackForStrategicIndicator (Long strategicIndicatorId) {
        return feedbackRepository.findAllBySiId(strategicIndicatorId);
    }

    public List<FeedbackFactors> getFeedbackReport(Long strategicIndicatorId) throws IOException, CategoriesException {
        List<Feedback> feedbackList = feedbackRepository.findAllBySiId(strategicIndicatorId);
        Optional<Strategic_Indicator> strategicIndicatorOptional = strategicIndicatorRepository.findById(strategicIndicatorId);
        if (strategicIndicatorOptional.isPresent()) {
            return getFeedbackReportFromFeedbackList(feedbackList, strategicIndicatorOptional.get());
        }
        return new ArrayList<>();
    }

    private List<FeedbackFactors> getFeedbackReportFromFeedbackList (List<Feedback> feedbackList, Strategic_Indicator strategicIndicator) throws IOException, CategoriesException {
        List<FeedbackFactors> feedbackFactorsList = new ArrayList<>();
        for (int i = 0; i < feedbackList.size(); ++i) {
            Long siId = strategicIndicator.getId();
            String siName = strategicIndicator.getName();
            String date = feedbackList.get(i).getDate().toString();
            String author = feedbackList.get(i).getAuthor();

            float oldValue = feedbackList.get(i).getOldvalue();
            float newValue = feedbackList.get(i).getNewvalue();
            String oldCategory = null;
            String oldCategoryColor = null;
            String newCategory = strategicIndicatorsController.getLabel(newValue);
            String newCategoryColor = null;
            List<DTOStrategicIndicatorEvaluation> csi = qmaStrategicIndicators.CurrentEvaluation(strategicIndicator.getProject().getExternalId());
            OldAndNewCategories oldAndNewCategories = new OldAndNewCategories(strategicIndicator, csi, newCategory).invoke();
            oldCategory = oldAndNewCategories.getOldCategory();
            oldCategoryColor = oldAndNewCategories.getOldCategoryColor();
            newCategoryColor = oldAndNewCategories.getNewCategoryColor();

            Date feedbackDate = feedbackList.get(i).getDate();
            List<FeedbackValues> feedbackValues = feedbackValueRepository.findAllBySiIdAndFeedbackDate(strategicIndicator.getId(), feedbackDate);
            List<String> factors = new ArrayList<>();
            List<Float> factorsVal = new ArrayList<>();
            for (int j = 0; j < feedbackValues.size(); ++j) {
                factors.add(feedbackValues.get(j).getFactorName());
                factorsVal.add(feedbackValues.get(j).getFactorValue());
            }

            FeedbackFactors feedbackFactors = new FeedbackFactors(siId, siName, date, factors, factorsVal, author, oldValue, oldCategory, oldCategoryColor, newValue, newCategory, newCategoryColor);
            feedbackFactorsList.add(feedbackFactors);
        }
        return feedbackFactorsList;
    }

    private static class OldAndNewCategories {
        private Strategic_Indicator strategicIndicator;
        private String oldCategory;
        private String oldCategoryColor;
        private String newCategory;
        private String newCategoryColor;
        private List<DTOStrategicIndicatorEvaluation> csi;

        OldAndNewCategories(Strategic_Indicator strategicIndicator, List<DTOStrategicIndicatorEvaluation> csi, String newCategory) {
            this.strategicIndicator = strategicIndicator;
            this.csi = csi;
            this.newCategory = newCategory;
        }

        String getOldCategory() {
            return oldCategory;
        }

        String getOldCategoryColor() {
            return oldCategoryColor;
        }

        String getNewCategoryColor() {
            return newCategoryColor;
        }

        public OldAndNewCategories invoke() {
            for (DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation : csi) {
                if (dtoStrategicIndicatorEvaluation.getId().equals(strategicIndicator.getExternalId())) {
                    oldCategory = dtoStrategicIndicatorEvaluation.getValue().getSecond();
                    List<DTOSIAssessment> probabilities = dtoStrategicIndicatorEvaluation.getProbabilities();
                    for (DTOSIAssessment probability : probabilities) {
                        if (probability.getLabel().equals(oldCategory)) {
                            oldCategoryColor = probability.getColor();
                        }
                        if (probability.getLabel().equals(newCategory)) {
                            newCategoryColor = probability.getColor();
                        }
                    }
                }
            }
            return this;
        }
    }
}