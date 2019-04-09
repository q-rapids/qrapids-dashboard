package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.JsonArray;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMASimulation;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOFactorCategory;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Body;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private QFCategoryRepository qfCategoryRepository;

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

    @RequestMapping("/api/QualityFactors/categories")
    public List<DTOFactorCategory> getQualityFactorsCategories () throws IOException {
        List<QFCategory> categories = qfCategoryRepository.findAll();
        List<DTOFactorCategory> dtoFactorCategories = new ArrayList<>();
        for (QFCategory category : categories) {
            dtoFactorCategories.add(new DTOFactorCategory(category.getId(), category.getName(), category.getColor(), category.getUpperThreshold()));
        }
        return dtoFactorCategories;
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

    @PostMapping("/api/QualityFactors/Simulate")
    public List<DTOFactor> simulate (@RequestParam("prj") String prj, @RequestParam("date") String date, @RequestBody List<DTOMetric> metrics) throws IOException {
        //TODO: remove
        if (prj.equals("test"))
            prj = "modelio38";
        Map<String, Float> metricsMap = new HashMap<>();
        for (DTOMetric metric : metrics) {
            metricsMap.put(metric.getId(), metric.getValue());
        }
        return qmaSimulation.simulateQualityFactors(metricsMap, prj, LocalDate.parse(date));
    }
}
