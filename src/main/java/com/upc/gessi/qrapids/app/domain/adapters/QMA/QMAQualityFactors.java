package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.FactorMetricEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
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

    public List<DTOQualityFactor> CurrentEvaluation(String id, String prj) throws IOException {
        qmacon.initConnexion();
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id);
        }
//            Connection.closeConnection();
        return FactorMetricEvaluationDTOListToDTOQualityFactorList(evals);
    }

    public DTOFactor SingleCurrentEvaluation(String factorId, String prj) throws IOException {
        qmacon.initConnexion();
        FactorEvaluationDTO factorEvaluationDTO = Factor.getSingleEvaluation(prj, factorId);
        return QMADetailedStrategicIndicators.FactorEvaluationDTOToDTOFactor(factorEvaluationDTO, factorEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTOQualityFactor> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        List<DTOQualityFactor> qf;

        qmacon.initConnexion();
        if (id == null) {
            evals = Factor.getMetricsEvaluations(prj, from, to);
        } else {
            evals = StrategicIndicator.getMetricsEvaluations(prj, id, from, to);
        }
//        Connection.closeConnection();
        qf = FactorMetricEvaluationDTOListToDTOQualityFactorList(evals);

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
    private static List<DTOQualityFactor> FactorMetricEvaluationDTOListToDTOQualityFactorList(List<FactorMetricEvaluationDTO> evals) {
        List<DTOQualityFactor> qf = new ArrayList<>();

        // The evaluations (eval param) has the following structure:
        // - list of factors (first iterator/for)
        for (Iterator<FactorMetricEvaluationDTO> iterFactors = evals.iterator(); iterFactors.hasNext(); )
        {
            // For each factor, we have the factor inforamtion + the list of metrics evaluations
            FactorMetricEvaluationDTO qualityFactor = iterFactors.next();
            qf.add(new DTOQualityFactor(qualityFactor.getID(), qualityFactor.getName(), QMAMetrics.MetricEvaluationDTOListToDTOMetricList(qualityFactor.getMetrics())));
        }
        return qf;
    }
}
