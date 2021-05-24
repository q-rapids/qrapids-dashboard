package com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern;

public class DTOQRParameter {
    private Integer id;
    private String name;
    private String description;
    private String correctnessCondition;
    private Integer metricId;
    private String metricName;

    public DTOQRParameter(Integer id, String name, String description, String correctnessCondition, Integer metricId, String metricName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.correctnessCondition = correctnessCondition;
        this.metricId = metricId;
        this.metricName = metricName;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCorrectnessCondition() {
        return this.correctnessCondition;
    }

    public void setCorrectnessCondition(String correctnessCondition) {
        this.correctnessCondition = correctnessCondition;
    }

    public Integer getMetricId() {
        return this.metricId;
    }

    public void setMetricId(Integer metricId) {
        this.metricId = metricId;
    }

    public String getMetricName() {
        return this.metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
