package com.upc.gessi.qrapids.app.domain.adapters;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.dto.DTOBacklog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class Backlog {

    @Value("${backlog.url}")
    private String url;

    public QualityRequirement postNewQualityRequirement (QualityRequirement qualityRequirement) {
        if (url != null && qualityRequirement.getProject().getBacklogId() != null) {
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

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
}
