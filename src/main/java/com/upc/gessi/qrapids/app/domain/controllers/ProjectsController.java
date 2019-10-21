package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private QMAProjects qmaProjects;

    @Autowired
    private Backlog backlog;

    public Project findProjectByExternalId (String externalId) throws ProjectNotFoundException {
        Project project = projectRepository.findByExternalId(externalId);
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }

    public List<String> getAllProjects () throws IOException, CategoriesException {
        return qmaProjects.getAssessedProjects();
    }

    public List<String> importProjectsAndUpdateDatabase() throws IOException, CategoriesException {
        List<String> projects = getAllProjects();
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

    public List<DTOMilestone> getMilestonesForProject (String projectExternalId, LocalDate date) throws ProjectNotFoundException {
        Project project = findProjectByExternalId(projectExternalId);
        return backlog.getMilestones(project.getBacklogId(), date);
    }

    public List<DTOPhase> getPhasesForProject (String projectExternalId, LocalDate date) throws ProjectNotFoundException {
        Project project = findProjectByExternalId(projectExternalId);
        return backlog.getPhases(project.getBacklogId(), date);
    }

}
