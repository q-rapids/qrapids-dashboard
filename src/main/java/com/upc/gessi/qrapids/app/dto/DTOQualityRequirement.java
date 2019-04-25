package com.upc.gessi.qrapids.app.dto;

public class DTOQualityRequirement {

    private Long id;
    private String requirement;
    private String description;
    private String goal;
    private String backlogId;
    private String backlogUrl;

    public DTOQualityRequirement(Long id, String requirement, String description, String goal, String backlogId, String backlogUrl) {
        this.id = id;
        this.requirement = requirement;
        this.description = description;
        this.goal = goal;
        this.backlogId = backlogId;
        this.backlogUrl = backlogUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getBacklogId() {
        return backlogId;
    }

    public void setBacklogId(String backlogId) {
        this.backlogId = backlogId;
    }

    public String getBacklogUrl() {
        return backlogUrl;
    }

    public void setBacklogUrl(String backlogUrl) {
        this.backlogUrl = backlogUrl;
    }
}
