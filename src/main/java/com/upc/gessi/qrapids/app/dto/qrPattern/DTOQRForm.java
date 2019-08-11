package com.upc.gessi.qrapids.app.dto.qrPattern;

public class DTOQRForm {
    private String name;
    private String description;
    private String comments;
    private DTOQRFixedPart fixedPart;

    public DTOQRForm(String name, String description, String comments, DTOQRFixedPart fixedPart) {
        this.name = name;
        this.description = description;
        this.comments = comments;
        this.fixedPart = fixedPart;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public DTOQRFixedPart getFixedPart() {
        return this.fixedPart;
    }

    public void setFixedPart(DTOQRFixedPart fixedPart) {
        this.fixedPart = fixedPart;
    }
}
