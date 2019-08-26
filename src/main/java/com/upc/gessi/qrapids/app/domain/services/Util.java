package com.upc.gessi.qrapids.app.domain.services;


import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTOMilestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Util {

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private QMAQualityFactors qmaqf;

    @Autowired
    private QMAProjects qmaPrj;

    @Autowired
    private QMARelations qmaRelations;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private SICategoryRepository SICatRep;

    @Autowired
    private AssesSI AssesSI;

    @Value("${rawdata.dashboard}")
    private String rawdataDashboard;

    @Value("${pabre.url}")
    private String pabreUrl;

    @Value("${server.url}")
    private String serverUrl;

    @Autowired
    private Forecast forecast;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private Backlog backlog;

    @Autowired
    StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    QualityFactorsController qualityFactorsController;

    @GetMapping("/api/rawdataDashboard")
    @ResponseStatus(HttpStatus.OK)
    public String RawDataDashboard() {
        return rawdataDashboard;
    }

    @GetMapping("/api/serverUrl")
    @ResponseStatus(HttpStatus.OK)
    public String serverUrl() {
        return "{\"serverUrl\":\""+serverUrl+"\"}";
    }

    @PostMapping("/api/createIssueTest")
    @ResponseStatus(HttpStatus.OK)
    public String addToBacklogUrl() {
        return "{\"issue_url\":\"https://essi.upc.edu/jira/issue/999\"," +
                "\"issue_id\":\"ID-999\"}";
    }

    @GetMapping("/api/milestonesTest")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMilestone> getMilestonesTest(@RequestParam("project_id") String projectId, @RequestParam(value = "date_from", required = false) String dateFrom) {
        List<DTOMilestone> milestoneList = new ArrayList<>();

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        milestoneList.add(new DTOMilestone(date.toString(), "Version 1.3", "Version 1.3 adding new features", "Release"));

        LocalDate date2 = LocalDate.now();
        date2 = date2.plusDays(20);
        milestoneList.add(new DTOMilestone(date2.toString(), "Version 1.4", "Version 1.4 adding new features", "Release"));

        LocalDate date3 = LocalDate.now();
        date3 = date3.plusDays(40);
        milestoneList.add(new DTOMilestone(date3.toString(), "Version 1.5", "Version 1.5 adding new features", "Release"));

        return milestoneList;
    }

    @GetMapping("/api/forecastTechniques")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getForecastTechniques() {
        return forecast.getForecastTechniques();
    }

    @GetMapping("api/me")
    @ResponseStatus(HttpStatus.OK)
    public String getUserName (HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "{}";
        } else {
            return "{\"userName\":\"" + authentication.getName() + "\"}";
        }
    }
}
