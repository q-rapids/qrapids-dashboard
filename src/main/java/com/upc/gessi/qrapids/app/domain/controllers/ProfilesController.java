package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.ProfileNotFoundException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProfileProjectsNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProfile;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import javafx.util.Pair;
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
    private ProfileProjectsController profileProjectsCont;


    public boolean checkNewProfileByName(String name) throws ProfileNotFoundException {
        Profile pr = profileRep.findByName(name);
        return (pr == null);
    }

    public void newProfile(String name, String description, Map<String, org.springframework.data.util.Pair<Boolean,List<String>>> projectInfo) {
        // TODO use project info map
        List<Project> projects = new ArrayList<>();
        for ( Map.Entry<String, org.springframework.data.util.Pair<Boolean, List<String>>> project : projectInfo.entrySet()) {
            Optional<Project> projectOptional = projectRep.findById(Long.parseLong(project.getKey()));
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
            List<Pair<Long,Boolean>> relatedAllSIs = new ArrayList<>();
            for (ProfileProjects pp : profile.getProfileProjectsList()) {
                DTOProject project = new DTOProject(pp.getProject().getId(), pp.getProject().getExternalId(), pp.getProject().getName(), pp.getProject().getDescription(), pp.getProject().getLogo(), pp.getProject().getActive(), pp.getProject().getBacklogId());
                relatedProjects.add(project);
                Pair<Long,Boolean> allSI = new Pair<Long, Boolean>(pp.getProject().getId(), pp.isAllSI());
                relatedAllSIs.add(allSI);
            }
            Collections.sort(relatedProjects, new Comparator<DTOProject>() {
                @Override
                public int compare(DTOProject o1, DTOProject o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return new DTOProfile(profile.getId(), profile.getName(), profile.getDescription(), relatedProjects, relatedAllSIs);
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
            List<Pair<Long,Boolean>> relatedAllSIs = new ArrayList<>();
            for (ProfileProjects pp : pr.getProfileProjectsList()) {
                DTOProject project = new DTOProject(pp.getProject().getId(), pp.getProject().getExternalId(), pp.getProject().getName(), pp.getProject().getDescription(), pp.getProject().getLogo(), pp.getProject().getActive(), pp.getProject().getBacklogId());
                relatedProjects.add(project);
                Pair<Long,Boolean> allSI = new Pair<Long, Boolean>(pp.getProject().getId(), pp.isAllSI());
                relatedAllSIs.add(allSI);
            }
            Collections.sort(relatedProjects, new Comparator<DTOProject>() {
                @Override
                public int compare(DTOProject o1, DTOProject o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            DTOProfile profile = new DTOProfile(pr.getId(), pr.getName(), pr.getDescription(), relatedProjects, relatedAllSIs);
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

    public void updateProfile(Long id, String name, String description, List<String> projectIds) throws ProfileProjectsNotFoundException {
        Optional<Profile> profileOptional = profileRep.findById(id);
        Profile profile = profileOptional.get();
        // update profile information
        profile.setName(name);
        profile.setDescription(description);
        // delete old ProfileProjects List
        List<ProfileProjects> oldProfileProjectsList = profile.getProfileProjectsList();
        profile.setProfileProjectsList(null);
        for (ProfileProjects pp: oldProfileProjectsList) {
            profileProjectsCont.deleteProfileProject(pp.getId());
        }
        // create new ProfileProjects List
        List<ProfileProjects> newProfileProjectsList = new ArrayList<>();
        for (int i=0; i<projectIds.size(); i++) {
            Optional<Project> projectOptional = projectRep.findById(Long.parseLong(projectIds.get(i)));
            // TODO not hardcoded allSI
            ProfileProjects pp = profileProjectsCont.saveProfileProject(profile,projectOptional.get(),true);
            newProfileProjectsList.add(pp);
        }
        // set new ProfileProjects List to this profile
        profile.setProfileProjectsList(newProfileProjectsList);
        profileRep.save(profile);
    }
}
