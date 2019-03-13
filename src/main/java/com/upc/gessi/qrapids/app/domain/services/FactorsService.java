package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class FactorsService {

    @Autowired
    private QMAQualityFactors qmaqf;

    @Autowired
    private Forecast qmaf;

    @RequestMapping("/api/QualityFactors/CurrentEvaluation")
    public List<DTOQualityFactor> getQualityFactorsEvaluations(@RequestParam(value = "prj", required=false) String prj) throws IOException {
        return qmaqf.CurrentEvaluation(null, prj);
    }

    @RequestMapping("/api/QualityFactors/{id}/CurrentEvaluation")
    public DTOFactor getSingleFactorEvaluation (@RequestParam("prj") String prj, @PathVariable String id) throws IOException {
        return qmaqf.SingleCurrentEvaluation(id, prj);
    }

    @RequestMapping("/api/QualityFactors/CurrentEvaluation/{id}")
    public List<DTOQualityFactor> getQualityFactorsEvaluations(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id) throws IOException {
        return qmaqf.CurrentEvaluation(id, prj);
    }

    @RequestMapping("/api/QualityFactors/HistoricalData")
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        return qmaqf.HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), prj);
    }

    @RequestMapping("/api/QualityFactors/HistoricalData/{id}")
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        return qmaqf.HistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
    }

    @RequestMapping("/api/QualityFactors/getAll")
    public List<DTOFactor> getAllQualityFactors(@RequestParam(value = "prj", required=false) String prj) throws IOException {
        return qmaqf.getAllFactors(prj);
    }

    @RequestMapping("/api/QualityFactors/PredictionData/{id}")
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) throws IOException {
        return qmaf.ForecastFactor(qmaqf.CurrentEvaluation(id, prj), technique,"7", horizon, prj);
    }

    @RequestMapping("/api/QualityFactors/PredictionData")
    public @ResponseBody
    List<DTOQualityFactor> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon) throws IOException {
        return qmaf.ForecastFactor(qmaqf.CurrentEvaluation(null, prj), technique, "7", horizon, prj);
    }

}
