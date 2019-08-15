package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectsController {

    @Autowired
    private ProjectRepository projectRepository;

    public Project findProjectByExternalId (String externalId) throws ProjectNotFoundException {
        Project project = projectRepository.findByExternalId(externalId);
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }
}
