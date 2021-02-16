package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;

@Entity
@Table(name="metric_categories")
public class MetricCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="color")
    private String color;

    @Column(name="upperThreshold")
    private float upperThreshold;


    public MetricCategory() {
    }

    public MetricCategory(String name, String color, float upperThreshold) {
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
