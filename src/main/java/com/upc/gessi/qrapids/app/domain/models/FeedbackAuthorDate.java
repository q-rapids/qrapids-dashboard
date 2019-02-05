package com.upc.gessi.qrapids.app.domain.models;

import java.io.Serializable;
import java.sql.Date;

public class FeedbackAuthorDate implements Serializable {

    protected Long siId;
    protected String author;
    protected Date date;

    public FeedbackAuthorDate(Long siId, String author, Date date) {
        this.siId = siId;
        this.author = author;
        this.date = date;
    }

    public FeedbackAuthorDate() {
    }

    public Long getSiId() {
        return siId;
    }

    public void setSiId(Long siId) {
        this.siId = siId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}