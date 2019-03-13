package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.MetricEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.dto.DTOMetric;
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
    private QMAFakedata qmafake;

    @Autowired
    private QMAConnection qmacon;

    public List<DTOMetric> CurrentEvaluation(String id, String prj) throws IOException {
        List<DTOMetric> result;

        if (qmafake.usingFakeData()){
            result = qmafake.getMetrics(id);
        }
        else {
            List<MetricEvaluationDTO> evals;

            qmacon.initConnexion();


            if (id == null)
                evals = Metric.getEvaluations(prj);
            else
                evals = Factor.getMetricsEvaluations(prj, id).getMetrics();
            //Connection.closeConnection();
            result = MetricEvaluationDTOListToDTOMetricList(evals);
        }
        return result;
    }

    public DTOMetric SingleCurrentEvaluation(String metricId, String prj) throws IOException {
        qmacon.initConnexion();
        MetricEvaluationDTO metricEvaluationDTO = Metric.getSingleEvaluation(prj, metricId);
        return MetricEvaluationDTOToDTOMetric(metricEvaluationDTO, metricEvaluationDTO.getEvaluations().get(0));
    }

    public List<DTOMetric> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<DTOMetric> result;

        if (qmafake.usingFakeData()){
            result = qmafake.getHistoricalMetrics(id);
        }
        else {
            List<MetricEvaluationDTO> evals;

            qmacon.initConnexion();
            if (id == null)
                evals = Metric.getEvaluations(prj, from, to);
            else
                evals = Factor.getMetricsEvaluations(prj, id, from, to).getMetrics();
            //Connection.closeConnection();
            result = MetricEvaluationDTOListToDTOMetricList(evals);

        }
        return result;
    }

    public static List<DTOMetric> MetricEvaluationDTOListToDTOMetricList(List<MetricEvaluationDTO> evals) {
        List<DTOMetric> m = new ArrayList<>();
        for (Iterator<MetricEvaluationDTO> iterMetrics = evals.iterator(); iterMetrics.hasNext(); ) {
            MetricEvaluationDTO metric = iterMetrics.next();
            for (Iterator<EvaluationDTO> iterEvals = metric.getEvaluations().iterator(); iterEvals.hasNext(); ) {
                EvaluationDTO evaluation = iterEvals.next();
                m.add(MetricEvaluationDTOToDTOMetric(metric, evaluation));
            }
        }
        return m;
    }

    public static DTOMetric MetricEvaluationDTOToDTOMetric (MetricEvaluationDTO metric, EvaluationDTO evaluation) {
        return new DTOMetric(metric.getID(),
                metric.getName(),
                metric.getDescription(),
                evaluation.getDatasource(),
                evaluation.getRationale(),
                evaluation.getEvaluationDate(),
                evaluation.getValue());
    }

}
