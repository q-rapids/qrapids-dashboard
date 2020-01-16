package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.StrategicIndicatorQualityFactorNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorQualityFactorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StrategicIndicatorQualityFactorsController {

    @Autowired
    private StrategicIndicatorQualityFactorsRepository strategicIndicatorQualityFactorsRepository;

    public StrategicIndicatorQualityFactors saveStrategicIndicatorQualityFactor (String quality_factor, Float weight, Strategic_Indicator strategicIndicator) {
        StrategicIndicatorQualityFactors strategicIndicatorQualityFactor;
        strategicIndicatorQualityFactor = new StrategicIndicatorQualityFactors(quality_factor, weight, strategicIndicator);
        strategicIndicatorQualityFactorsRepository.save(strategicIndicatorQualityFactor);
        return strategicIndicatorQualityFactor;
    }

    public void deleteStrategicIndicatorQualityFactor (Long strategicIndicatorQualityFactorId) throws StrategicIndicatorQualityFactorNotFoundException {
        if (strategicIndicatorQualityFactorsRepository.existsById(strategicIndicatorQualityFactorId)) {
            strategicIndicatorQualityFactorsRepository.deleteById(strategicIndicatorQualityFactorId);
        } else {
            throw new StrategicIndicatorQualityFactorNotFoundException();
        }
    }

}
