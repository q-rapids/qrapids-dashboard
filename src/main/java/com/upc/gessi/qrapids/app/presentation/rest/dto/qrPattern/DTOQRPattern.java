package com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern;

import java.util.List;

public class DTOQRPattern {
    private Integer id;
    private String name;
    private String comments;
    private String description;
    private String goal;
    private List<DTOQRForm> forms;
    private String costFunction;

    public DTOQRPattern() {
    }

    public DTOQRPattern(Integer id, String name, String comments, String description, String goal, List<DTOQRForm> forms, String costFunction) {
        this.id = id;
        this.name = name;
        this.comments = comments;
        this.description = description;
        this.goal = goal;
        this.forms = forms;
        this.costFunction = costFunction;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoal() {
        return this.goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<DTOQRForm> getForms() {
        return this.forms;
    }

    public void setForms(List<DTOQRForm> forms) {
        this.forms = forms;
    }

    public String getCostFunction() {
        return this.costFunction;
    }

    public void setCostFunction(String costFunction) {
        this.costFunction = costFunction;
    }
}
