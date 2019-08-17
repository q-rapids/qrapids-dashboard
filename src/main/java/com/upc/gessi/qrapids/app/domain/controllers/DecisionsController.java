package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qr.models.QualityRequirementPattern;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DecisionsController {

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private QRRepository qrRepository;

    @Autowired
    private QRPatternsController qrPatternsController;

    public List<Decision> getAllDecisionsByProject (Project project) {
        return decisionRepository.findByProject_Id(project.getId());
    }

    public List<DTODecisionQualityRequirement> getAllDecisionsWithQRByProjectAndDates (Project project, Date fromDate, Date toDate) {
        List<QualityRequirementPattern> qualityRequirementPatterns = qrPatternsController.getAllPatterns();
        Map<Integer, QualityRequirementPattern> qualityRequirementPatternMap = new HashMap<>();
        for (QualityRequirementPattern qualityRequirementPattern : qualityRequirementPatterns) {
            qualityRequirementPatternMap.put(qualityRequirementPattern.getId(), qualityRequirementPattern);
        }
        List<Integer> patternIds = new ArrayList<>(qualityRequirementPatternMap.keySet());
        Map<Integer, String> metricForPatternMap = qrPatternsController.getMetricsForPatterns(patternIds);

        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirements = qrRepository.getAllDecisionsAndQRsByProject_Id(project.getId(), fromDate, toDate);
        for (DTODecisionQualityRequirement dtoDecisionQualityRequirement : dtoDecisionQualityRequirements) {
            dtoDecisionQualityRequirement.setElementId(metricForPatternMap.get(dtoDecisionQualityRequirement.getPatternId()));
            if (dtoDecisionQualityRequirement.getRequirement() == null) {
                QualityRequirementPattern qualityRequirementPattern = qualityRequirementPatternMap.get(dtoDecisionQualityRequirement.getPatternId());
                dtoDecisionQualityRequirement.setRequirement(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText());
                dtoDecisionQualityRequirement.setDescription(qualityRequirementPattern.getForms().get(0).getDescription());
                dtoDecisionQualityRequirement.setGoal(qualityRequirementPattern.getGoal());
            }
        }
        return dtoDecisionQualityRequirements;
    }
}
