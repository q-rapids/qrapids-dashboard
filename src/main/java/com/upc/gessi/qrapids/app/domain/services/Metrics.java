package com.upc.gessi.qrapids.app.domain.services;


import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.models.MetricCategory;
import com.upc.gessi.qrapids.app.dto.DTOCategoryThreshold;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class Metrics {

    @Autowired
    private MetricsController metricsController;

    @GetMapping("/api/metrics/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOCategoryThreshold> getMetricCategories () {
        Iterable<MetricCategory> metricCategoryList = metricsController.getMetricCategories();
        List<DTOCategoryThreshold> dtoCategoryList = new ArrayList<>();
        for (MetricCategory metricCategory : metricCategoryList) {
            dtoCategoryList.add(new DTOCategoryThreshold(metricCategory.getId(), metricCategory.getName(), metricCategory.getColor(), metricCategory.getUpperThreshold()));
        }
        return dtoCategoryList;
    }

    @PostMapping("/api/metrics/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void newMetricsCategories (@RequestBody List<Map<String, String>> categories) {
        try {
            metricsController.newMetricCategories(categories);
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough categories");
        }
    }

    @RequestMapping("/api/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsEvaluations(@RequestParam(value = "prj") String prj) {
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
    public DTOMetric getSingleMetricEvaluation(@RequestParam("prj") String prj, @PathVariable String id) {
        try {
            return metricsController.getSingleMetricCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsHistoricalData(@RequestParam(value = "prj") String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return metricsController.getAllMetricsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/{id}/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getHistoricalDataForMetric(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return metricsController.getSingleMetricHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String techinique, @RequestParam("horizon") String horizon) throws IOException {
        try {
            List<DTOMetric> currentEvaluation = metricsController.getAllMetricsCurrentEvaluation(prj);
            return metricsController.getMetricsPrediction(currentEvaluation, prj, techinique, "7", horizon);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

}
