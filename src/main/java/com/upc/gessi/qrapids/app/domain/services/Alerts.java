package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTOAlertDecision;
import com.upc.gessi.qrapids.app.dto.DTONewAlerts;
import com.upc.gessi.qrapids.app.dto.DTOQualityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import qr.QRGenerator;
import qr.models.QualityRequirementPattern;
import com.upc.gessi.qrapids.app.dto.DTOAlert;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import qr.models.enumerations.Type;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/api/alerts")
    public List<DTOAlert> getAlerts(@RequestParam(value = "prj") String prj) throws Exception {
        Project project = projectRepository.findByExternalId(prj);
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
    }

    @GetMapping("/api/alerts/new")
    public DTONewAlerts countNewAlerts(@RequestParam(value = "prj") String prj) throws Exception {
        Project project = projectRepository.findByExternalId(prj);
        long newAlerts = ari.countByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
        long newAlertsWithQR = ari.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
        return new DTONewAlerts(newAlerts, newAlertsWithQR);
    }

    @GetMapping("/api/alerts/{id}/qrPatterns")
    public List<QualityRequirementPattern> getQR(@PathVariable String id) throws Exception {
        Alert alert = ari.findAlertById(Long.parseLong(id));
        qr.models.Alert alertModel = new qr.models.Alert(alert.getId_element(), alert.getName(), Type.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), null);
        QRGenerator gen = new QRGenerator(pabreUrl);
        return gen.generateQRs(alertModel);
    }

    @GetMapping("/api/alerts/{id}/decision")
    public DTOAlertDecision getAlertDecision(@PathVariable String id) throws Exception {
        Alert alert = ari.findAlertById(Long.parseLong(id));
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
                QRGenerator qrGenerator = new QRGenerator(pabreUrl);
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
    }

    @PostMapping("/api/alerts/{id}/ignore")
    public void ignoreAlert(@PathVariable String id, @RequestParam(value = "prj") String prj, HttpServletRequest request) throws Exception {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        ignoreQR(rationale, patternId, id, prj);
    }

    @PostMapping("/api/qr/ignore")
    public void ignoreQR (@RequestParam(value = "prj") String prj, HttpServletRequest request) {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        ignoreQR(rationale, patternId, null, prj);
    }

    private void ignoreQR (String rationale, String patternId, String alertId, String prj) {
        Project project = projectRepository.findByExternalId(prj);
        Decision decisionAux = new Decision(DecisionType.IGNORE, new Date(), null, rationale, Integer.valueOf(patternId), project);
        Decision decision = decisionRepository.save(decisionAux);

        if (alertId != null) {
            Alert alert = ari.findAlertById(Long.parseLong(alertId));
            alert.setDecision(decision);
            alert.setStatus(AlertStatus.RESOLVED);
            ari.save(alert);
        }
    }

    @PostMapping("/api/alerts/{id}/qr")
    public @ResponseBody
    DTOQualityRequirement newQRFromAlert(@PathVariable String id, @RequestParam(value = "prj") String prj, HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e2) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    @PostMapping("/api/qr")
    public DTOQualityRequirement newQR (@RequestParam(value = "prj") String prj, HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
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
                    new java.sql.Date(qualityRequirement.getAlert().getDate().getTime()),
                    qualityRequirement.getRequirement(),
                    qualityRequirement.getDescription(),
                    qualityRequirement.getGoal(),
                    qualityRequirement.getBacklogId(),
                    qualityRequirement.getBacklogUrl());
            return dtoQualityRequirement;
        } catch (HttpClientErrorException e1) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    private QualityRequirement addQR (String requirement, String description, String goal, String rationale, String patternId, String alertId, AppUser user, String prj) {
        Project project = projectRepository.findByExternalId(prj);
        Decision decisionAux = new Decision(DecisionType.ADD, new Date(), user, rationale, Integer.valueOf(patternId), project);
        Decision decision = decisionRepository.save(decisionAux);

        Alert alert = null;
        if (alertId != null) {
            alert = ari.findAlertById(Long.parseLong(alertId));
            alert.setDecision(decision);
            alert.setStatus(AlertStatus.RESOLVED);
            ari.save(alert);
        }

        QualityRequirement newQualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qrRepository.save(newQualityRequirement);

        newQualityRequirement = backlog.postNewQualityRequirement(newQualityRequirement);
        qrRepository.save(newQualityRequirement);

        return newQualityRequirement;
    }

    @GetMapping("/api/qr")
    public List<DTOQualityRequirement> getQRs(@RequestParam(value = "prj") String prj) {
        Project project = projectRepository.findByExternalId(prj);
        List<DTOQualityRequirement> dtoQualityRequirements = new ArrayList<>();
        List<QualityRequirement> qualityRequirements = qrRepository.findByProjectIdOrderByDecision_DateDesc(project.getId());
        for (QualityRequirement qualityRequirement : qualityRequirements) {
            DTOQualityRequirement dtoQualityRequirement = new DTOQualityRequirement(
                    qualityRequirement.getId(),
                    new java.sql.Date(qualityRequirement.getAlert().getDate().getTime()),
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
    }


    @RequestMapping(value="/api/notifyAlert", method = RequestMethod.POST)
    public void notify(@RequestBody Map<String, Map<String, String>> requestBody, HttpServletResponse response) throws Exception {
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

                QRGenerator qrGenerator = new QRGenerator(pabreUrl);
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
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
        else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing attributes in body");
        }
    }

    @GetMapping("/api/qrPatterns")
    public List<QualityRequirementPattern> getAllQRPatterns () {
        QRGenerator gen = new QRGenerator(pabreUrl);
        return gen.getAllQRPatterns();
    }

    @GetMapping("/api/qrPatterns/{id}")
    public QualityRequirementPattern getQRPattern (@PathVariable String id) {
        QRGenerator gen = new QRGenerator(pabreUrl);
        return gen.getQRPattern(Long.parseLong(id));
    }

    @GetMapping("/api/qrPatterns/{id}/metric")
    public String getMetricsForQRPattern (@PathVariable String id) {
        QRGenerator gen = new QRGenerator(pabreUrl);
        List<Integer> ids = new ArrayList<>();
        Integer patternId = Integer.parseInt(id);
        ids.add(patternId);
        return gen.getMetricsForPatterns(ids).get(patternId);
    }

}
