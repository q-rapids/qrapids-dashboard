package com.upc.gessi.qrapids.app.domain.models;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profile_project",
        uniqueConstraints={@UniqueConstraint(columnNames={"profile_id", "project_id"})})
@IdClass(ProfileProjectsId.class)
public class ProfileProjects implements Serializable {

    @Id
    @ManyToOne
    Profile profile;

    @Id
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


    public Profile getProfile() {
        return profile;
    }

    public Project getProject() {
        return project;
    }

    public boolean isAllSI() {
        return allSI;
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

