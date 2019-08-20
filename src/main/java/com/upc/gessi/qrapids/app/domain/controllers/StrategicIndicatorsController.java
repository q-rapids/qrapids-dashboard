package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class StrategicIndicatorsController {

    @Autowired
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Autowired
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

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

}
