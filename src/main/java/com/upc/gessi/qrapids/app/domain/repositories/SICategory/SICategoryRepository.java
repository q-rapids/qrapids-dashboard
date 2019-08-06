package com.upc.gessi.qrapids.app.domain.repositories.SICategory;

import com.upc.gessi.qrapids.app.domain.models.SICategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SICategoryRepository extends CrudRepository<SICategory, Long> {
    SICategory findByName (String name);
}
