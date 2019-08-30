package com.upc.gessi.qrapids.app.presentation.rest.dto;

public class DTOMilestone {

    private String date;
    private String name;
    private String description;
    private String type;

    public DTOMilestone(String date, String name, String description, String type) {
        this.date = date;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
