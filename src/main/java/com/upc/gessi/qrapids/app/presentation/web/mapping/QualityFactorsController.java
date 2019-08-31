package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller("/QualityFactor")
public class QualityFactorsController {

    @RequestMapping("/QualityFactors/CurrentChart")
    public String CurrentChart(){
        return "QualityFactors/CurrentChart";
    }

    @RequestMapping("/QualityFactors/CurrentTable")
    public String CurrentTable(){
        return "QualityFactors/CurrentTable";
    }

    @RequestMapping("/QualityFactors/HistoricTable")
    public String HistoricTable(){
        return "QualityFactors/HistoricTable";
    }

    @RequestMapping("/QualityFactors/HistoricChart")
    public String HistoricChart(){
        return "QualityFactors/HistoricChart";
    }

    @RequestMapping("/QualityFactors/PredictionChart")
    public String PredictionChart(){
        return "QualityFactors/PredictionChart";
    }
}
