package com.upc.gessi.qrapids.app.presentation.rest.dto;

import java.util.List;

public class DTOSI {
    private Long id;
    private String externalId;
    private String name;
    private String description;
    private byte[] network;
    private List<String> qualityFactors;

    public DTOSI(Long id, String externalId, String name, String description, byte[] network, List<String> qualityFactors) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.network = network;
        this.qualityFactors = qualityFactors;
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

    public void setExternalId(String externalId) {
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

    public byte[] getNetwork() {
        return network;
    }

    public void setNetwork(byte[] network) {
        this.network = network;
    }

    public List<String> getQualityFactors() {
        return qualityFactors;
    }

    public void setQualityFactors(List<String> qualityFactors) {
        this.qualityFactors = qualityFactors;
    }
}
