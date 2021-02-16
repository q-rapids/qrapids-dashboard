package com.upc.gessi.qrapids.app.presentation.rest.dto;

import org.springframework.data.util.Pair;

import java.util.List;

public class DTOProfile {
    private Long id;
    private String name;
    private String description;
    private String qualityLevel;
    private String dsiView;
    private String dqfView;
    private String qmView;
    private List<DTOProject> projects;
    private List<Pair<Long, Boolean>> allSIs;


    public DTOProfile(Long id, String name, String description, String qualityLevel, String dsiview, String dqfview,
                      String qmview, List<DTOProject> projects, List<Pair<Long, Boolean>> allSIs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.qualityLevel = qualityLevel;
        this.dsiView = dsiview;
        this.dqfView = dqfview;
        this.qmView = qmview;
        this.projects = projects;
        this.allSIs = allSIs;
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

    public String getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(String qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public String getDsiView() {
        return dsiView;
    }

    public void setDsiView(String dsiView) {
        this.dsiView = dsiView;
    }

    public String getDqfView() {
        return dqfView;
    }

    public void setDqfView(String dqfView) {
        this.dqfView = dqfView;
    }

    public String getQmView() {
        return qmView;
    }

    public void setQmView(String qmView) {
        this.qmView = qmView;
    }

    public List<DTOProject> getProjects() {
        return projects;
    }

    public void setProjects(List<DTOProject> projects) {
        this.projects = projects;
    }

    public List<Pair<Long, Boolean>> getAllSIs() {
        return allSIs;
    }

    public void setAllSIs(List<Pair<Long, Boolean>> allSIs) {
        this.allSIs = allSIs;
    }
}
