package com.upc.gessi.qrapids.app.dto.assessmentSI;

public class DTOCategorySI {
    private String idcategorySI;
    private float probCategorySI;

    public DTOCategorySI(String idcategorySI, float probCategorySI) {
        this.idcategorySI = idcategorySI;
        this.probCategorySI = probCategorySI;
    }

    public String getIdcategorySI() {
        return idcategorySI;
    }

    public void setIdcategorySI(String idcategorySI) {
        this.idcategorySI = idcategorySI;
    }

    public float getProbCategorySI() {
        return probCategorySI;
    }

    public void setProbCategorySI(float probCategorySI) {
        this.probCategorySI = probCategorySI;
    }
}
