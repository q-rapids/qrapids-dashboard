package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.*;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.FactorsController;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import evaluation.Factor;
import org.springframework.data.util.Pair;
import evaluation.StrategicIndicator;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.Queries;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators.*;

@Component
public class QMAQualityFactors {

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private QFCategoryRepository QFCatRep;

    @Autowired
    private ProjectRepository prjRep;

    @Autowired
    private QualityFactorRepository qfRep;

    @Autowired
    private ProfilesController profilesController;

    @Autowired
    private ProjectsController projectsController;
    
    @Autowired
    private FactorsController factorsController;

    @Autowired
    private ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Autowired
    QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    public boolean prepareQFIndex(String projectExternalId) throws IOException {
        qmacon.initConnexion();
        return Queries.prepareQFIndex(projectExternalId);
    }

    public boolean setQualityFactorValue(String prj,
                                         String qualityFactorID,
                                         String qualityFactorName,
                                         String qualityFactorDescription,
                                         Float value,
                                         String info,
                                         LocalDate date,
                                         List<DTOAssessment> assessment,
                                         List<String> missingMetrics,
                                         long dates_mismatch,
                                         List<String> indicators
    ) throws IOException {

        RestStatus status;
        if (assessment == null) {

            status = Factor.setFactorEvaluation(prj,
                    qualityFactorID,
                    qualityFactorName,
                    qualityFactorDescription,
                    value,
                    info,
                    date,
                    null,
                    missingMetrics,
                    dates_mismatch,
                    indicators)
                    .status();
        } else {
            status = Factor.setFactorEvaluation(prj,
                    qualityFactorID,
                    qualityFactorName,
                    qualityFactorDescription,
                    value,
                    info,
                    date,
                    listDTOQFAssessmentToEstimationEvaluationDTO(assessment),
                    missingMetrics,
                    dates_mismatch,
                    indicators)
                    .status();
        }
        return status.equals(RestStatus.OK) || status.equals(RestStatus.CREATED);
    }

    private EstimationEvaluationDTO listDTOQFAssessmentToEstimationEvaluationDTO(List<DTOAssessment> assessment) {
        List<QuadrupletDTO<Integer, String, Float, Float>> estimation = new ArrayList<>();
        for (DTOAssessment dsa : assessment) {
            estimation.add(new QuadrupletDTO<Integer, String, Float, Float>(dsa.getId() != null ? dsa.getId().intValue() : null, dsa.getLabel(), dsa.getValue(), dsa.getUpperThreshold()));
        }
        return new EstimationEvaluationDTO(estimation);
    }

    public List<DTODetailedFactorEvaluation> CurrentEvaluation(String id, String prj, String profile, boolean filterDB) throws IOException, ProjectNotFoundException {
        qmacon.initConnexion();
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id);
            profile = null; // if we are asking for concrete indicator, we don't need to filter by profile
        }
//            Connection.closeConnection();
        return FactorMetricEvaluationDTOListToDTOQualityFactorList(prjRep.findByExternalId(prj).getId(),evals, profile, filterDB, true);
    }

    private static List<FactorEvaluationDTO> FactorEvaluationDTOtoDTOFactor(List<DTOFactorEvaluation> factors, String prj)
    {
        List<FactorEvaluationDTO> f = new ArrayList<>();

        // - list of factors (first iterator/for)
        for (Iterator<DTOFactorEvaluation> iterFactors = factors.iterator(); iterFactors.hasNext(); )
        {
            List <EvaluationDTO> eval = new ArrayList<>();
            // For each factor, we have the factor information
            DTOFactorEvaluation factor = iterFactors.next();

            eval.add(new EvaluationDTO(factor.getId(),
                    factor.getDatasource(),
                    factor.getDate(),
                    factor.getValue().getFirst(),
                    factor.getRationale()));

            f.add(new FactorEvaluationDTO(factor.getId(),
                    factor.getName(),
                    factor.getDescription(),
                    prj,
                    eval,
                    factor.getStrategicIndicators())
            );
        }
        return f;

    }

    public DTOFactorEvaluation SingleCurrentEvaluation(String factorId, String prj) throws IOException {
        qmacon.initConnexion();
        FactorEvaluationDTO factorEvaluationDTO = Factor.getSingleEvaluation(prj, factorId);
        return qmaDetailedStrategicIndicators.FactorEvaluationDTOToDTOFactor(factorEvaluationDTO, factorEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTODetailedFactorEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj, String profile) throws IOException, ProjectNotFoundException {
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        List<DTODetailedFactorEvaluation> qf;

        qmacon.initConnexion();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj, from, to);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id, from, to);
            profile = null; // if we are asking for concrete indicator, we don't need to filter by profile
        }
