package com.upc.gessi.qrapids.app.dto.relations;

import java.util.ArrayList;
import java.util.List;

public class DTORelationsSI {
    private String id;
    private String value;
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
