package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.FactorEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import simulation.Model;
import simulation.Simulator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class QMASimulation {

    @Autowired
    private QMAConnection qmacon;

    public List<DTOFactor> simulateQualityFactors (Map<String, Float> metrics, String prj, LocalDate date) throws IOException {
        qmacon.initConnexion();
        Model model = Simulator.createModel(prj, date.toString());
        for(Map.Entry<String, Float> metric : metrics.entrySet()) {
            String metricId = prj + "-" + metric.getKey() + "-" + date.toString();
            model.setMetric(metricId, metric.getValue().doubleValue());
        }
        Collection<FactorEvaluationDTO> factors = model.simulate();
        List<FactorEvaluationDTO> factorsList = new ArrayList<>(factors);
        return QMADetailedStrategicIndicators.FactorEvaluationDTOListToDTOFactorList(factorsList);
    }

}
