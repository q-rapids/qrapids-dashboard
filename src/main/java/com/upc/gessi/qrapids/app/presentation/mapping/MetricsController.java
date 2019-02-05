package com.upc.gessi.qrapids.app.presentation.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller("/Metrics")
public class MetricsController {

    @RequestMapping("/Metrics/CurrentChart")
    public String CurrentChart(){
        return "Metrics/CurrentChart";
    }

    @RequestMapping("/Metrics/CurrentTable")
    public String CurrentTable(){
        return "Metrics/CurrentTable";
    }

    @RequestMapping("/Metrics/HistoricTable")
    public String HistoricTable(){
        return "Metrics/HistoricTable";
    }

    @RequestMapping("/Metrics/HistoricChart")
    public String HistoricChart(){
        return "Metrics/HistoricChart";
    }

    @RequestMapping("/Metrics/PredictionChart")
    public String PredictionChart(){
        return "Metrics/PredictionChart";
    }
}
