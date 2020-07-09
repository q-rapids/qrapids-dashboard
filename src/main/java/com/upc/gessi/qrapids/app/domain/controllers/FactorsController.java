package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.exceptions.MetricNotFoundException;
import com.upc.gessi.qrapids.app.domain.exceptions.QualityFactorNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedFactorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FactorsController {

    @Autowired
    private QMAQualityFactors qmaQualityFactors;

    @Autowired
    private Forecast qmaForecast;

    @Autowired
    private QMASimulation qmaSimulation;

    @Autowired
    private QFCategoryRepository factorCategoryRepository;

    @Autowired
    private QualityFactorRepository qualityFactorRepository;

    @Autowired
    private MetricsController metricsController;

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

    // TODO new functions
    public List<Factor> getQualityFactorsByProject (Project project) {
        return qualityFactorRepository.findByProject_IdOrderByName(project.getId());
    }

    public Factor getQualityFactorById (Long qualityFactorId) throws QualityFactorNotFoundException {
        Optional<Factor> qualityFactorOptional = qualityFactorRepository.findById(qualityFactorId);
        if (qualityFactorOptional.isPresent()) {
            return qualityFactorOptional.get();
        } else {
            throw new QualityFactorNotFoundException();
        }
    }

    public Factor saveQualityFactor(String name, String description, List<String> qualityMetrics, Project project) throws MetricNotFoundException {
        Factor qualityFactor;
        // create Quality Factor minim (without quality factors and weighted)
        qualityFactor = new Factor (name, description, project);
        qualityFactorRepository.save(qualityFactor);
        boolean weighted = assignQualityMetricsToQualityFactor (qualityMetrics, qualityFactor);
        qualityFactor.setWeighted(weighted);
        qualityFactorRepository.save(qualityFactor);
        return qualityFactor;
    }

    private boolean assignQualityMetricsToQualityFactor (List<String> qualityMetrics, Factor qualityFactor) throws MetricNotFoundException {
        List<QualityFactorMetrics> qualityMetricsWeights = new ArrayList();
        boolean weighted = false;
        String metricId;
        Float weight;
        // generate QualityFactorMetrics class objects from List<String> qualityMetrics
        while (!qualityMetrics.isEmpty()) {
            QualityFactorMetrics qfm;
            metricId = qualityMetrics.get(0);
            Metric metric = metricsController.getMetricById(Long.parseLong(metricId));
            weight = Float.parseFloat(qualityMetrics.get(1));
            if (weight == -1) {
                qfm = QualityFactorMetricsController.saveQualityFactorMetric(weight, metric, qualityFactor);
                weighted = false;
            } else {
                qfm = QualityFactorMetricsController.saveQualityFactorMetric(weight, metric, qualityFactor);
                weighted = true;
            }
            qualityMetricsWeights.add(qfm);
            qualityMetrics.remove(1);
            qualityMetrics.remove(0);
        }
        // create the association between Quality Factor and its Metrics
        qualityFactor.setQualityFactorMetricsList(qualityMetricsWeights);
        return weighted;
    }

    public DTOFactorEvaluation getSingleFactorEvaluation(String factorId, String projectExternalId) throws IOException {
        return qmaQualityFactors.SingleCurrentEvaluation(factorId, projectExternalId);
    }

    public List<DTOFactorEvaluation> getAllFactorsEvaluation(String projectExternalId) throws IOException {
        return qmaQualityFactors.getAllFactors(projectExternalId);
    }

    public List<DTODetailedFactorEvaluation> getAllFactorsWithMetricsCurrentEvaluation(String projectExternalId) throws IOException {
        return qmaQualityFactors.CurrentEvaluation(null, projectExternalId);
    }

    public List<DTODetailedFactorEvaluation> getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(String strategicIndicatorId, String projectExternalId) throws IOException {
        return qmaQualityFactors.CurrentEvaluation(strategicIndicatorId, projectExternalId);
    }

    public List<DTOFactorEvaluation> getAllFactorsHistoricalEvaluation (String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.getAllFactorsHistoricalData(projectExternalId, dateFrom, dateTo);
    }

    public List<DTODetailedFactorEvaluation> getAllFactorsWithMetricsHistoricalEvaluation(String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.HistoricalData(null, dateFrom, dateTo, projectExternalId);
    }

    public List<DTODetailedFactorEvaluation> getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(String strategicIndicatorId, String projectExternalId, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        return qmaQualityFactors.HistoricalData(strategicIndicatorId, dateFrom, dateTo, projectExternalId);
    }

    public List<DTODetailedFactorEvaluation> getFactorsWithMetricsPrediction(List<DTODetailedFactorEvaluation> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException {
        return qmaForecast.ForecastFactor(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

    public List<DTOFactorEvaluation> simulate (Map<String, Float> metricsValue, String projectExternalId, LocalDate date) throws IOException {
        return qmaSimulation.simulateQualityFactors(metricsValue, projectExternalId, date);
    }

    public void setFactorStrategicIndicatorRelation (List<DTOFactorEvaluation> factorList, String projectExternalId) throws IOException {
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
