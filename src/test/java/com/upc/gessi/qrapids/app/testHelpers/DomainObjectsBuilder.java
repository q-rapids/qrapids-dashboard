package com.upc.gessi.qrapids.app.testHelpers;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODecisionQualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.presentation.rest.dto.relations.DTORelationsFactor;
import com.upc.gessi.qrapids.app.presentation.rest.dto.relations.DTORelationsMetric;
import com.upc.gessi.qrapids.app.presentation.rest.dto.relations.DTORelationsSI;
import org.springframework.data.util.Pair;
import qr.models.Classifier;
import qr.models.FixedPart;
import qr.models.Form;
import qr.models.Param;
import qr.models.QualityRequirementPattern;

import java.time.LocalDate;
import java.util.*;


public class DomainObjectsBuilder {

    public Project buildProject() {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "prj-1";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        project.setId(projectId);
        project.setBacklogId(projectBacklogId);
        return project;
    }

    public Alert buildAlert(Project project) {
        long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert = new Alert(idElement, name, alertType, value, threshold, category, date, alertStatus, true, project);
        alert.setId(alertId);
        return alert;
    }

    public Metric buildMetric(Project project){
        long metricId = 1L;
        String externalId = "duplication";
        String name = "Duplication";
        String description = "Percentage of files lying within a defined range of duplication density";
        float threshold = 0.5f;
        Metric metric = new Metric(externalId, name, description, project);
        metric.setThreshold(threshold);
        metric.setId(metricId);
        return metric;
    }

