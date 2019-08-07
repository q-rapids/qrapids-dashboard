package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedFactorController {

    @Autowired
    private FeedbackRepository fRep;

    @Autowired
    private FeedbackValueRepository fvRep;

    @Autowired
    private StrategicIndicatorRepository siRep;

    public List<FeedbackFactors> getFeedbackReport(Long id, String prj) throws Exception {
        List<FeedbackFactors> feedbackFactorsList = new ArrayList<>();
        List<com.upc.gessi.qrapids.app.domain.models.Feedback> feedbacks = fRep.findAllBySiId(id);
        for (int i = 0; i < feedbacks.size(); ++i) {
            Optional<Strategic_Indicator> strategicIndicatorOptional = siRep.findById(id);
            if (strategicIndicatorOptional.isPresent()) {
                Strategic_Indicator strategicIndicator = strategicIndicatorOptional.get();
                Long siId = strategicIndicator.getId();
                String siName = strategicIndicator.getName();
                String date = feedbacks.get(i).getDate().toString();
                String author = feedbacks.get(i).getAuthor();

                float oldValue = feedbacks.get(i).getOldvalue();
                float newValue = feedbacks.get(i).getNewvalue();
                String oldCategory = null;
                String oldCategoryColor = null;
                String newCategory = null; //util.getLabel(newValue);
                String newCategoryColor = null;
                List<DTOStrategicIndicatorEvaluation> csi = new ArrayList<>(); //qmasi.CurrentEvaluation(prj);
                for (int j = 0; j < csi.size(); ++j) {
                    if (csi.get(j).getId().equals(strategicIndicator.getExternalId())) {
                        oldCategory = csi.get(j).getValue().getSecond();
                        List<DTOSIAssesment> probabilities = csi.get(j).getProbabilities();
                        for (int k = 0; k < probabilities.size(); k++) {
                            if (probabilities.get(k).getLabel().equals(oldCategory)) {
                                oldCategoryColor = probabilities.get(k).getColor();
                            }
                            if (probabilities.get(k).getLabel().equals(newCategory)) {
                                newCategoryColor = probabilities.get(k).getColor();
                            }
                        }
                    }
                }

                Date feedbackDate = feedbacks.get(i).getDate();
                List<FeedbackValues> feedbackValues = fvRep.findAllBySiIdAndFeedbackDate(id, feedbackDate);
                List<String> factors = new ArrayList<>();
                List<Float> factorsVal = new ArrayList<>();
                for (int j = 0; j < feedbackValues.size(); ++j) {
                    factors.add(feedbackValues.get(j).getFactorName());
                    factorsVal.add(feedbackValues.get(j).getFactorValue());
                }

                FeedbackFactors feedback_factors = new FeedbackFactors(siId, siName, date, factors, factorsVal, author, oldValue, oldCategory, oldCategoryColor, newValue, newCategory, newCategoryColor);
                feedbackFactorsList.add(feedback_factors);
            }
        }
        return feedbackFactorsList;
    }

}