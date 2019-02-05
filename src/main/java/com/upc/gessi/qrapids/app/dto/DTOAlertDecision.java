package com.upc.gessi.qrapids.app.dto;

import com.upc.gessi.qrapids.app.domain.models.DecisionType;

public class DTOAlertDecision {
    private String qrGoal;
    private String qrRequirement;
    private String qrDescription;
    private String qrBacklogUrl;
    private DecisionType decisionType;
    private String decisionRationale;

    public DTOAlertDecision () {

    }

    public DTOAlertDecision(String qrGoal, String qrRequirement, String qrDescription, String qrBacklogUrl, DecisionType decisionType, String decisionRationale) {
        this.qrGoal = qrGoal;
        this.qrRequirement = qrRequirement;
        this.qrDescription = qrDescription;
        this.qrBacklogUrl = qrBacklogUrl;
        this.decisionType = decisionType;
        this.decisionRationale = decisionRationale;
    }

    public String getQrGoal() {
        return qrGoal;
    }

    public void setQrGoal(String qrGoal) {
        this.qrGoal = qrGoal;
    }

    public String getQrRequirement() {
        return qrRequirement;
    }

    public void setQrRequirement(String qrRequirement) {
        this.qrRequirement = qrRequirement;
    }

    public String getQrDescription() {
        return qrDescription;
    }

    public void setQrDescription(String qrDescription) {
        this.qrDescription = qrDescription;
    }

    public String getQrBacklogUrl() {
        return qrBacklogUrl;
    }

    public void setQrBacklogUrl(String qrBacklogUrl) {
        this.qrBacklogUrl = qrBacklogUrl;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getDecisionRationale() {
        return decisionRationale;
    }

    public void setDecisionRationale(String decisionRationale) {
        this.decisionRationale = decisionRationale;
    }
}
