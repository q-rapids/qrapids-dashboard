package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ProjectsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private QMAProjects qmaProjects;

    public Project findProjectByExternalId (String externalId) throws ProjectNotFoundException {
        Project project = projectRepository.findByExternalId(externalId);
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }

    public List<String> importProjectsAndUpdateDatabase() throws IOException, CategoriesException {
        List<String> projects = qmaProjects.getAssessedProjects();
        updateDataBaseWithNewProjects(projects);
        return projects;
    }

    private void updateDataBaseWithNewProjects (List<String> projects) {
        for (String project : projects) {
            Project projectSaved = projectRepository.findByExternalId(project);
            if (projectSaved == null) {
                Project newProject = new Project(project, project, "No description specified", null, true);
                projectRepository.save(newProject);
            }
        }
    }
}
