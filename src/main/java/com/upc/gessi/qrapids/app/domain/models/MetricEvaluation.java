package com.upc.gessi.qrapids.app.domain.models;

import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetricEvaluation;
import util.FormattedDates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MetricEvaluation {

    private List<DTOMetricEvaluation> elements;

    public void Metrics() {
        this.elements = new ArrayList<>();
    }
    public void Metrics (List <DTOMetricEvaluation> metrics) {
        setMetrics(metrics);
    }

    public List <DTOMetricEvaluation> getMetrics() {
        return elements;
    }
    public void setMetrics(List <DTOMetricEvaluation> metrics) {
        this.elements = metrics;
    }

    public void clearQualityFactorsRelations(LocalDate date) {
        String qma_date = FormattedDates.formatDate(date);
        for  (DTOMetricEvaluation metric: elements){
            metric.getQualityFactors().removeIf((String qf_id)  ->  qf_id.contains(qma_date));
        }
    }

    public void clearQualityFactorsRelations( String factorExternalId) {
        for  (DTOMetricEvaluation metric: elements){
            metric.getQualityFactors().removeIf((String qf_id)  ->  qf_id.contains(factorExternalId));
        }
    }

    public void clearQualityFactorsRelations(LocalDate date, String factorExternalId) {
        String qf_hardID = factorExternalId + "-" + FormattedDates.formatDate(date);
        for  (DTOMetricEvaluation metric: elements){
            metric.getQualityFactors().removeIf((String qf_id)  ->  qf_id.contains(qf_hardID));
        }
    }

}
