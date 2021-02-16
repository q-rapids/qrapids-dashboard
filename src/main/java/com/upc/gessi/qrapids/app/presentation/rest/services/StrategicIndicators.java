package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.upc.gessi.qrapids.app.domain.controllers.*;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.presentation.rest.dto.relations.DTORelationsSI;
import com.upc.gessi.qrapids.app.domain.exceptions.*;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RestController
public class StrategicIndicators {

    @Autowired
    private FactorsController factorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    private ProjectsController projectsController;

    private Logger logger = LoggerFactory.getLogger(StrategicIndicators.class);

    @Value("${forecast.technique}")
    private String forecastTechnique;

    @GetMapping("/api/strategicIndicators/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsEvaluation(@RequestParam(value = "prj") String prj,@RequestParam(value = "profile", required = false) String profile) {
        try {
            return strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj,profile);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/current")
    @ResponseStatus(HttpStatus.OK)
    public DTOStrategicIndicatorEvaluation getSingleStrategicIndicatorEvaluation(@RequestParam("prj") String prj,@RequestParam(value = "profile", required = false) String profile, @PathVariable String id) {
        try {
            return strategicIndicatorsController.getSingleStrategicIndicatorsCurrentEvaluation(id,prj,profile);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getDetailedSICurrentEvaluation(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile) {
        try {
            return strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(prj, profile,true);
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getSingleDetailedSICurrentEvaluation(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @PathVariable String id) {
        try {
            return strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(id, prj, profile);
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedFactorEvaluation> getQualityFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(@RequestParam(value = "prj") String prj, @PathVariable String id) {
        try {
            return factorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsHistoricalData(@RequestParam(value = "prj", required=false) String prj,@RequestParam(value = "profile", required = false) String profile, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(prj, profile, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(prj, profile, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(id, prj, profile, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/historical")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<DTODetailedFactorEvaluation> getQualityFactorsHistoricalData(@RequestParam(value = "prj") String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return factorsController.getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            List<DTOStrategicIndicatorEvaluation> currentEvaluation = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj,profile);
            return strategicIndicatorsController.getStrategicIndicatorsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException | CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getDetailedStrategicIndicatorsPredictionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            List<DTODetailedStrategicIndicatorEvaluation> currentEvaluation = strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(prj, profile,true);
            return strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicatorEvaluation> getSingleQualityFactorsPredictionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required = false) String profile, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTODetailedStrategicIndicatorEvaluation> currentEvaluation = strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(id, prj, profile);
            return strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedFactorEvaluation> getQualityFactorsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTODetailedFactorEvaluation> currentEvaluation = factorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(id, prj);
            return factorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOSI> getAllStrategicIndicators (@RequestParam(value = "prj") String prj, @RequestParam(value = "profile", required = false) String profile ) {
        try {
            List<Strategic_Indicator> strategicIndicatorList = strategicIndicatorsController.getStrategicIndicatorsByProjectAndProfile(prj, profile);
            List<DTOSI> dtoSIList = new ArrayList<>();
            for (Strategic_Indicator strategic_indicator : strategicIndicatorList) {
                DTOSI dtosi = new DTOSI(strategic_indicator.getId(),
                        strategic_indicator.getExternalId(),
                        strategic_indicator.getName(),
                        strategic_indicator.getDescription(),
                        strategic_indicator.getQuality_factorsIds(),
                        strategic_indicator.isWeighted(),
                        strategic_indicator.getWeights());
                dtosi.setThreshold(strategic_indicator.getThreshold());
                dtosi.setNetwork(strategic_indicator.getNetwork());
                dtoSIList.add(dtosi);
            }
            return dtoSIList;
        } catch (ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        }
    }

    @GetMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOSI getSI(@PathVariable Long id) {
        try {
            Strategic_Indicator strategicIndicator = strategicIndicatorsController.getStrategicIndicatorById(id);
            DTOSI dtosi = new DTOSI(strategicIndicator.getId(),
                    strategicIndicator.getExternalId(),
                    strategicIndicator.getName(),
                    strategicIndicator.getDescription(),
                    strategicIndicator.getQuality_factorsIds(),
                    strategicIndicator.isWeighted(),
                    strategicIndicator.getWeights());
            dtosi.setThreshold(strategicIndicator.getThreshold());
            dtosi.setNetwork(strategicIndicator.getNetwork());
            return dtosi;
        } catch (StrategicIndicatorNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.STRATEGIC_INDICATOR_NOT_FOUND);
        }
    }

    @PostMapping("/api/strategicIndicators")
    @ResponseStatus(HttpStatus.CREATED)
    public void newSI(HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String prj = request.getParameter("prj");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String threshold = request.getParameter("threshold");
            byte[] file = null;
            if (network != null) {
                file = IOUtils.toByteArray(network.getInputStream());
            }
            List<String> qualityFactors = new ArrayList<>(Arrays.asList(request.getParameter("quality_factors").split(",")));
            if (!name.equals("") && !qualityFactors.isEmpty()) {
                Project project = projectsController.findProjectByExternalId(prj);
                strategicIndicatorsController.saveStrategicIndicator(name, description, threshold, file, qualityFactors, project);
                if (!strategicIndicatorsController.assessStrategicIndicator(name, prj)) {
                    throw new AssessmentErrorException();
                }
            }
        } catch (AssessmentErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ASSESSMENT_ERROR + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @PutMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void editSI(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String name;
            String description;
            String threshold;
            byte[] file = null;
            List<String> qualityFactors;
            try {
                name = request.getParameter("name");
                description = request.getParameter("description");
                threshold = request.getParameter("threshold");
                if (network != null) {
                    file = IOUtils.toByteArray(network.getInputStream());
                }
                qualityFactors = new ArrayList<>(Arrays.asList(request.getParameter("quality_factors").split(",")));
            } catch (Exception e) {
                throw new MissingParametersException();
            }
            if (!name.equals("") && !qualityFactors.isEmpty()) {
                Strategic_Indicator oldStrategicIndicator = strategicIndicatorsController.getStrategicIndicatorById(id);
                strategicIndicatorsController.editStrategicIndicator(oldStrategicIndicator.getId(), name, description, threshold, file, qualityFactors);
                if (!strategicIndicatorsController.assessStrategicIndicator(name, oldStrategicIndicator.getProject().getExternalId())) {
                    throw new AssessmentErrorException();
                }
            }
        } catch (MissingParametersException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.MISSING_ATTRIBUTES_IN_BODY);
        } catch (StrategicIndicatorNotFoundException | StrategicIndicatorQualityFactorNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.STRATEGIC_INDICATOR_NOT_FOUND);
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

    @DeleteMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSI (@PathVariable Long id) {
        try {
            strategicIndicatorsController.deleteStrategicIndicator(id);
        } catch (StrategicIndicatorNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.STRATEGIC_INDICATOR_NOT_FOUND);
        }
    }

    @GetMapping("/api/strategicIndicators/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOCategory> getSICategories () {
        List<SICategory> siCategoryList = strategicIndicatorsController.getStrategicIndicatorCategories();
        List<DTOCategory> dtoCategoryList = new ArrayList<>();
        for (SICategory siCategory : siCategoryList) {
            dtoCategoryList.add(new DTOCategory(siCategory.getId(), siCategory.getName(), siCategory.getColor()));
        }
        return dtoCategoryList;
    }

    @PostMapping("/api/strategicIndicators/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void newSICategories (@RequestBody List<Map<String, String>> categories) {
        try {
            strategicIndicatorsController.newStrategicIndicatorCategories(categories);
        } catch (CategoriesException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.NOT_ENOUGH_CATEGORIES);
        }
    }

    private enum TrainType {
        NONE, ONE, ALL
    }

    // assess Strategic Indicators function legacy
    @RequestMapping("/api/assessStrategicIndicators")
    @ResponseStatus(HttpStatus.OK)
    public void assesStrategicIndicatorsLegacy(@RequestParam(value = "prj", required=false) String prj,
                                  @RequestParam(value = "from", required=false) String from,
                                  @RequestParam(value = "train", required = false, defaultValue = "ONE") TrainType trainType) {
        assesStrategicIndicators(prj, from, trainType);
    }

    @GetMapping("/api/strategicIndicators/assess")
    @ResponseStatus(HttpStatus.OK)
    public void assesStrategicIndicators(@RequestParam(value = "prj", required=false) String prj,
                                         @RequestParam(value = "from", required=false) String from,
                                         @RequestParam(value = "train", required = false, defaultValue = "ONE") TrainType trainType) {
        boolean correct = true;
        LocalDate dateFrom = null;

        try {
            if (from != null && !from.isEmpty()) {
                dateFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            // first assess Factors
            correct = factorsController.assessQualityFactors(prj, dateFrom);
            if (correct) {
                correct = strategicIndicatorsController.assessStrategicIndicators(prj, dateFrom);
            }
            if(correct) {
                // Train forecast models
                if (trainType != TrainType.NONE) {
                    String technique = null;
                    if (trainType == TrainType.ONE) {
                        technique = forecastTechnique;
                    }
                    if (prj == null) {
                        strategicIndicatorsController.trainForecastModelsAllProjects(technique);
                    } else {
                        strategicIndicatorsController.trainForecastModelsSingleProject(prj, null, technique);
                    }
                }
            }
            if (!correct) {
                throw new AssessmentErrorException();
            }

        } catch (AssessmentErrorException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ASSESSMENT_ERROR + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error in the request parameters");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/fetch")
    @ResponseStatus(HttpStatus.OK)
    public void fetchSIs() {
        try {
            strategicIndicatorsController.fetchStrategicIndicators();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @PostMapping("/api/strategicIndicators/simulate")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> simulate(@RequestParam(value = "prj", required=false) String prj, @RequestParam(value = "profile", required=false) String profile, HttpServletRequest request) {
        try {
            JsonParser parser = new JsonParser();
            JsonArray simulatedFactorsJsonArray = parser.parse(request.getParameter("factors")).getAsJsonArray();
            Map<String, Float> simulatedFactorsMap = new HashMap<>();
            for (int i = 0; i < simulatedFactorsJsonArray.size(); i++) {
                String factorName = simulatedFactorsJsonArray.get(i).getAsJsonObject().get("id").getAsString();
                Float factorValue = simulatedFactorsJsonArray.get(i).getAsJsonObject().get("value").getAsFloat();
                simulatedFactorsMap.put(factorName, factorValue);
            }
            return strategicIndicatorsController.simulateStrategicIndicatorsAssessment(simulatedFactorsMap, prj, profile);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulation error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityModel")
    @ResponseStatus(HttpStatus.OK)
    public List<DTORelationsSI> getQualityModel(@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date,@RequestParam(value = "profile", required = false) String profile) {
        try {
            if (date == null)
                return strategicIndicatorsController.getQualityModel(prj, profile, null);
            else
                return strategicIndicatorsController.getQualityModel(prj, profile, LocalDate.parse(date));
        } catch (IOException | ArithmeticException | CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }

    @GetMapping("/api/forecastTechniques")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getForecastTechniques() {
        return strategicIndicatorsController.getForecastTechniques();
    }

    @GetMapping("/api/strategicIndicators/currentDate")
    @ResponseStatus(HttpStatus.OK)
    public LocalDate getcurrentDate(@RequestParam(value = "prj") String prj,@RequestParam(value = "profile", required = false) String profile) {
        try {
            List<DTOStrategicIndicatorEvaluation> si = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj, profile);
            return si.get(0).getDate();
        } catch (IOException | CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        // if the response is null
        return null;
    }

    @GetMapping("/api/strategicIndicators/current_and_historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOSICurrentHistoricEvaluation> getStrategicIndicatorsCurrentHistoricEvaluation(@RequestParam(value = "prj", required=false) String prj,@RequestParam(value = "profile", required = false) String profile, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<DTOStrategicIndicatorEvaluation> currentData = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj, profile);
            List<DTOStrategicIndicatorEvaluation> historicData = strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(prj, profile, LocalDate.parse(from), LocalDate.parse(to));
            List<DTOSICurrentHistoricEvaluation> result = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < currentData.size(); i++) {
                DTOStrategicIndicatorEvaluation aux = currentData.get(i);
                DTOSICurrentHistoricEvaluation siInfo = new DTOSICurrentHistoricEvaluation(aux.getId(),project.getName(),aux.getName(),aux.getDescription(),
                        aux.getValue(), aux.getDbId(),aux.getRationale(),aux.getProbabilities(),aux.getDate());
                List<DTOSICurrentHistoricEvaluation.DTOHistoricalData> siHistInfo = new ArrayList<>();
                while (j < historicData.size() && aux.getId().equals(historicData.get(j).getId())) {
                    DTOStrategicIndicatorEvaluation histAux = historicData.get(j);
                    DTOSICurrentHistoricEvaluation.DTOHistoricalData histInfo = new DTOSICurrentHistoricEvaluation.DTOHistoricalData(histAux.getValue(),histAux.getRationale(),histAux.getDate());
                    siHistInfo.add(histInfo);
                    j++;
                }
                siInfo.setHistoricalDataList(siHistInfo);
                result.add(siInfo);
            }
            return result;
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
}
