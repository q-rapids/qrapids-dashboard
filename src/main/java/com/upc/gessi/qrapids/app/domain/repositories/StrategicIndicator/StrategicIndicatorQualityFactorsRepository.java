package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StrategicIndicatorQualityFactorsRepository extends CrudRepository<StrategicIndicatorQualityFactors, Long> {

    @Query("from StrategicIndicatorQualityFactors r  where r.strategic_indicator = :strategicIndicator")
    List<StrategicIndicatorQualityFactors> findByStrategic_indicatorId(@Param("strategicIndicator") Strategic_Indicator strategicIndicator);

}
