package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.ProfileProjectsNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileProjectsController {

    @Autowired
    private ProfileProjectsRepository profileProjectsRepository;

    public ProfileProjects saveProfileProject (Profile profile, Project project, boolean allSI) {
        ProfileProjects profileProject;
        profileProject = new ProfileProjects(profile, project, allSI);
        profileProjectsRepository.save(profileProject);
        return profileProject;
    }

    public void deleteProfileProject (Long profileProjectsId) throws ProfileProjectsNotFoundException {
        if (profileProjectsRepository.existsById(profileProjectsId)) {
            profileProjectsRepository.deleteById(profileProjectsId);
        } else {
            throw new ProfileProjectsNotFoundException();
        }
    }

}
