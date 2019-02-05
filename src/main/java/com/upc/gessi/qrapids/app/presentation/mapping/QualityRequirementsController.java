package com.upc.gessi.qrapids.app.presentation.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/QualityRequirements")
public class QualityRequirementsController {
    @RequestMapping("/QualityRequirements/QualityRequirementCost")
    public String QualityRequirementCost(){
        return "QualityRequirements/QualityRequirementCost";
    }

    @RequestMapping("/QualityRequirements/QualityRequirement")
    public String QualityRequirement(){
        return "QualityRequirements/QualityRequirement";
    }

}
