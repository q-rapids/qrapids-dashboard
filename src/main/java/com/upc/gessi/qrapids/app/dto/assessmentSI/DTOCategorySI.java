package com.upc.gessi.qrapids.app.dto.assessmentSI;

public class DTOCategorySI {
    private String idSICategory;
    private float probSICategory;

    public DTOCategorySI(String idSICategory, float probSICategory) {
        this.idSICategory = idSICategory;
        this.probSICategory = probSICategory;
    }

    public String getIdSICategory() {
        return idSICategory;
    }

    public void setIdSICategory(String idSICategory) {
        this.idSICategory = idSICategory;
    }

    public float getProbSICategory() {
        return probSICategory;
    }

    public void setProbSICategory(float probSICategory) {
        this.probSICategory = probSICategory;
    }
}
