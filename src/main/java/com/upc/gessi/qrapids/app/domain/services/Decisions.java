package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTODecision;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import qr.QRGenerator;
import qr.models.QualityRequirementPattern;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Decisions {

    @Autowired
    DecisionRepository decisionRepository;

    @Autowired
    QRRepository qrRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    QRGeneratorFactory qrGeneratorFactory;

    @GetMapping("/api/decisions")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODecision> getDecisions (@RequestParam(value = "prj") String prj, @RequestParam(required = false, defaultValue = "false") boolean qrs, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        LocalDate fromDate = LocalDate.ofEpochDay(0);
        if (from != null) {
            fromDate = LocalDate.parse(from);
        }
        LocalDate toDate = LocalDate.now();
        if (to != null) {
            toDate = LocalDate.parse(to);
        }
        toDate = toDate.plusDays(1);

        Project project = projectRepository.findByExternalId(prj);
        List<DTODecision> DTODecisions = new ArrayList<>();
        if (qrs) {
            QRGenerator qrGenerator = qrGeneratorFactory.getQRGenerator();
            List<QualityRequirementPattern> qualityRequirementPatterns = qrGenerator.getAllQRPatterns();
            Map<Integer, QualityRequirementPattern> qualityRequirementPatternMap = new HashMap<>();
            for (QualityRequirementPattern qualityRequirementPattern : qualityRequirementPatterns) {
                qualityRequirementPatternMap.put(qualityRequirementPattern.getId(), qualityRequirementPattern);
            }
            List<Integer> patternIds = new ArrayList<>();
            patternIds.addAll(qualityRequirementPatternMap.keySet());
            Map<Integer, String> metricForPatternMap = qrGenerator.getMetricsForPatterns(patternIds);

            List<DTODecisionQualityRequirement> dtoDecisionQualityRequirements = qrRepository.getAllDecisionsAndQRsByProject_Id(project.getId(), Date.valueOf(fromDate), Date.valueOf(toDate));
            for (DTODecisionQualityRequirement dtoDecisionQualityRequirement : dtoDecisionQualityRequirements) {
                dtoDecisionQualityRequirement.setElementId(metricForPatternMap.get(dtoDecisionQualityRequirement.getPatternId()));
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
