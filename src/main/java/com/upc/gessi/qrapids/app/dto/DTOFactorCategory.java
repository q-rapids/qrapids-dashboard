package com.upc.gessi.qrapids.app.dto;

public class DTOFactorCategory {
    private Long id;
    private String name;
    private String color;
    private float upperThreshold;

    public DTOFactorCategory(Long id, String name, String color, float upperThreshold) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.upperThreshold = upperThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getUpperThreshold() {
        return upperThreshold;
    }

    public void setUpperThreshold(float upperThreshold) {
        this.upperThreshold = upperThreshold;
    }
}
