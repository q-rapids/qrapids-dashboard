package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "strategic_indicator_evaluations")
@IdClass(StrategicDate.class)
public class StrategicIndicatorEvaluation implements Serializable{

    @Id
    @Column(name = "si_id")
    private String id;
    @Id
    @Column(name = "evaluationdate")
    private Date date;
    @Column(name = "name")
    private String name;
    @Column(name = "target")
    private float target;
    @Column(name = "lowerthreshold")
    private float lowerthreshold;
    @Column(name = "upperthreshold")
    private float upperthreshold;
    @Column(name = "value")
    private float value;

    public StrategicIndicatorEvaluation() {
    }

    public StrategicIndicatorEvaluation(String id, Date date, String name, float target, float lowerthreshold, float upperthreshold, float value) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.target = target;
        this.lowerthreshold = lowerthreshold;
        this.upperthreshold = upperthreshold;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getLowerthreshold() {
        return lowerthreshold;
    }

    public void setLowerthreshold(float lowerthreshold) {
        this.lowerthreshold = lowerthreshold;
    }

    public float getUpperthreshold() {
        return upperthreshold;
    }

    public void setUpperthreshold(float upperthreshold) {
        this.upperthreshold = upperthreshold;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
