package com.upc.gessi.qrapids.app.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "quality_requirement")
public class QualityRequirement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "requirement")
    private String requirement;
    @Column(name = "description")
    private String description;
    @Column(name = "goal")
    private String goal;
    @Column(name = "backlogId")
    private String backlogId;
    @Column(name = "backlogUrl")
    private String backlogUrl;
    @ManyToOne
    @JoinColumn(name="alertId", referencedColumnName = "id")
    private Alert alert;
    @ManyToOne
    @JoinColumn(name="decisionId", referencedColumnName = "id")
    private Decision decision;
    @ManyToOne
    @JoinColumn(name="projectId", referencedColumnName = "id")
    private Project project;

    public QualityRequirement () {}

    public QualityRequirement(String requirement, String description, String goal, String backlogId, String backlogUrl, Alert alert, Decision decision, Project project) {
        this.requirement = requirement;
        this.description = description;
        this.goal = goal;
        this.backlogId = backlogId;
        this.backlogUrl = backlogUrl;
        this.alert = alert;
        this.decision = decision;
        this.project = project;
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

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}