package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller("/DetailedStrategicIndicators")
public class DetailedStrategicIndicatorsController {

    @RequestMapping("/DetailedStrategicIndicators/CurrentChart")
    public String CurrentChart(){
        return "DetailedStrategicIndicators/CurrentChart";
    }

    @RequestMapping("/DetailedStrategicIndicators/CurrentChartRadar")
    public String CurrentChartRadar(){
        return "DetailedStrategicIndicators/CurrentChart";
    }

    @RequestMapping("/DetailedStrategicIndicators/CurrentChartStacked")
    public String CurrentChartStacked(){
        return "DetailedStrategicIndicators/CurrentStacked";
    }

    @RequestMapping("/DetailedStrategicIndicators/CurrentTable")
    public String CurrentTable(){
        return "DetailedStrategicIndicators/CurrentTable";
    }

    @RequestMapping("/DetailedStrategicIndicators/HistoricTable")
    public String HistoricTable(){
        return "DetailedStrategicIndicators/HistoricTable";
    }

    @RequestMapping("/DetailedStrategicIndicators/HistoricChart")
    public String HistoricChart(){
        return "DetailedStrategicIndicators/HistoricChart";
    }

    @RequestMapping("/DetailedStrategicIndicators/PredictionChart")
    public String PredictionChart(){
        return "DetailedStrategicIndicators/PredictionChart";
    }
}