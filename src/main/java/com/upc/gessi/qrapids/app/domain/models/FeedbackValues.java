package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@IdClass(FeedbackValueDate.class)
@Table(name="feedback_values")
public class FeedbackValues implements Serializable {

    @Id
    @Column(name="factor_id")
    private String factorId;

    @Column(name="factor_name")
    private String factorName;

    @Column(name="factor_value")
    private float factorValue;

    @Column(name="evaluation_date")
    private Date evaluationDate;

    @Id
    @Column(name="si_id")
    private Long siId;

    @Id
    @Column(name="feedback_date")
    private Date feedbackDate;

    public FeedbackValues() {
    }

    public FeedbackValues(String factorId, String factorName, float factor_value, Date evaluationDate, Long siId, Date feedbackDate) {
        this.factorId = factorId;
        this.factorName = factorName;
        this.factorValue = factor_value;
        this.evaluationDate = evaluationDate;
        this.siId = siId;
        this.feedbackDate = feedbackDate;
    }

    public String getFactorId() {
        return factorId;
    }

    public void setFactorId(String factor_id) {
        this.factorId = factor_id;
    }

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public float getFactorValue() {
        return factorValue;
    }

    public void setFactorValue(float factorValue) {
        this.factorValue = factorValue;
    }

    public Date getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Date evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Long getSiId() {
        return siId;
    }

    public void setSiId(Long siId) {
        this.siId = siId;
    }

    public Date getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(Date feedbackDate) {
        this.feedbackDate = feedbackDate;
    }
}
