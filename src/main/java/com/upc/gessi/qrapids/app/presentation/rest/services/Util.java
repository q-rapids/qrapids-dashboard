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
import java.util.Arrays;
import java.util.List;

@RestController
public class Util {

    @Value("${rawdata.dashboard}")
    private String rawdataDashboard;

    @Value("${pabre.url}")
    private String pabreUrl;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${assessSI.url:}")
    private String assessSIUrl;

    // add jasterserver data
    @Value("${jasperServer.url}")
    private String jasperserverURL;
    @Value("${jasperserver.user}")
    private String jasperserverUser;
    @Value("${jasperserver.password}")
    private String jasperserverPassword;

    @GetMapping("/api/jasperserverInfo")
    @ResponseStatus(HttpStatus.OK)
    public List<String> jasperserverInfo() {
        return Arrays.asList(jasperserverURL, jasperserverUser, jasperserverPassword);
    }

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

    @GetMapping("/api/assessSIUrl")
    @ResponseStatus(HttpStatus.OK)
    public String assessSIUrl() {
        return "{\"assessSIUrl\":\""+assessSIUrl+"\"}";
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

        return milestoneList;
    }

    @GetMapping("/api/phasesTest")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOPhase> getPhasesTest(@RequestParam("project_id") String projectId, @RequestParam(value = "date_from", required = false) String dateFrom) {
        List<DTOPhase> phaseList = new ArrayList<>();

        // today phase
        LocalDate dateFrom4 = LocalDate.now().minusDays(3);
        LocalDate dateTo4 = dateFrom4.plusDays(5);

        // future phase
        LocalDate dateFrom5 = dateTo4;
        LocalDate dateTo5 = dateFrom5.plusDays(5);

        // past phases
        LocalDate dateFrom3 = dateFrom4.minusDays(5);
        LocalDate dateTo3 = dateFrom4;

        LocalDate dateFrom2 = dateFrom3.minusDays(5);
        LocalDate dateTo2 = dateFrom3;

        LocalDate dateFrom1 = dateFrom2.minusDays(5);
        LocalDate dateTo1 = dateFrom2;

        phaseList.add(new DTOPhase(dateFrom3.toString(), "Development", "Implementation of the project functionalities", dateTo3.toString()));
        phaseList.add(new DTOPhase(dateFrom2.toString(), "Design", "Defining project requirements and initial design", dateTo2.toString()));
        phaseList.add(new DTOPhase(dateFrom4.toString(), "Testing", "Testing of the project functionalities", dateTo4.toString()));
        phaseList.add(new DTOPhase(dateFrom1.toString(), "Project set-up", "Eliciting project goals & setting-up the infrastruture and teams", dateTo1.toString()));
        phaseList.add(new DTOPhase(dateFrom5.toString(), "Closing", "Releasing the final deliverables to the customer and communicating the closure of the project to all stakeholders", dateTo5.toString()));

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
