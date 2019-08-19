package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class MetricsController {

    @Autowired
    private QMAMetrics qmaMetrics;

    public List<DTOMetric> getAllMetricsCurrentEvaluation (String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.CurrentEvaluation(null, projectExternalId);
    }

    public DTOMetric getSingleMetricCurrentEvaluation (String metricId, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaMetrics.SingleCurrentEvaluation(metricId, projectExternalId);
    }

}
