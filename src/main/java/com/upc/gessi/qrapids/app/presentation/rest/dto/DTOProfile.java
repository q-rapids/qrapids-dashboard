package com.upc.gessi.qrapids.app.presentation.rest.dto;

import java.util.List;

public class DTOProfile {
    private Long id;
    private String name;
    private String description;
    private List<DTOProject> projects;

    public DTOProfile(){}

    public DTOProfile(Long id, String name, String description, List<DTOProject> projects) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projects = projects;
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

    public List<DTOProject> getProjects() {
        return projects;
    }

    public void setProjectIds(List<DTOProject> projects) {
        this.projects = projects;
    }
}
