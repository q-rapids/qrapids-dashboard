package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/QualityRequirements")
public class QualityRequirementsController {

    @RequestMapping("/QualityRequirements")
    public String QualityRequirement(){
        return "QualityRequirements/QualityRequirements";
    }

    @RequestMapping("/Decisions")
    public String Decisions() {
        return "Decisions/Decisions";
    }

}
