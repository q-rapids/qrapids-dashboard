package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.AlertType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.Type;
import com.upc.gessi.qrapids.app.exceptions.AlertNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import qr.QRGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AlertsController {

    @Autowired
    private AlertRepository alertRepository;

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
}
