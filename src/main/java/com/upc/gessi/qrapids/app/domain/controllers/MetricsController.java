package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.domain.repositories.MetricCategory.MetricCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MetricsController {

    @Autowired
    private QMAMetrics qmaMetrics;

    @Autowired
    private Forecast qmaForecast;

    @Autowired
    private MetricCategoryRepository metricCategoryRepository;

    public List<MetricCategory> getMetricCategories () {
        List<MetricCategory> metricCategoryList = new ArrayList<>();
        Iterable<MetricCategory> metricCategoryIterable = metricCategoryRepository.findAll();
        metricCategoryIterable.forEach(metricCategoryList::add);
        return metricCategoryList;
    }

    public void newMetricCategories (List<Map<String, String>> categories) throws CategoriesException {
        if (categories.size() > 1) {
            metricCategoryRepository.deleteAll();
            for (Map<String, String> c : categories) {
                MetricCategory metricCategory = new MetricCategory();
                metricCategory.setName(c.get("name"));
                metricCategory.setColor(c.get("color"));
                float upperThreshold = Float.parseFloat(c.get("upperThreshold"));
                metricCategory.setUpperThreshold(upperThreshold/100f);
                metricCategoryRepository.save(metricCategory);
            }
        } else {
            throw new CategoriesException();
        }
    }

    public List<DTOMetric> getAllMetricsCurrentEvaluation (String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.CurrentEvaluation(null, projectExternalId);
    }

    public DTOMetric getSingleMetricCurrentEvaluation (String metricId, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.SingleCurrentEvaluation(metricId, projectExternalId);
    }

    public List<DTOMetric> getMetricsForQualityFactorCurrentEvaluation (String qualityFactorId, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.CurrentEvaluation(qualityFactorId, projectExternalId);
    }

    public List<DTOMetric> getSingleMetricHistoricalEvaluation (String metricId, String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.SingleHistoricalData(metricId, from, to, projectExternalId);
    }

    public List<DTOMetric> getAllMetricsHistoricalEvaluation (String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.HistoricalData(null, from, to, projectExternalId);
    }

    public List<DTOMetric> getMetricsForQualityFactorHistoricalEvaluation (String qualityFactorId, String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.HistoricalData(qualityFactorId, from, to, projectExternalId);
    }

    public List<DTOMetric> getMetricsPrediction (List<DTOMetric> currentEvaluation, String projectExternalId, String technique, String freq, String horizon) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastMetric(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

}
