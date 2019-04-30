package com.upc.gessi.qrapids.app.domain.repositories.Decision;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DecisionRepository extends CrudRepository<Decision, Long> {
    List<Decision> findByProject_Id (Long projectId);
}
