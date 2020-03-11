package com.upc.gessi.qrapids.app.presentation.rest.dto.relations;

import java.util.ArrayList;
import java.util.List;

public class DTORelationsFactor {
    private String id;
    private String name;
    private String weightedValue;
    private String weight;
    private String assessmentValue;
    private List<DTORelationsMetric> metrics;

    public DTORelationsFactor(String id) {
        this.id = id;
        this.metrics = new ArrayList<>();
    }

    public DTORelationsFactor(DTORelationsFactor factor) {
        this.id = factor.getId();
        this.name = factor.getName();
        this.weightedValue = factor.getWeightedValue();
        this.weight = factor.getWeight();
        this.metrics = factor.getMetrics();
        this.assessmentValue = factor.getAssessmentValue();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeightedValue() {
        return weightedValue;
    }

    public void setWeightedValue(String weightedValue) {
        this.weightedValue = weightedValue;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public List<DTORelationsMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<DTORelationsMetric> metrics) {
        this.metrics = metrics;
    }

    public void setMetric(DTORelationsMetric metric) {
        this.metrics.add(metric);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAssessmentValue() {
        return assessmentValue;
    }

    public void setAssessmentValue(String assessmentValue) {
        this.assessmentValue = assessmentValue;
    }
}
