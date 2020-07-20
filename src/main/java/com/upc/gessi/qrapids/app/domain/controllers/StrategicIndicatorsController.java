package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.exceptions.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorQualityFactorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.presentation.rest.dto.relations.DTORelationsSI;
import evaluation.StrategicIndicator;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.lang.Math.abs;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class StrategicIndicatorsController {

    @Autowired
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Autowired
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Autowired
    private Forecast qmaForecast;

    @Autowired
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Autowired
    private SICategoryRepository strategicIndicatorCategoryRepository;

    @Autowired
    private ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private ProfilesController profilesController;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private MetricsController metricsController;

    @Autowired
    private AssesSI assesSI;

    @Autowired
    private QMARelations qmaRelations;

    @Autowired
    private StrategicIndicatorQualityFactorsController strategicIndicatorQualityFactorsController;

    @Autowired
    private StrategicIndicatorQualityFactorsRepository strategicIndicatorQualityFactorsRepository;

    private Logger logger = LoggerFactory.getLogger(StrategicIndicatorsController.class);

    public List<Strategic_Indicator> getStrategicIndicatorsByProjectAndProfile (String prjExternalId, String profileId) throws ProjectNotFoundException {
        Project project = projectsController.findProjectByExternalId(prjExternalId);
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)){ // if allSI true
                return strategicIndicatorRepository.findByProject_IdOrderByName(project.getId());
            } else { // if allSI false
                List<ProfileProjectStrategicIndicators> ppsiList =
                        profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project);
                List<Strategic_Indicator> result = new ArrayList<>();
                for (ProfileProjectStrategicIndicators ppsi : ppsiList) {
                    result.add(ppsi.getStrategicIndicator());
                }
                return result;
            }
        } else { // if profile is null
            return strategicIndicatorRepository.findByProject_IdOrderByName(project.getId());
        }
    }

    public boolean existsByExternalIdAndProjectAndProfile (String siExternalId, String prjExternalId, String profileId) throws ProjectNotFoundException {
        Project project = projectsController.findProjectByExternalId(prjExternalId);
        if ((profileId != null) && (!profileId.equals("null"))) { // if profile not null
            Profile profile = profilesController.findProfileById(profileId);
            if (profile.getAllSIByProject(project)){ // if allSI true
                return strategicIndicatorRepository.existsByExternalIdAndProject_Id(siExternalId,project.getId());
            } else { // if allSI false
                boolean result = false;
                int i = 0;
                List<ProfileProjectStrategicIndicators> ppsiList =
                        profileProjectStrategicIndicatorsRepository.findByProfileAndProject(profile,project);
                while (i < ppsiList.size() && !result) {
                    if (ppsiList.get(i).getStrategicIndicator().getExternalId().equals(siExternalId)) result = true;
                    i++;
                }
                return result;
            }
        } else { // if profile is null —> see if exist in data base
            return strategicIndicatorRepository.existsByExternalIdAndProject_Id(siExternalId,project.getId());
        }
    }

    public Strategic_Indicator getStrategicIndicatorById (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        Optional<Strategic_Indicator> strategicIndicatorOptional = strategicIndicatorRepository.findById(strategicIndicatorId);
        if (strategicIndicatorOptional.isPresent()) {
            return strategicIndicatorOptional.get();
        } else {
            throw new StrategicIndicatorNotFoundException();
        }
    }


    public Strategic_Indicator editStrategicIndicator (Long strategicIndicatorId, String name, String description, byte[] file, List<String> qualityFactors) throws StrategicIndicatorNotFoundException, StrategicIndicatorQualityFactorNotFoundException {
        Strategic_Indicator strategicIndicator = getStrategicIndicatorById(strategicIndicatorId);
        if (file != null && file.length > 10) strategicIndicator.setNetwork(file);
        strategicIndicator.setName(name);
        strategicIndicator.setDescription(description);
        // Actualize Quality Factors
        boolean weighted = reassignQualityFactorsToStrategicIndicator (qualityFactors, strategicIndicator);
        strategicIndicator.setWeighted(weighted);
        strategicIndicatorRepository.save(strategicIndicator);
        return  strategicIndicator;
    }

    private  boolean reassignQualityFactorsToStrategicIndicator (List<String> qualityFactors, Strategic_Indicator strategicIndicator) throws StrategicIndicatorQualityFactorNotFoundException {
        List<StrategicIndicatorQualityFactors> newQualityFactorsWeights = new ArrayList();
        // Delete oldQualityFactorsWeights
        List<StrategicIndicatorQualityFactors> oldQualityFactorsWeights = strategicIndicatorQualityFactorsRepository.findByStrategic_indicator(strategicIndicator);
        strategicIndicator.setQuality_factors(null);
        for (StrategicIndicatorQualityFactors old : oldQualityFactorsWeights) {
            strategicIndicatorQualityFactorsController.deleteStrategicIndicatorQualityFactor(old.getId());
        }
        boolean weighted = false;
        String f;
        Float w;

        // generate StrategicIndicatorQualityFactors class objects from List<String> qualityFactors
        while (!qualityFactors.isEmpty()) {
            StrategicIndicatorQualityFactors siqf;
            f = qualityFactors.get(0);
            w = Float.parseFloat(qualityFactors.get(1));
            if (w == -1) {
                siqf = strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(f, w, strategicIndicator);
                weighted = false;
            } else {
                siqf = strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(f, w, strategicIndicator);
                weighted = true;
            }
            newQualityFactorsWeights.add(siqf);
            qualityFactors.remove(1);
            qualityFactors.remove(0);
        }
        // create the association between Strategic Indicator and its Quality Factors
        strategicIndicator.setQuality_factors(newQualityFactorsWeights);
        return weighted;
    }


    public Strategic_Indicator saveStrategicIndicator (String name, String description, byte[] file, List<String> qualityFactors, Project project) {
        Strategic_Indicator strategicIndicator;
        // create Strategic Indicator minim (without quality factors and weighted)
        strategicIndicator = new Strategic_Indicator(name, description, file, project);
        strategicIndicatorRepository.save(strategicIndicator);
        // Associate it with Quality Factors (set weighted)
        boolean weighted = assignQualityFactorsToStrategicIndicator (qualityFactors, strategicIndicator);
        strategicIndicator.setWeighted(weighted);
        strategicIndicatorRepository.save(strategicIndicator);
        return strategicIndicator;
    }

    private boolean assignQualityFactorsToStrategicIndicator (List<String> qualityFactors, Strategic_Indicator strategicIndicator ) {
        List<StrategicIndicatorQualityFactors> qualityFactorsWeights = new ArrayList();
        boolean weighted = false;
        String f;
        Float w;
        // generate StrategicIndicatorQualityFactors class objects from List<String> qualityFactors
        while (!qualityFactors.isEmpty()) {
            StrategicIndicatorQualityFactors siqf;
            f = qualityFactors.get(0);
            w = Float.parseFloat(qualityFactors.get(1));
            if (w == -1) {
                siqf = strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(f, w, strategicIndicator);
                weighted = false;
            } else {
                siqf = strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(f, w, strategicIndicator);
                weighted = true;
            }
            qualityFactorsWeights.add(siqf);
            qualityFactors.remove(1);
            qualityFactors.remove(0);
        }
        // create the association between Strategic Indicator and its Quality Factors
        strategicIndicator.setQuality_factors(qualityFactorsWeights);
        return weighted;
    }

    public void deleteStrategicIndicator (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        if (strategicIndicatorRepository.existsById(strategicIndicatorId)) {
            // if we delete si, it has to disappear in profile_project_indicator table
            ProfileProjectStrategicIndicators ppsi = profileProjectStrategicIndicatorsRepository.findByStrategic_indicator(
                    strategicIndicatorRepository.findById(strategicIndicatorId).get());
            if (ppsi != null) profileProjectStrategicIndicatorsRepository.delete(ppsi);
            strategicIndicatorRepository.deleteById(strategicIndicatorId);
        } else {
            throw new StrategicIndicatorNotFoundException();
        }
    }

    public List<SICategory> getStrategicIndicatorCategories () {
        List<SICategory> strategicIndicatorCategoriesList = new ArrayList<>();
        Iterable<SICategory> strategicIndicatorCategoriesIterable = strategicIndicatorCategoryRepository.findAll();
        strategicIndicatorCategoriesIterable.forEach(strategicIndicatorCategoriesList::add);
        return strategicIndicatorCategoriesList;
    }

    public void newStrategicIndicatorCategories (List<Map<String, String>> categories) throws CategoriesException {
        if (categories.size() > 1) {
            strategicIndicatorCategoryRepository.deleteAll();
            for (Map<String, String> c : categories) {
                SICategory sic = new SICategory();
                sic.setName(c.get("name"));
                sic.setColor(c.get("color"));
                strategicIndicatorCategoryRepository.save(sic);
            }
        } else {
            throw new CategoriesException();
        }
    }

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsCurrentEvaluation (String projectExternalId, String profileId) throws IOException, CategoriesException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaStrategicIndicators.CurrentEvaluation(projectExternalId, profileId);
    }

    public DTOStrategicIndicatorEvaluation getSingleStrategicIndicatorsCurrentEvaluation (String strategicIndicatorId, String projectExternalId, String profileId) throws IOException, CategoriesException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, profileId, strategicIndicatorId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsCurrentEvaluation (String projectExternalId, String profileId, boolean filterDB) throws IOException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId, profileId, filterDB);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorCurrentEvaluation (String strategicIndicatorId, String projectExternalId, String profileId) throws IOException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(strategicIndicatorId, projectExternalId, profileId, true);
    }

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsHistoricalEvaluation (String projectExternalId, String profileId, LocalDate from, LocalDate to) throws IOException, CategoriesException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaStrategicIndicators.HistoricalData(from, to, projectExternalId, profileId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsHistoricalEvaluation (String projectExternalId, String profileId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaDetailedStrategicIndicators.HistoricalData(null, from, to, projectExternalId, profileId);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorsHistoricalEvaluation (String strategicIndicatorId, String projectExternalId, String profileId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException, ProjectNotFoundException {
        return qmaDetailedStrategicIndicators.HistoricalData(strategicIndicatorId, from, to, projectExternalId, profileId);
    }

    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction (List<DTOStrategicIndicatorEvaluation> si, String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastSI(si,technique, freq, horizon, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getDetailedStrategicIndicatorsPrediction (List<DTODetailedStrategicIndicator> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastDSI(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

    public void trainForecastModelsAllProjects(String technique) throws IOException, CategoriesException, ProjectNotFoundException {
        List<String> projects = projectsController.getAllProjectsExternalID();
        for (String prj: projects) { // if we train all projects profile isn't important
            trainForecastModelsSingleProject(prj, "null", technique);
        }
    }

    public void trainForecastModelsSingleProject(String project, String profile, String technique) throws IOException, CategoriesException, ProjectNotFoundException {
        List<DTOMetric> metrics = metricsController.getAllMetricsCurrentEvaluation(project);
        qmaForecast.trainMetricForecast(metrics, "7", project, technique);

        List<DTOQualityFactor> factors = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(project);
        qmaForecast.trainFactorForecast(factors, "7", project, technique);

        List<DTOStrategicIndicatorEvaluation> strategicIndicators = getAllStrategicIndicatorsCurrentEvaluation(project, profile);
        qmaForecast.trainStrategicIndicatorForecast(strategicIndicators, "7", project, technique);
    }

    public boolean assessStrategicIndicators(String projectExternalId, LocalDate dateFrom) throws IOException, CategoriesException, ProjectNotFoundException {
        boolean correct = true;
        if (dateFrom != null) {
            LocalDate dateTo = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            while (correct && dateFrom.compareTo(dateTo) <= 0) {
                correct = assessDateStrategicIndicators(projectExternalId, dateFrom);
                dateFrom = dateFrom.plusDays(1);
            }
        } else {
            correct = assessDateStrategicIndicators(projectExternalId, null);
        }
        return correct;
    }

    private boolean assessDateStrategicIndicators(String projectExternalId, LocalDate dateFrom) throws IOException, CategoriesException, ProjectNotFoundException {
        boolean correct = true;

        // if there is no specific project as a parameter, all the projects are assessed
        if (projectExternalId == null) {
            // Every time when SI are computed, we update the table projects in the BD in case new projects appear
            List<String> projects = projectsController.importProjectsAndUpdateDatabase();
            int i=0;
            while (i<projects.size() && correct) {
                qmaStrategicIndicators.prepareSIIndex(projects.get(i));
                correct = assessDateProjectStrategicIndicators(projects.get(i), dateFrom);
                i++;
            }
        }
        else {
            qmaStrategicIndicators.prepareSIIndex(projectExternalId);
            correct = assessDateProjectStrategicIndicators(projectExternalId, dateFrom);
        }
        return correct;
    }

    private boolean assessDateProjectStrategicIndicators(String project, LocalDate evaluationDate) throws IOException, ProjectNotFoundException {
        Factors factorsQma= new Factors(); //factors list, each of them includes list of SI in which is involved
        List<DTOFactor> factorList;

        // If we receive an evaluationData is because we are recomputing historical data. We need the factors for an
        // specific day, not the last evaluation
        if (evaluationDate == null) {
            evaluationDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            factorList = qualityFactorsController.getAllFactorsEvaluation(project);
        }
        else
            factorList = qualityFactorsController.getAllFactorsHistoricalEvaluation(project, evaluationDate, evaluationDate);
        factorsQma.setFactors(factorList);

        return assessProjectStrategicIndicators(evaluationDate, project, factorsQma);
    }

    private boolean assessProjectStrategicIndicators(LocalDate evaluationDate, String  projectExternalId, Factors factorsQMA) throws IOException, ProjectNotFoundException {
        // List of ALL the strategic indicators in the local database
        Project project = new Project();
        try {
            project = projectsController.findProjectByExternalId(projectExternalId);
        } catch (ProjectNotFoundException e) {
            List <String> prj = Arrays.asList(projectExternalId);
            projectsController.updateDataBaseWithNewProjects(prj);
        }
        Iterable<Strategic_Indicator> strategicIndicatorIterable = strategicIndicatorRepository.findByProject_Id(project.getId());

        boolean correct = true;

        // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
        factorsQMA.clearStrategicIndicatorsRelations(evaluationDate);

        // 2.- We will compute the evaluation values for the SIs, adding the corresponding relations to the factors
        //      used for these computation
        for (Strategic_Indicator si : strategicIndicatorIterable) {
            correct = assessStrategicIndicator(evaluationDate, projectExternalId, si, factorsQMA);
        }

        // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
        // the strategic indicators using them
        qualityFactorsController.setFactorStrategicIndicatorRelation(factorsQMA.getFactors(), projectExternalId);

        return correct;
    }

    public boolean assessStrategicIndicator(String name) throws IOException, CategoriesException {
        boolean correct = false;
        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluationDate = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Strategic Indicator
        Strategic_Indicator si = strategicIndicatorRepository.findByName(name);

        // All the factors' assessment from QMA external service
        Factors factorsQma= new Factors();

        // List of component, the SI is assessed for all the components
        List <String> projects = projectsController.getAllProjectsExternalID();

        // We will compute the evaluation values for the SI for all the components
        for (String prj: projects) {
            // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
            factorsQma.setFactors(qualityFactorsController.getAllFactorsEvaluation(prj));
            factorsQma.clearStrategicIndicatorsRelations(evaluationDate, name);

            correct = assessStrategicIndicator(evaluationDate, prj, si, factorsQma);

            // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
            // the strategic indicators using them
            qualityFactorsController.setFactorStrategicIndicatorRelation(factorsQma.getFactors(), prj);
        }

        return correct;
    }

    // Function for AssessStrategicIndicator to concrete project
    public boolean assessStrategicIndicator(String name, String prj) throws IOException, ProjectNotFoundException {
        boolean correct = false;
        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluationDate = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Strategic Indicator
        Project project = projectsController.findProjectByExternalId(prj);
        Strategic_Indicator si = strategicIndicatorRepository.findByNameAndProject_Id(name, project.getId());

        // All the factors' assessment from QMA external service
        Factors factorsQma= new Factors();

        // We will compute the evaluation values for the SI for THIS CONCRETE component

        // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
        factorsQma.setFactors(qualityFactorsController.getAllFactorsEvaluation(prj));
        factorsQma.clearStrategicIndicatorsRelations(evaluationDate, name);

        correct = assessStrategicIndicator(evaluationDate, prj, si, factorsQma);

        // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
        // the strategic indicators using them
        qualityFactorsController.setFactorStrategicIndicatorRelation(factorsQma.getFactors(), prj);

        return correct;
    }

    private boolean assessStrategicIndicator(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, Factors factorsQMA)
            throws IOException {
        boolean correct = true;
        // We need the evaluation for the factors used to compute "si"
        List<Float> listFactorsAssessmentValues = new ArrayList<>();
        // List of factor impacting in ONE strategic indicator
        List<String> siFactors;
        DTOFactor factor;
        List<DTOFactor> factorList = new ArrayList<>();
        List<String> missingFactors = new ArrayList<>(); //List of factors without assessment ---> SI assessment incomplete
        int index;
        boolean factorFound;
        long factorsMismatch=0;

        // We need to identify the factors in factors_qma that are used to compute SI
        Map<String,String> mapSIFactors = new HashMap<>();
        siFactors = strategicIndicator.getQuality_factors();
        factorsMismatch = buildFactorsInfoAndCalculateMismatch(evaluationDate, project, strategicIndicator, factorsQMA, listFactorsAssessmentValues, siFactors, factorList, missingFactors, factorsMismatch, mapSIFactors);

        String assessmentValueOrLabel = "";
        // The computations depends on having a BN or not
        try {
            if (strategicIndicator.getNetwork() != null && strategicIndicator.getNetwork().length > 10) {
                assessmentValueOrLabel = assessStrategicIndicatorWithBayesianNetwork(evaluationDate, project, strategicIndicator, missingFactors, factorsMismatch, mapSIFactors);
            } else {
                assessmentValueOrLabel = assessStrategicIndicatorWithoutBayesianNetwork(evaluationDate, project, strategicIndicator, listFactorsAssessmentValues, siFactors, missingFactors, factorsMismatch, assessmentValueOrLabel);
            }
        } catch (AssessmentErrorException e) {
            logger.error(e.getMessage(), e);
            correct = false;
        }

        // Save relations of factor -> SI
        if (correct) {
            correct = buildAndSaveFactorSIRelation(evaluationDate, project, strategicIndicator, factorList, assessmentValueOrLabel);
        }

        return correct;
    }

    private String assessStrategicIndicatorWithoutBayesianNetwork(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, List<Float> listFactorsAssessmentValues, List<String> siFactors, List<String> missingFactors, long factorsMismatch, String assessmentValueOrLabel) throws IOException, AssessmentErrorException {
        if (!listFactorsAssessmentValues.isEmpty()) {
            float value;
            List<Float> weights = new ArrayList<>();
            boolean weighted = strategicIndicator.isWeighted();
            if (weighted) {
                List<String> qfWeights = strategicIndicator.getWeights();
                for ( int i = 1; i < qfWeights.size(); i+=2) {
                    weights.add(Float.valueOf(qfWeights.get(i)));
                }
                value = assesSI.assesSI_weighted(listFactorsAssessmentValues, weights);
            } else {
                value = assesSI.assesSI(listFactorsAssessmentValues, siFactors.size());
            }
            assessmentValueOrLabel = String.valueOf(value);
            String info = "factors: {";
            for (int j = 0; j < siFactors.size(); j++) {
                String factorInfo = " " + siFactors.get(j) + " (value: " +  listFactorsAssessmentValues.get(j) + ", ";
                if (weighted) factorInfo += "weight: " + weights.get(j).intValue() + "%);";
                else factorInfo += "no weighted);";
                info += factorInfo;
            }
            if (weighted) {
                info += " }, formula: weighted average, value: " + value + ", category: " + getLabel(value);
            } else {
                info += " }, formula: average, value: " + value + ", category: " + getLabel(value);
            }
            // saving the SI's assessment
            if (!qmaStrategicIndicators.setStrategicIndicatorValue(
                    project,
                    strategicIndicator.getExternalId(),
                    strategicIndicator.getName(),
                    strategicIndicator.getDescription(),
                    value,
                    info,
                    evaluationDate,
                    null,
                    missingFactors,
                    factorsMismatch
            ))
                throw new AssessmentErrorException();
        }
        return assessmentValueOrLabel;
    }

    private String assessStrategicIndicatorWithBayesianNetwork(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, List<String> missingFactors, long factorsMismatch, Map<String, String> mapSIFactors) throws IOException, AssessmentErrorException {
        String assessmentValueOrLabel;
        File tempFile = File.createTempFile("network", ".dne", null);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(strategicIndicator.getNetwork());
        }
        List<DTOSIAssessment> assessment = assesSI.assesSI(strategicIndicator.getExternalId(), mapSIFactors, tempFile);
        Pair<Float, String> valueAndLabel = getValueAndLabelFromCategories(assessment);
        if (!valueAndLabel.getFirst().isNaN()) {
            assessmentValueOrLabel = valueAndLabel.getSecond();
            // saving the SI's assessment
            String info = "";
            if (!qmaStrategicIndicators.setStrategicIndicatorValue(
                    project,
                    strategicIndicator.getExternalId(),
                    strategicIndicator.getName(),
                    strategicIndicator.getDescription(),
                    valueAndLabel.getFirst(),
                    info,
                    evaluationDate,
                    assessment,
                    missingFactors,
                    factorsMismatch))
                throw new AssessmentErrorException();
        }
        else {
            throw new AssessmentErrorException();
        }
        return assessmentValueOrLabel;
    }

    private long buildFactorsInfoAndCalculateMismatch(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, Factors factorsQMA, List<Float> listFactorsAssessmentValues, List<String> siFactors, List<DTOFactor> factorList, List<String> missingFactors, long factorsMismatch, Map<String, String> mapSIFactors) throws IOException {
        int index;
        boolean factorFound;
        DTOFactor factor;//siFactors is the list of factors that are needed to compute the SI
        //missingFactors will contain the factors not found in QMA
        for (String qfId : siFactors) {
            // qfID contains a factor that is used to compute the sI
            // We need to find the assessment of the factor in the SI definition, in case the factor is missing
            // this factor will be added to the missing factors list
            index =0;
            factorFound = false;
            while (!factorFound && index < factorsQMA.getFactors().size()){
                factor = factorsQMA.getFactors().get(index++);
                if (factor.getId().equals(qfId)) {
                    factorFound = true;
                    factorList.add(factor);
                    listFactorsAssessmentValues.add(factor.getValue());
                    mapSIFactors.put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
                    factor.addStrategicIndicator(StrategicIndicator.getHardID(project, strategicIndicator.getExternalId(), evaluationDate));
                    // If there is some missing days, we keep the maximum gap to be materialised
                    long mismach = DAYS.between(factor.getDate(), evaluationDate);
                    if (mismach > factorsMismatch)
                        factorsMismatch=mismach;
                }
            }
            // qfId is the factor searched in QMA results
            if (!factorFound)
                missingFactors.add(qfId);
        }
        return factorsMismatch;
    }

    private boolean buildAndSaveFactorSIRelation(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, List<DTOFactor> factorList, String assessmentValueOrLabel) throws IOException {
        boolean correct;
        List<String> factorIds = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (DTOFactor dtoFactor : factorList) {
            factorIds.add(dtoFactor.getId());
            Float weight = -1f; // when SI is computed with network
            if (strategicIndicator.getNetwork() == null) {
                // when SI is not weighted the weight of factor value is computed as average
                if (!strategicIndicator.isWeighted()) {
                    weight = 1f/factorList.size();
                } else { // when SI is weighted the weight of factor has corresponding value
                    List<String> qfw = strategicIndicator.getWeights();
                    weight = Float.parseFloat(qfw.get(qfw.indexOf(dtoFactor.getId()) + 1)) / 100;
                }
            }
            weights.add(weight);
            if (weight == -1f){
                values.add(dtoFactor.getValue()*1f/factorList.size()); // value for representation (average)
            } else {
                values.add(dtoFactor.getValue() * weight); // value of weighted factor
            }
            labels.add(qualityFactorsController.getFactorLabelFromValue(dtoFactor.getValue()));
        }
        correct = saveFactorSIRelation(project, factorIds, strategicIndicator.getExternalId(), evaluationDate, weights, values, labels, assessmentValueOrLabel);
        return correct;
    }

    private boolean saveFactorSIRelation (String prj, List<String> factorIds, String si, LocalDate evaluationDate, List<Float> weights, List<Float> factorValues, List<String> factorLabels, String siValueOrLabel) throws IOException {
        return qmaRelations.setStrategicIndicatorFactorRelation(prj, factorIds, si, evaluationDate, weights, factorValues, factorLabels, siValueOrLabel);
    }

    public Pair<Float,String> getValueAndLabelFromCategories(final List<DTOSIAssessment> assessments) {
        Float max = -1.0f;
        Float maxIndex = -1.f;
        for (Float i = 0.f; i < assessments.size(); i++) {
            DTOSIAssessment assessment = assessments.get(i.intValue());
            if (max < assessment.getValue()) {
                max = assessment.getValue();
                maxIndex = i;
            }
        }
        if (maxIndex > -1.f) {
            String label = assessments.get(maxIndex.intValue()).getLabel();
            Float value = getValueFromLabel(label);
            return Pair.of(value, label);
        }
        else return Pair.of(Float.NaN,"");
    }

    public Float getValueFromLabel (String label) {
        Iterable<SICategory> siCategoryIterable = strategicIndicatorCategoryRepository.findAll();
        List<SICategory> siCategoryList = new ArrayList<>();
        siCategoryIterable.forEach(siCategoryList::add);
        Collections.reverse(siCategoryList);
        Float index = -1.f;
        for (Float i = 0.f; i < siCategoryList.size(); i++) {
            if (siCategoryList.get(i.intValue()).getName().equals(label))
                index = i;
        }
        return (index/siCategoryList.size() + (index+1)/siCategoryList.size())/2.0f;
    }

    public void fetchStrategicIndicators () throws IOException, CategoriesException, ProjectNotFoundException {
        List<String> projects = projectsController.importProjectsAndUpdateDatabase();
        for(String projectExternalId : projects) {
            List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicators = new ArrayList<>();
            try {
                dtoDetailedStrategicIndicators = getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId, null, false);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            for (DTODetailedStrategicIndicator dtoDetailedStrategicIndicator : dtoDetailedStrategicIndicators) {
                List<String> factors = new ArrayList<>();
                for (DTOFactor f : dtoDetailedStrategicIndicator.getFactors()) {
                    factors.add(f.getId());
                    factors.add("-1");
                }
                Project project = projectsController.findProjectByExternalId(projectExternalId);
                saveStrategicIndicator(dtoDetailedStrategicIndicator.getName(), "", null, factors, project);
            }
        }
    }

    public List<DTOStrategicIndicatorEvaluation> simulateStrategicIndicatorsAssessment (Map<String, Float> factorsNameValueMap, String projectExternalId, String profileId) throws IOException, ProjectNotFoundException {
        List<DTOFactor> factors = qualityFactorsController.getAllFactorsEvaluation(projectExternalId);
        for (DTOFactor factor : factors) {
            if (factorsNameValueMap.containsKey(factor.getId())) {
                factor.setValue(factorsNameValueMap.get(factor.getId()));
            }
        }
        Project project = projectsController.findProjectByExternalId(projectExternalId);
        Iterable<Strategic_Indicator> listSI = getStrategicIndicatorsByProjectAndProfile(projectExternalId,profileId);
                //strategicIndicatorRepository.findByProject_Id(project.getId());
        List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
        for (Strategic_Indicator si : listSI) {
            Map<String,String> mapSIFactors = new HashMap<>();
            List<DTOFactor> listSIFactors = new ArrayList<>();
            buildMapAndListOfFactors(factors, si, mapSIFactors, listSIFactors);
            if (si.getNetwork() != null && si.getNetwork().length > 10) {
                File tempFile = File.createTempFile("network", ".dne", null);
                try(FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(si.getNetwork());
                }
                List<DTOSIAssessment> assessment = assesSI.assesSI(si.getName().replaceAll("\\s+","").toLowerCase(), mapSIFactors, tempFile);
                float value = getValueAndLabelFromCategories(assessment).getFirst();
                result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+","").toLowerCase(),
                        si.getName(),
                        si.getDescription(),
                        Pair.of(value, getLabel(value)),
                        "",
                        assessment,
                        null,
                        "Simulation",
                        si.getId(),
                        "",
                        si.getNetwork() != null));
            }
            else {
                float value = computeStrategicIndicatorValue(listSIFactors);
                result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+","").toLowerCase(),
                        si.getName(),
                        si.getDescription(),
                        Pair.of(value, getLabel(value)),
                        "",
                        getCategories(),
                        null,
                        "Simulation",
                        si.getId(),
                        "",
                        si.getNetwork() != null));
            }
        }
        return result;
    }

    private void buildMapAndListOfFactors(List<DTOFactor> factors, Strategic_Indicator si, Map<String, String> mapSIFactors, List<DTOFactor> listSIFactors) {
        for (String qfId : si.getQuality_factors()) {
            for (DTOFactor factor : factors) {
                if (factor.getId().equals(qfId)) {
                    mapSIFactors.put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
                    listSIFactors.add(factor);
                }
            }
        }
    }

    public float computeStrategicIndicatorValue(List<DTOFactor> factors) {
        float result = 0;
        int nFactors = 0;
        for (DTOFactor f : factors) {
            if (f.getValue() != null) {
                result += f.getValue();
                nFactors++;
            }
        }
        if (nFactors > 0) result /= nFactors;
        return result;
    }

    public String getLabel(Float f) {
        Iterable<SICategory> siCategoryIterable = strategicIndicatorCategoryRepository.findAll();
        List<SICategory> siCategoryList = new ArrayList<>();
        siCategoryIterable.forEach(siCategoryList::add);
        if (f != null && !siCategoryList.isEmpty()) {
            if (f < 1.0f)
                return siCategoryList.get(siCategoryList.size() - 1 - (int) (f * (float) siCategoryList.size())).getName();
            else
                return siCategoryList.get(0).getName();
        } else return "No Category";
    }

    public List<DTOSIAssessment> getCategories() {
        Iterable<SICategory> siCategoryIterable = strategicIndicatorCategoryRepository.findAll();
        List<SICategory> siCategoryList = new ArrayList<>();
        siCategoryIterable.forEach(siCategoryList::add);
        List<DTOSIAssessment> result = new ArrayList<>();
        float thresholdsInterval = 1.0f/(float)siCategoryList.size();
        float upperThreshold=1;
        for (SICategory c : siCategoryIterable) {
            result.add(new DTOSIAssessment(c.getId(), c.getName(), null, c.getColor(), abs((float)upperThreshold)));
            upperThreshold -=  thresholdsInterval;
        }
        return result;
    }

    public static String buildDescriptiveLabelAndValue (Pair<Float, String> value) {
        String labelAndValue;

        String numeric_value = String.format(Locale.ENGLISH, "%.2f", value.getFirst());

        if (value.getSecond().isEmpty())
            labelAndValue = numeric_value;
        else{
            labelAndValue = value.getSecond();
            if (!numeric_value.isEmpty())
                labelAndValue += " (" + numeric_value + ')';
        }

        return labelAndValue;
    }

    public String getColorFromLabel (String label) {
        SICategory category = strategicIndicatorCategoryRepository.findByName(label);
        return category.getColor();
    }

    public List<DTORelationsSI> getQualityModel(String projectExternalId, String profileId, LocalDate date) throws IOException, CategoriesException, ArithmeticException, ProjectNotFoundException {
        return qmaRelations.getRelations(projectExternalId, profileId, date);
    }

    public List<String> getForecastTechniques() {
        return qmaForecast.getForecastTechniques();
    }
}
