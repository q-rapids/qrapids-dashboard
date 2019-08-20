package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.*;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@RestController
public class StrategicIndicators {

    @Autowired
    private QMAStrategicIndicators qmasi;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private Forecast qmaf;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

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
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) throws IOException {
        try {
            return qmaf.ForecastSI(technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/strategicIndicators/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) throws IOException {
        try {
            return qmaf.ForecastDSI(qmadsi.CurrentEvaluation(null, prj), technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
        }
    }

    @GetMapping("/api/strategicIndicators/{id}/qualityFactors/prediction")
    @ResponseStatus(HttpStatus.OK)
    public List<DTODetailedStrategicIndicator> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) throws IOException {
        try {
            return qmaf.ForecastDSI(qmadsi.CurrentEvaluation(id, prj), technique, "7", horizon, prj);
        } catch (ElasticsearchStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project identifier does not exist");
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
        Project project = projectRepository.findByExternalId(prj);
        List<Strategic_Indicator> strategic_indicators = siRep.findByProject_Id(project.getId());
        List<DTOSI> dtosis = new ArrayList<>();
        for (Strategic_Indicator strategic_indicator : strategic_indicators) {
            DTOSI dtosi = new DTOSI(strategic_indicator.getId(),
                    strategic_indicator.getExternalId(),
                    strategic_indicator.getName(),
                    strategic_indicator.getDescription(),
                    strategic_indicator.getNetwork(),
                    strategic_indicator.getQuality_factors());
            dtosis.add(dtosi);
        }
        return dtosis;
    }

    @DeleteMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSI (@PathVariable Long id) {
        siRep.deleteById(id);
    }
}
