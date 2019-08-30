package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOCategoryThreshold;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactor;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetric;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QualityFactors {

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private MetricsController metricsController;

    private Logger logger = LoggerFactory.getLogger(QualityFactors.class);

    @GetMapping("/api/qualityFactors/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOCategoryThreshold> getFactorCategories () {
        List<QFCategory> factorCategoryList = qualityFactorsController.getFactorCategories();
        List<DTOCategoryThreshold> dtoCategoryList = new ArrayList<>();
        for (QFCategory factorCategory : factorCategoryList) {
            dtoCategoryList.add(new DTOCategoryThreshold(factorCategory.getId(), factorCategory.getName(), factorCategory.getColor(), factorCategory.getUpperThreshold()));
        }
        return dtoCategoryList;
    }

    @PostMapping("/api/qualityFactors/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void newFactorCategories (@RequestBody List<Map<String, String>> categories) {
        try {
            qualityFactorsController.newFactorCategories(categories);
        } catch (CategoriesException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough categories");
        }
    }

    @GetMapping("/api/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsEvaluations(@RequestParam(value = "prj") String prj) {
        try {
            return qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOFactor getSingleFactorEvaluation (@RequestParam("prj") String prj, @PathVariable String id) {
        try {
            return qualityFactorsController.getSingleFactorEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactor> getAllQualityFactors(@RequestParam(value = "prj") String prj) {
        try {
            return qualityFactorsController.getAllFactorsEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsPrediction(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            List<DTOQualityFactor> currentEvaluation = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(prj);
            return qualityFactorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsCurrentEvaluationForQualityFactor(@RequestParam(value = "prj") String prj, @PathVariable String id) {
        try {
            return metricsController.getMetricsForQualityFactorCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsHistoricalDataForQualityFactor(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return metricsController.getMetricsForQualityFactorHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetric> getMetricsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTOMetric> currentEvaluation = metricsController.getMetricsForQualityFactorCurrentEvaluation(id, prj);
            return metricsController.getMetricsPrediction(currentEvaluation, prj, technique, "7", horizon);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
}
