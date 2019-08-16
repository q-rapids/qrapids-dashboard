package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.controllers.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.services.Helpers.Mappers;
import com.upc.gessi.qrapids.app.dto.DTOAlert;
import com.upc.gessi.qrapids.app.dto.DTOAlertDecision;
import com.upc.gessi.qrapids.app.dto.DTONewAlerts;
import com.upc.gessi.qrapids.app.dto.DTOQualityRequirement;
import com.upc.gessi.qrapids.app.dto.qrPattern.DTOQRPattern;
import com.upc.gessi.qrapids.app.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import qr.models.QualityRequirementPattern;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class Alerts {

    @Autowired
    private SimpMessagingTemplate smt;

    @Autowired
    private AlertsController alertsController;

    @Autowired
    private QRPatternsController qrPatternsController;

    @Autowired
    private QualityRequirementController qualityRequirementController;

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private UsersController usersController;

    @GetMapping("/api/alerts")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOAlert> getAlerts(@RequestParam(value = "prj") String prj) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<Alert> alerts = alertsController.getAlerts(project);
            alertsController.setViewedStatusForAlerts(alerts);

            List<DTOAlert> dtoAlerts = new ArrayList<>();
            for (Alert a : alerts) {
                DTOAlert dtoAlert = new DTOAlert(a.getId(), a.getId_element(), a.getName(), a.getType(), a.getValue(), a.getThreshold(), a.getCategory(), new java.sql.Date(a.getDate().getTime()), a.getStatus(), a.isReqAssociat(), null);
                dtoAlerts.add(dtoAlert);
            }
            return dtoAlerts;
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/alerts/countNew")
    @ResponseStatus(HttpStatus.OK)
    public DTONewAlerts countNewAlerts(@RequestParam(value = "prj") String prj) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            Pair<Long, Long> newAlerts = alertsController.countNewAlerts(project);
            return new DTONewAlerts(newAlerts.getFirst(), newAlerts.getSecond());
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/alerts/{id}/qrPatterns")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQRPattern> getQrPatternsForAlert(@PathVariable String id) {
        try {
            List<DTOQRPattern> dtoQRPatternList = new ArrayList<>();
            Alert alert = alertsController.getAlertById(Long.parseLong(id));
            List<QualityRequirementPattern> qrPatternList = qrPatternsController.getPatternsForAlert(alert);
            for (QualityRequirementPattern qrPattern : qrPatternList) {
                dtoQRPatternList.add(Mappers.mapQualityRequirementPatternToDTOQRPattern(qrPattern));
            }
            return dtoQRPatternList;
        } catch (AlertNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @GetMapping("/api/alerts/{id}/decision")
    @ResponseStatus(HttpStatus.OK)
    public DTOAlertDecision getAlertDecision(@PathVariable String id) {
        try {
            Alert alert = alertsController.getAlertById(Long.parseLong(id));
            Decision decision = alert.getDecision();
            DTOAlertDecision alertDecision = new DTOAlertDecision();
            switch (decision.getType()) {
                case ADD:
                    QualityRequirement qr = qualityRequirementController.getQualityRequirementForDecision(decision);
                    alertDecision.setQrGoal(qr.getGoal());
                    alertDecision.setQrRequirement(qr.getRequirement());
                    alertDecision.setQrDescription(qr.getDescription());
                    alertDecision.setQrBacklogUrl(qr.getBacklogUrl());
                    alertDecision.setDecisionType(decision.getType());
                    alertDecision.setDecisionRationale(decision.getRationale());
                    break;
                case IGNORE:
                    List<QualityRequirementPattern> qrPatternsList = qrPatternsController.getPatternsForAlert(alert);
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
        } catch (AlertNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @PostMapping("/api/alerts/{id}/qr/ignore")
    @ResponseStatus(HttpStatus.CREATED)
    public void ignoreAlert(@PathVariable String id, @RequestParam(value = "prj") String prj, HttpServletRequest request) {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        try {
            Alert alert = alertsController.getAlertById(Long.parseLong(id));
            Project project = projectsController.findProjectByExternalId(prj);
            qualityRequirementController.ignoreQualityRequirementForAlert(project, alert, rationale, Integer.parseInt(patternId));
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (AlertNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
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
                user = usersController.findUserByName(author);
            }

            String requirement = request.getParameter("requirement");
            String description = request.getParameter("description");
            String goal = request.getParameter("goal");

            Project project = projectsController.findProjectByExternalId(prj);
            Alert alert = alertsController.getAlertById(Long.parseLong(id));
            QualityRequirement qualityRequirement = qualityRequirementController.addQualityRequirementForAlert(requirement, description, goal, rationale, Integer.parseInt(patternId), alert, user, project);

            return new DTOQualityRequirement(
                    qualityRequirement.getId(),
                    new java.sql.Date(qualityRequirement.getDecision().getDate().getTime()),
                    qualityRequirement.getRequirement(),
                    qualityRequirement.getDescription(),
                    qualityRequirement.getGoal(),
                    qualityRequirement.getBacklogId(),
                    qualityRequirement.getBacklogUrl());
        } catch (HttpClientErrorException e1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when saving the quality requirement in the backlog");
        } catch (AlertNotFoundException e2) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }catch (Exception e3) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    // Legacy
    @PostMapping("/api/notifyAlert")
    @ResponseStatus(HttpStatus.OK)
    public void newAlertLegacy(@RequestBody Map<String, Map<String, String>> requestBody) {
        createAlert(requestBody);
    }

    @PostMapping("/api/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public void newAlert(@RequestBody Map<String, Map<String, String>> requestBody) {
        createAlert(requestBody);
    }

    private void createAlert (Map<String, Map<String, String>> requestBody) {
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
                AlertType type = AlertType.valueOf(typeString);
                float value = Float.parseFloat(valueString);
                float threshold = Float.parseFloat(thresholdString);

                Project project = projectsController.findProjectByExternalId(prj);
                alertsController.createAlert(id, name, type, value, threshold, category, project);

                smt.convertAndSend(
                        "/queue/notify",
                        new Notification("New Alert")
                );
            } catch (ProjectNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more arguments have the wrong type");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more attributes are missing in the request body");
        }
    }

}
