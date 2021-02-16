package com.upc.gessi.qrapids.app.presentation.rest.dto.assessmentSI;

import java.util.ArrayList;

public class DTOAssessmentSI {
    private ArrayList<DTOCategorySI> probsSICategories;

    DTOAssessmentSI(ArrayList<DTOCategorySI> probsSICategories) {
        this.probsSICategories = probsSICategories;
    }

    public DTOAssessmentSI() {
        this.probsSICategories = new ArrayList<>();
    }

    public ArrayList<DTOCategorySI> getProbsSICategories() {
        return probsSICategories;
    }
}
