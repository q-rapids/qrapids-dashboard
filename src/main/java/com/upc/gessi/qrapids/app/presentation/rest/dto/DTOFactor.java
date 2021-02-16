package com.upc.gessi.qrapids.app.presentation.rest.dto;

import java.util.List;


public class DTOFactor { // get factor information from DB

    private Long id;
    private String externalId;
    private String name;
    private String description;
    private Float threshold;
    private List<String> metrics;
    private boolean weighted;
    private List<String> metricsWeights;

    public DTOFactor(Long id, String externalId, String name, String description, List<String> metrics, boolean weighted, List<String> metricsWeights) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.metrics = metrics;
        this.weighted = weighted;
        this.metricsWeights = metricsWeights;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getThreshold() {
        return threshold;
    }

    public void setThreshold(Float threshold) {
        this.threshold = threshold;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<String> metrics) {
        this.metrics = metrics;
    }

    public void setWeighted(boolean weighted) { this.weighted = weighted; }

    public boolean isWeighted() { return weighted; }

    public List<String> getMetricsWeights() { return metricsWeights; }


}
