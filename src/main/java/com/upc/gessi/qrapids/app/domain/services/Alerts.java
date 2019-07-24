package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTOAlert;
import com.upc.gessi.qrapids.app.dto.DTOAlertDecision;
import com.upc.gessi.qrapids.app.dto.DTONewAlerts;
import com.upc.gessi.qrapids.app.dto.DTOQualityRequirement;
import com.upc.gessi.qrapids.app.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import qr.QRGenerator;
import qr.models.QualityRequirementPattern;
import qr.models.enumerations.Type;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class Alerts {

    @Autowired
    private AlertRepository ari;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private QRRepository qrRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SimpMessagingTemplate smt;

    @Autowired
    private Backlog backlog;

    @Value("${pabre.url}")
    String pabreUrl;

    @Autowired
    QRGeneratorFactory qrGeneratorFactory;

    @GetMapping("/api/alerts")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOAlert> getAlerts(@RequestParam(value = "prj") String prj) {
        Project project = projectRepository.findByExternalId(prj);
        if (project != null) {
            List<Alert> alerts = ari.findByProject_IdOrderByDateDesc(project.getId());
            List<Long> alertIds = new ArrayList<>();
            List<DTOAlert> dtoAlerts = new ArrayList<>();
            for (Alert a : alerts) {
                DTOAlert dtoAlert = new DTOAlert(a.getId(), a.getId_element(), a.getName(), a.getType(), a.getValue(), a.getThreshold(), a.getCategory(), new java.sql.Date(a.getDate().getTime()), a.getStatus(), a.isReqAssociat(), null);
                dtoAlerts.add(dtoAlert);
                alertIds.add(a.getId());
            }
            if (!alertIds.isEmpty())
                ari.setViewedStatusFor(alertIds);
            return dtoAlerts;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/alerts/countNew")
    @ResponseStatus(HttpStatus.OK)
    public DTONewAlerts countNewAlerts(@RequestParam(value = "prj") String prj) {
        Project project = projectRepository.findByExternalId(prj);
        if (project != null) {
            long newAlerts = ari.countByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
            long newAlertsWithQR = ari.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
            return new DTONewAlerts(newAlerts, newAlertsWithQR);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/alerts/{id}/qrPatterns")
    @ResponseStatus(HttpStatus.OK)
    public List<QualityRequirementPattern> getQR(@PathVariable String id) {
        Alert alert = ari.findAlertById(Long.parseLong(id));
        if (alert != null) {
            qr.models.Alert alertModel = new qr.models.Alert(alert.getId_element(), alert.getName(), Type.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), null);
            QRGenerator gen = qrGeneratorFactory.getQRGenerator();
            return gen.generateQRs(alertModel);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @GetMapping("/api/alerts/{id}/decision")
    @ResponseStatus(HttpStatus.OK)
    public DTOAlertDecision getAlertDecision(@PathVariable String id) {
        Alert alert = ari.findAlertById(Long.parseLong(id));
        if (alert != null) {
            Decision decision = alert.getDecision();
            DTOAlertDecision alertDecision = new DTOAlertDecision();
            switch (decision.getType()) {
                case ADD:
                    QualityRequirement qr = qrRepository.findByDecisionId(decision.getId());
                    alertDecision.setQrGoal(qr.getGoal());
                    alertDecision.setQrRequirement(qr.getRequirement());
                    alertDecision.setQrDescription(qr.getDescription());
                    alertDecision.setQrBacklogUrl(qr.getBacklogUrl());
                    alertDecision.setDecisionType(decision.getType());
                    alertDecision.setDecisionRationale(decision.getRationale());
                    break;
                case IGNORE:
                    QRGenerator qrGenerator = qrGeneratorFactory.getQRGenerator();
                    qr.models.Alert alertModel = new qr.models.Alert(alert.getId_element(), alert.getName(), Type.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), null);
                    List<QualityRequirementPattern> qrPatternsList = qrGenerator.generateQRs(alertModel);
                    QualityRequirementPattern qrPatternIgnored = null;
                    for (QualityRequirementPattern qrPattern : qrPatternsList) {
                        if (qrPattern.getId() == decision.getPatternId()) {
                            qrPatternIgnored = qrPattern;
                            break;
                        }
                    }
                    if (qrPatternIgnored != null) {
                        alertDecision.setQrGoal(qrPatternIgnored.getGoal());
                        alertDecision.setQrRequirement(qrPatternIgnored.getForms().get(0).getFixedPart().getFormText());
                        alertDecision.setQrDescription(qrPatternIgnored.getForms().get(0).getDescription());
                        alertDecision.setQrBacklogUrl(null);
                        alertDecision.setDecisionType(decision.getType());
                        alertDecision.setDecisionRationale(decision.getRationale());
                    }
                    break;
            }
            return alertDecision;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @PostMapping("/api/alerts/{id}/qr/ignore")
    @ResponseStatus(HttpStatus.CREATED)
    public void ignoreAlert(@PathVariable String id, @RequestParam(value = "prj") String prj, HttpServletRequest request) {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        try {
            ignoreQR(rationale, patternId, id, prj);
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (AlertNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @PostMapping("/api/qr/ignore")
    @ResponseStatus(HttpStatus.CREATED)
    public void ignoreQR (@RequestParam(value = "prj") String prj, HttpServletRequest request) {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        try {
            ignoreQR(rationale, patternId, null, prj);
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (AlertNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    private void ignoreQR (String rationale, String patternId, String alertId, String prj) throws ProjectNotFoundException, AlertNotFoundException {
        Project project = projectRepository.findByExternalId(prj);
        if (project != null) {
            Decision decisionAux = new Decision(DecisionType.IGNORE, new Date(), null, rationale, Integer.valueOf(patternId), project);
            Decision decision = decisionRepository.save(decisionAux);

            if (alertId != null) {
                Alert alert = ari.findAlertById(Long.parseLong(alertId));
                if (alert != null) {
                    alert.setDecision(decision);
                    alert.setStatus(AlertStatus.RESOLVED);
                    ari.save(alert);
                } else {
                    throw new AlertNotFoundException();
                }
            }
        } else {
            throw new ProjectNotFoundException();
        }
    }

    @PostMapping("/api/alerts/{id}/qr")
    @ResponseStatus(HttpStatus.CREATED)
    public DTOQualityRequirement newQRFromAlert(@PathVariable String id, @RequestParam(value = "prj") String prj, HttpServletRequest request, Authentication authentication) {
        try {
            String rationale = request.getParameter("rationale");
            String patternId = request.getParameter("patternId");
            AppUser user = null;
            if (authentication != null) {
                String author = authentication.getName();
                user = userRepository.findByUsername(author);
            }

            String requirement = request.getParameter("requirement");
            String description = request.getParameter("description");
            String goal = request.getParameter("goal");

            QualityRequirement qualityRequirement = addQR(requirement, description, goal, rationale, patternId, id, user, prj);
            DTOQualityRequirement dtoQualityRequirement = new DTOQualityRequirement(
                    qualityRequirement.getId(),
                    new java.sql.Date(qualityRequirement.getAlert().getDate().getTime()),
                    qualityRequirement.getRequirement(),
                    qualityRequirement.getDescription(),
                    qualityRequirement.getGoal(),
                    qualityRequirement.getBacklogId(),
                    qualityRequirement.getBacklogUrl());
            return dtoQualityRequirement;
        } catch (HttpClientErrorException e1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when saving the quality requirement in the backlog");
        } catch (AlertNotFoundException e2) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        } catch (Exception e3) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @PostMapping("/api/qr")
    @ResponseStatus(HttpStatus.CREATED)
    public DTOQualityRequirement newQR (@RequestParam(value = "prj") String prj, HttpServletRequest request, Authentication authentication) {
        try {
            String rationale = request.getParameter("rationale");
            String patternId = request.getParameter("patternId");
            AppUser user = null;
            if (authentication != null) {
                String author = authentication.getName();
                user = userRepository.findByUsername(author);
            }

            String requirement = request.getParameter("requirement");
            String description = request.getParameter("description");
            String goal = request.getParameter("goal");

            QualityRequirement qualityRequirement = addQR(requirement, description, goal, rationale, patternId, null, user, prj);
            DTOQualityRequirement dtoQualityRequirement = new DTOQualityRequirement(
                    qualityRequirement.getId(),
                    new java.sql.Date(qualityRequirement.getDecision().getDate().getTime()),
                    qualityRequirement.getRequirement(),
                    qualityRequirement.getDescription(),
                    qualityRequirement.getGoal(),
                    qualityRequirement.getBacklogId(),
                    qualityRequirement.getBacklogUrl());
            return dtoQualityRequirement;
        } catch (HttpClientErrorException e1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when saving the quality requirement in the backlog");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private QualityRequirement addQR (String requirement, String description, String goal, String rationale, String patternId, String alertId, AppUser user, String prj) throws AlertNotFoundException {
        Project project = projectRepository.findByExternalId(prj);
        Decision decisionAux = new Decision(DecisionType.ADD, new Date(), user, rationale, Integer.valueOf(patternId), project);
        Decision decision = decisionRepository.save(decisionAux);

        Alert alert = null;
        if (alertId != null) {
            alert = ari.findAlertById(Long.parseLong(alertId));
            if (alert != null) {
                alert.setDecision(decision);
                alert.setStatus(AlertStatus.RESOLVED);
                ari.save(alert);
            } else {
                throw new AlertNotFoundException();
            }
        }

        QualityRequirement newQualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qrRepository.save(newQualityRequirement);

        newQualityRequirement = backlog.postNewQualityRequirement(newQualityRequirement);
        qrRepository.save(newQualityRequirement);

        return newQualityRequirement;
    }

    @GetMapping("/api/qr")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityRequirement> getQRs(@RequestParam(value = "prj") String prj) {
        Project project = projectRepository.findByExternalId(prj);
        if (project != null) {
            List<DTOQualityRequirement> dtoQualityRequirements = new ArrayList<>();
            List<QualityRequirement> qualityRequirements = qrRepository.findByProjectIdOrderByDecision_DateDesc(project.getId());
            for (QualityRequirement qualityRequirement : qualityRequirements) {
                DTOQualityRequirement dtoQualityRequirement = new DTOQualityRequirement(
                        qualityRequirement.getId(),
                        new java.sql.Date(qualityRequirement.getDecision().getDate().getTime()),
                        qualityRequirement.getRequirement(),
                        qualityRequirement.getDescription(),
                        qualityRequirement.getGoal(),
                        qualityRequirement.getBacklogId(),
                        qualityRequirement.getBacklogUrl());

                Alert alert = qualityRequirement.getAlert();
                if (alert != null) {
                    DTOAlert dtoAlert = new DTOAlert(
                            alert.getId(),
                            alert.getId_element(),
                            alert.getName(),
                            alert.getType(),
                            alert.getValue(),
                            alert.getThreshold(),
                            alert.getCategory(),
                            new java.sql.Date(alert.getDate().getTime()),
                            alert.getStatus(),
                            alert.isReqAssociat(),
                            null);
                    dtoQualityRequirement.setAlert(dtoAlert);
                }

                dtoQualityRequirement.setBacklogProjectId(project.getBacklogId());

                dtoQualityRequirements.add(dtoQualityRequirement);
            }
            return dtoQualityRequirements;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }


    @PostMapping("/api/notifyAlert")
    @ResponseStatus(HttpStatus.OK)
    public void notify(@RequestBody Map<String, Map<String, String>> requestBody) throws Exception {
        Map<String, String> element = requestBody.get("element");

        String id = element.get("id");
        String name = element.get("name");
        String typeString = element.get("type");
        String valueString = element.get("value");
        String thresholdString = element.get("threshold");
        String category = element.get("category");
        String prj = element.get("project_id");

        if (id != null && name != null && typeString != null && valueString != null && thresholdString != null && category != null && prj != null) {
            try {
                Type type = Type.valueOf(typeString);
                float value = Float.parseFloat(valueString);
                float threshold = Float.parseFloat(thresholdString);

                qr.models.Alert alert = new qr.models.Alert(id, name, type, value, threshold, category, null);

                QRGenerator qrGenerator = qrGeneratorFactory.getQRGenerator();
                boolean existsQR = qrGenerator.existsQRPattern(alert);
                Project project = projectRepository.findByExternalId(prj);
                Alert al = new Alert(alert.getId_element(), alert.getName(), AlertType.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), new Date(), AlertStatus.NEW, existsQR, project);
                ari.save(al);
                smt.convertAndSend(
                        "/queue/notify",
                        new Notification("New Alert")
                );
            }
            catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more arguments have the wrong type");
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more attributes are missing in the request body");
        }
    }

    @PostMapping("/api/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public void newAlert(@RequestBody Map<String, Map<String, String>> requestBody) {
        Map<String, String> element = requestBody.get("element");

        String id = element.get("id");
        String name = element.get("name");
        String typeString = element.get("type");
        String valueString = element.get("value");
        String thresholdString = element.get("threshold");
        String category = element.get("category");
        String prj = element.get("project_id");

        if (id != null && name != null && typeString != null && valueString != null && thresholdString != null && category != null && prj != null) {
            try {
                Type type = Type.valueOf(typeString);
                float value = Float.parseFloat(valueString);
                float threshold = Float.parseFloat(thresholdString);

                qr.models.Alert alert = new qr.models.Alert(id, name, type, value, threshold, category, null);

                QRGenerator qrGenerator = qrGeneratorFactory.getQRGenerator();
                boolean existsQR = qrGenerator.existsQRPattern(alert);
                Project project = projectRepository.findByExternalId(prj);
                Alert al = new Alert(alert.getId_element(), alert.getName(), AlertType.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), new Date(), AlertStatus.NEW, existsQR, project);
                ari.save(al);
                smt.convertAndSend(
                        "/queue/notify",
                        new Notification("New Alert")
                );
            }
            catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more arguments have the wrong type");
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more attributes are missing in the request body");
        }
    }

    @GetMapping("/api/qrPatterns")
    @ResponseStatus(HttpStatus.OK)
    public List<QualityRequirementPattern> getAllQRPatterns () {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllQRPatterns();
    }

    @GetMapping("/api/qrPatterns/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QualityRequirementPattern getQRPattern (@PathVariable String id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getQRPattern(Long.parseLong(id));
    }

    @GetMapping("/api/qrPatterns/{id}/metric")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> getMetricsForQRPattern (@PathVariable String id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        List<Integer> ids = new ArrayList<>();
        Integer patternId = Integer.parseInt(id);
        ids.add(patternId);
        String metric = gen.getMetricsForPatterns(ids).get(patternId);

        Map<String, String> object = new HashMap<>();
        object.put("metric", metric);
        return object;
    }

}
