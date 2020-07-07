package com.upc.gessi.qrapids.app.domain.models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    @OneToMany (cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name="profile_id")
    private List<ProfileProjects> profileProjectsList;

    public Profile() {}

    public Profile(String name, String description, List<Project> projects, boolean allSI) {
        this.name = name;
        this.description = description;
        setProjects(projects, allSI);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Project> getProjects() {
        List<Project> result = new ArrayList<>();
        for (ProfileProjects pp : this.profileProjectsList) {
            result.add(pp.project);
        }
        return result;
    }

    public List<ProfileProjects> getProfileProjectsList() {
        return profileProjectsList;
    }

    public void setProjects(List<Project> projects, boolean allSI) {
        List<ProfileProjects> pp = new ArrayList<>();
        for (Project p : projects) {
            pp.add(new ProfileProjects(this, p, allSI));
        }
        this.profileProjectsList = pp;
    }

}
