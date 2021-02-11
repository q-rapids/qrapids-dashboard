package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Mappers;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOAlert;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOAlertDecision;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTONewAlerts;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRPattern;
import com.upc.gessi.qrapids.app.domain.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Locale;
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
    private MetricsController metricsController;

    @Autowired
    private FactorsController factorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    private UsersController usersController;

    private Logger logger = LoggerFactory.getLogger(Alerts.class);

    @GetMapping("/api/alerts")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOAlert> getAlerts(@RequestParam(value = "prj") String prj, @RequestParam(value = "profile", required = false) String profile) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<Alert> alerts = alertsController.getAlertsByProjectAndProfile(project, profile);
            alertsController.setViewedStatusForAlerts(alerts);

            List<DTOAlert> dtoAlerts = new ArrayList<>();
            for (Alert a : alerts) {
                String value_description = String.format(Locale.ENGLISH, "%.2f", a.getValue());
                if (a.getType() == AlertType.METRIC) {
                    value_description = metricsController.getMetricLabelFromValue(a.getValue()) + " (" + value_description + ")";
                } else if (a.getType() == AlertType.FACTOR) {
                    value_description = factorsController.getFactorLabelFromValue(a.getValue()) + " (" + value_description + ")";
                } else if (a.getType() == AlertType.STRATEGIC_INDICATOR) {
                    value_description = strategicIndicatorsController.getLabel(a.getValue()) + " (" + value_description + ")";
                }
                DTOAlert dtoAlert = new DTOAlert(a.getId(), a.getId_element(), a.getName(), a.getType(), a.getValue(), a.getThreshold(), a.getCategory(), new java.sql.Date(a.getDate().getTime()), a.getStatus(), a.isReqAssociat(), null);
                dtoAlert.setValue_description(value_description);
                dtoAlerts.add(dtoAlert);
            }
            return dtoAlerts;
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }

    @GetMapping("/api/alerts/countNew")
    @ResponseStatus(HttpStatus.OK)
    public DTONewAlerts countNewAlerts(@RequestParam(value = "prj") String prj, @RequestParam(value = "profile", required = false) String profile) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            Pair<Long, Long> newAlerts = alertsController.countNewAlertsByProfile(project, profile);
            return new DTONewAlerts(newAlerts.getFirst(), newAlerts.getSecond());
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.ALERT_NOT_FOUND);
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
                default:
                    throw new IllegalStateException("Unexpected value: " + decision.getType());
            }
            return alertDecision;
        } catch (AlertNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.ALERT_NOT_FOUND);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (AlertNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.ALERT_NOT_FOUND);
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
        } catch (HttpClientErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when saving the quality requirement in the backlog");
        } catch (AlertNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.ALERT_NOT_FOUND);
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR);
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
                logger.error(e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more arguments have the wrong type");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.MISSING_ATTRIBUTES_IN_BODY);
        }
    }

}
