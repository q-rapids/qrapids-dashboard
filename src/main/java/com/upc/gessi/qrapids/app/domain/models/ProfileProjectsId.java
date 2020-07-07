package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProfileProjectsId implements Serializable {
    private Long profile;
    private Long project;

    public ProfileProjectsId() {}

    public ProfileProjectsId(Long profile_id, Long project_id) {
        this.profile = profile_id;
        this.project = project_id;
    }

    // Getter and Setter
    public Long getProfile_id() {
        return profile;
    }

    public void setProfile_id(Long profile_id) {
        this.profile = profile_id;
    }

    public Long getProject_id() {
        return project;
    }

    public void setProject_id(Long project_id) {
        this.project = project_id;
    }
}
