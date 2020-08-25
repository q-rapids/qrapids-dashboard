package com.upc.gessi.qrapids.app.domain.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssessQF {


    public float assessQF(List<Float> metrics_assessment, int n_metrics) {
        try {
            float total = 0.f;
            float result =0.f;

            for (Float factor : metrics_assessment) {
                total += factor;
            }
            if (total>0)
                result = total/n_metrics;

            return result;

        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AssesSI.class);
            logger.error(e.getMessage(), e);
            return 0.f;
        }
    }

    public float assessQF_weighted(List<Float> metricsAssessment, List<Float> weights) {
        try {
            float total = 0.f;
            float result =0.f;

            for (int i = 0; i < metricsAssessment.size(); i++) {
                total += ((weights.get(i)/100)*metricsAssessment.get(i));
            }
            if (total>0)
                result = total/1; // sum of weights always is 1 = 100%

            return result;

        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AssesSI.class);
            logger.error(e.getMessage(), e);
            return 0.f;
        }
    }
}
