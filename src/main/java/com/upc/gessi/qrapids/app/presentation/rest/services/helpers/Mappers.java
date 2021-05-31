package com.upc.gessi.qrapids.app.presentation.rest.services.helpers;

import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQRPatternsClassifier;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQRPatternsMetric;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRFixedPart;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRForm;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRParameter;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRPattern;
import qr.models.Classifier;
import qr.models.FixedPart;
import qr.models.Form;
import qr.models.Metric;
import qr.models.Param;
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
            List<DTOQRParameter> dtoQRParameterList = new ArrayList<>();
            for(Param parameter : fixedPart.getParameters()) {
                DTOQRParameter dtoQRParameter = new DTOQRParameter(parameter.getId(), parameter.getName(), parameter.getDescription(), parameter.getCorrectnessCondition(), parameter.getMetricId(), parameter.getMetricName());
                dtoQRParameterList.add(dtoQRParameter);
            }
            DTOQRFixedPart dtoQRFixedPart = new DTOQRFixedPart(fixedPart.getFormText(), dtoQRParameterList);
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

    public static DTOQRPatternsMetric mapMetricToDTOQRPatternsMetric(Metric metric) {
        String type = metric.getType();
        DTOQRPatternsMetric mappedMetric = new DTOQRPatternsMetric(metric.getId(), metric.getName(), metric.getDescription(), type);
        if (type.equals("integer") || type.equals("float")) {
            mappedMetric.setMinValue(metric.getMinValue());
            mappedMetric.setMaxValue(metric.getMaxValue());
        } else if (type.equals("domain")) {
            mappedMetric.setPossibleValues(metric.getPossibleValues());
        }
        return mappedMetric;
    }

}
