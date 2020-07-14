package com.upc.gessi.qrapids.app.domain.repositories.QualityFactor;

import com.upc.gessi.qrapids.app.domain.models.Factor;
import com.upc.gessi.qrapids.app.domain.models.QualityFactorMetrics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QualityFactorMetricsRepository extends CrudRepository<QualityFactorMetrics, Long> {

    @Query("from QualityFactorMetrics r  where r.quality_factor = :factor")
    List<QualityFactorMetrics> findByFactor(@Param("factor") Factor factor);



}
