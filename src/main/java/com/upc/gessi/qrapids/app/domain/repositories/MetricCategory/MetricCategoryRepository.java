package com.upc.gessi.qrapids.app.domain.repositories.MetricCategory;

import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MetricCategoryRepository extends CrudRepository<MetricCategory, Long> {

    List<MetricCategory> findAllByOrderByUpperThresholdAsc();
}
