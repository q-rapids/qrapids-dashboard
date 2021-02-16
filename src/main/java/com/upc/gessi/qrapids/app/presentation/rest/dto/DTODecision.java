package com.upc.gessi.qrapids.app.presentation.rest.dto;

import com.upc.gessi.qrapids.app.domain.models.DecisionType;

import java.util.Date;

public class DTODecision {

    private long id;
    private DecisionType type;
    private java.sql.Date date;
    private String author;
    private String rationale;
    private int patternId;
    private String elementId;

    public DTODecision(long id, DecisionType type, Date date, String author, String rationale, int patternId) {
        this.id = id;
        this.type = type;
        this.date = new java.sql.Date(date.getTime());
        this.author = author;
        this.rationale = rationale;
        this.patternId = patternId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DecisionType getType() {
        return type;
    }

    public void setType(DecisionType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = new java.sql.Date(date.getTime());
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
