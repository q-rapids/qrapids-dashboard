package com.upc.gessi.qrapids.app.domain.repositories.SICategory;

import com.upc.gessi.qrapids.app.domain.models.SICategory;
import org.springframework.data.repository.CrudRepository;

public interface SICategoryRepository extends CrudRepository<SICategory, Long> {
    SICategory findByName (String name);
}
