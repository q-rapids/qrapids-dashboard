package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "decision")
public class Decision implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "type")
    private DecisionType type;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "author")
    private String author;
    @Column(name = "rationale")
    private String rationale;
    @Column(name = "patternId")
    private int patternId;

    public Decision () {

    }

    public Decision(DecisionType type, Date date, String author, String rationale, int patternId) {
        this.type = type;
        this.date = date;
        this.author = author;
        this.rationale = rationale;
        this.patternId = patternId;
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

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }
}
