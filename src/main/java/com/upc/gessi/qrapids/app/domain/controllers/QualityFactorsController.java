package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Autowired
    private QFCategoryRepository factorCategoryRepository;

    public List<QFCategory> getFactorCategories () {
        List<QFCategory> factorCategoriesList = new ArrayList<>();
        Iterable<QFCategory> factorCategoriesIterable = factorCategoryRepository.findAll();
        factorCategoriesIterable.forEach(factorCategoriesList::add);
        return factorCategoriesList;
    }

    public void newFactorCategories(List<Map<String, String>> categories) throws CategoriesException {
        if (categories.size() > 1) {
            factorCategoryRepository.deleteAll();
            for (Map<String, String> c : categories) {
                QFCategory sic = new QFCategory();
                sic.setName(c.get("name"));
                sic.setColor(c.get("color"));
                Float upperThreshold = Float.valueOf(c.get("upperThreshold"));
                sic.setUpperThreshold(upperThreshold / 100f);
                factorCategoryRepository.save(sic);
            }
        } else {
            throw new CategoriesException();
        }
    }

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

    public List<DTOFactor> getAllFactorsHistoricalEvaluation (String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.getAllFactorsHistoricalData(projectExternalId, dateFrom, dateTo);
    }

    public List<DTOQualityFactor> getAllFactorsWithMetricsHistoricalEvaluation(String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.HistoricalData(null, dateFrom, dateTo, projectExternalId);
    }

    public List<DTOQualityFactor> getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(String strategicIndicatorId, String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.HistoricalData(strategicIndicatorId, dateFrom, dateTo, projectExternalId);
    }

    public List<DTOQualityFactor> getFactorsWithMetricsPrediction(List<DTOQualityFactor> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException {
        return qmaForecast.ForecastFactor(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

    public List<DTOFactor> simulate (Map<String, Float> metricsValue, String projectExternalId, LocalDate date) throws IOException {
        return qmaSimulation.simulateQualityFactors(metricsValue, projectExternalId, date);
    }

    public void setFactorStrategicIndicatorRelation (List<DTOFactor> factorList, String projectExternalId) throws IOException {
        qmaQualityFactors.setFactorStrategicIndicatorRelation(factorList, projectExternalId);
    }

    public String getFactorLabelFromValue (Float f) {
        List <QFCategory> qfCategoryList = factorCategoryRepository.findAllByOrderByUpperThresholdAsc();
        if (f != null) {
            for (QFCategory qfCategory : qfCategoryList) {
                if (f <= qfCategory.getUpperThreshold())
                    return qfCategory.getName();
            }
        }
        return "No Category";
    }
}
