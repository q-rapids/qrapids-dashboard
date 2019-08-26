package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Factors;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.exceptions.StrategicIndicatorNotFoundException;
import evaluation.StrategicIndicator;
import org.elasticsearch.ElasticsearchStatusException;
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
    private ProjectsController projectsController;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private MetricsController metricsController;

    @Autowired
    private AssesSI assesSI;

    @Autowired
    private QMARelations qmaRelations;

    public List<Strategic_Indicator> getStrategicIndicatorsByProject (Project project) {
        return strategicIndicatorRepository.findByProject_Id(project.getId());
    }

    public Strategic_Indicator getStrategicIndicatorById (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        Optional<Strategic_Indicator> strategicIndicatorOptional = strategicIndicatorRepository.findById(strategicIndicatorId);
        if (strategicIndicatorOptional.isPresent()) {
            return strategicIndicatorOptional.get();
        } else {
            throw new StrategicIndicatorNotFoundException();
        }
    }

    public Strategic_Indicator editStrategicIndicator (Long strategicIndicatorId, String name, String description, byte[] file, List<String> qualityFactors) throws StrategicIndicatorNotFoundException {
        Strategic_Indicator strategicIndicator = getStrategicIndicatorById(strategicIndicatorId);
        if (file != null && file.length > 10) strategicIndicator.setNetwork(file);
        strategicIndicator.setName(name);
        strategicIndicator.setDescription(description);
        strategicIndicator.setQuality_factors(qualityFactors);
        strategicIndicatorRepository.save(strategicIndicator);
        return  strategicIndicator;
    }

    public Strategic_Indicator saveStrategicIndicator (String name, String description, byte[] file, List<String> qualityFactors, Project project) {
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(name, description, file, qualityFactors, project);
        strategicIndicatorRepository.save(strategicIndicator);
        return strategicIndicator;
    }

    public void deleteStrategicIndicator (Long strategicIndicatorId) throws StrategicIndicatorNotFoundException {
        if (strategicIndicatorRepository.existsById(strategicIndicatorId)) {
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

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsCurrentEvaluation (String projectExternalId) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.CurrentEvaluation(projectExternalId);
    }

    public DTOStrategicIndicatorEvaluation getSingleStrategicIndicatorsCurrentEvaluation (String strategicIndicatorId, String projectExternalId) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, strategicIndicatorId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsCurrentEvaluation (String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorCurrentEvaluation (String strategicIndicatorId, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.CurrentEvaluation(strategicIndicatorId, projectExternalId);
    }

    public List<DTOStrategicIndicatorEvaluation> getAllStrategicIndicatorsHistoricalEvaluation (String projectExternalId, LocalDate from, LocalDate to) throws IOException, CategoriesException, ElasticsearchStatusException {
        return qmaStrategicIndicators.HistoricalData(from, to, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getAllDetailedStrategicIndicatorsHistoricalEvaluation (String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.HistoricalData(null, from, to, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getSingleDetailedStrategicIndicatorsHistoricalEvaluation (String strategicIndicatorId, String projectExternalId, LocalDate from, LocalDate to) throws IOException, ElasticsearchStatusException {
        return qmaDetailedStrategicIndicators.HistoricalData(strategicIndicatorId, from, to, projectExternalId);
    }

    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction (String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastSI(technique, freq, horizon, projectExternalId);
    }

    public List<DTODetailedStrategicIndicator> getDetailedStrategicIndicatorsPrediction (List<DTODetailedStrategicIndicator> currentEvaluation, String technique, String freq, String horizon, String projectExternalId) throws IOException, ElasticsearchStatusException {
        return qmaForecast.ForecastDSI(currentEvaluation, technique, freq, horizon, projectExternalId);
    }

    public void trainForecastModelsAllProjects(String technique) throws IOException, CategoriesException{
        List<String> projects = projectsController.getAllProjects();
        for (String prj: projects) {
            trainForecastModelsSingleProject(prj, technique);
        }
    }

    public void trainForecastModelsSingleProject(String project, String technique) throws IOException {
        List<DTOMetric> metrics = metricsController.getAllMetricsCurrentEvaluation(project);
        qmaForecast.trainMetricForecast(metrics, "7", project, technique);

        List<DTOQualityFactor> factors = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(project);
        qmaForecast.trainFactorForecast(factors, "7", project, technique);
    }

    public boolean assessStrategicIndicators(String projectExternalId, LocalDate dateFrom) throws IOException, CategoriesException {
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

    private boolean assessDateStrategicIndicators(String projectExternalId, LocalDate dateFrom) throws IOException, CategoriesException {
        boolean correct = true;

        // if there is no specific project as a parameter, all the projects are assessed
        if (projectExternalId == null) {
            List<String> projects = projectsController.getAllProjects();
            int i=0;
            while (i<projects.size() && correct) {
                correct = assessDateProjectStrategicIndicators(projects.get(i), dateFrom);
                i++;
            }
        }
        else {
            correct = assessDateProjectStrategicIndicators(projectExternalId, dateFrom);
        }
        return correct;
    }

    private boolean assessDateProjectStrategicIndicators(String project, LocalDate evaluationDate) throws IOException, CategoriesException {
        Factors factors_qma= new Factors(); //factors list, each of them includes list of SI in which is involved
        List<DTOFactor> list_of_factors;

        // If we receive an evaluationData is because we are recomputing historical data. We need the factors for an
        // specific day, not the last evaluation
        if (evaluationDate == null) {
            evaluationDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            list_of_factors = qualityFactorsController.getAllFactorsEvaluation(project);
        }
        else
            list_of_factors = qualityFactorsController.getAllFactorsHistoricalEvaluation(project, evaluationDate, evaluationDate);
        factors_qma.setFactors(list_of_factors);

        return assessProjectStrategicIndicators(evaluationDate, project, factors_qma);
    }

    private boolean assessProjectStrategicIndicators(LocalDate evaluationDate, String  project, Factors factorsQMA) throws IOException {
        // List of ALL the strategic indicators in the local database
        Iterable<Strategic_Indicator> strategicIndicatorIterable = strategicIndicatorRepository.findAll();

/*        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluation_date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
*/
        boolean correct = true;

        // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
        factorsQMA.clearStrategicIndicatorsRelations(evaluationDate);

        // 2.- We will compute the evaluation values for the SIs, adding the corresponding relations to the factors
        //      used for these computation
        for (Strategic_Indicator si : strategicIndicatorIterable) {
            correct = assessStrategicIndicator(evaluationDate, project, si, factorsQMA);
        }

        // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
        // the strategic indicators using them
        qualityFactorsController.setFactorStrategicIndicatorRelation(factorsQMA.getFactors(), project);

        return correct;
    }

    public boolean assessStrategicIndicator(String name) throws IOException, CategoriesException {
        boolean correct = false;
        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluation_date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Strategic Indicator
        Strategic_Indicator si = strategicIndicatorRepository.findByName(name);

        // All the factors' assessment from QMA external service
        Factors factors_qma= new Factors();

        // List of component, the SI is assessed for all the components
        List <String> projects = projectsController.getAllProjects();

        // We will compute the evaluation values for the SI for all the components
        for (String prj: projects) {
            // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
            factors_qma.setFactors(qualityFactorsController.getAllFactorsEvaluation(prj));
            factors_qma.clearStrategicIndicatorsRelations(evaluation_date, name);

            correct = assessStrategicIndicator(evaluation_date, prj, si, factors_qma);

            // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
            // the strategic indicators using them
            qualityFactorsController.setFactorStrategicIndicatorRelation(factors_qma.getFactors(), prj);
        }

        return correct;
    }

    private boolean assessStrategicIndicator(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, Factors factorsQMA)
            throws IOException {
        boolean correct = true;
        // We need the evaluation for the factors used to compute "si"
        List<Float> listFactors_assessment_values = new ArrayList<>();
        // List of factor impacting in ONE strategic indicator
        List<String> si_factors;
        DTOFactor factor;
        List<DTOFactor> factorList = new ArrayList<>();
        List<String> missing_factors = new ArrayList<>(); //List of factors without assessment ---> SI assessment incomplete
        int index;
        boolean factor_found;
        long factors_mismatch=0;
//        listFactors_assessment_values.clear();

        // We need to identify the factors in factors_qma that are used to compute SI
        Map<String,String> mapSIFactors = new HashMap<>();
        si_factors = strategicIndicator.getQuality_factors();
        missing_factors.clear();

        //si_factors is the list of factors that are needed to compute the SI
        //missing_factors will contain the factors not found in QMA
        for (String qfId : si_factors) {
            // qfID contains a factor that is used to compute the sI
            // We need to find the assessment of the factor in the SI definition, in case the factor is missing
            // this factor will be added to the missing factors list
            index =0;
            factor_found = false;
            while (!factor_found && index < factorsQMA.getFactors().size()){
                factor = factorsQMA.getFactors().get(index++);
                if (factor.getId().equals(qfId)) {
                    factor_found = true;
                    factorList.add(factor);
                    listFactors_assessment_values.add(factor.getValue());
                    mapSIFactors.put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
                    factor.addStrategicIndicator(StrategicIndicator.getHardID(project, strategicIndicator.getExternalId(), evaluationDate));
//                        factor.addStrategicIndicator( si.getExternalId());
                    // If there is some missing days, we keep the maximum gap to be materialised
                    long mismach = DAYS.between(factor.getDate(), evaluationDate);
                    if (mismach > factors_mismatch)
                        factors_mismatch=mismach;
                }
            }
            // qfId is the factor searched in QMA results
            if (!factor_found)
                missing_factors.add(qfId);
        }

        String assessmentValueOrLabel = "";
        // The computations depends on having a BN or not
        if (strategicIndicator.getNetwork() != null && strategicIndicator.getNetwork().length > 10) {
            File tempFile = File.createTempFile("network", ".dne", null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(strategicIndicator.getNetwork());
            List<DTOSIAssessment> assessment = assesSI.AssesSI(strategicIndicator.getExternalId(), mapSIFactors, tempFile);
            Pair<Float, String> valueAndLabel = getValueAndLabelFromCategories(assessment);
            if (!valueAndLabel.getFirst().isNaN()) {
                assessmentValueOrLabel = valueAndLabel.getSecond();
                // saving the SI's assessment
                if (!qmaStrategicIndicators.setStrategicIndicatorValue(
                        project,
                        strategicIndicator.getExternalId(),
                        strategicIndicator.getName(),
                        strategicIndicator.getDescription(),
                        valueAndLabel.getFirst(),
                        evaluationDate,
                        assessment,
                        missing_factors,
                        factors_mismatch))
                    correct = false;
            }
            else {
                correct = false;
            }
        }
        else {
            if (listFactors_assessment_values.size()>0) {
                float value = assesSI.AssesSI(listFactors_assessment_values, si_factors.size());
                assessmentValueOrLabel = String.valueOf(value);
                // saving the SI's assessment
                if (!qmaStrategicIndicators.setStrategicIndicatorValue(
                        project,
                        strategicIndicator.getExternalId(),
                        strategicIndicator.getName(),
                        strategicIndicator.getDescription(),
                        value,
                        evaluationDate,
                        null,
                        missing_factors,
                        factors_mismatch
                ))
                    correct = false;
            }
        }

        // Save relations of factor -> SI
        if (correct) {
            List<String> factorIds = new ArrayList<>();
            List<Float> weights = new ArrayList<>();
            List<Float> values = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (DTOFactor dtoFactor : factorList) {
                factorIds.add(dtoFactor.getId());
                Float weight = 0f;
                if (strategicIndicator.getNetwork() == null)
                    weight = 1f;
                weights.add(weight);
                values.add(dtoFactor.getValue());
                labels.add(qualityFactorsController.getFactorLabelFromValue(dtoFactor.getValue()));
            }
            correct = saveFactorSIRelation(project, factorIds, strategicIndicator.getExternalId(), evaluationDate, weights, values, labels, assessmentValueOrLabel);
        }

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
                dtoDetailedStrategicIndicators = getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId);
            } catch (Exception e) {}
            for (DTODetailedStrategicIndicator dtoDetailedStrategicIndicator : dtoDetailedStrategicIndicators) {
                List<String> factors = new ArrayList<>();
                for (DTOFactor f : dtoDetailedStrategicIndicator.getFactors()) {
                    factors.add(f.getId());
                }
                Project project = projectsController.findProjectByExternalId(projectExternalId);
                Strategic_Indicator newSI = new Strategic_Indicator(dtoDetailedStrategicIndicator.getName(), "", null, factors, project);
                if (!strategicIndicatorRepository.existsByExternalIdAndProject_Id(newSI.getExternalId(), project.getId())) {
                    strategicIndicatorRepository.save(newSI);
                }
            }
        }
    }

    public List<DTOStrategicIndicatorEvaluation> simulateStrategicIndicatorsAssessment (Map<String, Float> factorsNameValueMap, String projectExternalId) throws IOException {
        List<DTOFactor> factors = qualityFactorsController.getAllFactorsEvaluation(projectExternalId);
        for (DTOFactor factor : factors) {
            if (factorsNameValueMap.containsKey(factor.getId())) {
                factor.setValue(factorsNameValueMap.get(factor.getId()));
            }
        }
        Iterable<Strategic_Indicator> listSI = strategicIndicatorRepository.findAll();
        List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
        for (Strategic_Indicator si : listSI) {
            Map<String,String> mapSIFactors = new HashMap<>();
            List<DTOFactor> listSIFactors = new ArrayList<>();
            for (String qfId : si.getQuality_factors()) {
                for (DTOFactor factor : factors) {
                    if (factor.getId().equals(qfId)) {
                        mapSIFactors.put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
                        listSIFactors.add(factor);
                    }
                }
            }
            if (si.getNetwork() != null && si.getNetwork().length > 10) {
                File tempFile = File.createTempFile("network", ".dne", null);
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(si.getNetwork());
                List<DTOSIAssessment> assessment = assesSI.AssesSI(si.getName().replaceAll("\\s+","").toLowerCase(), mapSIFactors, tempFile);
                float value = getValueAndLabelFromCategories(assessment).getFirst();
                result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+","").toLowerCase(),
                        si.getName(),
                        si.getDescription(),
                        Pair.of(value, getLabel(value)), assessment,
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
}
