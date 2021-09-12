package com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern;

import java.util.List;

public class DTOQRFixedPart {
    private String formText;
    private List<DTOQRParameter> parameters;

    public DTOQRFixedPart(String formText, List<DTOQRParameter> parameters) {
        this.formText = formText;
        this.parameters = parameters;
    }

    public String getFormText() {
        return this.formText;
    }

    public void setFormText(String formText) {
        this.formText = formText;
    }

    public List<DTOQRParameter> getParameters() {
        return this.parameters;
    }

    public void setParameters(List<DTOQRParameter> parameters) {
        this.parameters = parameters;
    }
}
