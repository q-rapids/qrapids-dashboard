package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.MetricsController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.FactorsController;
import com.upc.gessi.qrapids.app.domain.exceptions.*;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.models.Factor;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
public class Factors {

    @Autowired
    private FactorsController factorsController;

    @Autowired
    private MetricsController metricsController;

    @Autowired
    private ProjectsController projectsController;

    private Logger logger = LoggerFactory.getLogger(Factors.class);

    @GetMapping("/api/qualityFactors/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOCategoryThreshold> getFactorCategories () {
        List<QFCategory> factorCategoryList = factorsController.getFactorCategories();
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
            factorsController.newFactorCategories(categories);
        } catch (CategoriesException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.NOT_ENOUGH_CATEGORIES);
        }
    }

    @GetMapping("/api/qualityFactors/import")
    @ResponseStatus(HttpStatus.OK)
    public void importFactors() {
        try {
            factorsController.importFactorsAndUpdateDatabase();
        } catch (CategoriesException | ProjectNotFoundException | MetricNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on ElasticSearch connection");
        }
    }

    @GetMapping("/api/qualityFactors")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactor> getAllQualityFactors (@RequestParam(value = "prj") String prj) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<Factor> factorsList = factorsController.getQualityFactorsByProject(project);
            List<DTOFactor> dtoFactorsList = new ArrayList<>();
            for (Factor factor : factorsList) {
                DTOFactor dtoFactor = new DTOFactor(factor.getId(),
                        factor.getExternalId(),
                        factor.getName(),
                        factor.getDescription(),
                        factor.getMetricsIds(),
                        factor.isWeighted(),
                        factor.getWeights());
                dtoFactorsList.add(dtoFactor);
            }
            return dtoFactorsList;
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }

    @GetMapping("/api/qualityFactors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOFactor getQualityFactor(@PathVariable Long id) {
        try {
            Factor factor = factorsController.getQualityFactorById(id);
            return new DTOFactor(factor.getId(),
                        factor.getExternalId(),
                        factor.getName(),
                        factor.getDescription(),
                        factor.getMetricsIds(),
                        factor.isWeighted(),
                        factor.getWeights());

        } catch (QualityFactorNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.STRATEGIC_INDICATOR_NOT_FOUND);
        }
    }

    @PostMapping("/api/qualityFactors")
    @ResponseStatus(HttpStatus.CREATED)
    public void newQualityFactor (HttpServletRequest request) {
        try {
            String prj = request.getParameter("prj");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            List<String> metrics = new ArrayList<>(Arrays.asList(request.getParameter("metrics").split(",")));
            if (!name.equals("") && !metrics.isEmpty()) {
                Project project = projectsController.findProjectByExternalId(prj);
                factorsController.saveQualityFactor(name, description, metrics, project);
                // TODO assessQualityFactor functionality
                if (!factorsController.assessQualityFactor(name, prj)) {
                    throw new AssessmentErrorException();
                }
            }
        }  catch (AssessmentErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ASSESSMENT_ERROR + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactorEvaluation> getQualityFactorsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return factorsController.getAllFactorsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @PutMapping("/api/qualityFactors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void editQualityFactor(@PathVariable Long id, HttpServletRequest request) {
        try {
            String name;
            String description;
            List<String> qualityMetrics;
            try {
                name = request.getParameter("name");
                description = request.getParameter("description");
                qualityMetrics = new ArrayList<>(Arrays.asList(request.getParameter("metrics").split(",")));
            } catch (Exception e) {
                throw new MissingParametersException();
            }
            if (!name.equals("") && !qualityMetrics.isEmpty()) {
                Factor oldFactor = factorsController.getQualityFactorById(id);
                factorsController.editQualityFactor(oldFactor.getId(), name, description, qualityMetrics);
                // TODO assessQualityFactor functionality
                if (!factorsController.assessQualityFactor(name, oldFactor.getProject().getExternalId())) {
                    throw new AssessmentErrorException();
                }
            }
        } catch (MissingParametersException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.MISSING_ATTRIBUTES_IN_BODY);
        } catch (AssessmentErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ASSESSMENT_ERROR + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integrity violation: " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @DeleteMapping("/api/qualityFactors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteQualityFactor (@PathVariable Long id) {
        try {
            factorsController.deleteFactor(id);
        } catch (QualityFactorNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DeleteFactorException e) {
            logger.error(e.getMessage(), e);
            // 403 - Forbidden
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Messages.FACTOR_DELETE_FORBIDDEN);
        }
    }

    @GetMapping("/api/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedFactorEvaluation> getQualityFactorsEvaluations(@RequestParam(value = "prj") String prj) {
        try {
            // TODO pass real profile
            return factorsController.getAllFactorsWithMetricsCurrentEvaluation(prj, null, true);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/{id}/current")
    @ResponseStatus(HttpStatus.OK)
    public DTOFactorEvaluation getSingleFactorEvaluation (@RequestParam("prj") String prj, @PathVariable String id) {
        try {
            return factorsController.getSingleFactorEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTODetailedFactorEvaluation> getDetailedQualityFactorsHistoricalData(@RequestParam(value = "prj") String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            // TODO pass real profile
            return factorsController.getAllFactorsWithMetricsHistoricalEvaluation(prj,null, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactorEvaluation> getAllQualityFactorsEvaluation(@RequestParam(value = "prj") String prj) {
        try {
            return factorsController.getAllFactorsEvaluation(prj, true);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
// TODO: Quality Factors Prediction
    @RequestMapping("/api/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactorEvaluation> getQualityFactorsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) throws IOException {
        try {
            List<DTOFactorEvaluation> currentEvaluation = factorsController.getAllFactorsEvaluation(prj, true);
            return factorsController.getFactorsPrediction(currentEvaluation, prj, technique, "7", horizon);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedFactorEvaluation> getQualityFactorsPrediction(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            // TODO pass real profile
            List<DTODetailedFactorEvaluation> currentEvaluation = factorsController.getAllFactorsWithMetricsCurrentEvaluation(prj, null, true);
            return factorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @PostMapping("/api/qualityFactors/simulate")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOFactorEvaluation> simulate (@RequestParam("prj") String prj, @RequestParam("date") String date, @RequestBody List<DTOMetricEvaluation> metrics) {
        try {
            Map<String, Float> metricsMap = new HashMap<>();
            for (DTOMetricEvaluation metric : metrics) {
                metricsMap.put(metric.getId(), metric.getValue());
            }
            return factorsController.simulate(metricsMap, prj, LocalDate.parse(date));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetricEvaluation> getMetricsCurrentEvaluationForQualityFactor(@RequestParam(value = "prj") String prj, @PathVariable String id) {
        try {
            return metricsController.getMetricsForQualityFactorCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetricEvaluation> getMetricsHistoricalDataForQualityFactor(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return metricsController.getMetricsForQualityFactorHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @RequestMapping("/api/qualityFactors/{id}/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMetricEvaluation> getMetricsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTOMetricEvaluation> currentEvaluation = metricsController.getMetricsForQualityFactorCurrentEvaluation(id, prj);
            return metricsController.getMetricsPrediction(currentEvaluation, prj, technique, "7", horizon);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
}