    // build Strategic Indicator without weights
    public Strategic_Indicator buildStrategicIndicator (Project project) {
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);
        strategicIndicator.setThreshold(0.5f);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        // define factor1 with its metric composition
        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code",project);
        metric1.setId(1L);
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", project);
        factor1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);
        // define si with factor1 union
        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        // define factor2 with its metric composition
        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", project);
        metric2.setId(2L);
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", project);
        factor2.setId(2L);
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);
        // define si with factor2 union
        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        // define factor3 with its metric composition
        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold",project);
        metric3.setId(3L);
        Factor factor3 =  new Factor("testingstatus", "Performance of testing phases", project);
        factor3.setId(3L);
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);
        // define si with factor3 union
        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors( factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        // finish define si with its factors composition
        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        return strategicIndicator;
    }

    public Strategic_Indicator buildStrategicIndicatorForSimulation (Project project) {
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        // define factor1 with its metric composition
        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code",project);
        metric1.setId(1L);
        Factor factor1 =  new Factor("testingperformance", "Performance of the tests", project);
        factor1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);
        // define si with factor1 union
        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);


        // finish define si with its factors composition
        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        return strategicIndicator;
    }

    // build Factor without weights
    public Factor buildFactor (Project project) {
        // define factor with its metric composition
        List<QualityFactorMetrics> qualityMetrics = new ArrayList<>();

        Factor factor =  new Factor("codequality", "Quality of the implemented code", project);
        factor.setId(1L);
        factor.setThreshold(0.3f);
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code",project);
        metric1.setId(1L);
        metric1.setThreshold(null);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor);
        qfm1.setId(1L);
        qualityMetrics.add(qfm1);
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", project);
        metric2.setId(2L);
        metric2.setThreshold(null);
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor);
        qfm1.setId(2L);
        qualityMetrics.add(qfm2);
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold",project);
        metric3.setId(3L);
        metric3.setThreshold(null);
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor);
        qfm1.setId(3L);
        qualityMetrics.add(qfm3);
        factor.setQualityFactorMetricsList(qualityMetrics);
        factor.setWeighted(false);

        return factor;
    }

    public Strategic_Indicator addFactorToStrategicIndicator (Strategic_Indicator si, Factor factor, float weight) {
        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", si.getProject());
        metric1.setId(1L);
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", si.getProject());
        factor1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);

        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, si);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);


        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", si.getProject());
        metric2.setId(2L);
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", si.getProject());
        factor2.setId(2L);
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);

        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, si);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);


        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", si.getProject());
        metric3.setId(3L);
        Factor factor3 =  new Factor("testingstatus", "Performance of testing phases", si.getProject());
        factor3.setId(3L);
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);

        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors(factor3, -1, si);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);



        Long siqf4Id = 4L;
        StrategicIndicatorQualityFactors siqf4 = new StrategicIndicatorQualityFactors(factor, weight, si);
        siqf4.setId(siqf4Id);
        qualityFactors.add(siqf4);

        si.setStrategicIndicatorQualityFactorsList(qualityFactors);
        return si;
    }

    public List<StrategicIndicatorQualityFactors> buildQualityFactors (Strategic_Indicator strategicIndicator) {

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        // define factor1 with its metric composition
        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", strategicIndicator.getProject());
        metric1.setId(1L);
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", strategicIndicator.getProject());
        factor1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);
        // define si with factor1 union
        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        // define factor2 with its metric composition
        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", strategicIndicator.getProject());
        metric2.setId(2L);
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", strategicIndicator.getProject());
        factor2.setId(2L);
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);
        // define si with factor2 union
        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        // define factor3 with its metric composition
        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", strategicIndicator.getProject());
        metric3.setId(3L);
        Factor factor3 =  new Factor("testingstatus", "Performance of testing phases", strategicIndicator.getProject());
        factor3.setId(3L);
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);
        // define si with factor3 union
        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors( factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        return qualityFactors;
    }

    public DTOStrategicIndicatorEvaluation buildDtoStrategicIndicatorEvaluation (Strategic_Indicator strategicIndicator) {
        List<DTOAssessment> dtoSIAssessmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOAssessment dtoSIAssessment1 = new DTOAssessment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOAssessment dtoSIAssessment2 = new DTOAssessment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOAssessment dtoSIAssessment3 = new DTOAssessment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment3);

        Float strategicIndicatorValue = 0.7f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicator.getExternalId(), strategicIndicator.getName(), strategicIndicator.getDescription(), strategicIndicatorValuePair, strategicIndicatorRationale, dtoSIAssessmentList, LocalDate.now(), datasource, strategicIndicator.getId(), categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);

        return dtoStrategicIndicatorEvaluation;
    }

    public QualityRequirementPattern buildQualityRequirementPattern () {
        Integer parameterId = 120;
        String parameterName = "value";
        String parameterDescription = "value in percentage";
        String parameterCorrectnessCondition = "Stay between 0 and 100";
        String parameterType = "integer";
        String parameterValue = null;
        Integer parameterMetricId = 172;
        String parameterMetricName = "Integer that represents a percentage";
        Param parameter = new Param(parameterId, parameterName, parameterDescription, parameterCorrectnessCondition, parameterType, parameterValue, parameterMetricId, parameterMetricName);
        List<Param> parameterList = new ArrayList<>();
        parameterList.add(parameter);
        String formText = "The ratio of files without duplications should be at least %value%";
        FixedPart fixedPart = new FixedPart(formText, parameterList);
        String formName = "Duplications";
        String formDescription = "The ratio of files without duplications should be at least the given value";
        String formComments = "No comments";
        Form form = new Form(formName, formDescription, formComments, fixedPart);
        List<Form> formList = new ArrayList<>();
        formList.add(form);
        Integer requirementId = 100;
        String requirementName = "Duplications";
        String requirementComments = "No comments";
        String requirementDescription = "No description";
        String requirementGoal = "Improve the quality of the source code";
        String requirementCostFunction = "No cost function";
        QualityRequirementPattern qualityRequirementPattern = new QualityRequirementPattern(requirementId, requirementName, requirementComments, requirementDescription, requirementGoal, formList, requirementCostFunction);

        return qualityRequirementPattern;
    }

    public Classifier buildClassifier() {
        Integer classifierId = 130;
        String classifierName = "commitresponsetime";
        List<Classifier> internalClassifierList = new ArrayList<>();
        List<QualityRequirementPattern> requirementPatternList = new ArrayList<>();
        Classifier classifier = new Classifier();
        classifier.setId(classifierId);
        classifier.setName(classifierName);
        classifier.setInternalClassifiers(internalClassifierList);
        classifier.setRequirementPatterns(requirementPatternList);

        return classifier;
    }

    public qr.models.Metric buildQRPatternsMetric() {
        Integer metricId = 172;
        String metricName = "Integer that represents a percentage";
        String metricDescription = "Integer value that can have a percentage.";
        String metricType = "integer";
        Float metricMinValue = 0f;
        Float metricMaxValue = 100f;
        List<String> possibleValuesList = new ArrayList<>();
        qr.models.Metric metric = new qr.models.Metric(metricId, metricName, metricType);
        metric.setDescription(metricDescription);
        metric.setMinValue(metricMinValue);
        metric.setMaxValue(metricMaxValue);
        metric.setPossibleValues(possibleValuesList);

        return metric;
    }

    public Decision buildDecision (Project project, DecisionType type) {
        Long decisionId = 2L;
        DecisionType decisionType = type;
        Date date = new Date();
        String rationale = "User comments";
        int patternId = 100;
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, project);
        decision.setId(decisionId);
        return decision;
    }

    public QualityRequirement buildQualityRequirement (Alert alert, Decision decision, Project project) {
        Long requirementId = 3L;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qualityRequirement.setId(requirementId);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);
        qualityRequirement.setBacklogId(qrBacklogId);
        return qualityRequirement;
    }

    public DTODecisionQualityRequirement buildDecisionWithQualityRequirement (QualityRequirement qualityRequirement) {
        return new DTODecisionQualityRequirement(qualityRequirement.getDecision().getId(),
                qualityRequirement.getDecision().getType(),
                qualityRequirement.getDecision().getDate(),
                null,
                qualityRequirement.getDecision().getRationale(),
                qualityRequirement.getDecision().getPatternId(),
                qualityRequirement.getRequirement(),
                qualityRequirement.getDescription(),
                qualityRequirement.getGoal(),
                qualityRequirement.getBacklogId(),
                qualityRequirement.getBacklogUrl());
    }

    public DTODecisionQualityRequirement buildDecisionWithoutQualityRequirement (Decision decision) {
        return new DTODecisionQualityRequirement(decision.getId(),
                decision.getType(),
                decision.getDate(),
                null,
                decision.getRationale(),
                decision.getPatternId(),
                null,
                null,
                null,
                null,
                null);
    }

    public DTODetailedFactorEvaluation buildDTOQualityFactor () {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";

        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        float metricValue = 0.8f;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        List<String> qualityFactors = new ArrayList<>();
        qualityFactors.add(factorId);
        DTOMetricEvaluation dtoMetricEvaluation = new DTOMetricEvaluation(metricId, metricName, metricDescription, null, metricRationale, qualityFactors, evaluationDate, metricValue);
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);

        return new DTODetailedFactorEvaluation(factorId, factorName, dtoMetricEvaluationList);
    }

    public DTODetailedFactorEvaluation buildDTOQualityFactorForPrediction () {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";

        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        String metricDataSource = "Forecast";
        Double metricValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "Forecast";
        List<String> qualityFactors = new ArrayList<>();
        qualityFactors.add(factorId);
        DTOMetricEvaluation dtoMetricEvaluation = new DTOMetricEvaluation(metricId, metricName, metricDescription, metricDataSource, metricRationale, qualityFactors, evaluationDate, metricValue.floatValue());
        Double first80 = 0.97473043;
        Double second80 = 0.9745246;
        Pair<Float, Float> confidence80 = Pair.of(first80.floatValue(), second80.floatValue());
        dtoMetricEvaluation.setConfidence80(confidence80);
        Double first95 = 0.9747849;
        Double second95 = 0.97447014;
        Pair<Float, Float> confidence95 = Pair.of(first95.floatValue(), second95.floatValue());
        dtoMetricEvaluation.setConfidence95(confidence95);
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);

        return new DTODetailedFactorEvaluation(factorId, factorName, dtoMetricEvaluationList);
    }

    public DTOFactorEvaluation buildDTOFactor () {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String factorDescription = "Performance of the tests";
        float factorValue = 0.8f;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        return new DTOFactorEvaluation(factorId, factorName, factorDescription, Pair.of(factorValue,"Good"), evaluationDate, null, factorRationale, strategicIndicatorsList);
    }

    public DTOMetricEvaluation buildDTOMetric () {
        String metricId = "fasttests";
        String metricName = "Fast Tests";
        String metricDescription = "Percentage of tests under the testing duration threshold";
        float metricValue = 0.8f;
        LocalDate evaluationDate = LocalDate.now();
        String metricRationale = "parameters: {...}, formula: ...";
        String factorId = "testingperformance";
        List<String> qualityFactors = new ArrayList<>();
        qualityFactors.add(factorId);
        return new DTOMetricEvaluation(metricId, metricName, metricDescription, null, metricRationale, qualityFactors, evaluationDate, metricValue);
    }

    public DTOStrategicIndicatorEvaluation buildDTOStrategicIndicatorEvaluation () {
        List<DTOAssessment> dtoSIAssessmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOAssessment dtoSIAssessment1 = new DTOAssessment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOAssessment dtoSIAssessment2 = new DTOAssessment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOAssessment dtoSIAssessment3 = new DTOAssessment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment3);

        String strategicIndicatorId = "processperformance";
        Long strategicIndicatorDbId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance of the processes involved in the development";
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicatorId, strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, strategicIndicatorRationale, dtoSIAssessmentList, date, datasource, strategicIndicatorDbId, categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);
        return dtoStrategicIndicatorEvaluation;
    }

    public List<SICategory> buildSICategoryList () {
        Long strategicIndicatorGoodCategoryId = 10L;
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        SICategory siGoodCategory = new SICategory(strategicIndicatorGoodCategoryName, strategicIndicatorGoodCategoryColor);
        siGoodCategory.setId(strategicIndicatorGoodCategoryId);

        Long strategicIndicatorNeutralCategoryId = 11L;
        String strategicIndicatorNeutralCategoryName = "Neutral";
        String strategicIndicatorNeutralCategoryColor = "#ff8000";
        SICategory siNeutralCategory = new SICategory(strategicIndicatorNeutralCategoryName, strategicIndicatorNeutralCategoryColor);
        siNeutralCategory.setId(strategicIndicatorNeutralCategoryId);

        Long strategicIndicatorBadCategoryId = 12L;
        String strategicIndicatorBadCategoryName = "Bad";
        String strategicIndicatorBadCategoryColor = "#ff0000";
        SICategory siBadCategory = new SICategory(strategicIndicatorBadCategoryName, strategicIndicatorBadCategoryColor);
        siBadCategory.setId(strategicIndicatorBadCategoryId);

        List<SICategory> siCategoryList = new ArrayList<>();
        siCategoryList.add(siGoodCategory);
        siCategoryList.add(siNeutralCategory);
        siCategoryList.add(siBadCategory);

        return siCategoryList;
    }

    public List<Map<String, String>> buildRawSICategoryList () {
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        Map<String, String> strategicIndicatorGoodCategory = new HashMap<>();
        strategicIndicatorGoodCategory.put("name", strategicIndicatorGoodCategoryName);
        strategicIndicatorGoodCategory.put("color", strategicIndicatorGoodCategoryColor);

        String strategicIndicatorNeutralCategoryName = "Neutral";
        String strategicIndicatorNeutralCategoryColor = "#ff8000";
        Map<String, String> strategicIndicatorNeutralCategory = new HashMap<>();
        strategicIndicatorNeutralCategory.put("name", strategicIndicatorNeutralCategoryName);
        strategicIndicatorNeutralCategory.put("color", strategicIndicatorNeutralCategoryColor);

        String strategicIndicatorBadCategoryName = "Bad";
        String strategicIndicatorBadCategoryColor = "#ff0000";
        Map<String, String> strategicIndicatorBadCategory = new HashMap<>();
        strategicIndicatorBadCategory.put("name", strategicIndicatorBadCategoryName);
        strategicIndicatorBadCategory.put("color", strategicIndicatorBadCategoryColor);

        List<Map<String, String>> strategicIndicatorCategoriesList = new ArrayList<>();
        strategicIndicatorCategoriesList.add(strategicIndicatorGoodCategory);
        strategicIndicatorCategoriesList.add(strategicIndicatorNeutralCategory);
        strategicIndicatorCategoriesList.add(strategicIndicatorBadCategory);

        return strategicIndicatorCategoriesList;
    }

    public List<QFCategory> buildFactorCategoryList () {
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorGoodCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorBadCategory);

        return factorCategoryList;
    }

    public List<Map<String,String>> buildRawFactorCategoryList () {
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> factorGoodCategory = new HashMap<>();
        factorGoodCategory.put("name", factorGoodCategoryName);
        factorGoodCategory.put("color", factorGoodCategoryColor);
        factorGoodCategory.put("upperThreshold", Float.toString(factorGoodCategoryUpperThreshold));

        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        Map<String, String> factorNeutralCategory = new HashMap<>();
        factorNeutralCategory.put("name", factorNeutralCategoryName);
        factorNeutralCategory.put("color", factorNeutralCategoryColor);
        factorNeutralCategory.put("upperThreshold", Float.toString(factorNeutralCategoryUpperThreshold));

        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        Map<String, String> factorBadCategory = new HashMap<>();
        factorBadCategory.put("name", factorBadCategoryName);
        factorBadCategory.put("color", factorBadCategoryColor);
        factorBadCategory.put("upperThreshold", Float.toString(factorBadCategoryUpperThreshold));

        List<Map<String, String>> factorCategoriesList = new ArrayList<>();
        factorCategoriesList.add(factorGoodCategory);
        factorCategoriesList.add(factorNeutralCategory);
        factorCategoriesList.add(factorBadCategory);

        return factorCategoriesList;
    }

    public List<MetricCategory> buildMetricCategoryList () {
        Long metricGoodCategoryId = 10L;
        String metricGoodCategoryName = "Good";
        String metricGoodCategoryColor = "#00ff00";
        float metricGoodCategoryUpperThreshold = 1f;
        MetricCategory metricGoodCategory = new MetricCategory(metricGoodCategoryName, metricGoodCategoryColor, metricGoodCategoryUpperThreshold);
        metricGoodCategory.setId(metricGoodCategoryId);

        Long metricNeutralCategoryId = 11L;
        String metricNeutralCategoryName = "Neutral";
        String metricNeutralCategoryColor = "#ff8000";
        float metricNeutralCategoryUpperThreshold = 0.67f;
        MetricCategory metricNeutralCategory = new MetricCategory(metricNeutralCategoryName, metricNeutralCategoryColor, metricNeutralCategoryUpperThreshold);
        metricNeutralCategory.setId(metricNeutralCategoryId);

        Long metricBadCategoryId = 12L;
        String metricBadCategoryName = "Bad";
        String metricBadCategoryColor = "#ff0000";
        float metricBadCategoryUpperThreshold = 0.33f;
        MetricCategory metricBadCategory = new MetricCategory(metricBadCategoryName, metricBadCategoryColor, metricBadCategoryUpperThreshold);
        metricBadCategory.setId(metricBadCategoryId);

        List<MetricCategory> metricCategoryList = new ArrayList<>();
        metricCategoryList.add(metricGoodCategory);
        metricCategoryList.add(metricNeutralCategory);
        metricCategoryList.add(metricBadCategory);

        return metricCategoryList;
    }

    public List<Map<String, String>> buildRawMetricCategoryList () {
        String metricGoodCategoryName = "Good";
        String metricGoodCategoryColor = "#00ff00";
        float metricGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> metricGoodCategory = new HashMap<>();
        metricGoodCategory.put("name", metricGoodCategoryName);
        metricGoodCategory.put("color", metricGoodCategoryColor);
        metricGoodCategory.put("upperThreshold", Float.toString(metricGoodCategoryUpperThreshold));

        String metricNeutralCategoryName = "Neutral";
        String metricNeutralCategoryColor = "#ff8000";
        float metricNeutralCategoryUpperThreshold = 0.67f;
        Map<String, String> metricNeutralCategory = new HashMap<>();
        metricNeutralCategory.put("name", metricNeutralCategoryName);
        metricNeutralCategory.put("color", metricNeutralCategoryColor);
        metricNeutralCategory.put("upperThreshold", Float.toString(metricNeutralCategoryUpperThreshold));

        String metricBadCategoryName = "Bad";
        String metricBadCategoryColor = "#ff0000";
        float metricBadCategoryUpperThreshold = 0.33f;
        Map<String, String> metricBadCategory = new HashMap<>();
        metricBadCategory.put("name", metricBadCategoryName);
        metricBadCategory.put("color", metricBadCategoryColor);
        metricBadCategory.put("upperThreshold", Float.toString(metricBadCategoryUpperThreshold));

        List<Map<String, String>> metricCategoriesList = new ArrayList<>();
        metricCategoriesList.add(metricGoodCategory);
        metricCategoriesList.add(metricNeutralCategory);
        metricCategoriesList.add(metricBadCategory);

        return metricCategoriesList;
    }

    public List<DTOAssessment> buildDTOSIAssessmentList () {
        List<DTOAssessment> dtoSIAssessmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = 0.5f;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOAssessment dtoSIAssessment1 = new DTOAssessment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = 0.3f;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOAssessment dtoSIAssessment2 = new DTOAssessment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = 0.2f;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOAssessment dtoSIAssessment3 = new DTOAssessment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment3);

        return dtoSIAssessmentList;
    }

    public List<DTORelationsSI> buildDTORelationsSI () {
        String metricId = "nonblockingfiles";
        String metricValue = "0.8";
        String metricWeight = "1";
        DTORelationsMetric dtoRelationsMetric = new DTORelationsMetric(metricId);
        dtoRelationsMetric.setWeightedValue(metricValue);
        dtoRelationsMetric.setWeight(metricWeight);
        List<DTORelationsMetric> dtoRelationsMetricList = new ArrayList<>();
        dtoRelationsMetricList.add(dtoRelationsMetric);

        String factorId = "blockingcode";
        String factorValue = "0.8";
        String factorWeight = "1";
        DTORelationsFactor dtoRelationsFactor = new DTORelationsFactor(factorId);
        dtoRelationsFactor.setWeightedValue(factorValue);
        dtoRelationsFactor.setWeight(factorWeight);
        dtoRelationsFactor.setMetrics(dtoRelationsMetricList);
        List<DTORelationsFactor> dtoRelationsFactorList = new ArrayList<>();
        dtoRelationsFactorList.add(dtoRelationsFactor);

        String strategicIndicatorId = "blocking";
        String strategicIndicatorValue = "0.8";
        String strategicIndicatorValueDescription = "Good (0.8)";
        String strategicIndicatorColor = "#00ff00";
        DTORelationsSI dtoRelationsSI = new DTORelationsSI(strategicIndicatorId);
        dtoRelationsSI.setValue(strategicIndicatorValue);
        dtoRelationsSI.setValueDescription(strategicIndicatorValueDescription);
        dtoRelationsSI.setColor(strategicIndicatorColor);
        dtoRelationsSI.setFactors(dtoRelationsFactorList);
        List<DTORelationsSI> dtoRelationsSIList = new ArrayList<>();
        dtoRelationsSIList.add(dtoRelationsSI);

        return dtoRelationsSIList;
    }

    public List<DTOMilestone> buildDTOMilestoneList () {
        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        String milestoneName = "Version 1.3";
        String milestoneDescription = "Version 1.3 adding new features";
        String milestoneType = "Release";
        List<DTOMilestone> milestoneList = new ArrayList<>();
        milestoneList.add(new DTOMilestone(date.toString(), milestoneName, milestoneDescription, milestoneType));
        return milestoneList;
    }

    public List<DTOPhase> buildDTOPhaseList () {
        LocalDate dateFrom = LocalDate.now().minusDays(15);
        LocalDate dateTo = LocalDate.now().plusDays(15);
        String phaseName = "Development";
        String phaseDescription = "Implementation of project functionalities";
        List<DTOPhase> phaseList = new ArrayList<>();
        phaseList.add(new DTOPhase(dateFrom.toString(), phaseName, phaseDescription, dateTo.toString()));
        return phaseList;
    }

    public DTOSICurrentHistoricEvaluation buildDTOSICurrentHistoricEvaluation() {
        List<DTOAssessment> dtoSIAssessmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOAssessment dtoSIAssessment1 = new DTOAssessment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOAssessment dtoSIAssessment2 = new DTOAssessment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOAssessment dtoSIAssessment3 = new DTOAssessment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssessmentList.add(dtoSIAssessment3);

        String strategicIndicatorId = "processperformance";
        Long strategicIndicatorDbId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance of the processes involved in the development";
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";
        DTOSICurrentHistoricEvaluation dtoSICurrentHistoricEvaluationEvaluation = new DTOSICurrentHistoricEvaluation(strategicIndicatorId,"Test", strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, strategicIndicatorDbId, strategicIndicatorRationale, dtoSIAssessmentList, date);


        return dtoSICurrentHistoricEvaluationEvaluation;

    }

    public DTOSICurrentHistoricEvaluation.DTOHistoricalData buildDTOHistoricalData() {
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";
        DTOSICurrentHistoricEvaluation.DTOHistoricalData dtoHistoricalData = new DTOSICurrentHistoricEvaluation.DTOHistoricalData(strategicIndicatorValuePair,strategicIndicatorRationale,date);
        return dtoHistoricalData;
    }
}
