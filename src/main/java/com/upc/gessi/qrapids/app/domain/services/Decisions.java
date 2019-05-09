package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTODecision;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qr.QRGenerator;
import qr.models.QualityRequirementPattern;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class Decisions {

    @Autowired
    DecisionRepository decisionRepository;

    @Autowired
    QRRepository qrRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Value("${pabre.url}")
    String pabreUrl;

    @GetMapping("/api/decisions")
    public List<DTODecision> getDecisions (@RequestParam(value = "prj") String prj, @RequestParam(required = false, defaultValue = "false") boolean qrs) throws Exception {
        Project project = projectRepository.findByExternalId(prj);
        List<DTODecision> DTODecisions = new ArrayList<>();
        if (qrs) {
            QRGenerator qrGenerator = new QRGenerator(pabreUrl);
            List<QualityRequirementPattern> qualityRequirementPatterns = qrGenerator.getAllQRPatterns();
            Map<Integer, QualityRequirementPattern> qualityRequirementPatternMap = new HashMap<>();
            for (QualityRequirementPattern qualityRequirementPattern : qualityRequirementPatterns) {
                qualityRequirementPatternMap.put(qualityRequirementPattern.getId(), qualityRequirementPattern);
            }
            List<DTODecisionQualityRequirement> dtoDecisionQualityRequirements = qrRepository.getAllDecisionsAndQRsByProject_Id(project.getId());
            for (DTODecisionQualityRequirement dtoDecisionQualityRequirement : dtoDecisionQualityRequirements) {
                if (dtoDecisionQualityRequirement.getRequirement() != null) {
                    DTODecisions.add(dtoDecisionQualityRequirement);
                } else {
                    QualityRequirementPattern qualityRequirementPattern = qualityRequirementPatternMap.get(dtoDecisionQualityRequirement.getPatternId());
                    dtoDecisionQualityRequirement.setRequirement(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText());
                    dtoDecisionQualityRequirement.setDescription(qualityRequirementPattern.getForms().get(0).getDescription());
                    dtoDecisionQualityRequirement.setGoal(qualityRequirementPattern.getGoal());
                    DTODecisions.add(dtoDecisionQualityRequirement);
                }
            }
        } else {
            Iterable<Decision> decisions = decisionRepository.findByProject_Id(project.getId());
            for (Decision decision : decisions) {
                String username = "";
                if (decision.getAuthor() != null)
                    username = decision.getAuthor().getUsername();
                DTODecision dtoDecision = new DTODecision(decision.getId(), decision.getType(), new Date(decision.getDate().getTime()), username, decision.getRationale(), decision.getPatternId());
                DTODecisions.add(dtoDecision);
            }
        }
        return DTODecisions;
    }


}
