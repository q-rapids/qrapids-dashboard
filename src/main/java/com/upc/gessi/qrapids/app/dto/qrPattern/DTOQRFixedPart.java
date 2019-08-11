package com.upc.gessi.qrapids.app.dto.qrPattern;

public class DTOQRFixedPart {
    private String formText;

    public DTOQRFixedPart(String formText) {
        this.formText = formText;
    }

    public String getFormText() {
        return this.formText;
    }

    public void setFormText(String formText) {
        this.formText = formText;
    }
}
