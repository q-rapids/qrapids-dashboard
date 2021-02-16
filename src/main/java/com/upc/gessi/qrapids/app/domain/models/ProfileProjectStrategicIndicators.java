package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profile_project_indicator",
        uniqueConstraints={@UniqueConstraint(columnNames={"profile_id", "project_id","strategic_indicator_id"})})
public class ProfileProjectStrategicIndicators implements Serializable {

    // SerialVersion UID
    private static final long serialVersionUID = 14L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    Profile profile;

    @ManyToOne
    Project project;

    @ManyToOne
    Strategic_Indicator strategic_indicator;

    public ProfileProjectStrategicIndicators() {}

    public ProfileProjectStrategicIndicators(Profile profile, Project project, Strategic_Indicator si) {
        this.profile = profile;
        this.project = project;
        this.strategic_indicator = si;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setStrategicIndicator(Strategic_Indicator strategicIndicator) {
        this.strategic_indicator = strategicIndicator;
    }

    public Strategic_Indicator getStrategicIndicator() {
        return strategic_indicator;
    }
}
