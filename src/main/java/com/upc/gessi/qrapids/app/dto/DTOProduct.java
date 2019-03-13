package com.upc.gessi.qrapids.app.dto;

import java.util.List;

public class DTOProduct {
    private Long id;
    private String name;
    private String description;
    private byte[] logo;
    private List<DTOProject> projects;
    
    public DTOProduct(){}
    
    public DTOProduct(Long id, String name, String description, byte[] logo, List<DTOProject> projects) {
    	this.id = id;
    	this.name = name;
    	this.description = description;
    	this.logo = logo;
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

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }
    
    public List<DTOProject> getProjects() {
        return projects;
    }

    public void setProjectIds(List<DTOProject> projects) {
        this.projects = projects;
    }
}