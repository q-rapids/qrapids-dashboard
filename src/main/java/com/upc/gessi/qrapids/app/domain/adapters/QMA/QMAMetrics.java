package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.MetricEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOMetricEvaluation;
import evaluation.Factor;
import evaluation.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QMAMetrics {

    @Autowired
    private QMAConnection qmacon;

    // it was made to use these variables in static method
    private static ProfilesController profilesController;
    private static QualityFactorRepository factorRepository;
    private static ProjectRepository prjRep;
    private static ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Autowired
    public QMAMetrics(ProfilesController profilesController, QualityFactorRepository factorRepository, ProjectRepository prjRep, ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository) {
        QMAMetrics.profilesController = profilesController;
        QMAMetrics.factorRepository = factorRepository;
        QMAMetrics.prjRep = prjRep;
        QMAMetrics.profileProjectStrategicIndicatorsRepository = profileProjectStrategicIndicatorsRepository;
    }

    public List<DTOMetricEvaluation> CurrentEvaluation(String id, String prj, String profile) throws IOException {
        List<DTOMetricEvaluation> result;

        List<MetricEvaluationDTO> evals;

        qmacon.initConnexion();

        if (id == null)
            evals = Metric.getEvaluations(prj);
        else
            evals = Factor.getMetricsEvaluations(prj, id).getMetrics();
        //Connection.closeConnection();
        result = MetricEvaluationDTOListToDTOMetricList(id, evals, prj, profile);

        return result;
    }

    public DTOMetricEvaluation SingleCurrentEvaluation(String metricId, String prj) throws IOException {
        qmacon.initConnexion();
        MetricEvaluationDTO metricEvaluationDTO = Metric.getSingleEvaluation(prj, metricId);
        return MetricEvaluationDTOToDTOMetric(metricEvaluationDTO, metricEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTOMetricEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj, String profile) throws IOException {
        List<DTOMetricEvaluation> result;

        List<MetricEvaluationDTO> evals;

        qmacon.initConnexion();
        if (id == null)
            evals = Metric.getEvaluations(prj, from, to);
        else
            evals = Factor.getMetricsEvaluations(prj, id, from, to).getMetrics();
        //Connection.closeConnection();
        result = MetricEvaluationDTOListToDTOMetricList(null, evals, prj, profile);

        return result;
    }

    public List<DTOMetricEvaluation> SingleHistoricalData (String metricId, LocalDate from, LocalDate to, String prj, String profile) throws IOException {
        qmacon.initConnexion();
        MetricEvaluationDTO metricEvaluationDTO = Metric.getSingleEvaluation(prj, metricId, from, to);
        List<MetricEvaluationDTO> metricEvaluationDTOList = new ArrayList<>();
        metricEvaluationDTOList.add(metricEvaluationDTO);
        return MetricEvaluationDTOListToDTOMetricList(null, metricEvaluationDTOList, prj, profile);
    }


    static List<DTOMetricEvaluation> MetricEvaluationDTOListToDTOMetricList(String factorExternalID, List<MetricEvaluationDTO> evals, String prj, String profileId) {
        List<DTOMetricEvaluation> m = new ArrayList<>();
        Project project = prjRep.findByExternalId(prj);
        for (Iterator<MetricEvaluationDTO> iterMetrics = evals.iterator(); iterMetrics.hasNext(); ) {
            MetricEvaluationDTO metric = iterMetrics.next();
            if (metric != null) {
                for (Iterator<EvaluationDTO> iterEvals = metric.getEvaluations().iterator(); iterEvals.hasNext(); ) {
                    EvaluationDTO evaluation = iterEvals.next();
                    // check if the metric belongs to the factor
                    boolean addMetric = true;
                    if (factorExternalID != null && !factorRepository.findByExternalIdAndProjectId(factorExternalID,project.getId()).getMetrics().contains(metric.getID())) {
                        addMetric = false;
                    }
                    if (addMetric)
                        m.add(MetricEvaluationDTOToDTOMetric(metric, evaluation));
                }
            }
        }
        // filter by profile
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)) { // if allSI true, return all metrics
                return m;
            } else { // if allSI false, return metrics involved in SIs
                List<ProfileProjectStrategicIndicators> ppsiList =
                        profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project);
                List<String> metricsOfSIs = new ArrayList<>();
                for (ProfileProjectStrategicIndicators ppsi : ppsiList) {
                    for (StrategicIndicatorQualityFactors siqf : ppsi.getStrategicIndicator().getStrategicIndicatorQualityFactorsList()) {
                        for (String metric : siqf.getFactor().getMetrics()){ // metrics externalId
                            metricsOfSIs.add(metric);
                        }
                    }
                }
                List<DTOMetricEvaluation> reviewM = new ArrayList<>();
                for (int i = 0; i < m.size(); i++) {
                    if (metricsOfSIs.contains(m.get(i).getId())) reviewM.add(m.get(i));
                }
                return reviewM;
            }
        } else { // if profile is null, return all metrics
            return m;
        }
    }

    private static DTOMetricEvaluation MetricEvaluationDTOToDTOMetric(MetricEvaluationDTO metric, EvaluationDTO evaluation) {
        return new DTOMetricEvaluation(metric.getID(),
                metric.getName(),
                metric.getDescription(),
                evaluation.getDatasource(),
                evaluation.getRationale(),
                metric.getFactors(),
                evaluation.getEvaluationDate(),
                evaluation.getValue());
    }

    public void setMetricQualityFactorRelation(List<DTOMetricEvaluation> metricList, String projectExternalId) throws IOException {
        qmacon.initConnexion();
        List<MetricEvaluationDTO> qma_metrics = MetricEvaluationDTOtoDTOMetric(metricList, projectExternalId);
        Metric.setQualityFactorsRelation(qma_metrics);
    }

    private static List<MetricEvaluationDTO> MetricEvaluationDTOtoDTOMetric(List<DTOMetricEvaluation> metrics, String prj)
    {
        List<MetricEvaluationDTO> m = new ArrayList<>();


        // - list of metrics (first iterator/for)
        for (Iterator<DTOMetricEvaluation> iterMetrics = metrics.iterator(); iterMetrics.hasNext(); )
        {
            List <EvaluationDTO> eval = new ArrayList<>();
            // For each metric, we have the metric information
            DTOMetricEvaluation metric = iterMetrics.next();

            eval.add(new EvaluationDTO(metric.getId(),
                                        metric.getDatasource(),
                                        metric.getDate(),
                                        metric.getValue(),
                                        metric.getRationale()));

            m.add(new MetricEvaluationDTO(metric.getId(),
                                            metric.getName(),
                                            metric.getDescription(),
                                            prj,
                                            eval,
                                            metric.getQualityFactors())
            );
        }
        return m;

    }

    public List<DTOMetricEvaluation> getAllMetrics(String prj, String profile) throws IOException {
        qmacon.initConnexion();
        return MetricEvaluationDTOListToDTOMetricList(null, Metric.getEvaluations(prj), prj, profile);
    }

}
