package com.upc.gessi.qrapids.app.presentation.rest.dto.relations;

public class DTORelationsMetric {
    private String id;
    private String name;
    private String weightedValue;
    private String weight;
    private String assessmentValue;

    public DTORelationsMetric(String id) {
        this.id = id;
    }

    public DTORelationsMetric(DTORelationsMetric metric) {
        this.id = metric.getId();
        this.name = metric.getName();
        this.weightedValue = metric.getWeightedValue();
        this.weight = metric.getWeight();
        this.assessmentValue = metric.getAssessmentValue();
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAssessmentValue(String assessmentValue) {
        this.assessmentValue = assessmentValue;
    }

    public String getAssessmentValue() {
        return assessmentValue;
    }
}
