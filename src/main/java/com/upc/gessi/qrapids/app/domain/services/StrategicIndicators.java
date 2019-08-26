package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import com.upc.gessi.qrapids.app.exceptions.*;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchStatusException;
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
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    private ProjectsController projectsController;

    @Value("${forecast.technique}")
    private String forecastTechnique;

    @GetMapping("/api/strategicIndicators/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsEvaluation(@RequestParam(value = "prj") String prj) {
        try {
            return strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/current")
    @ResponseStatus(HttpStatus.OK)
    public DTOStrategicIndicatorEvaluation getSingleStrategicIndicatorEvaluation(@RequestParam("prj") String prj, @PathVariable String id) {
        try {
            return strategicIndicatorsController.getSingleStrategicIndicatorsCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getDetailedSICurrentEvaluation(@RequestParam(value = "prj", required=false) String prj) {
        try {
            return strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getSingleDetailedSICurrentEvaluation(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id) {
        try {
            return strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(@RequestParam(value = "prj") String prj, @PathVariable String id) {
        try {
            return qualityFactorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(id, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/historical")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            return strategicIndicatorsController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
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
            return qualityFactorsController.getFactorsWithMetricsForOneStrategicIndicatorHistoricalEvaluation(id, prj, LocalDate.parse(from), LocalDate.parse(to));
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            return strategicIndicatorsController.getStrategicIndicatorsPrediction(technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getQualityFactorsPredictionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) {
        try {
            List<DTODetailedStrategicIndicator> currentEvaluation = strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(prj);
            return strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getSingleQualityFactorsPredictionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTODetailedStrategicIndicator> currentEvaluation = strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(id, prj);
            return strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/metrics/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOQualityFactor> getQualityFactorsPredictionData(@RequestParam(value = "prj") String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) {
        try {
            List<DTOQualityFactor> currentEvaluation = qualityFactorsController.getFactorsWithMetricsForOneStrategicIndicatorCurrentEvaluation(id, prj);
            return qualityFactorsController.getFactorsWithMetricsPrediction(currentEvaluation, technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOSI> getAllStrategicIndicators (@RequestParam(value = "prj") String prj) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<Strategic_Indicator> strategic_indicators = strategicIndicatorsController.getStrategicIndicatorsByProject(project);
            List<DTOSI> dtoSIList = new ArrayList<>();
            for (Strategic_Indicator strategic_indicator : strategic_indicators) {
                DTOSI dtosi = new DTOSI(strategic_indicator.getId(),
                        strategic_indicator.getExternalId(),
                        strategic_indicator.getName(),
                        strategic_indicator.getDescription(),
                        strategic_indicator.getNetwork(),
                        strategic_indicator.getQuality_factors());
                dtoSIList.add(dtosi);
            }
            return dtoSIList;
        } catch (ProjectNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DTOSI getSI(@PathVariable Long id) {
        try {
            Strategic_Indicator strategicIndicator = strategicIndicatorsController.getStrategicIndicatorById(id);
            return new DTOSI(strategicIndicator.getId(),
                    strategicIndicator.getExternalId(),
                    strategicIndicator.getName(),
                    strategicIndicator.getDescription(),
                    strategicIndicator.getNetwork(),
                    strategicIndicator.getQuality_factors());
        } catch (StrategicIndicatorNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic indicator not found");
        }
    }

    @PostMapping("/api/strategicIndicators")
    @ResponseStatus(HttpStatus.CREATED)
    public void newSI(HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String prj = request.getParameter("prj");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] file = null;
            if (network != null) {
                file = IOUtils.toByteArray(network.getInputStream());
            }
            List<String> qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            if (!name.equals("") && qualityFactors.size() > 0) {
                Project project = projectsController.findProjectByExternalId(prj);
                strategicIndicatorsController.saveStrategicIndicator(name, description, file, qualityFactors, project);
                if (!strategicIndicatorsController.assessStrategicIndicator(name)) {
                    throw new AssessmentErrorException();
                }
            }
        } catch (AssessmentErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assessment error: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void editSI(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String name;
            String description;
            byte[] file = null;
            List<String> qualityFactors;
            try {
                name = request.getParameter("name");
                description = request.getParameter("description");
                if (network != null) {
                    file = IOUtils.toByteArray(network.getInputStream());
                }
                qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            } catch (Exception e) {
                throw new MissingParametersException();
            }
            if (!name.equals("") && qualityFactors.size() > 0) {
                Strategic_Indicator oldStrategicIndicator = strategicIndicatorsController.getStrategicIndicatorById(id);
                strategicIndicatorsController.editStrategicIndicator(id, name, description, file, qualityFactors);

                List<String> strategicIndicatorQualityFactors = oldStrategicIndicator.getQuality_factors();
                boolean sameFactors = (strategicIndicatorQualityFactors.size() == qualityFactors.size());
                int i = 0;
                while (i < strategicIndicatorQualityFactors.size() && sameFactors) {
                    if (qualityFactors.indexOf(strategicIndicatorQualityFactors.get(i)) == -1)
                        sameFactors = false;
                    i++;
                }
                if (!sameFactors) {
                    if (!strategicIndicatorsController.assessStrategicIndicator(name)) {
                        throw new AssessmentErrorException();
                    }
                }
            }
        } catch (MissingParametersException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing parameters in the request");
        } catch (StrategicIndicatorNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic Indicator not found");
        } catch (AssessmentErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assessment error: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integrity violation: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSI (@PathVariable Long id) {
        try {
            strategicIndicatorsController.deleteStrategicIndicator(id);
        } catch (StrategicIndicatorNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic indicator not found");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough categories");
        }
    }

    private enum TrainType {
        NONE, ONE, ALL
    }

    @GetMapping("/api/strategicIndicators/assess")
    @ResponseStatus(HttpStatus.OK)
    public void assesStrategicIndicators(@RequestParam(value = "prj", required=false) String prj,
                                         @RequestParam(value = "from", required=false) String from,
                                         @RequestParam(value = "train", required = false, defaultValue = "ONE") TrainType trainType) {
        boolean correct = true;

        try {

            if (from != null && !from.isEmpty()) {
                LocalDate dateFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                correct = strategicIndicatorsController.assessStrategicIndicators(prj, dateFrom);
            }
            else
                correct = strategicIndicatorsController.assessStrategicIndicators(prj, null);

            // Train forecast models
            if (trainType != TrainType.NONE) {
                String technique = null;
                if (trainType == TrainType.ONE) {
                    technique = forecastTechnique;
                }
                if (prj == null) {
                    strategicIndicatorsController.trainForecastModelsAllProjects(technique);
                } else {
                    strategicIndicatorsController.trainForecastModelsSingleProject(prj, technique);
                }
            }

            if (!correct) {
                throw new AssessmentErrorException();
            }
        } catch (AssessmentErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assessment error: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error in the request parameters");
        }
    }

    @GetMapping("/api/strategicIndicators/fetch")
    @ResponseStatus(HttpStatus.OK)
    public void fetchSIs() {
        try {
            strategicIndicatorsController.fetchStrategicIndicators();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/api/strategicIndicators/simulate")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> Simulate(@RequestParam(value = "prj", required=false) String prj, HttpServletRequest request) {
        try {
            JsonParser parser = new JsonParser();
            JsonArray simulatedFactorsJsonArray = parser.parse(request.getParameter("factors")).getAsJsonArray();
            Map<String, Float> simulatedFactorsMap = new HashMap<>();
            for (int i = 0; i < simulatedFactorsJsonArray.size(); i++) {
                String factorName = simulatedFactorsJsonArray.get(i).getAsJsonObject().get("id").getAsString();
                Float factorValue = simulatedFactorsJsonArray.get(i).getAsJsonObject().get("value").getAsFloat();
                simulatedFactorsMap.put(factorName, factorValue);
            }
            return strategicIndicatorsController.simulateStrategicIndicatorsAssessment(simulatedFactorsMap, prj);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulation error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/qualityModel")
    @ResponseStatus(HttpStatus.OK)
    public List<DTORelationsSI> getQualityModel(@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) {
        try {
            if (date == null)
                return strategicIndicatorsController.getQualityModel(prj, null);
            else
                return strategicIndicatorsController.getQualityModel(prj, LocalDate.parse(date));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/forecastTechniques")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getForecastTechniques() {
        return strategicIndicatorsController.getForecastTechniques();
    }
}
