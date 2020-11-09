package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="strategic_indicator_quality_factors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"strategic_indicator_id", "factor_id"}))
public class StrategicIndicatorQualityFactors implements Serializable {

    // SerialVersion UID
    private static final long serialVersionUID = 14L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Factor factor;

    @Column(name = "weight")
    private Float weight;

    @ManyToOne
    private Strategic_Indicator strategic_indicator;

    public StrategicIndicatorQualityFactors() {
    }

    public StrategicIndicatorQualityFactors(Factor qualityFactor, float weight, Strategic_Indicator strategicIndicator) {
        setFactor(qualityFactor);
        setWeight(weight);
        setStrategic_indicator(strategicIndicator);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStrategic_indicator(Strategic_Indicator strategicIndicator) {
        this.strategic_indicator = strategicIndicator;
    }

    public Strategic_Indicator getStrategic_indicator() {
        return strategic_indicator;
    }

    public void setFactor(Factor qualityFactor) {
        this.factor = qualityFactor;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    public Long getId() { return id; }
}
