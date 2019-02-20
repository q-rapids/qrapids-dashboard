package com.upc.gessi.qrapids.app.domain.services;


import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAMetrics;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class Metrics {

    @Autowired
    private QMAMetrics qmam;

    @Autowired
    private Forecast qmaf;

    @RequestMapping("/api/Metrics/CurrentEvaluation")
    public List<DTOMetric> getMetricsEvaluations(@RequestParam(value = "prj", required=false) String prj) throws IOException {
        return qmam.CurrentEvaluation(null, prj);
    }
    @RequestMapping("/api/Metrics/CurrentEvaluation/{id}")
    public List<DTOMetric> getMetricsEvaluation(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id) throws IOException {
        return qmam.CurrentEvaluation(id, prj);
    }
    @RequestMapping("/api/Metrics/HistoricalData")
    public @ResponseBody
    List<DTOMetric> getMetricsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        return qmam.HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), prj);
    }

    @RequestMapping("/api/Metrics/HistoricalData/{id}")
    public @ResponseBody
    List<DTOMetric> getMetricsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to) throws IOException {
        return qmam.HistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
    }

    @RequestMapping("/api/Metrics/PredictionData/{id}")
    public @ResponseBody
    List<DTOMetric> getMetricsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String technique, @RequestParam("horizon") String horizon, @PathVariable String id) throws IOException {
        return qmaf.ForecastMetric(qmam.CurrentEvaluation(id,prj), technique, "7", horizon, prj);
    }

    @RequestMapping("/api/Metrics/PredictionData")
    public @ResponseBody
    List<DTOMetric> getMetricsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("technique") String techinique, @RequestParam("horizon") String horizon) throws IOException {
        return qmaf.ForecastMetric(qmam.CurrentEvaluation(null, prj), techinique, "7", horizon, prj);
    }

}
