package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;

@Entity
@Table(name="quality_factor_metrics")
public class QualityFactorMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weight")
    private Float weight;

    @ManyToOne
    private QualityFactor quality_factor;

    @ManyToOne
    private Metric metric;

    public QualityFactorMetrics() {}

    public QualityFactorMetrics(Float weight, Metric metric, QualityFactor qf) {
        setWeight(weight);
        setMetric(metric);
        setQuality_factor(qf);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public QualityFactor getQuality_factor() {
        return quality_factor;
    }

    public void setQuality_factor(QualityFactor quality_factor) {
        this.quality_factor = quality_factor;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
}
