package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class QualityFactorsController {

    @Autowired
    private QMAQualityFactors qmaQualityFactors;

    @Autowired
    private Forecast qmaForecast;

    @Autowired
    private QMASimulation qmaSimulation;

    public DTOFactor getSingleFactorEvaluation(String factorId, String projectExternalId) throws IOException {
        return qmaQualityFactors.SingleCurrentEvaluation(factorId, projectExternalId);
    }

    public List<DTOFactor> getAllFactorsEvaluation(String projectExternalId) throws IOException {
        return qmaQualityFactors.getAllFactors(projectExternalId);
    }

    public List<DTOQualityFactor> getAllFactorsWithMetricsCurrentEvaluation(String projectExternalId) throws IOException {
        return qmaQualityFactors.CurrentEvaluation(null, projectExternalId);
    }

    public List<DTOQualityFactor> getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(String strategicIndicatorId, String projectExternalId) throws IOException {
        return qmaQualityFactors.CurrentEvaluation(strategicIndicatorId, projectExternalId);
    }

    public List<DTOQualityFactor> getAllFactorsWithMetricsHistoricalEvaluation(String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.HistoricalData(null, dateFrom, dateTo, projectExternalId);
    }

    public List<DTOQualityFactor> getAllFactorsWithMetricsPrediction(List<DTOQualityFactor> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException {
        return qmaForecast.ForecastFactor(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

    public List<DTOFactor> simulate (Map<String, Float> metricsValue, String projectExternalId, LocalDate date) throws IOException {
        return qmaSimulation.simulateQualityFactors(metricsValue, projectExternalId, date);
    }

}
