package com.upc.gessi.qrapids.app.dto.assessmentSI;

import java.util.ArrayList;

public class DTOAssessmentSI {
    private ArrayList<DTOCategorySI> probsCategories;

    DTOAssessmentSI(ArrayList<DTOCategorySI> probsCategories) {
        this.probsCategories = probsCategories;
    }

    public DTOAssessmentSI() {
        this.probsCategories = new ArrayList<>();
    }

    public ArrayList<DTOCategorySI> getProbsCategories() {
        return probsCategories;
    }
}
