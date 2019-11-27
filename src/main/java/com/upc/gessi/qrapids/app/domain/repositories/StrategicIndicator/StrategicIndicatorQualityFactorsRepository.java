package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StrategicIndicatorQualityFactorsRepository extends CrudRepository<StrategicIndicatorQualityFactors, Long> {
    List<StrategicIndicatorQualityFactors> findBystrategic_indicator_Id (Long strategic_indicator_Id);
}
