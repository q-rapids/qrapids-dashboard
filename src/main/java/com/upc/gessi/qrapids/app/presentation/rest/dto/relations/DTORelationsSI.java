package com.upc.gessi.qrapids.app.presentation.rest.dto.relations;

import java.util.ArrayList;
import java.util.List;

public class DTORelationsSI {
    private String id;
    private String value;
    private String valueDescription;
    private String color;
    private List<DTORelationsFactor> factors;

    public DTORelationsSI(String id) {
        this.id = id;
        this.factors = new ArrayList<>();
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

    public String getValueDescription() {
        return valueDescription;
    }

    public void setValueDescription(String valueDescription) {
        this.valueDescription = valueDescription;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<DTORelationsFactor> getFactors() {
        return factors;
    }

    public void setFactors(List<DTORelationsFactor> factors) {
        this.factors = factors;
    }

    public void setFactor(DTORelationsFactor factor) {
        this.factors.add(factor);
    }
}
