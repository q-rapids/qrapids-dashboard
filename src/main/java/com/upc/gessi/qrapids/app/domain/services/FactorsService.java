package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FactorsService {

    @Autowired
    private QMAQualityFactors qmaqf;

    @Autowired
    private QMASimulation qmaSimulation;

    @Autowired
    private Forecast qmaf;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @GetMapping("/api/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsEvaluations(@RequestParam(value = "prj") String prj) {
        try {
            return qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOFactor getSingleFactorEvaluation (@RequestParam("prj") String prj, @PathVariable String id) {
        try {
            return qualityFactorsController.getSingleFactorEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsEvaluations(@RequestParam(value = "prj") String prj, @PathVariable String id) {
        try {
            return qmaqf.CurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsHistoricalData(@RequestParam(value = "prj") String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return qualityFactorsController.getAllFactorsWithMetricsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsHistoricalData(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return qmaqf.HistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactor> getAllQualityFactors(@RequestParam(value = "prj") String prj) {
        try {
            return qualityFactorsController.getAllFactorsEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsPredicitionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            return qmaf.ForecastFactor(qmaqf.CurrentEvaluation(id, prj), technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsPrediction(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            List<DTOQualityFactor> currentEvaluation = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(prj);
            return qualityFactorsController.getAllFactorsWithMetricsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/api/qualityFactors/simulate")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactor> simulate (@RequestParam("prj") String prj, @RequestParam("date") String date, @RequestBody List<DTOMetric> metrics) {
        try {
            Map<String, Float> metricsMap = new HashMap<>();
            for (DTOMetric metric : metrics) {
                metricsMap.put(metric.getId(), metric.getValue());
            }
            return qualityFactorsController.simulate(metricsMap, prj, LocalDate.parse(date));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
}
