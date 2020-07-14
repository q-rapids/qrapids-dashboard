package com.upc.gessi.qrapids.app.domain.repositories.Profile;

import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfileProjectStrategicIndicatorsRepository extends CrudRepository<ProfileProjectStrategicIndicators, Long> {

    List<ProfileProjectStrategicIndicators>  findByProfileAndProject(Profile profile, Project project);

}
