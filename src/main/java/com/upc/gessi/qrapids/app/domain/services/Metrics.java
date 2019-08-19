package com.upc.gessi.qrapids.app.domain.services;


import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class Metrics {

    @Autowired
    private QMAMetrics qmam;

    @Autowired
    private Forecast qmaf;

    @Autowired
    private MetricsController metricsController;

    @RequestMapping("/api/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsEvaluations(@RequestParam(value = "prj") String prj) throws IOException {
        try {
            return metricsController.getAllMetricsCurrentEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/{id}/current")
    @ResponseStatus(HttpStatus.OK)
    public DTOMetric getSingleMetricEvaluation(@RequestParam("prj") String prj, @PathVariable String id) throws IOException {
        try {
            return qmam.SingleCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsEvaluation(@RequestParam(value = "prj") String prj, @PathVariable String id) throws IOException {
        try {
            return qmam.CurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsHistoricalData(@RequestParam(value = "prj") String prj, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        try {
            return qmam.HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsHistoricalData(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        try {
            return qmam.HistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/{id}/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getHistoricalDataForMetric(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        try {
            return qmam.SingleHistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsPredicitionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) throws IOException {
        try {
            return qmaf.ForecastMetric(qmam.CurrentEvaluation(id, prj), technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsPredicitionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String techinique, @RequestParam("horizon") String horizon) throws IOException {
        try {
            return qmaf.ForecastMetric(qmam.CurrentEvaluation(null, prj), techinique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

}
