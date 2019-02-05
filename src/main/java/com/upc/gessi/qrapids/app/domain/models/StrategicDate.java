package com.upc.gessi.qrapids.app.domain.models;

import java.io.Serializable;
import java.sql.Date;

public class StrategicDate implements Serializable {

    protected String id;
    protected Date date;

    public StrategicDate() {
    }

    public StrategicDate(String id, Date date) {
        this.id = id;
        this.date = date;
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
}
