package com.upc.gessi.qrapids.app.dto;

public class DTOFeedback {
    private Long strategicIndicatorId;
    private String date;
    private String author;
    private float oldValue;
    private float newValue;

    public DTOFeedback(Long strategicIndicatorId, String date, String author, float oldValue, float newValue) {
        this.strategicIndicatorId = strategicIndicatorId;
        this.date = date;
        this.author = author;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getStrategicIndicatorId() {
        return strategicIndicatorId;
    }

    public void setStrategicIndicatorId(Long strategicIndicatorId) {
        this.strategicIndicatorId = strategicIndicatorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public float getOldValue() {
        return oldValue;
    }

    public void setOldValue(float oldValue) {
        this.oldValue = oldValue;
    }

    public float getNewValue() {
        return newValue;
    }

    public void setNewValue(float newValue) {
        this.newValue = newValue;
    }
}
