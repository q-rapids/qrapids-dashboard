package com.upc.gessi.qrapids.app.domain.models;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profile_project",
        uniqueConstraints={@UniqueConstraint(columnNames={"profile_id", "project_id"})})
public class ProfileProjects implements Serializable {

    // SerialVersion UID
    private static final long serialVersionUID = 14L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    Profile profile;

    @ManyToOne
    Project project;

    @Column(name = "all_si")
    boolean allSI;

    public ProfileProjects() {}

    public ProfileProjects(Profile profile, Project project, boolean allSI) {
        this.profile = profile;
        this.project = project;
        this.allSI = allSI;
    }

    public Long getId() {
        return id;
    }

    public Profile getProfile() {
        return profile;
    }

    public Project getProject() {
        return project;
    }

    public boolean isAllSI() {
        return allSI;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setAllSI(boolean allSI) {
        this.allSI = allSI;
    }

}

