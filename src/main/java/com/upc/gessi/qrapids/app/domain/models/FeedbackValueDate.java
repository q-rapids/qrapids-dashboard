package com.upc.gessi.qrapids.app.domain.models;

import java.io.Serializable;
import java.sql.Date;

public class FeedbackValueDate implements Serializable {

    private String factorId;
    private Long siId;
    private Date feedbackDate;

    public FeedbackValueDate(String factorId, Long siId, Date feedbackDate) {
        this.factorId = factorId;
        this.siId = siId;
        this.feedbackDate = feedbackDate;
    }

    public FeedbackValueDate() {
    }

    public String getFactorId() {
        return factorId;
    }

    public void setFactorId(String factorId) {
        this.factorId = factorId;
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
