package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.exceptions.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorMetricsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorQualityFactorsRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetricEvaluation;
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
    private QualityFactorMetricsRepository qualityFactorMetricsRepository;

    @Autowired
    private StrategicIndicatorQualityFactorsRepository strategicIndicatorQualityFactorsRepository;

    @Autowired
    private MetricsController metricsController;

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private QualityFactorMetricsController qualityFactorMetricsController;

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
    public Factor findFactorByExternalIdAndProjectId(String externalId, Long prjId) throws QualityFactorNotFoundException {
        Factor factor = qualityFactorRepository.findByExternalIdAndProjectId(externalId,prjId);
        if (factor == null) {
            throw new QualityFactorNotFoundException();
        }
        return factor;
    }

    public void importFactorsAndUpdateDatabase() throws IOException, CategoriesException, ProjectNotFoundException, MetricNotFoundException {
        List<String> projects = projectsController.getAllProjects();
        for (String prj : projects) {
            List<DTOFactorEvaluation> factors = getAllFactorsEvaluation(prj);
            List<DTODetailedFactorEvaluation> factorsWithMetrics = getAllFactorsWithMetricsCurrentEvaluation(prj);
            updateDataBaseWithNewFactors(prj, factors, factorsWithMetrics);
        }
    }

    public void updateDataBaseWithNewFactors (String prjExternalID,List<DTOFactorEvaluation> factors, List<DTODetailedFactorEvaluation> factorsWithMetrics) throws ProjectNotFoundException, MetricNotFoundException {
        Project project = projectsController.findProjectByExternalId(prjExternalID);
        for (DTOFactorEvaluation factor : factors) {
            Factor factorsSaved = qualityFactorRepository.findByExternalIdAndProjectId(factor.getId(),project.getId());
            if (factorsSaved == null) {
                // ToDo factor composition with corresponding metrics weights (default all metrics are not weighted)
                List<String> qualityMetrics = new ArrayList<>();
                int cont = 0;
                boolean found = false;
                while (cont < factorsWithMetrics.size() && !found){
                    DTODetailedFactorEvaluation df = factorsWithMetrics.get(cont);
                    if (df.getId().equals(factor.getId())) {
                        found = true;
                        for (DTOMetricEvaluation m : df.getMetrics()) {
                            Metric metric = metricsController.findMetricByExternalIdAndProjectId(m.getId(), project.getId());
                            qualityMetrics.add(String.valueOf(metric.getId()));
                            qualityMetrics.add(String.valueOf(-1l));
                        }
                    }
                    cont += 1;
                }
                Factor newFactor = saveImportedQualityFactor(factor.getId(),factor.getName(),factor.getDescription(),qualityMetrics,project);
                qualityFactorRepository.save(newFactor);
            }
        }
    }

    private Factor saveImportedQualityFactor(String id, String name, String description, List<String> qualityMetrics, Project project) throws MetricNotFoundException {
        Factor qualityFactor;
        // create Quality Factor minim (without quality factors and weighted)
        qualityFactor = new Factor (id, name, description, project);
        qualityFactorRepository.save(qualityFactor);
        boolean weighted = assignQualityMetricsToQualityFactor (qualityMetrics, qualityFactor);
        qualityFactor.setWeighted(weighted);
        qualityFactorRepository.save(qualityFactor);
        return qualityFactor;
    }

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
                qfm = qualityFactorMetricsController.saveQualityFactorMetric(weight, metric, qualityFactor);
                weighted = false;
            } else {
                qfm = qualityFactorMetricsController.saveQualityFactorMetric(weight, metric, qualityFactor);
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

    public Factor editQualityFactor (Long factorId, String name, String description, List<String> qualityMetrics) throws QualityFactorNotFoundException, QualityFactorMetricsNotFoundException, MetricNotFoundException {
        Factor factor = getQualityFactorById(factorId);
        factor.setName(name);
        factor.setDescription(description);
        // Actualize Quality Metrics
        boolean weighted = reassignQualityMetricsToQualityFactor (qualityMetrics, factor);
        factor.setWeighted(weighted);
        qualityFactorRepository.save(factor);
        return  factor;
    }

    private  boolean reassignQualityMetricsToQualityFactor (List<String> qualityMetrics, Factor factor) throws QualityFactorMetricsNotFoundException, MetricNotFoundException {
        List<QualityFactorMetrics> newQualityFactorsWeights = new ArrayList();
        // Delete oldQualityMetricsWeights
        List<QualityFactorMetrics> oldQualityMetricsWeights = qualityFactorMetricsRepository.findByFactor(factor);
        factor.setQualityFactorMetricsList(null);
        for (QualityFactorMetrics old : oldQualityMetricsWeights) {
            qualityFactorMetricsController.deleteQualityFactorMetric(old.getId());
        }
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
                qfm = qualityFactorMetricsController.saveQualityFactorMetric(weight, metric, factor);
                weighted = false;
            } else {
                qfm = qualityFactorMetricsController.saveQualityFactorMetric(weight, metric, factor);
                weighted = true;
            }
            newQualityFactorsWeights.add(qfm);
            qualityMetrics.remove(1);
            qualityMetrics.remove(0);
        }
        // create the association between Strategic Indicator and its Quality Factors
        factor.setQualityFactorMetricsList(newQualityFactorsWeights);
        return weighted;
    }

    public void deleteFactor(Long id) throws QualityFactorNotFoundException, DeleteFactorException {
        if(qualityFactorRepository.existsById(id)){
            Optional<Factor> factor = qualityFactorRepository.findById(id);
            if (strategicIndicatorQualityFactorsRepository.findByQuality_factor(factor.get()).size() == 0) {
                qualityFactorRepository.deleteById(id);
            } else {
                throw new DeleteFactorException();
            }
        } else {
            throw new QualityFactorNotFoundException();
        }
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
