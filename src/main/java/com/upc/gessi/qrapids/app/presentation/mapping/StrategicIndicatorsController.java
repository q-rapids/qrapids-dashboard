package com.upc.gessi.qrapids.app.presentation.mapping;


import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/StrategicIndicators")
public class StrategicIndicatorsController {

    @Autowired
    private QMAStrategicIndicators qmasi;

    @Autowired
    private QMAQualityFactors qmaqf;

/*
    @RequestMapping("/")
    public String index(){
        return "redirect:/StrategicIndicators/CurrentChart";
    }
*/
    @RequestMapping("/StrategicIndicators/CurrentChart")
    public String CurrentChart(){ return "StrategicIndicators/CurrentChart"; }

    @RequestMapping("/StrategicIndicators/CurrentTable")
    public String CurrentTable(){ return "StrategicIndicators/CurrentTable"; }

    @RequestMapping("/StrategicIndicators/HistoricTable")
    public String HistoricTable(){
        return "StrategicIndicators/HistoricTable";
    }

    @RequestMapping("/StrategicIndicators/HistoricChart")
    public String HistoricChart(){
        return "StrategicIndicators/HistoricChart";
    }

    @RequestMapping("/StrategicIndicators/PredictionChart")
    public String PredictionChart(){
        return "StrategicIndicators/PredictionChart";
    }

    @RequestMapping("/StrategicIndicators/New")
    public String NewStrategicIndicator(){
        if (qmasi.isCategoriesEmpty() && qmaqf.isCategoriesEmpty())
            return "StrategicIndicators/AdditionalScreens/CreateCategories";
        else
            return "StrategicIndicators/AdditionalScreens/NewStrategicIndicator";
    }

    @RequestMapping("/EditStrategicIndicators/{id}")
    public String EditStrategicIndicator(@PathVariable Long id){
        return "StrategicIndicators/AdditionalScreens/NewStrategicIndicator";
    }

    @RequestMapping("/StrategicIndicators/Feedback")
    public String Feedback(){
        return "StrategicIndicators/AdditionalScreens/Feedback";
    }

    @RequestMapping("/StrategicIndicators/FeedbackReport")
    public String FeedbackReport(){
        return "StrategicIndicators/AdditionalScreens/FeedbackReport";
    }

    @RequestMapping("/StrategicIndicators/Configuration")
    public String Config(){
        return "StrategicIndicators/AdditionalScreens/StrategicIndicatorsConfig";
    }
}
