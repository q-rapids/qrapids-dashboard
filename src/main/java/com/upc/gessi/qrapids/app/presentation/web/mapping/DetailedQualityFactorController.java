package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller("/DetailedQualityFactor")
public class DetailedQualityFactorController {

    @RequestMapping("/DetailedQualityFactors/CurrentChart")
    public String CurrentChart(){
        return "DetailedQualityFactors/CurrentChart";
    }

    @RequestMapping("/DetailedQualityFactors/CurrentChartRadar")
    public String CurrentChartRadar(){
        return "DetailedQualityFactors/CurrentChart";
    }

    @RequestMapping("/DetailedQualityFactors/CurrentChartStacked")
    public String CurrentChartStacked(){
        return "DetailedQualityFactors/CurrentStacked";
    }

    @RequestMapping("/DetailedQualityFactors/CurrentTable")
    public String CurrentTable(){
        return "DetailedQualityFactors/CurrentTable";
    }

    @RequestMapping("/DetailedQualityFactors/HistoricTable")
    public String HistoricTable(){
        return "DetailedQualityFactors/HistoricTable";
    }

    @RequestMapping("/DetailedQualityFactors/HistoricChart")
    public String HistoricChart(){
        return "DetailedQualityFactors/HistoricChart";
    }

    @RequestMapping("/DetailedQualityFactors/PredictionChart")
    public String PredictionChart(){
        return "DetailedQualityFactors/PredictionChart";
    }
}
