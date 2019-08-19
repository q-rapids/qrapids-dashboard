package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class Projects {

    @Autowired
    private ProjectsController projectsController;

    @GetMapping("/api/projects/import")
    @ResponseStatus(HttpStatus.OK)
    public List<String> importProjects() {
    	try {
            return projectsController.importProjectsAndUpdateDatabase();
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on ElasticSearch connection");
        }
    }
}
