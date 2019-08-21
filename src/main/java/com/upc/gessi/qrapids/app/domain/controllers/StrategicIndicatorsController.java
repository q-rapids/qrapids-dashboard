package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.StrategicIndicatorNotFoundException;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StrategicIndicatorsController {

    @Autowired
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Autowired
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Autowired
    private Forecast qmaForecast;

    @Autowired
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Autowired
    private SICategoryRepository strategicIndicatorCategoryRepository;

    public List<Strategic_Indicator> getStrategicIndicatorsByProject (Project project) {
        return strategicIndicatorRepository.findByProject_Id(project.getId());
    }

    public Strategic_Indicator getStrategicIndicatorById (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        Optional<Strategic_Indicator> strategicIndicatorOptional = strategicIndicatorRepository.findById(strategicIndicatorId);
        if (strategicIndicatorOptional.isPresent()) {
            return strategicIndicatorOptional.get();
        } else {
            throw new StrategicIndicatorNotFoundException();
        }
    }

    public void deleteStrategicIndicator (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        if (strategicIndicatorRepository.existsById(strategicIndicatorId)) {
            strategicIndicatorRepository.deleteById(strategicIndicatorId);
        } else {
            throw new StrategicIndicatorNotFoundException();
        }
    }

    public List<SICategory> getStrategicIndicatorCategories () {
        List<SICategory> strategicIndicatorCategoriesList = new ArrayList<>();
        Iterable<SICategory> strategicIndicatorCategoriesIterable = strategicIndicatorCategoryRepository.findAll();
        strategicIndicatorCategoriesIterable.forEach(strategicIndicatorCategoriesList::add);
        return strategicIndicatorCategoriesList;
    }

    public void newStrategicIndicatorCategories (List<Map<String, String>> categories) throws CategoriesException {
        if (categories.size() > 1) {
            strategicIndicatorCategoryRepository.deleteAll();
            for (Map<String, String> c : categories) {
                SICategory sic = new SICategory();
                sic.setName(c.get("name"));
                sic.setColor(c.get("color"));
                strategicIndicatorCategoryRepository.save(sic);
            }
        } else {
            throw new CategoriesException();
        }
    }

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsCurrentEvaluation (String projectExternalId) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.CurrentEvaluation(projectExternalId);
    }

    public DTOStrategicIndicatorEvaluation getSingleStrategicIndicatorsCurrentEvaluation (String strategicIndicatorId, String projectExternalId) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, strategicIndicatorId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsCurrentEvaluation (String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorCurrentEvaluation (String strategicIndicatorId, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(strategicIndicatorId, projectExternalId);
    }

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsHistoricalEvaluation (String projectExternalId, LocalDate from, LocalDate to) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.HistoricalData(from, to, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsHistoricalEvaluation (String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.HistoricalData(null, from, to, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorsHistoricalEvaluation (String strategicIndicatorId, String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.HistoricalData(strategicIndicatorId, from, to, projectExternalId);
    }

    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction (String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastSI(technique, freq, horizon, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getDetailedStrategicIndicatorsPrediction (List<DTODetailedStrategicIndicator> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastDSI(currentEvaluation, technique, freq, horizon, projectExternalId);
    }
}
