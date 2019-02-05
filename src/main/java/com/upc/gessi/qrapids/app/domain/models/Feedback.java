package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "feedback")
@IdClass(FeedbackAuthorDate.class)
public class Feedback implements Serializable {

    @Id
    @Column(name = "si_id")
    private Long siId;
    @Id
    @Column(name = "evaluationdate")
    private Date date;
    @Id
    @Column(name = "author")
    private String author;
    @Column(name = "newvalue")
    private float newvalue;
    @Column(name = "oldvalue")
    private float oldvalue;

    public Feedback(Long siId, Date date, String author, float newvalue, float oldvalue) {
        this.siId = siId;
        this.date = date;
        this.author = author;
        this.newvalue = newvalue;
        this.oldvalue = oldvalue;
    }

    public Feedback() {
    }

    public Long getSiId() {
        return siId;
    }

    public void setSiId(Long si_id) {
        this.siId = si_id;
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

    public float getNewvalue() {
        return newvalue;
    }

    public void setNewvalue(float newvalue) {
        this.newvalue = newvalue;
    }

    public float getOldvalue() {
        return oldvalue;
    }

    public void setOldvalue(float oldvalue) {
        this.oldvalue = oldvalue;
    }
}
