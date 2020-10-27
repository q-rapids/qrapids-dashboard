package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EstimationEvaluationDTO;
import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.StrategicIndicatorFactorEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOSIAssessment;
import evaluation.StrategicIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QMADetailedStrategicIndicators {

    // it was made to use qfRep in FactorEvaluationDTOListToDTOFactorList method
    private static QualityFactorRepository qfRep;

    @Autowired
    public QMADetailedStrategicIndicators(QualityFactorRepository qfRep) {
        QMADetailedStrategicIndicators.qfRep = qfRep;
    }

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private ProjectRepository prjRep;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    public List<DTODetailedStrategicIndicatorEvaluation> CurrentEvaluation(String id, String prj, boolean filterDB) throws IOException {
        List<DTODetailedStrategicIndicatorEvaluation> dsi;

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

        dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(prjRep.findByExternalId(prj).getId(), evals, filterDB);
        //Connection.closeConnection();
        return dsi;
    }

    public List<DTODetailedStrategicIndicatorEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj) throws IOException {
        List<DTODetailedStrategicIndicatorEvaluation> dsi;

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
        dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(prjRep.findByExternalId(prj).getId(), evals, true);
        //Connection.closeConnection();
        return dsi;
    }

    private List<DTODetailedStrategicIndicatorEvaluation> StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(Long prjID, List<StrategicIndicatorFactorEvaluationDTO> evals, boolean filterDB) {
        List<DTODetailedStrategicIndicatorEvaluation> dsi = new ArrayList<>();
        boolean found; // to check if the SI is in the database
        //for each Detailed Strategic Indicador
        for (Iterator<StrategicIndicatorFactorEvaluationDTO> iterDSI = evals.iterator(); iterDSI.hasNext(); ) {
            StrategicIndicatorFactorEvaluationDTO element = iterDSI.next();
            if (filterDB) found = siRep.existsByExternalIdAndProject_Id(element.getID(), prjID);
            else found = true; // because we want make fetch
            // only return Detailed Strategic Indicator if it is in local database
            if (found) {
                EvaluationDTO evaluation = element.getEvaluations().get(0);
                //Create Detailed Strategic Indicator with name, id and null factors
                DTODetailedStrategicIndicatorEvaluation d = new DTODetailedStrategicIndicatorEvaluation(element.getID(), element.getName(), null);
                d.setDate(evaluation.getEvaluationDate());
                d.setMismatchDays(evaluation.getMismatchDays());
                d.setMissingFactors(evaluation.getMissingElements());
                //set Factors to Detailed Strategic Indicator
                d.setFactors(FactorEvaluationDTOListToDTOFactorList(element.getFactors(),prjID, false));

                // Get value
                List<DTOSIAssessment> categories = strategicIndicatorsController.getCategories();
                EstimationEvaluationDTO estimation = element.getEstimation().get(0);

                boolean hasEstimation = true;
                if (estimation == null || estimation.getEstimation() == null || estimation.getEstimation().size() == 0)
                    hasEstimation = false;

                if (hasEstimation && estimation.getEstimation() != null && estimation.getEstimation().size() == categories.size()) {
                    setValueAndThresholdToCategories(categories, estimation);
                }

                if (hasEstimation) {
                    Float value = strategicIndicatorsController.getValueAndLabelFromCategories(categories).getFirst();
                    d.setValue(Pair.of(value, strategicIndicatorsController.getLabel(value)));
                } else {
                    d.setValue(Pair.of(evaluation.getValue(), strategicIndicatorsController.getLabel(evaluation.getValue())));
                }

                dsi.add(d);
            }
        }
        return dsi;
    }

    private void setValueAndThresholdToCategories(List<DTOSIAssessment> categories, EstimationEvaluationDTO estimation) {
        int i = 0;
        for (DTOSIAssessment c : categories) {
            if (c.getLabel().equals(estimation.getEstimation().get(i).getSecond())) {
                c.setValue(estimation.getEstimation().get(i).getThird());
                c.setUpperThreshold(estimation.getEstimation().get(i).getFourth());
            }
            ++i;
        }
    }

    public static List<DTOFactorEvaluation> FactorEvaluationDTOListToDTOFactorList(List<FactorEvaluationDTO> factors, Long prjID, boolean filterDB) {
        List<DTOFactorEvaluation> listFact = new ArrayList<>();
        //for each factor in the Detailed Strategic Indicator
        for (Iterator<FactorEvaluationDTO> iterFactor = factors.iterator(); iterFactor.hasNext(); ) {
            FactorEvaluationDTO factor = iterFactor.next();
            boolean found = true;
            if(filterDB)
                found = qfRep.existsByExternalIdAndProject_Id(factor.getID(), prjID);
            if (found) {
                //for each evaluation create new factor with factor name and id, and evaluation date and value
                for (Iterator<EvaluationDTO> iterFactEval = factor.getEvaluations().iterator(); iterFactEval.hasNext(); ) {
                    EvaluationDTO evaluation = iterFactEval.next();
                    listFact.add(FactorEvaluationDTOToDTOFactor(factor, evaluation));
                }
            }
        }
        return listFact;
    }

    static DTOFactorEvaluation FactorEvaluationDTOToDTOFactor(FactorEvaluationDTO factor, EvaluationDTO evaluation) {
        DTOFactorEvaluation factorEval = new DTOFactorEvaluation(
                factor.getID(),
                factor.getName(),
                factor.getDescription(),
                evaluation.getValue(), evaluation.getEvaluationDate(),
                evaluation.getDatasource(),evaluation.getRationale(),
                factor.getStrategicIndicators());
        factorEval.setMismatchDays(evaluation.getMismatchDays());
        factorEval.setMissingMetrics(evaluation.getMissingElements());
        return factorEval;
    }


}
