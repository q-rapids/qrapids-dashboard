package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.domain.repositories.Metric.MetricRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AlertsController {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private QualityFactorRepository factorRepository;

    @Autowired
    private QRPatternsController qrPatternsController;

    public Alert getAlertById(long alertId) throws AlertNotFoundException {
        Optional<Alert> alertOptional = alertRepository.findById(alertId);
        if (alertOptional.isPresent()) {
            return alertOptional.get();
        } else {
            throw new AlertNotFoundException();
        }
    }

    public List<Alert> getAlerts(Project project) {
        return alertRepository.findByProject_IdOrderByDateDesc(project.getId());
    }

    public void setViewedStatusForAlerts(List<Alert> alerts) {
        List<Long> alertIds = new ArrayList<>();
        for (Alert alert : alerts) {
            alertIds.add(alert.getId());
        }
        if (!alerts.isEmpty())
            alertRepository.setViewedStatusFor(alertIds);
    }

    public Pair<Long, Long> countNewAlerts(Project project) {
        long newAlerts = alertRepository.countByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
        long newAlertsWithQR = alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
        return Pair.of(newAlerts, newAlertsWithQR);
    }

    public void createAlert (String id, String name, AlertType type, float value, float threshold, String category, Project project) {
        Alert alert = new Alert(id, name, type, value, threshold, category, new Date(), AlertStatus.NEW, false, project);
        boolean hasReq = qrPatternsController.existsPatternForAlert(alert);
        alert.setReqAssociat(hasReq);
        alertRepository.save(alert);
    }

    public void checkMetricAlert(String externalId, float value, String prj){
        // get project from data base
        Project p = projectRepository.findByExternalId(prj);
        // get metric threshold from data base
        Metric m = metricRepository.findByExternalIdAndProjectId(externalId, p.getId());
        // check if the value is below the threshold then create new alert for this metric
        if (m.getThreshold() != null && value < m.getThreshold()) {
            // createAlert( id, name, type, value, threshold, category, project)
            createAlert(externalId, m.getName(), AlertType.METRIC, value, m.getThreshold(), externalId, p);
        }
    }

    public void checkFactorAlert(String externalId, float value, String prj){
        // get project from data base
        Project p = projectRepository.findByExternalId(prj);
        // get factor threshold from data base
        Factor f = factorRepository.findByExternalIdAndProjectId(externalId, p.getId());
        // check if the value is below the threshold then create new alert for this factor
        if (f.getThreshold() != null && value < f.getThreshold()) {
            // createAlert( id, name, type, value, threshold, category, project)
            createAlert(externalId, f.getName(), AlertType.FACTOR, value, f.getThreshold(), externalId, p);
        }
    }
}
