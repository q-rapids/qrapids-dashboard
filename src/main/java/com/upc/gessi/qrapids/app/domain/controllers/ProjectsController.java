package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class ProjectsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProfileRepository profileRepository;

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

    public DTOProject getProjectByExternalId(String externalId) throws ProjectNotFoundException {
        Project p = projectRepository.findByExternalId(externalId);
        return new DTOProject(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive(), p.getBacklogId());
    }

    public List<DTOProject> getProjects(Long id) throws ProjectNotFoundException {
        List<DTOProject> projects = new ArrayList<>();
        Iterable<Project> projectIterable;
        if (id == null) { // Without Profile get all Projects
            projectIterable = projectRepository.findAll();
        } else { // With Profile get specific Projects
            Optional<Profile> profileOptional = profileRepository.findById(id);
            Profile profile = profileOptional.get();
            projectIterable = profile.getProjects();
        }
        List<Project> projectsBD = new ArrayList<>();
        projectIterable.forEach(projectsBD::add);
        for (Project p : projectsBD) {
            DTOProject project = new DTOProject(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive(), p.getBacklogId());
            projects.add(project);
        }
        Collections.sort(projects, new Comparator<DTOProject>() {
            @Override
            public int compare(DTOProject o1, DTOProject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return projects;
    }

    public DTOProject getProjectById(String id) throws ProjectNotFoundException {
        Optional<Project> projectOptional = projectRepository.findById(Long.parseLong(id));
        DTOProject dtoProject = null;
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            dtoProject = new DTOProject(project.getId(), project.getExternalId(), project.getName(), project.getDescription(), project.getLogo(), project.getActive(), project.getBacklogId());
        }
        return dtoProject;
    }

    public boolean checkProjectByName(Long id, String name) throws ProjectNotFoundException {
        Project p = projectRepository.findByName(name);
        return (p == null || p.getId() == id);
    }

    public void updateProject(DTOProject p) {
        Project project = new Project(p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive());
        project.setId(p.getId());
        project.setBacklogId(p.getBacklogId());
        projectRepository.save(project);
    }

    public List<String> getAllProjectsExternalID() throws IOException, CategoriesException {
        return qmaProjects.getAssessedProjects();
    }

    public List<String> importProjectsAndUpdateDatabase() throws IOException, CategoriesException {
        List<String> projects = getAllProjectsExternalID();
        updateDataBaseWithNewProjects(projects);
        return projects;
    }

    public void updateDataBaseWithNewProjects (List<String> projects) {
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