//        Connection.closeConnection();
        qf = FactorMetricEvaluationDTOListToDTOQualityFactorList(prjRep.findByExternalId(prj).getId(),evals, profile, true, false);

        return qf;
    }

    public boolean isCategoriesEmpty() {
        if (QFCatRep.count() == 0)
            return true;
        else
            return false;
    }

    public List<DTOFactorEvaluation> getAllFactors(String prj, String profile, boolean filterDB) throws IOException {
        qmacon.initConnexion();
        return qmaDetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(null, Factor.getEvaluations(prj), prjRep.findByExternalId(prj).getId(), profile, filterDB);
    }

    public List<DTOFactorEvaluation> getAllFactorsHistoricalData(String prj, String profile, LocalDate from, LocalDate to) throws IOException {
        qmacon.initConnexion();
        return qmaDetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(null, Factor.getEvaluations(prj, from, to), prjRep.findByExternalId(prj).getId(), profile,true);
    }

    public void setFactorStrategicIndicatorRelation(List<DTOFactorEvaluation> factors, String prj) throws IOException {
        qmacon.initConnexion();
        List<FactorEvaluationDTO> qma_factors = FactorEvaluationDTOtoDTOFactor(factors, prj);
        Factor.setStrategicIndicatorRelation(qma_factors);
    }

    private List<DTODetailedFactorEvaluation> FactorMetricEvaluationDTOListToDTOQualityFactorList(Long prjID,List<FactorMetricEvaluationDTO> evals, String profileId,  boolean filterDB, boolean currentData) throws ProjectNotFoundException {
        List<DTODetailedFactorEvaluation> qf = new ArrayList<>();
        boolean found; // to check if the factor is in the database

        // get project info
        Iterator<FactorMetricEvaluationDTO> iter = evals.iterator();
        FactorMetricEvaluationDTO firstQualityFactor = iter.next();
        Project project = projectsController.findProjectByExternalId(firstQualityFactor.getProject());

        // The evaluations (eval param) has the following structure:
        // - list of factors (first iterator/for)
        for (Iterator<FactorMetricEvaluationDTO> iterFactors = evals.iterator(); iterFactors.hasNext(); ) {
            // For each factor, we have the factor inforamtion + the list of metrics evaluations
            FactorMetricEvaluationDTO qualityFactor = iterFactors.next();
            if (filterDB) found = qfRep.existsByExternalIdAndProject_Id(qualityFactor.getID(), prjID);
            else found = true; // because we want make fetch
            // only return Detailed Quality Factor if it is in local database
            if (found) {
                // check metric composition this factor, if we don't fetch it
                String factorExternalID = null;
                if ((filterDB != false) && currentData) factorExternalID = qualityFactor.getID();
                DTODetailedFactorEvaluation df = new DTODetailedFactorEvaluation(qualityFactor.getID(), qualityFactor.getName(), QMAMetrics.MetricEvaluationDTOListToDTOMetricList(factorExternalID, qualityFactor.getMetrics(), project.getExternalId() ,profileId));
                EvaluationDTO evaluation = qualityFactor.getEvaluations().get(0);
                df.setDate(evaluation.getEvaluationDate());
                df.setValue(Pair.of(evaluation.getValue(), factorsController.getFactorLabelFromValue(evaluation.getValue())));
                df.setMismatchDays(evaluation.getMismatchDays());
                if (evaluation.getMissingElements() == null)
                    df.setMissingMetrics(new ArrayList<>());
                else
                    df.setMissingMetrics(evaluation.getMissingElements());
                qf.add(df);
            }
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
                List<DTODetailedFactorEvaluation> reviewQF = new ArrayList<>();
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
