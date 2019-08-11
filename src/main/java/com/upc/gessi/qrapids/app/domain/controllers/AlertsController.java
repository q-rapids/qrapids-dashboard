package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> getAlerts(String projectExternalId) throws ProjectNotFoundException {
        Project project = projectRepository.findByExternalId(projectExternalId);
        if (project != null) {
            return alertRepository.findByProject_IdOrderByDateDesc(project.getId());
        } else {
            throw new ProjectNotFoundException();
        }
    }

    public void setViewedStatusForAlerts(List<Alert> alerts) {
        List<Long> alertIds = new ArrayList<>();
        for (Alert alert : alerts) {
            alertIds.add(alert.getId());
        }
        if (!alerts.isEmpty())
            alertRepository.setViewedStatusFor(alertIds);
    }

    public Pair<Long, Long> countNewAlerts(String projectExternalId) throws ProjectNotFoundException {
        Project project = projectRepository.findByExternalId(projectExternalId);
        if (project != null) {
            long newAlerts = alertRepository.countByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
            long newAlertsWithQR = alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
            return Pair.of(newAlerts, newAlertsWithQR);
        } else {
            throw new ProjectNotFoundException();
        }
    }
}
