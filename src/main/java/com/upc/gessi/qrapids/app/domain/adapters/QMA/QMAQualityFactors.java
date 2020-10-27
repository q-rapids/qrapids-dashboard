package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.*;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import evaluation.Factor;
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


    public boolean prepareQFIndex(String projectExternalId) throws IOException {
        qmacon.initConnexion();
        // TODO in qma-elastic-0.18
        //  DONE
        return Queries.prepareQFIndex(projectExternalId);
    }

    public boolean setQualityFactorValue(String prj,
                                         String qualityFactorID,
                                         String qualityFactorName,
                                         String qualityFactorDescription,
                                         Float value,
                                         String info,
                                         LocalDate date,
                                         List<DTOQFAssessment> assessment,
                                         List<String> missingMetrics,
                                         long dates_mismatch,
                                         List<String> indicators
    ) throws IOException {

        RestStatus status;
        if (assessment == null) {
            // TODO in qma-elastic-0.18
            //  DONE
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
            // TODO in qma-elastic-0.18
            //  DONE
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

    private EstimationEvaluationDTO listDTOQFAssessmentToEstimationEvaluationDTO(List<DTOQFAssessment> assessment) {
        List<QuadrupletDTO<Integer, String, Float, Float>> estimation = new ArrayList<>();
        for (DTOQFAssessment dsa : assessment) {
            estimation.add(new QuadrupletDTO<Integer, String, Float, Float>(dsa.getId() != null ? dsa.getId().intValue() : null, dsa.getLabel(), dsa.getValue(), dsa.getUpperThreshold()));
        }
        return new EstimationEvaluationDTO(estimation);
    }

    public List<DTODetailedFactorEvaluation> CurrentEvaluation(String id, String prj, boolean filterDB) throws IOException {
        qmacon.initConnexion();
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id);
        }
//            Connection.closeConnection();
        return FactorMetricEvaluationDTOListToDTOQualityFactorList(prjRep.findByExternalId(prj).getId(), evals, filterDB);
    }

    public DTOFactorEvaluation SingleCurrentEvaluation(String factorId, String prj) throws IOException {
        qmacon.initConnexion();
        FactorEvaluationDTO factorEvaluationDTO = Factor.getSingleEvaluation(prj, factorId);
        return FactorEvaluationDTOToDTOFactor(factorEvaluationDTO, factorEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTODetailedFactorEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        List<DTODetailedFactorEvaluation> qf;

        qmacon.initConnexion();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj, from, to);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id, from, to);
        }
//        Connection.closeConnection();
        qf = FactorMetricEvaluationDTOListToDTOQualityFactorList(prjRep.findByExternalId(prj).getId(), evals, true);

        return qf;
    }

    public boolean isCategoriesEmpty() {
        if (QFCatRep.count() == 0)
            return true;
        else
            return false;
    }

    public List<DTOFactorEvaluation> getAllFactors(String prj, boolean filterDB) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(Factor.getEvaluations(prj), prjRep.findByExternalId(prj).getId(), filterDB);
    }

    public List<DTOFactorEvaluation> getAllFactorsHistoricalData(String prj, LocalDate from, LocalDate to) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(Factor.getEvaluations(prj, from, to), prjRep.findByExternalId(prj).getId(), true);
    }

    public void setFactorStrategicIndicatorRelation(List<DTOFactorEvaluation> factors, String prj) throws IOException {
        qmacon.initConnexion();
        List<FactorEvaluationDTO> qma_factors = FactorEvaluationDTOtoDTOFactor(factors, prj);
        Factor.setStrategicIndicatorRelation(qma_factors);
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
                    factor.getValue(),
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
    private List<DTODetailedFactorEvaluation> FactorMetricEvaluationDTOListToDTOQualityFactorList(Long prjID, List<FactorMetricEvaluationDTO> evals, boolean filterDB) {
        List<DTODetailedFactorEvaluation> qf = new ArrayList<>();
        boolean found; // to check if the SI is in the database
        // The evaluations (eval param) has the following structure:
        // - list of factors (first iterator/for)
        for (Iterator<FactorMetricEvaluationDTO> iterFactors = evals.iterator(); iterFactors.hasNext(); ) {
            // For each factor, we have the factor inforamtion + the list of metrics evaluations
            FactorMetricEvaluationDTO qualityFactor = iterFactors.next();
            if (filterDB) found = qfRep.existsByExternalIdAndProject_Id(qualityFactor.getID(), prjID);
            else found = true; // because we want make fetch
            // only return Detailed Quality Factor if it is in local database
            if (found) {
                qf.add(new DTODetailedFactorEvaluation(qualityFactor.getID(), qualityFactor.getName(), QMAMetrics.MetricEvaluationDTOListToDTOMetricList(qualityFactor.getMetrics())));
            }
        }
        return qf;
    }
}
