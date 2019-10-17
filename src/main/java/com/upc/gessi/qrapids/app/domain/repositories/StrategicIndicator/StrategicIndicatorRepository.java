package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StrategicIndicatorRepository extends CrudRepository<Strategic_Indicator, Long> {
    Strategic_Indicator findByName (String name);
    List<Strategic_Indicator> findByProject_Id (Long projectId);
    Strategic_Indicator findByNameAndProject_Id (String name, Long projectId);
    boolean existsByExternalIdAndProject_Id (String externalId, Long projectId);
}
