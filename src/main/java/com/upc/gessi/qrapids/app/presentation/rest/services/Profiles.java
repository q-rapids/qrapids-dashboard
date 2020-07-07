package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.exceptions.ElementAlreadyPresentException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProfile;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@RestController
public class Profiles {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Autowired
    private ProfilesController profileCont;

    private Logger logger = LoggerFactory.getLogger(Products.class);

    @PostMapping("/api/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public void newProfile(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter(NAME);
            String description = request.getParameter(DESCRIPTION);
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (profileCont.checkNewProfileByName(name)) {
                profileCont.newProfile(name, description, projectIds);
            } else {
                throw new ElementAlreadyPresentException();
            }
        } catch (ElementAlreadyPresentException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile name already exists");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProfile> getProfiles() {
	    try {
            return profileCont.getProfiles();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

	@GetMapping("/api/profiles/{id}")
    @ResponseStatus(HttpStatus.OK)
	public DTOProfile getProfileById(@PathVariable String id) {
        try {
            return profileCont.getProfileById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @DeleteMapping("/api/profiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProfile(@PathVariable Long id) {
        profileCont.deleteProfile(id);
    }

    @PutMapping("/api/profiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProfile(@PathVariable Long id, HttpServletRequest request) {
        try {
            String name = request.getParameter(NAME);
            String description = request.getParameter(DESCRIPTION);
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (profileCont.checkProfileByName(id, name)) {
                profileCont.updateProfile(id, name, description, projectIds);
            } else {
                throw new ElementAlreadyPresentException();
            }
        } catch (ElementAlreadyPresentException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile name already exists");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }


}
