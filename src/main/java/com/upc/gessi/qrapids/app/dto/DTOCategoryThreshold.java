package com.upc.gessi.qrapids.app.dto;

public class DTOCategoryThreshold extends DTOCategory {

    private float upperThreshold;

    public DTOCategoryThreshold(Long id, String name, String color, float upperThreshold) {
        super(id, name, color);
        this.upperThreshold = upperThreshold;
    }

    public float getUpperThreshold() {
        return upperThreshold;
    }

    public void setUpperThreshold(float upperThreshold) {
        this.upperThreshold = upperThreshold;
    }
}
