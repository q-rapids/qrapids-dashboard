package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.FactorMetricEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactor;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOQualityFactor;
import evaluation.Factor;
import evaluation.StrategicIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QMAQualityFactors {

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private QFCategoryRepository QFCatRep;

    @Autowired
    private ProfilesController profilesController;

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    public List<DTOQualityFactor> CurrentEvaluation(String id, String prj, String profile) throws IOException, ProjectNotFoundException {
        qmacon.initConnexion();
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id);
            profile = null; // if we are asking for concrete indicator, we don't need to filter by profile
        }
//            Connection.closeConnection();
        return FactorMetricEvaluationDTOListToDTOQualityFactorList(evals, profile);
    }

    public DTOFactor SingleCurrentEvaluation(String factorId, String prj) throws IOException {
        qmacon.initConnexion();
        FactorEvaluationDTO factorEvaluationDTO = Factor.getSingleEvaluation(prj, factorId);
        return QMADetailedStrategicIndicators.FactorEvaluationDTOToDTOFactor(factorEvaluationDTO, factorEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTOQualityFactor> HistoricalData(String id, LocalDate from, LocalDate to, String prj, String profile) throws IOException, ProjectNotFoundException {
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        List<DTOQualityFactor> qf;

        qmacon.initConnexion();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj, from, to);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id, from, to);
            profile = null; // if we are asking for concrete indicator, we don't need to filter by profile
        }
//        Connection.closeConnection();
        qf = FactorMetricEvaluationDTOListToDTOQualityFactorList(evals, profile);

        return qf;
    }

    public boolean isCategoriesEmpty() {
        if (QFCatRep.count() == 0)
            return true;
        else
            return false;
    }

    public List<DTOFactor> getAllFactors(String prj) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(Factor.getEvaluations(prj));
    }

    public List<DTOFactor> getAllFactorsHistoricalData(String prj, LocalDate from, LocalDate to) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(Factor.getEvaluations(prj, from, to));
    }

    public void setFactorStrategicIndicatorRelation(List<DTOFactor> factors, String prj) throws IOException {

        qmacon.initConnexion();
        List<FactorEvaluationDTO> qma_factors = FactorEvaluationDTOFactortoDTO(factors, prj);
        Factor.setStrategicIndicatorRelation(qma_factors);
    }

    private static List<FactorEvaluationDTO> FactorEvaluationDTOFactortoDTO(List<DTOFactor> factors, String prj)
    {
        List<FactorEvaluationDTO> qf = new ArrayList<>();
        List <EvaluationDTO> eval = new ArrayList<>();

        // - list of factors (first iterator/for)
        for (Iterator<DTOFactor> iterFactors = factors.iterator(); iterFactors.hasNext(); )
        {
            // For each factor, we have the factor information
            DTOFactor factor = iterFactors.next();

            eval.clear();
            eval.add(new EvaluationDTO(factor.getId(),
                                        factor.getDatasource(),
                                        factor.getDate(),
                                        factor.getValue(),
                                        factor.getRationale()));

            qf.add(new FactorEvaluationDTO(factor.getId(),
                                            factor.getName(),
                                            factor.getDescription(),
                                            prj,
                                            eval,
                                            factor.getStrategicIndicators())
            );
        }
        return qf;

    }
    private List<DTOQualityFactor> FactorMetricEvaluationDTOListToDTOQualityFactorList(List<FactorMetricEvaluationDTO> evals, String profileId) throws ProjectNotFoundException {
        List<DTOQualityFactor> qf = new ArrayList<>();

        // get project info
        Iterator<FactorMetricEvaluationDTO> iter = evals.iterator();
        FactorMetricEvaluationDTO firstQualityFactor = iter.next();
        Project project = projectsController.findProjectByExternalId(firstQualityFactor.getProject());;

        // The evaluations (eval param) has the following structure:
        // - list of factors (first iterator/for)
        for (Iterator<FactorMetricEvaluationDTO> iterFactors = evals.iterator(); iterFactors.hasNext(); )
        {
            // For each factor, we have the factor inforamtion + the list of metrics evaluations
            FactorMetricEvaluationDTO qualityFactor = iterFactors.next();
            qf.add(new DTOQualityFactor(qualityFactor.getID(), qualityFactor.getName(), QMAMetrics.MetricEvaluationDTOListToDTOMetricList(qualityFactor.getMetrics())));
        }

        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)) { // if allSI true, return all quality factors
                return qf;
            } else { // if allSI false, return quality factors involved in SIs
                List<ProfileProjectStrategicIndicators> ppsiList =
                        profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project);
                List<String> qfOfSIs = new ArrayList<>();
                for (ProfileProjectStrategicIndicators ppsi : ppsiList) {
                    for (String factor : ppsi.getStrategicIndicator().getQuality_factors())
                        qfOfSIs.add(factor);
                }
                List<DTOQualityFactor> reviewQF = new ArrayList<>();
                for (int i = 0; i < qf.size(); i++) {
                    if (qfOfSIs.contains(qf.get(i).getId())) reviewQF.add(qf.get(i));
                }
                return reviewQF;
            }
        } else { // if profile is null, return all quality factors
            return qf;
        }
    }
}
