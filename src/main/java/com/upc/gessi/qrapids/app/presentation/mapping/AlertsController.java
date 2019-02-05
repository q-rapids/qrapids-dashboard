package com.upc.gessi.qrapids.app.presentation.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/QualityAlerts")
public class AlertsController
{
    @RequestMapping("/QualityAlerts")
    public String Alerts(){
        return "Alerts/QualityAlerts";
    }
}

