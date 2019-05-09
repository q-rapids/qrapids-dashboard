package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.StrategicIndicatorEvaluationDTO;
import DTOs.EstimationEvaluationDTO;
import DTOs.QuadrupletDTO;
import DTOs.EvaluationDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.upc.gessi.qrapids.app.domain.models.Feedback;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.services.Util;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.models.SICategory;

import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import evaluation.StrategicIndicator;
import org.springframework.data.util.Pair;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QMAStrategicIndicators {

    @Autowired
    private QMAFakedata qmafake;

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private SICategoryRepository SICatRep;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private Util util;

    public List<DTOStrategicIndicatorEvaluation> CurrentEvaluation(String prj) throws IOException, CategoriesException {
        List<DTOStrategicIndicatorEvaluation> result;

        if (qmafake.usingFakeData())
            result = qmafake.getSIs();
        else {
            // Data coming from QMA API
            qmacon.initConnexion();
            List<StrategicIndicatorEvaluationDTO> evals = StrategicIndicator.getEvaluations(prj);
            //Connection.closeConnection();
            result = StrategicIndicatorEvaluationDTOListToDTOStrategicIndicatorEvaluationList(evals);
        }
        return result;
    }

    public DTOStrategicIndicatorEvaluation SingleCurrentEvaluation(String prj, String strategicIndicatorId) throws IOException, CategoriesException {
        qmacon.initConnexion();
        StrategicIndicatorEvaluationDTO strategicIndicatorEvaluationDTO = StrategicIndicator.getSingleEvaluation(prj, strategicIndicatorId);
        List<StrategicIndicatorEvaluationDTO> strategicIndicatorEvaluationDTOList = new ArrayList<>();
        strategicIndicatorEvaluationDTOList.add(strategicIndicatorEvaluationDTO);
        return StrategicIndicatorEvaluationDTOListToDTOStrategicIndicatorEvaluationList(strategicIndicatorEvaluationDTOList).get(0);
    }

    public List<DTOStrategicIndicatorEvaluation> HistoricalData(LocalDate from, LocalDate to, String prj) throws IOException, CategoriesException  {
        List<DTOStrategicIndicatorEvaluation> result;

        if (qmafake.usingFakeData())
            result = qmafake.getHistoricalSIs();
        else {
            // Data coming from QMA API
            qmacon.initConnexion();
            //using dates from 1/1/2015 to now at the moment
            List<StrategicIndicatorEvaluationDTO> evals = StrategicIndicator.getEvaluations(prj, from, to);
            //Connection.closeConnection();
            result = StrategicIndicatorEvaluationDTOListToDTOStrategicIndicatorEvaluationList(evals);
        }
        return result;
    }

    public void newCategories(JsonArray categories) {
        if (SICatRep.count() == 0) {
            for (JsonElement c : categories) {
                SICategory sic = new SICategory();
                sic.setName(c.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
                sic.setColor(c.getAsJsonObject().getAsJsonPrimitive("color").getAsString());
                SICatRep.save(sic);
            }
        }
    }

    public void deleteAllCategories(){
        SICatRep.deleteAll();
    }

    public boolean isCategoriesEmpty() {
     if (SICatRep.count() == 0)
         return true;
     else
         return false;
    }

    public boolean setStrategicIndicatorValue(String prj,
                                              String strategicIndicatorID,
                                              String strategicIndicatorName,
                                              String strategicIndicatorDescription,
                                              Float value,
                                              LocalDate date,
                                              List<DTOSIAssesment> assessment,
                                              List<String> missingFactors,
                                              long dates_mismatch
                                              ) throws IOException {

        RestStatus status;
        if (assessment == null) {
            status = StrategicIndicator.setStrategicIndicatorEvaluation(prj,
                                                            strategicIndicatorID,
                                                            strategicIndicatorName,
                                                            strategicIndicatorDescription,
                                                            value,
                                                            date,
                                                            null,
                                                            missingFactors,
                                                            dates_mismatch)
                    .status();
        } else {
            status = StrategicIndicator.setStrategicIndicatorEvaluation(prj,
                                                            strategicIndicatorID,
                                                            strategicIndicatorName,
                                                            strategicIndicatorDescription,
                                                            value,
                                                            date,
                                                            ListDTOSIAssesmenttoEstimationEvaluationDTO(assessment),
                                                            missingFactors,
                                                            dates_mismatch)
                    .status();
        }
        return status.equals(RestStatus.OK) || status.equals(RestStatus.CREATED);
    }

    private List<DTOStrategicIndicatorEvaluation> StrategicIndicatorEvaluationDTOListToDTOStrategicIndicatorEvaluationList(List<StrategicIndicatorEvaluationDTO> evals) throws CategoriesException {
        List<DTOStrategicIndicatorEvaluation> si = new ArrayList<>();
        // si contains the list of evaluations for strategic indicators
        for (Iterator<StrategicIndicatorEvaluationDTO> iterSI = evals.iterator(); iterSI.hasNext(); ) {
            // For each SI
            StrategicIndicatorEvaluationDTO element = iterSI.next();
            Long id = null;
            boolean hasBN = false;
            boolean hasFeedback = false;
            for (Strategic_Indicator dbsi : siRep.findAll()) {
                if (dbsi.getName().replaceAll("\\s+","").toLowerCase().equals(element.getID())) {
                    id = dbsi.getId();
                    hasBN = dbsi.getNetwork() != null;
                    List<Feedback> feedback = new ArrayList<>();
                    try {
                        feedback = feedbackRepository.getFeedback(id);
                    } catch (Exception e) {}
                    if (!feedback.isEmpty()) hasFeedback = true;
                }
            }
            //get categories
            List<DTOSIAssesment> categories = util.getCategories();

            //bool that determines if the current SI has the estimation parameter
            if (element.getEstimation() == null || element.getEstimation().size() != element.getEvaluations().size())
                throw new CategoriesException();

            Iterator<EstimationEvaluationDTO> iterSIEst = element.getEstimation().iterator();

            for (Iterator<EvaluationDTO> iterSIEval = element.getEvaluations().iterator(); iterSIEval.hasNext() ; ) {
                EvaluationDTO evaluation = iterSIEval.next();
                EstimationEvaluationDTO estimation = iterSIEst.next();
                //merge categories and estimation
                boolean hasEstimation = true;
                if (estimation == null || estimation.getEstimation() == null || estimation.getEstimation().size()==0)
                    hasEstimation = false;

                if (hasEstimation && estimation.getEstimation() != null && estimation.getEstimation().size() == categories.size()) {
                    int changed = 0;
                    int i = 0;
                    for (DTOSIAssesment d : categories) {
                        if (d.getLabel().equals(estimation.getEstimation().get(i).getSecond())) {
                            d.setValue(estimation.getEstimation().get(i).getThird());
                            d.setUpperThreshold(estimation.getEstimation().get(i).getFourth());
                            ++changed;
                        }
                        ++i;
                    }
                    if (changed != categories.size())
                        throw new CategoriesException();
                } else if (hasEstimation) throw new CategoriesException();
                //calculate "fake" value if the SI has estimation
                if (hasEstimation) {
                    Float value = util.getValueAndLabelFromCategories(categories).getFirst();
                    DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(element.getID(),
                            element.getName(),
                            element.getDescription(),
                            Pair.of(value, util.getLabel(value)),
                            new ArrayList<>(categories),
                            evaluation.getEvaluationDate(),
                            evaluation.getDatasource(),
                            id,
                            categories.toString(),
                            hasBN);
                    dtoStrategicIndicatorEvaluation.setHasFeedback(hasFeedback);
                    si.add(dtoStrategicIndicatorEvaluation);
                } else {
                    DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(element.getID(),
                            element.getName(),
                            element.getDescription(),
                            Pair.of(evaluation.getValue(), util.getLabel(evaluation.getValue())),
                            new ArrayList<>(categories),
                            evaluation.getEvaluationDate(),
                            evaluation.getDatasource(),
                            id,
                            categories.toString(),
                            hasBN);
                    dtoStrategicIndicatorEvaluation.setHasFeedback(hasFeedback);
                    si.add(dtoStrategicIndicatorEvaluation);
                }
            }
        }
        return si;
    }

    private EstimationEvaluationDTO ListDTOSIAssesmenttoEstimationEvaluationDTO(List<DTOSIAssesment> assessment) {
        List<QuadrupletDTO<Integer, String, Float, Float>> estimation = new ArrayList<>();
        for (DTOSIAssesment dsa : assessment) {
            estimation.add(new QuadrupletDTO<Integer, String, Float, Float>(dsa.getId() != null ? dsa.getId().intValue() : null, dsa.getLabel(), dsa.getValue(), dsa.getUpperThreshold()));
        }
        return new EstimationEvaluationDTO(estimation);
    }


}