package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.StrategicIndicatorFactorEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import evaluation.StrategicIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QMADetailedStrategicIndicators {

    @Autowired
    private QMAFakedata qmafake;

    @Autowired
    private QMAConnection qmacon;

    public List<DTODetailedStrategicIndicator> CurrentEvaluation(String id, String prj) throws IOException {
        List<DTODetailedStrategicIndicator> dsi;

        if (qmafake.usingFakeData())
            dsi = qmafake.getDetailedSIs(id);
        else {
            // Data coming from QMA API
            qmacon.initConnexion();

            List<StrategicIndicatorFactorEvaluationDTO> evals;
            // All the strategic indicators
            if (id == null) {
                evals = StrategicIndicator.getFactorsEvaluations(prj);
            } else {
                evals = new ArrayList<>();
                evals.add(StrategicIndicator.getFactorsEvaluations(prj, id));
            }

            dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(evals);
            //Connection.closeConnection();
        }
        return dsi;
    }

    public List<DTODetailedStrategicIndicator> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<DTODetailedStrategicIndicator> dsi;

        if (qmafake.usingFakeData())
            dsi=qmafake.getHistoricalDetailedSIs(id);
        else {
            // Data coming from QMA API
            qmacon.initConnexion();

            List<StrategicIndicatorFactorEvaluationDTO> evals;
            if (id == null) {
                //using dates from 1/1/2015 to now at the moment
                evals = StrategicIndicator.getFactorsEvaluations(prj, from, to);
            } else {
                //using dates from 1/1/2015 to now at the moment
                evals = new ArrayList<>();
                evals.add(StrategicIndicator.getFactorsEvaluations(prj, id, from, to));
            }
            dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(evals);
            //Connection.closeConnection();
        }
        return dsi;
    }

    private List<DTODetailedStrategicIndicator> StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(List<StrategicIndicatorFactorEvaluationDTO> evals) {
        List<DTODetailedStrategicIndicator> dsi = new ArrayList<>();
        //for each Detailed Strategic Indicador
        for (Iterator<StrategicIndicatorFactorEvaluationDTO> iterDSI = evals.iterator(); iterDSI.hasNext(); ) {
            StrategicIndicatorFactorEvaluationDTO element = iterDSI.next();
            //Create Detailed Strategic Indicator with name, id and null factors
            DTODetailedStrategicIndicator d = new DTODetailedStrategicIndicator(element.getID(), element.getName(), null);
            //set Factors to Detailed Strategic Indicator
            d.setFactors(FactorEvaluationDTOListToDTOFactorList(element.getFactors()));
            dsi.add(d);
        }
        return dsi;
    }

    public static List<DTOFactor> FactorEvaluationDTOListToDTOFactorList(List<FactorEvaluationDTO> factors) {
        List<DTOFactor> listFact = new ArrayList<>();
        //for each factor in the Detailed Strategic Indicator
        for (Iterator<FactorEvaluationDTO> iterFactor = factors.iterator(); iterFactor.hasNext(); ) {
            FactorEvaluationDTO factor = iterFactor.next();
            //for each evaluation create new factor with factor name and id, and evaluation date and value
            for (Iterator<EvaluationDTO> iterFactEval = factor.getEvaluations().iterator(); iterFactEval.hasNext(); ) {
                EvaluationDTO evaluation = iterFactEval.next();
                listFact.add(FactorEvaluationDTOToDTOFactor(factor, evaluation));
            }
        }
        return listFact;
    }

    public static DTOFactor FactorEvaluationDTOToDTOFactor(FactorEvaluationDTO factor, EvaluationDTO evaluation) {
        return new DTOFactor(
                factor.getID(),
                factor.getName(),
                factor.getDescription(),
                evaluation.getValue(), evaluation.getEvaluationDate(),
                evaluation.getDatasource(),evaluation.getRationale(),
                factor.getStrategicIndicators());
    }


}
