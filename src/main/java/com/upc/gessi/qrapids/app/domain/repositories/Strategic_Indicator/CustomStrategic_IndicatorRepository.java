package com.upc.gessi.qrapids.app.domain.repositories.Strategic_Indicator;

import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;

import java.io.Serializable;
import java.util.List;

public interface CustomStrategic_IndicatorRepository extends Serializable {

    public List<DTOStrategicIndicatorEvaluation> CurrentEvaluation();
    public List<DTOStrategicIndicatorEvaluation> getSIData();
    public List<DTOStrategicIndicatorEvaluation> HistoricalData();
}
