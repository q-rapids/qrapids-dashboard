package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.EstimationEvaluationDTO;
import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.StrategicIndicatorFactorEvaluationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.FactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.domain.models.ProfileProjectStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QualityFactor.QualityFactorRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODetailedStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOAssessment;
import evaluation.StrategicIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class QMADetailedStrategicIndicators {

    // it was made to use these variables in FactorEvaluationDTOListToDTOFactorList static method
    private static QualityFactorRepository qfRep;
    private static ProfilesController profilesController;
    private static ProjectRepository prjRep;
    private static ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Autowired
    public QMADetailedStrategicIndicators(QualityFactorRepository qfRep, ProfilesController profilesController,
                                          ProjectRepository prjRep, ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository) {
        QMADetailedStrategicIndicators.qfRep = qfRep;
        QMADetailedStrategicIndicators.profilesController = profilesController;
        QMADetailedStrategicIndicators.prjRep = prjRep;
        QMADetailedStrategicIndicators.profileProjectStrategicIndicatorsRepository = profileProjectStrategicIndicatorsRepository;
    }

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    FactorsController factorsController;

    public List<DTODetailedStrategicIndicatorEvaluation> CurrentEvaluation(String id, String prj, String profile, boolean filterDB) throws IOException, ProjectNotFoundException {
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

        dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(prj, profile, evals, filterDB, true);
        //Connection.closeConnection();
        return dsi;
    }


    public List<DTODetailedStrategicIndicatorEvaluation> HistoricalData(String id, LocalDate from, LocalDate to, String prj, String profile) throws IOException, ProjectNotFoundException {
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
        dsi = StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(prj, profile, evals, true, false);
        //Connection.closeConnection();
        return dsi;
    }


    private List<DTODetailedStrategicIndicatorEvaluation> StrategicIndicatorFactorEvaluationDTOtoDTODetailedStrategicIndicator(String prj, String profile, List<StrategicIndicatorFactorEvaluationDTO> evals, boolean filterDB, boolean currentData) throws ProjectNotFoundException {
        List<DTODetailedStrategicIndicatorEvaluation> dsi = new ArrayList<>();
        boolean found; // to check if the SI is in the database
        //for each Detailed Strategic Indicador
        for (Iterator<StrategicIndicatorFactorEvaluationDTO> iterDSI = evals.iterator(); iterDSI.hasNext(); ) {
            StrategicIndicatorFactorEvaluationDTO element = iterDSI.next();
            if (filterDB) found = strategicIndicatorsController.existsByExternalIdAndProjectAndProfile(element.getID(), prj, profile);
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
                if(currentData)
                    d.setFactors(FactorEvaluationDTOListToDTOFactorList(element.getID(), element.getFactors(),prjRep.findByExternalId(prj).getId(), profile, false));
                else
                    d.setFactors(FactorEvaluationDTOListToDTOFactorList(null, element.getFactors(),prjRep.findByExternalId(prj).getId(), profile, false));
                // Get value
                List<DTOAssessment> categories = strategicIndicatorsController.getCategories();
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

    private void setValueAndThresholdToCategories(List<DTOAssessment> categories, EstimationEvaluationDTO estimation) {
        int i = 0;
        for (DTOAssessment c : categories) {
            if (c.getLabel().equals(estimation.getEstimation().get(i).getSecond())) {
                c.setValue(estimation.getEstimation().get(i).getThird());
                c.setUpperThreshold(estimation.getEstimation().get(i).getFourth());
            }
            ++i;
        }
    }

    public List<DTOFactorEvaluation> FactorEvaluationDTOListToDTOFactorList(String siExternalID, List<FactorEvaluationDTO> factors, Long prjID, String profileId, boolean filterDB) {
        List<DTOFactorEvaluation> listFact = new ArrayList<>();
        Optional<Project> project = prjRep.findById(prjID);
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
                    // check if the factor belongs to the si
                    boolean addFactor = true;
                    if (siExternalID != null) {
                        if (!siRep.findByExternalIdAndProjectId(siExternalID,project.get().getId()).getQuality_factors().contains(factor.getID()))
                            addFactor = false;
                    }
                    if (addFactor)
                        listFact.add(FactorEvaluationDTOToDTOFactor(factor, evaluation));
                }
            }
        }
        // filter by profile
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project.get())) { // if allSI true, return all quality factors
                return listFact;
            } else { // if allSI false, return quality factors involved in SIs
                List<ProfileProjectStrategicIndicators> ppsiList =
                        profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project.get());
                List<String> qfOfSIs = new ArrayList<>();
                for (ProfileProjectStrategicIndicators ppsi : ppsiList) {
                    for (String factor : ppsi.getStrategicIndicator().getQuality_factors())
                        qfOfSIs.add(factor);
                }
                List<DTOFactorEvaluation> reviewQF = new ArrayList<>();
                for (int i = 0; i < listFact.size(); i++) {
                    if (qfOfSIs.contains(listFact.get(i).getId())) reviewQF.add(listFact.get(i));
                }
                return reviewQF;
            }
        } else { // if profile is null, return all quality factors
            return listFact;
        }
    }

     public DTOFactorEvaluation FactorEvaluationDTOToDTOFactor(FactorEvaluationDTO factor, EvaluationDTO evaluation) {
        DTOFactorEvaluation factorEval = new DTOFactorEvaluation(
                factor.getID(),
                factor.getName(),
                factor.getDescription(),
                Pair.of(evaluation.getValue(), factorsController.getFactorLabelFromValue(evaluation.getValue())), evaluation.getEvaluationDate(),
                evaluation.getDatasource(),evaluation.getRationale(),
                factor.getStrategicIndicators());
        factorEval.setMismatchDays(evaluation.getMismatchDays());
        factorEval.setMissingMetrics(evaluation.getMissingElements());
        return factorEval;
    }


}
