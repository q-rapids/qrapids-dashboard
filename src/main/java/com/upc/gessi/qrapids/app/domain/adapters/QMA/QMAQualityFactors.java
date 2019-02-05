package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.FactorMetricEvaluationDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
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
    private QMAFakedata qmafake;

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private QFCategoryRepository QFCatRep;

    public List<DTOQualityFactor> CurrentEvaluation(String id, String prj) throws IOException {
        List<DTOQualityFactor> qf;

        if (qmafake.usingFakeData()) {
            qf=qmafake.getFactors(id);
        } else {
            qmacon.initConnexion();
            List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
            if (id == null) {
                evals = Factor.getMetricsEvaluations(prj);
            } else {
                evals = StrategicIndicator.getMetricsEvaluations(prj, id);
            }
//            Connection.closeConnection();
            return FactorMetricEvaluationDTOtoDTOFactor(evals);
        }
        return qf;
    }

    public List<DTOQualityFactor> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<FactorMetricEvaluationDTO> evals = new ArrayList<>();
        List<DTOQualityFactor> qf;

        if (qmafake.usingFakeData()) {
            qf = qmafake.getHistoricalFactors(id);
        } else {
            qmacon.initConnexion();
            if (id == null) {
                evals = Factor.getMetricsEvaluations(prj, from, to);
            } else {
                evals = StrategicIndicator.getMetricsEvaluations(prj, id, from, to);
            }
//        Connection.closeConnection();
            qf = FactorMetricEvaluationDTOtoDTOFactor(evals);
        }
        return qf;
    }

    public void newCategories(JsonArray categories) {
        if (QFCatRep.count() == 0) {
            for (JsonElement c : categories) {
                QFCategory sic = new QFCategory();
                sic.setName(c.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
                sic.setColor(c.getAsJsonObject().getAsJsonPrimitive("color").getAsString());
                sic.setUpperThreshold((float)c.getAsJsonObject().getAsJsonPrimitive("upperThreshold").getAsInt()/100f);
                QFCatRep.save(sic);
            }
        }
    }

    public void deleteAllCategories(){
        QFCatRep.deleteAll();
    }

    public boolean isCategoriesEmpty() {
        if (QFCatRep.count() == 0)
            return true;
        else
            return false;
    }

    public List<DTOFactor> getAllFactors(String prj) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOtoDTOFactor(Factor.getEvaluations(prj));
    }

    public List<DTOFactor> getAllFactorsHistoricalData(String prj, LocalDate from, LocalDate to) throws IOException {
        qmacon.initConnexion();
        return QMADetailedStrategicIndicators.FactorEvaluationDTOtoDTOFactor(Factor.getEvaluations(prj, from, to));
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
    private static List<DTOQualityFactor> FactorMetricEvaluationDTOtoDTOFactor(List<FactorMetricEvaluationDTO> evals) {
        List<DTOQualityFactor> qf = new ArrayList<>();

        // The evaluations (eval param) has the following structure:
        // - list of factors (first iterator/for)
        for (Iterator<FactorMetricEvaluationDTO> iterFactors = evals.iterator(); iterFactors.hasNext(); )
        {
            // For each factor, we have the factor inforamtion + the list of metrics evaluations
            FactorMetricEvaluationDTO qualityFactor = iterFactors.next();
            qf.add(new DTOQualityFactor(qualityFactor.getID(), qualityFactor.getName(), QMAMetrics.MetricEvaluationDTOtoDTOMetric(qualityFactor.getMetrics())));
        }
        return qf;
    }
}
