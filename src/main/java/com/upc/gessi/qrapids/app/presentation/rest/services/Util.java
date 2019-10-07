package com.upc.gessi.qrapids.app.presentation.rest.services;


import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
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

    @Value("${rawdata.dashboard}")
    private String rawdataDashboard;

    @Value("${pabre.url}")
    private String pabreUrl;

    @Value("${server.url}")
    private String serverUrl;

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

        String type = "Release";

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);

        LocalDate date2 = LocalDate.now();
        date2 = date2.plusDays(20);

        LocalDate date3 = LocalDate.now();
        date3 = date3.plusDays(40);

        milestoneList.add(new DTOMilestone(date3.toString(), "Version 1.5", "Version 1.5 adding new features", type));
        milestoneList.add(new DTOMilestone(date.toString(), "Version 1.3", "Version 1.3 adding new features", type));
        milestoneList.add(new DTOMilestone(date2.toString(), "Version 1.4", "Version 1.4 adding new features", type));
        milestoneList.add(new DTOMilestone("2019-08-15", "Version 1.2", "Version 1.2 adding new features", type));

        return milestoneList;
    }

    @GetMapping("/api/phasesTest")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOPhase> getPhasesTest(@RequestParam("project_id") String projectId, @RequestParam(value = "date_from", required = false) String dateFrom) {
        List<DTOPhase> phaseList = new ArrayList<>();

        LocalDate date_from = LocalDate.now();
        LocalDate date_to = date_from.plusDays(30);


        LocalDate date2_from = LocalDate.now();
        date2_from = date2_from.plusDays(30);
        LocalDate date2_to = date2_from.plusDays(30);


        LocalDate date3_from = LocalDate.now();
        date3_from = date3_from.plusDays(60);
        LocalDate date3_to = date3_from.plusDays(90);

        phaseList.add(new DTOPhase(date2_from.toString(), "Design", "Defining project requirements and initial design", date2_to.toString()));
        phaseList.add(new DTOPhase(date_from.toString(), "Project set-up", "Eliciting project goals & setting-up the infrastruture and teams", date_to.toString()));
        phaseList.add(new DTOPhase(date3_from.toString(), "Development", "Development, Testing, and Releasing", date3_to.toString()));

        return phaseList;
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
