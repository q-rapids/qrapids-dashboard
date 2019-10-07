package com.upc.gessi.qrapids.app.domain.adapters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOBacklog;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOPhase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

@Component
public class Backlog {

    @Value("${backlog.newIssue.url}")
    private String newIssueUrl;

    @Value("${backlog.milestones.url}")
    private String milestonesUrl;

    @Value("${backlog.phases.url}")
    private String phasesUrl;

    public QualityRequirement postNewQualityRequirement (QualityRequirement qualityRequirement) {
        if (newIssueUrl != null && qualityRequirement.getProject().getBacklogId() != null) {
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(newIssueUrl);

            Map<String, String> params = new HashMap<>();
            params.put("issue_summary", qualityRequirement.getRequirement());
            params.put("issue_description", qualityRequirement.getDescription());
            params.put("issue_type", "Story");
            params.put("project_id", qualityRequirement.getProject().getBacklogId());
            params.put("decision_rationale", qualityRequirement.getDecision().getRationale());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), requestEntity, String.class);

            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED) {
                Gson gson = new Gson();
                DTOBacklog dtoBacklog = gson.fromJson(responseEntity.getBody(), DTOBacklog.class);
                qualityRequirement.setBacklogId(dtoBacklog.getIssue_id());
                qualityRequirement.setBacklogUrl(dtoBacklog.getIssue_url());
            }
        }

        return qualityRequirement;
    }

    public List<DTOMilestone> getMilestones (String backlogProjectId, LocalDate dateFrom) {
        List<DTOMilestone> dtoMilestonesList = new ArrayList<>();
        if (milestonesUrl != null) {
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(milestonesUrl)
                    .queryParam("project_id", backlogProjectId);
            if (dateFrom != null) {
                builder.queryParam("date_from", dateFrom.toString());
            }

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().encode().toUri(), String.class);

            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<DTOMilestone>>(){}.getType();
                dtoMilestonesList = gson.fromJson(responseEntity.getBody(), listType);
                if (dtoMilestonesList.size() > 0) {
                    Collections.sort(dtoMilestonesList, (x, y) -> x.getDate().compareTo(y.getDate()));
                    if (dateFrom != null) {
                        int i = 0;
                        boolean b = false;
                        while (i < dtoMilestonesList.size() && !b) {
                            if (dtoMilestonesList.get(i).getDate().compareTo(dateFrom.toString()) == 1) b = true;
                            else {
                                dtoMilestonesList.remove(i);
                            }
                        }
                    }
                }
            }
        }
        return dtoMilestonesList;
    }

    public List<DTOPhase> getPhases (String backlogProjectId, LocalDate dateFrom) {
        List<DTOPhase> dtoPhasesList = new ArrayList<>();
        if (phasesUrl != null) {
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(phasesUrl)
                    .queryParam("project_id", backlogProjectId);
            if (dateFrom != null) {
                builder.queryParam("date_from", dateFrom.toString());
            }

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().encode().toUri(), String.class);

            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<DTOPhase>>(){}.getType();
                dtoPhasesList = gson.fromJson(responseEntity.getBody(), listType);
                if (dtoPhasesList.size() > 0) {
                    Collections.sort(dtoPhasesList, (x, y) -> x.getDateFrom().compareTo(y.getDateFrom()));
                    if (dateFrom != null) {
                        int i = 0;
                        boolean b = false;
                        while (i < dtoPhasesList.size() && !b) {
                            if (dtoPhasesList.get(i).getDateFrom().compareTo(dateFrom.toString()) == 1) b = true;
                            else {
                                dtoPhasesList.remove(i);
                            }
                        }
                    }
                }
            }
        }
        return dtoPhasesList;
    }
}
