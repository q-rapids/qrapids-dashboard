package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.Factor;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StrategicIndicatorQualityFactorsRepository extends CrudRepository<StrategicIndicatorQualityFactors, Long> {

    @Query("from StrategicIndicatorQualityFactors r  where r.strategic_indicator = :strategicIndicator")
    List<StrategicIndicatorQualityFactors> findByStrategic_indicator(@Param("strategicIndicator") Strategic_Indicator strategicIndicator);

    @Query("from StrategicIndicatorQualityFactors r  where r.factor = :factor")
    List<StrategicIndicatorQualityFactors> findByQuality_factor(@Param("factor") Factor factor);

}
