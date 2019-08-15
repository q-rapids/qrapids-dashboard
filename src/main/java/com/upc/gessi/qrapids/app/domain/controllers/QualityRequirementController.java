package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class QualityRequirementController {

    @Autowired
    private QRRepository qrRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    public QualityRequirement getQualityRequirementForDecision (Decision decision) {
        return qrRepository.findByDecisionId(decision.getId());
    }

    public void ignoreQualityRequirement (Project project, String rationale, int patternId) {
        ignoreQualityRequirementForAlert(project, null, rationale, patternId);
    }

    public void ignoreQualityRequirementForAlert (Project project, Alert alert, String rationale, int patternId) {
        Decision decisionAux = new Decision(DecisionType.IGNORE, new Date(), null, rationale, patternId, project);
        Decision decision = decisionRepository.save(decisionAux);
        if (alert != null) {
            alert.setDecision(decision);
            alert.setStatus(AlertStatus.RESOLVED);
            alertRepository.save(alert);
        }
    }
}
