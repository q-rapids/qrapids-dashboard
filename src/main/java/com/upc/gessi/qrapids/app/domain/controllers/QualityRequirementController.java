package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;

public class QualityRequirementController {

    @Autowired
    private QRRepository qrRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private Backlog backlog;

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

    public QualityRequirement addQualityRequirement(String requirement, String description, String goal, String rationale, int patternId, AppUser user, Project project) throws HttpClientErrorException {
        return addQualityRequirementForAlert(requirement, description, goal, rationale, patternId, null, user, project);
    }

    public QualityRequirement addQualityRequirementForAlert(String requirement, String description, String goal, String rationale, int patternId, Alert alert, AppUser user, Project project) throws HttpClientErrorException {
        Decision decisionAux = new Decision(DecisionType.ADD, new Date(), user, rationale, patternId, project);
        Decision decision = decisionRepository.save(decisionAux);

        QualityRequirement newQualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qrRepository.save(newQualityRequirement);

        newQualityRequirement = backlog.postNewQualityRequirement(newQualityRequirement);
        qrRepository.save(newQualityRequirement);

        if (alert != null) {
            alert.setDecision(decision);
            alert.setStatus(AlertStatus.RESOLVED);
            alertRepository.save(alert);
            newQualityRequirement.setAlert(alert);
        }

        return newQualityRequirement;
    }
}
