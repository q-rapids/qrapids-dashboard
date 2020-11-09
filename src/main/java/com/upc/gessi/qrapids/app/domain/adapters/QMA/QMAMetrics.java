package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.MetricEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
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

    public List<DTOMetricEvaluation> CurrentEvaluation(String id, String prj) throws IOException {
        List<DTOMetricEvaluation> result;

        List<MetricEvaluationDTO> evals;

        qmacon.initConnexion();

        if (id == null)
            evals = Metric.getEvaluations(prj);
        else
            evals = Factor.getMetricsEvaluations(prj, id).getMetrics();
        //Connection.closeConnection();
        result = MetricEvaluationDTOListToDTOMetricList(evals);

        return result;
    }

    public DTOMetricEvaluation SingleCurrentEvaluation(String metricId, String prj) throws IOException {
        qmacon.initConnexion();
        MetricEvaluationDTO metricEvaluationDTO = Metric.getSingleEvaluation(prj, metricId);
        return MetricEvaluationDTOToDTOMetric(metricEvaluationDTO, metricEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTOMetricEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<DTOMetricEvaluation> result;

        List<MetricEvaluationDTO> evals;

        qmacon.initConnexion();
        if (id == null)
            evals = Metric.getEvaluations(prj, from, to);
        else
            evals = Factor.getMetricsEvaluations(prj, id, from, to).getMetrics();
        //Connection.closeConnection();
        result = MetricEvaluationDTOListToDTOMetricList(evals);

        return result;
    }

    public List<DTOMetricEvaluation> SingleHistoricalData (String metricId, LocalDate from, LocalDate to, String prj) throws IOException {
        qmacon.initConnexion();
        MetricEvaluationDTO metricEvaluationDTO = Metric.getSingleEvaluation(prj, metricId, from, to);
        List<MetricEvaluationDTO> metricEvaluationDTOList = new ArrayList<>();
        metricEvaluationDTOList.add(metricEvaluationDTO);
        return MetricEvaluationDTOListToDTOMetricList(metricEvaluationDTOList);
    }


    static List<DTOMetricEvaluation> MetricEvaluationDTOListToDTOMetricList(List<MetricEvaluationDTO> evals) {
        List<DTOMetricEvaluation> m = new ArrayList<>();
        for (Iterator<MetricEvaluationDTO> iterMetrics = evals.iterator(); iterMetrics.hasNext(); ) {
            MetricEvaluationDTO metric = iterMetrics.next();
            if (metric != null) {
                for (Iterator<EvaluationDTO> iterEvals = metric.getEvaluations().iterator(); iterEvals.hasNext(); ) {
                    EvaluationDTO evaluation = iterEvals.next();
                    m.add(MetricEvaluationDTOToDTOMetric(metric, evaluation));
                }
            }
        }
        return m;
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

    public List<DTOMetricEvaluation> getAllMetrics(String prj) throws IOException {
        qmacon.initConnexion();
        return MetricEvaluationDTOListToDTOMetricList(Metric.getEvaluations(prj));
    }

}
