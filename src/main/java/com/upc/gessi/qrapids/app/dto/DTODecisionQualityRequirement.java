package com.upc.gessi.qrapids.app.dto;

import com.upc.gessi.qrapids.app.domain.models.DecisionType;

import java.util.Date;

public class DTODecisionQualityRequirement extends DTODecision {

    private String requirement;
    private String description;
    private String goal;
    private String backlogId;
    private String backlogUrl;

    public DTODecisionQualityRequirement(long id, DecisionType type, Date date, String author, String rationale, int patternId, String requirement, String description, String goal, String backlogId, String backlogUrl) {
        super(id, type, date, author, rationale, patternId);
        this.requirement = requirement;
        this.description = description;
        this.goal = goal;
        this.backlogId = backlogId;
        this.backlogUrl = backlogUrl;
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
