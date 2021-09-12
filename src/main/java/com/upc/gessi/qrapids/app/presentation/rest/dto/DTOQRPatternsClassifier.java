package com.upc.gessi.qrapids.app.presentation.rest.dto;

import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRPattern;

import java.util.List;

public class DTOQRPatternsClassifier {
    private Integer id;
    private String name;
    private List<DTOQRPatternsClassifier> internalClassifiers;
    private List<DTOQRPattern> requirementPatterns;

    public DTOQRPatternsClassifier() {}

    public DTOQRPatternsClassifier(Integer id, String name, List<DTOQRPatternsClassifier> internalClassifiers, List<DTOQRPattern> requirementPatterns) {
        this.id = id;
        this.name = name;
        this.internalClassifiers = internalClassifiers;
        this.requirementPatterns = requirementPatterns;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DTOQRPatternsClassifier> getInternalClassifiers() {
        return this.internalClassifiers;
    }

    public void setInternalClassifiers(List<DTOQRPatternsClassifier> internalClassifiers) {
        this.internalClassifiers = internalClassifiers;
    }

    public List<DTOQRPattern> getRequirementPatterns() {
        return this.requirementPatterns;
    }

    public void setRequirementPatterns(List<DTOQRPattern> requirementPatterns) {
        this.requirementPatterns = requirementPatterns;
    }
}
