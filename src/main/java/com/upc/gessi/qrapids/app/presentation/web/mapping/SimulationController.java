package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@Controller("/Simulation")
public class SimulationController {
    @RequestMapping("Simulation/Factors")
    public String SISimulation(){
        return "Simulation/SimulationFactors";
    }

    @RequestMapping("Simulation/Metrics")
    public String simulationMetrics() {
        return "Simulation/SimulationMetrics";
    }

    @RequestMapping("Simulation/QR")
    public String simulationQR() {
        return "Simulation/SimulationQR";
    }

}
