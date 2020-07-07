package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.ProfileNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjects;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectsId;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProfile;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProfilesController {

    @Autowired
    private ProjectRepository projectRep;
    @Autowired
    private ProfileRepository profileRep;
    @Autowired
    private ProfileProjectsRepository profileProjectsRep;


    public boolean checkNewProfileByName(String name) throws ProfileNotFoundException {
        Profile pr = profileRep.findByName(name);
        return (pr == null);
    }

    public void newProfile(String name, String description, List<String> projectIds) {
        List<Project> projects = new ArrayList<>();
        for (int i=0; i<projectIds.size(); i++) {
            Optional<Project> projectOptional = projectRep.findById(Long.parseLong(projectIds.get(i)));
            projectOptional.ifPresent(projects::add);
        }
        // TODO not hardcoded allSI
        Profile profile = new Profile(name, description, projects, true);
        profileRep.save(profile);
    }

    public DTOProfile getProfileById(String id) throws ProfileNotFoundException {
        Optional<Profile> profileOptional = profileRep.findById(Long.parseLong(id));
        if (profileOptional.isPresent()) {
            Profile profile = profileOptional.get();
            List<DTOProject> relatedProjects = new ArrayList<>();
            for (Project proj : profile.getProjects()) {
                DTOProject project = new DTOProject(proj.getId(), proj.getExternalId(), proj.getName(), proj.getDescription(), proj.getLogo(), proj.getActive(), proj.getBacklogId());
                relatedProjects.add(project);
            }
            Collections.sort(relatedProjects, new Comparator<DTOProject>() {
                @Override
                public int compare(DTOProject o1, DTOProject o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return new DTOProfile(profile.getId(), profile.getName(), profile.getDescription(), relatedProjects);
        }
        return null;
    }


    public List<DTOProfile> getProfiles() throws ProfileNotFoundException {
        Iterable<Profile> profileIterable = profileRep.findAll();
        List<Profile> profilesBD = new ArrayList<>();
        profileIterable.forEach(profilesBD::add);
        List<DTOProfile> profiles = new ArrayList<>();
        for (Profile pr : profilesBD) {
            List<DTOProject> relatedProjects = new ArrayList<>();
            for (Project proj : pr.getProjects()) {
                DTOProject project = new DTOProject(proj.getId(), proj.getExternalId(), proj.getName(), proj.getDescription(), proj.getLogo(), proj.getActive(), proj.getBacklogId());
                relatedProjects.add(project);
            }
            Collections.sort(relatedProjects, new Comparator<DTOProject>() {
                @Override
                public int compare(DTOProject o1, DTOProject o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            DTOProfile profile = new DTOProfile(pr.getId(), pr.getName(), pr.getDescription(), relatedProjects);
            profiles.add(profile);
        }
        Collections.sort(profiles, new Comparator<DTOProfile>() {
            @Override
            public int compare(DTOProfile o1, DTOProfile o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return profiles;
    }

    public void deleteProfile(Long id) { profileRep.deleteById(id); }

    public boolean checkProfileByName(Long id, String name) throws ProfileNotFoundException {
        Profile pr = profileRep.findByName(name);
        return (pr == null || pr.getId() == id);
    }

    public void updateProfile(Long id, String name, String description, List<String> projectIds) {
        // TODO delete old ProfileProjects List
        Optional<Profile> profileOptional = profileRep.findById(id);
        Profile oldProfile = profileOptional.get();
        for (ProfileProjects pp: oldProfile.getProfileProjectsList()) {
            if (profileProjectsRep.existsById(new ProfileProjectsId(pp.getProfile().getId(),pp.getProject().getId())))
                profileProjectsRep.deleteById(new ProfileProjectsId(pp.getProfile().getId(),pp.getProject().getId()));
        }
        // update profile information
        List<Project> projects = new ArrayList<>();
        for (int i=0; i<projectIds.size(); i++) {
            Optional<Project> projectOptional = projectRep.findById(Long.parseLong(projectIds.get(i)));
            projectOptional.ifPresent(projects::add);
        }
        // TODO not hardcoded allSI
        Profile profile = new Profile(name, description, projects, true);
        profile.setId(id);
        profileRep.save(profile);
    }
}
