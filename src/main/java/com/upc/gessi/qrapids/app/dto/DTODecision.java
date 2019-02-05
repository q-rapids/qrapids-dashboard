package com.upc.gessi.qrapids.app.dto;

import com.upc.gessi.qrapids.app.domain.models.DecisionType;
import java.sql.Date;

public class DTODecision {

    private Long id;
    private DecisionType type;
    private Date date;
    private String author;
    private String rationale;

    public DTODecision(Long id, DecisionType type, Date date, String author, String rationale) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.author = author;
        this.rationale = rationale;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.date = date;
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
}
