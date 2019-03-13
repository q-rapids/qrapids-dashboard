package com.upc.gessi.qrapids.app.dto.relations;

import java.util.ArrayList;
import java.util.List;

public class DTORelationsFactor {
    private String id;
    private String value;
    private String weight;
    private List<DTORelationsMetric> metrics;

    public DTORelationsFactor(String id) {
        this.id = id;
        this.metrics = new ArrayList<>();
    }

    public DTORelationsFactor(DTORelationsFactor factor) {
        this.id = factor.getId();
        this.value = factor.getValue();
        this.weight = factor.getWeight();
        this.metrics = factor.getMetrics();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
}
