package com.upc.gessi.qrapids.app.dto;



public class DTOProject {
    private Long id;
    private String externalId;
    private String name;
    private String description;
    private byte[] logo;
    private boolean active;
    private String backlogId;
    
    public DTOProject(){}
    
    public DTOProject(Long id, String externalId, String name, String description, byte[] logo, boolean active, String backlogId) {
    	this.id = id;
    	this.externalId = externalId;
    	this.name = name;
    	this.description = description;
    	this.logo = logo;
    	this.active = active;
    	this.backlogId = backlogId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setexternalId(String externalId) {
        this.externalId = externalId;
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
    
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBacklogId() {
        return backlogId;
    }

    public void setBacklogId(String backlogId) {
        this.backlogId = backlogId;
    }
}
