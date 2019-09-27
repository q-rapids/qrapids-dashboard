package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/Phases")
public class PhasesController {

    @RequestMapping("/Phases")
    public String Phases(){
        return "Phases/Phases";
    }

}
