package com.upc.gessi.qrapids.app.domain.repositories.Profile;

import com.upc.gessi.qrapids.app.domain.models.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfileProjectStrategicIndicatorsRepository extends CrudRepository<ProfileProjectStrategicIndicators, Long> {

    List<ProfileProjectStrategicIndicators>  findByProfileAndProject(Profile profile, Project project);

    @Query("from ProfileProjectStrategicIndicators r  where r.strategic_indicator = :strategicIndicator")
    ProfileProjectStrategicIndicators findByStrategic_indicator(@Param("strategicIndicator") Strategic_Indicator strategicIndicator);

}
