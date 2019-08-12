package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class QualityRequirementController {

    @Autowired
    private QRRepository qrRepository;

    public QualityRequirement getQualityRequirementForDecision (Decision decision) {
        return qrRepository.findByDecisionId(decision.getId());
    }
}
