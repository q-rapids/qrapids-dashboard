package com.upc.gessi.qrapids.app.presentation.rest.dto;

public class DTOPhase {

    private String dateFrom;
    private String name;
    private String description;
    private String dateTo;

    public DTOPhase(String dateF, String name, String description, String dateT) {
        this.dateFrom = dateF;
        this.name = name;
        this.description = description;
        this.dateTo = dateT;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateF) {
        this.dateFrom = dateF;
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

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateT) {
        this.dateTo = dateT;
    }


}
