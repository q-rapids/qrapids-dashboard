package com.upc.gessi.qrapids.app.dto.relations;

public class DTORelationsMetric {
    private String id;
    private String value;
    private String weight;

    public DTORelationsMetric(String id) {
        this.id = id;
    }

    public DTORelationsMetric(DTORelationsMetric metric) {
        this.id = metric.getId();
        this.value = metric.getValue();
        this.weight = metric.getWeight();
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
}
