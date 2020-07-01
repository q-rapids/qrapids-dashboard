package com.upc.gessi.qrapids.app.domain.repositories.Project;

import com.upc.gessi.qrapids.app.domain.models.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ProjectRepository extends CrudRepository<Project, Long> {

    Project findByExternalId(String externalId);

    Project findByName(String name);
}
