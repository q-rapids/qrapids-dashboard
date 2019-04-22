package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTOAlertDecision;
import com.upc.gessi.qrapids.app.dto.DTONewAlerts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
    private SimpMessagingTemplate smt;

    @Value("${pabre.url}")
    String pabreUrl;

    @RequestMapping("/api/alerts")
    public List<DTOAlert> getAlerts() throws Exception {
        List<Alert> alerts = ari.findAllByOrderByDateDesc();
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
    public DTONewAlerts countNewAlerts() throws Exception {
        long newAlerts = ari.countByStatus(AlertStatus.NEW);
        long newAlertsWithQR = ari.countByReqAssociatIsTrueAndStatusEquals(AlertStatus.NEW);
        return new DTONewAlerts(newAlerts, newAlertsWithQR);
    }

    @GetMapping("/api/alerts/{id}/qr")
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
    public void ignoreAlert(@PathVariable String id, HttpServletRequest request) throws Exception {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        Decision decisionAux = new Decision(DecisionType.IGNORE, new Date(), null, rationale, Integer.valueOf(patternId));
        Decision decision = decisionRepository.save(decisionAux);

        Alert alert = ari.findAlertById(Long.parseLong(id));
        alert.setDecision(decision);
        alert.setStatus(AlertStatus.RESOLVED);
        ari.save(alert);
    }

    @PostMapping("/api/alerts/{id}/qr")
    public @ResponseBody
    void newQR(@PathVariable String id, HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String rationale = request.getParameter("rationale");
            String patternId = request.getParameter("patternId");
            AppUser user = null;
            if (authentication != null) {
                String author = authentication.getName();
                user = userRepository.findByUsername(author);
            }
            Decision decisionAux = new Decision(DecisionType.ADD, new Date(), user, rationale, Integer.valueOf(patternId));
            Decision decision = decisionRepository.save(decisionAux);

            String requirement = request.getParameter("requirement");
            String description = request.getParameter("description");
            String goal = request.getParameter("goal");
            String backlogId = request.getParameter("backlogId");
            String backlogUrl = request.getParameter("backlogUrl");
            Alert alert = ari.findAlertById(Long.parseLong(id));
            alert.setDecision(decision);
            alert.setStatus(AlertStatus.RESOLVED);
            ari.save(alert);

            QualityRequirement newQualityRequirement = new QualityRequirement(requirement, description, goal, backlogId, backlogUrl, alert, decision);
            qrRepository.save(newQualityRequirement);

            response.setStatus(HttpServletResponse.SC_ACCEPTED);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping(value="/api/notifyAlert", method = RequestMethod.POST)
    public void notify(@RequestBody Map<String, Map<String, String>> requestBody) throws Exception {
        Map<String, String> element = requestBody.get("element");

        String id = element.get("id");
        String name = element.get("name");
        String type = element.get("type");
        float value = Float.parseFloat(element.get("value"));
        float threshold = Float.parseFloat(element.get("threshold"));
        String category = element.get("category");
        qr.models.Alert alert = new qr.models.Alert(id, name, Type.valueOf(type), value, threshold, category, null);

        QRGenerator qrGenerator = new QRGenerator(pabreUrl);
        boolean existsQR = qrGenerator.existsQRPattern(alert);
        Alert al = new Alert(alert.getId_element(), alert.getName(), AlertType.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), new Date(), AlertStatus.NEW, existsQR);
        ari.save(al);
        smt.convertAndSend(
                "/queue/notify",
                new Notification("New Alert")
        );
    }
}
