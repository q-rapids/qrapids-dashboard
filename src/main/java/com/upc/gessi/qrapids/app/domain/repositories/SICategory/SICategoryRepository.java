package com.upc.gessi.qrapids.app.domain.repositories.SICategory;

import com.upc.gessi.qrapids.app.domain.models.SICategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SICategoryRepository extends JpaRepository<SICategory, Long>, PagingAndSortingRepository<SICategory,Long>, CustomSICategoryRepository {
    SICategory findByName (String name);
}
