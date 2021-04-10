package com.upc.gessi.qrapids.app.presentation.rest.services.helpers;

import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQRPatternsClassifier;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRFixedPart;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRForm;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRPattern;
import qr.models.Classifier;
import qr.models.FixedPart;
import qr.models.Form;
import qr.models.QualityRequirementPattern;

import java.util.ArrayList;
import java.util.List;

public class Mappers {

    private Mappers() {
        throw new IllegalStateException("Utility class");
    }

    public static DTOQRPattern mapQualityRequirementPatternToDTOQRPattern (QualityRequirementPattern qrPattern) {
        List<DTOQRForm> dtoQRFormList = new ArrayList<>();
        for(Form form : qrPattern.getForms()) {
            FixedPart fixedPart = form.getFixedPart();
            DTOQRFixedPart dtoQRFixedPart = new DTOQRFixedPart(fixedPart.getFormText());
            DTOQRForm dtoQRForm = new DTOQRForm(form.getName(), form.getDescription(), form.getComments(), dtoQRFixedPart);
            dtoQRFormList.add(dtoQRForm);
        }
        return new DTOQRPattern(qrPattern.getId(), qrPattern.getName(), qrPattern.getComments(), qrPattern.getDescription(), qrPattern.getGoal(), dtoQRFormList, qrPattern.getCostFunction());
    }

    public static DTOQRPatternsClassifier mapClassifierToDTOQRPatternsClassifier(Classifier classifier) {
        List<DTOQRPatternsClassifier> internalClassifierList = new ArrayList<>();
        for(Classifier internalClassifier : classifier.getInternalClassifiers()) {
            internalClassifierList.add(mapClassifierToDTOQRPatternsClassifier(internalClassifier));
        }
        List<DTOQRPattern> requirementPatternList = new ArrayList<>();
        for(QualityRequirementPattern qrPattern : classifier.getRequirementPatterns()) {
            requirementPatternList.add(mapQualityRequirementPatternToDTOQRPattern(qrPattern));
        }
        return new DTOQRPatternsClassifier(classifier.getId(), classifier.getName(), internalClassifierList, requirementPatternList);
    }

}
