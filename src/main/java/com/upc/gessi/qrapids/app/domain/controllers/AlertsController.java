package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.exceptions.AlertNotFoundException;
import com.upc.gessi.qrapids.app.domain.repositories.Metric.MetricRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
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
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Autowired
    private ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Autowired
    private QRPatternsController qrPatternsController;

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private  ProfilesController profilesController;

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

    public Pair<Long, Long> countNewAlertsByProfile(Project project, String profileId) throws ProjectNotFoundException {
        long newAlertsCount = alertRepository.countByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
        long newAlertsWithQRCount = alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)) { // if allSI true --> return all new alerts count
                return Pair.of(newAlertsCount, newAlertsWithQRCount);
            } else { // if allSI false --> return filtered new alerts
                List<Alert> allNewAlerts = alertRepository.getByProject_IdAndStatus(project.getId(), AlertStatus.NEW);
                List<Alert> allNewAlertsWithQR = alertRepository.getByProject_IdAndReqAssociatIsTrueAndStatusEquals(project.getId(), AlertStatus.NEW);
                List<Alert> filteredNewAlerts = filterByProfile(project, profile, allNewAlerts);
                List<Alert> filteredNewAlertsWithQR = filterByProfile(project, profile, allNewAlertsWithQR);
                return Pair.of(Long.valueOf(filteredNewAlerts.size()), Long.valueOf(filteredNewAlertsWithQR.size()));
            }
        } else { // if profile is null --> return all alerts
            return Pair.of(newAlertsCount, newAlertsWithQRCount);
        }
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

    public void checkStrategicIndicatorAlert(String externalId, float value, String prj){
        // get project from data base
        Project p = projectRepository.findByExternalId(prj);
        // get factor threshold from data base
        Strategic_Indicator si = strategicIndicatorRepository.findByExternalIdAndProjectId(externalId, p.getId());
        // check if the value is below the threshold then create new alert for this factor
        if (si.getThreshold() != null && value < si.getThreshold()) {
            // createAlert( id, name, type, value, threshold, category, project)
            createAlert(externalId, si.getName(), AlertType.STRATEGIC_INDICATOR, value, si.getThreshold(), externalId, p);
        }
    }

    public List<Alert> getAlertsByProjectAndProfile(Project project, String profileId) throws ProjectNotFoundException {
        List<Alert> alerts = alertRepository.findByProject_IdOrderByDateDesc(project.getId());
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)) { // allSI true --> filter only by profile quality level
                if(profile.getQualityLevel().equals(Profile.QualityLevel.METRICS_FACTORS))
                    alerts.removeIf(a -> a.getType().equals(AlertType.STRATEGIC_INDICATOR));
                else if (profile.getQualityLevel().equals(Profile.QualityLevel.METRICS)) {
                    alerts.removeIf(a -> a.getType().equals(AlertType.STRATEGIC_INDICATOR));
                    alerts.removeIf(a -> a.getType().equals(AlertType.FACTOR));
                }
                return alerts;
            } else { // if allSI false --> return alerts filtered by profile quality level and SIs
                return filterByProfile(project,profile,alerts);
            }
        } else { // if profile is null --> return all alerts
            return alerts;
        }
    }

    public List <Alert> filterByProfile (Project project, Profile profile, List<Alert> alerts) {
        List<ProfileProjectStrategicIndicators> ppsiList =
                profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project);
        List<Alert> result = new ArrayList<>();
        Profile.QualityLevel ql = profile.getQualityLevel();
        for (Alert a : alerts) {
            for (ProfileProjectStrategicIndicators ppsi : ppsiList) {
                if (a.getType().equals(AlertType.STRATEGIC_INDICATOR) && a.getId_element().equals(ppsi.getStrategicIndicator().getExternalId()) && !result.contains(a)
                        && ql.equals(Profile.QualityLevel.ALL)) result.add(a);
                List<StrategicIndicatorQualityFactors> siqfList = ppsi.getStrategicIndicator().getStrategicIndicatorQualityFactorsList();
                for (StrategicIndicatorQualityFactors siqf : siqfList) {
                    if (a.getType().equals(AlertType.FACTOR) && a.getId_element().equals(siqf.getFactor().getExternalId()) && !result.contains(a)
                            && (ql.equals(Profile.QualityLevel.ALL) || ql.equals(Profile.QualityLevel.METRICS_FACTORS))) result.add(a);
                    List<String> metrics = siqf.getFactor().getMetrics();
                    for (String m : metrics) {
                        if (a.getType().equals(AlertType.METRIC) && a.getId_element().equals(m) && !result.contains(a))
                            result.add(a);
                    }
                }
            }
        }
        return result;
    }
}
