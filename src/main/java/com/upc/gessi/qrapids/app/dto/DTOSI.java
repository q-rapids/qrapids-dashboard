package com.upc.gessi.qrapids.app.dto;

import java.util.List;

public class DTOSI {
    private Long id;
    private String external_id;
    private String name;
    private String description;
    private byte[] network;
    private List<String> quality_factors;

    public DTOSI(Long id, String external_id, String name, String description, byte[] network, List<String> quality_factors) {
        this.id = id;
        this.external_id = external_id;
        this.name = name;
        this.description = description;
        this.network = network;
        this.quality_factors = quality_factors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
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

    public List<String> getQuality_factors() {
        return quality_factors;
    }

    public void setQuality_factors(List<String> quality_factors) {
        this.quality_factors = quality_factors;
    }
}
