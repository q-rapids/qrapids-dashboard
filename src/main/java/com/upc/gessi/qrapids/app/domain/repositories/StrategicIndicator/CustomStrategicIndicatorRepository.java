package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;

import java.io.Serializable;

public interface CustomStrategicIndicatorRepository extends Serializable {
    public Strategic_Indicator findByName(String name);
    public Strategic_Indicator findById(Long id);
}
