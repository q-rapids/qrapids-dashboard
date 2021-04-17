package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.QRPatternsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityRequirementController;
import com.upc.gessi.qrapids.app.domain.controllers.UsersController;
import com.upc.gessi.qrapids.app.domain.exceptions.MissingParametersException;
import com.upc.gessi.qrapids.app.domain.exceptions.QRPatternNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQRPatternsClassifier;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Mappers;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOAlert;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.qrPattern.DTOQRPattern;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import qr.models.Classifier;
import qr.models.QualityRequirementPattern;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QualityRequirements {

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private QualityRequirementController qualityRequirementController;

    @Autowired
    private QRPatternsController qrPatternsController;

    @Autowired
    private UsersController usersController;

    private Logger logger = LoggerFactory.getLogger(QualityRequirements.class);

    @PostMapping("/api/qr/ignore")
    @ResponseStatus(HttpStatus.CREATED)
    public void ignoreQR (@RequestParam(value = "prj") String prj, HttpServletRequest request) {
        String rationale = request.getParameter("rationale");
        String patternId = request.getParameter("patternId");
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            qualityRequirementController.ignoreQualityRequirement(project, rationale, Integer.parseInt(patternId));
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
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
                user = usersController.findUserByName(author);
            }

            String requirement = request.getParameter("requirement");
            String description = request.getParameter("description");
            String goal = request.getParameter("goal");

            Project project = projectsController.findProjectByExternalId(prj);
            QualityRequirement qualityRequirement = qualityRequirementController.addQualityRequirement(requirement, description, goal, rationale, Integer.parseInt(patternId), user, project);

            return new DTOQualityRequirement(
                    qualityRequirement.getId(),
                    new java.sql.Date(qualityRequirement.getDecision().getDate().getTime()),
                    qualityRequirement.getRequirement(),
                    qualityRequirement.getDescription(),
                    qualityRequirement.getGoal(),
                    qualityRequirement.getBacklogId(),
                    qualityRequirement.getBacklogUrl());
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }catch (HttpClientErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when saving the quality requirement in the backlog");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/qr")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityRequirement> getQRs(@RequestParam(value = "prj") String prj) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<QualityRequirement> qualityRequirements = qualityRequirementController.getAllQualityRequirementsForProject(project);
            List<DTOQualityRequirement> dtoQualityRequirements = new ArrayList<>();
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
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }

    @GetMapping("/api/qrPatterns")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQRPattern> getAllQRPatterns () {
        List<QualityRequirementPattern> qualityRequirementPatternList = qrPatternsController.getAllPatterns();
        List<DTOQRPattern> dtoQRPatternList = new ArrayList<>();
        for (QualityRequirementPattern qrPattern : qualityRequirementPatternList) {
            dtoQRPatternList.add(Mappers.mapQualityRequirementPatternToDTOQRPattern(qrPattern));
        }
        return dtoQRPatternList;
    }

    @GetMapping("/api/qrPatterns/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOQRPattern getQRPattern (@PathVariable String id) {
        QualityRequirementPattern qualityRequirementPattern = qrPatternsController.getOnePattern(Integer.parseInt(id));
        return Mappers.mapQualityRequirementPatternToDTOQRPattern(qualityRequirementPattern);
    }

    @GetMapping("/api/qrPatterns/{id}/metric")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> getMetricsForQRPattern (@PathVariable String id) {
        String metric = qrPatternsController.getMetricForPattern(Integer.parseInt(id));
        Map<String, String> object = new HashMap<>();
        object.put("metric", metric);
        return object;
    }

    @PutMapping("/api/qrPatterns/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void saveQRPattern(@PathVariable String id, HttpServletRequest request) {
        try {
            String name = request.getParameter("name");
            String goal = request.getParameter("goal");
            String description = request.getParameter("description");
            String requirement = request.getParameter("requirement");
            if (name == null || goal == null || description == null || requirement == null) {
                throw new MissingParametersException();
            }
            if (!name.equals("")) {
                QualityRequirementPattern oldQRPattern = qrPatternsController.getOnePattern(Integer.parseInt(id));
                if (oldQRPattern == null) {
                    throw new QRPatternNotFoundException();
                }
                qrPatternsController.editPattern(oldQRPattern.getId(), name, goal, description, requirement);
            }
        } catch (MissingParametersException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.MISSING_ATTRIBUTES_IN_BODY);
        } catch (QRPatternNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qrPatternsClassifiers")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQRPatternsClassifier> getAllQRPatternsClassifiers() {
        List<Classifier> classifierList = qrPatternsController.getAllClassifiers();
        List<DTOQRPatternsClassifier> dtoClassifierList = new ArrayList<>();
        for (Classifier classifier : classifierList) {
            dtoClassifierList.add(Mappers.mapClassifierToDTOQRPatternsClassifier(classifier));
        }
        return dtoClassifierList;
    }

}
