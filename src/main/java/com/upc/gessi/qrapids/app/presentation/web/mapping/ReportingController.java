package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@Controller("/Reporting")
public class ReportingController {
    @RequestMapping("/Reporting")
    public String reporting(){ return "Reporting/Reporting"; }
}
