package com.upc.gessi.qrapids.app.presentation.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/QualityModel")
public class QualityModelController {
    @RequestMapping("/QualityModel")
    public String CurrentChart(){
        return "QualityModel/QualityModel";
    }
}
