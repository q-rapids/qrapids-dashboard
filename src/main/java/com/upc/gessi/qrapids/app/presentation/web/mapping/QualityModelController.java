package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/QualityModel")
public class QualityModelController {
    @RequestMapping("/QualityModelGraph")
    public String CurrentChartGraph(){
        return "QualityModel/QualityModelGraph";
    }
    @RequestMapping("/QualityModelSunburst")
    public String CurrentChartSunburst(){
        return "QualityModel/QualityModelSunburst";
    }
}
