package com.upc.gessi.qrapids.app.domain.repositories.Profile;

import com.upc.gessi.qrapids.app.domain.models.ProfileProjects;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectsId;
import org.springframework.data.repository.CrudRepository;

public interface ProfileProjectsRepository extends CrudRepository<ProfileProjects, ProfileProjectsId> {
}
