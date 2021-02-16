package com.upc.gessi.qrapids.app.domain.repositories.Profile;

import com.upc.gessi.qrapids.app.domain.models.Profile;
import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<Profile, Long> {

    Profile findByName (String name);

}
