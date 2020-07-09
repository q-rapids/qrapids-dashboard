package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.QualityFactorMetricsNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityFactorMetricsController {

    @Autowired
    private QualityFactorMetricsRepository qualityFactorMetricsRepository;

    // TODO no se porque quiere que sea static !!!
    public static QualityFactorMetrics saveQualityFactorMetric(Float weight, Metric metric, Factor qf) {
        QualityFactorMetrics qualityFactorMetric;
        qualityFactorMetric = new QualityFactorMetrics(weight, metric, qf);
        //qualityFactorMetricsRepository.save(qualityFactorMetric);
        return qualityFactorMetric;
    }

    public void deleteQualityFactorMetric(Long qfMetricsId) throws QualityFactorMetricsNotFoundException {
        if (qualityFactorMetricsRepository.existsById(qfMetricsId)) {
            qualityFactorMetricsRepository.deleteById(qfMetricsId);
        } else {
            throw new QualityFactorMetricsNotFoundException();
        }
    }

}
