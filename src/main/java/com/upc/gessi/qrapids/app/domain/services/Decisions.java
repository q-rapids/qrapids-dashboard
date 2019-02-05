package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.dto.DTODecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Decisions {

    @Autowired
    DecisionRepository decisionRepository;

    @GetMapping("/api/decisions")
    public List<DTODecision> getDecisions () throws Exception {
        List<DTODecision> DTODecisions = new ArrayList<>();
        Iterable<Decision> decisions = decisionRepository.findAll();
        for (Decision decision : decisions) {
            DTODecision dtoDecision = new DTODecision(decision.getId(), decision.getType(), new Date(decision.getDate().getTime()), decision.getAuthor(), decision.getRationale());
            DTODecisions.add(dtoDecision);
        }
        return DTODecisions;
    }

}
