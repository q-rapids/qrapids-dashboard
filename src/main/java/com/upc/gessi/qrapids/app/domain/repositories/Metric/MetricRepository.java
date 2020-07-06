package com.upc.gessi.qrapids.app.domain.repositories.Metric;

import com.upc.gessi.qrapids.app.domain.models.Metric;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MetricRepository extends CrudRepository<Metric, Long> {

    Metric findByExternalId(String externalId);

    Metric findByName(String name);

    List<Metric> findByProject_IdOrderByName (Long projectId);

}
