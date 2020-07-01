package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.exceptions.ElementAlreadyPresentException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class Projects {

    @Autowired
    private ProjectsController projectsController;

    private Logger logger = LoggerFactory.getLogger(Projects.class);

    @GetMapping("/api/projects/import")
    @ResponseStatus(HttpStatus.OK)
    public List<String> importProjects() {
    	try {
            return projectsController.importProjectsAndUpdateDatabase();
        } catch (CategoriesException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on ElasticSearch connection");
        }
    }

    @GetMapping("/api/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProject> getProjects() {
        try {
            return projectsController.getProjects();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOProject getProjectById(@PathVariable String id) {
        try {
            return projectsController.getProjectById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/projects/profile")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProject> getProjectsByProfile(@RequestParam(value = "profile_id", required = false) String profile_id) {
        try {
            Long id = null;
            if (profile_id != null) id = Long.valueOf(profile_id);
            return projectsController.getProjectsByProfile(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @PutMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProject(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String externalId = request.getParameter("externalId");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String backlogId = request.getParameter("backlogId");
            byte[] logoBytes = null;
            if (logo != null) {
                logoBytes = IOUtils.toByteArray(logo.getInputStream());
            }
            if (logoBytes != null && logoBytes.length < 10) {
                DTOProject p = projectsController.getProjectById(Long.toString(id));
                logoBytes = p.getLogo();
            }
            if (projectsController.checkProjectByName(id, name)) {
                DTOProject p = new DTOProject(id, externalId, name, description, logoBytes, true, backlogId);
                projectsController.updateProject(p);
            } else {
                throw new ElementAlreadyPresentException();
            }
        } catch (ElementAlreadyPresentException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("api/milestones")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMilestone> getMilestones (@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) {
        LocalDate localDate = null;
        if (date != null) {
            localDate = LocalDate.parse(date);
        }
        try {
            return projectsController.getMilestonesForProject(prj, localDate);
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }

    @GetMapping("api/phases")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOPhase> getPhases (@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) {
        LocalDate localDate = null;
        if (date != null) {
            localDate = LocalDate.parse(date);
        }
        try {
            return projectsController.getPhasesForProject(prj, localDate);
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }
}
