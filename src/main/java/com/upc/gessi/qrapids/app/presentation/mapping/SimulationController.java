package com.upc.gessi.qrapids.app.presentation.mapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@Controller("/Simulation")
public class SimulationController {
    @RequestMapping("/Simulation")
    public String SISimulation(){
        return "Simulation/SimulationFactors";
    }

}
