package com.upc.gessi.qrapids.app.domain.services;


import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.*;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import evaluation.StrategicIndicator;
import org.springframework.dao.DataIntegrityViolationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;
import static java.time.temporal.ChronoUnit.DAYS;

@RestController
public class Util {

    @Autowired
    private QMAStrategicIndicators qmasi;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private QMAQualityFactors qmaqf;

    @Autowired
    private QMAMetrics qmam;

    @Autowired
    private QMAProjects qmaPrj;

    @Autowired
    private QMARelations qmaRelations;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private SICategoryRepository SICatRep;

    @Autowired
    private QFCategoryRepository QFCatRep;

    @Autowired
    private AssesSI AssesSI;

    @Value("${rawdata.dashboard}")
    private String rawdataDashboard;

    @Value("${pabre.url}")
    private String pabreUrl;

    @Value("${backlog.url}")
    private String backlogUrl;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${forecast.technique}")
    private String forecastTechnique;

    @Autowired
    private AlertRepository ari;

    @Autowired
    private DecisionRepository decisionRepository;

    private List<SICategory> allCats;

    @Autowired
    private Forecast forecast;


    @RequestMapping("/api/newCategories")
    public @ResponseBody
    void newCategories(HttpServletRequest request, HttpServletResponse response) {
        JsonParser parser = new JsonParser();
        JsonArray sic = parser.parse(request.getParameter("SICat")).getAsJsonArray();
        JsonArray qfc = parser.parse(request.getParameter("QFCat")).getAsJsonArray();
        try {
            if (sic.size() > 1 && qfc.size() > 1) {
                qmasi.newCategories(sic);
                qmaqf.newCategories(qfc);
            }
            allCats = SICatRep.findAll();
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
            qmasi.deleteAllCategories();
            qmaqf.deleteAllCategories();
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping(value = "/api/newStrategicIndicator", method = RequestMethod.POST)
    public @ResponseBody
    void newSI(HttpServletRequest request, HttpServletResponse response, @RequestParam("network") MultipartFile network) {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] file = IOUtils.toByteArray(network.getInputStream());
            List<String> qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            if (name != "" && file != null && qualityFactors.size() > 0) {
                Strategic_Indicator newSI = new Strategic_Indicator(name, description, file, qualityFactors);
                siRep.save(newSI);
            }
            if (AssessStrategicIndicator(name))
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            else
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping(value = "/api/EditStrategicIndicator/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Strategic_Indicator getEditSI(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        if (siRep.existsById(id)) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return siRep.getOne(id);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }



    @RequestMapping(value = "/api/EditStrategicIndicator/{id}", method = RequestMethod.POST)
    public @ResponseBody
    void editSI(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response, @RequestParam("network") MultipartFile network) throws IOException {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] file = IOUtils.toByteArray(network.getInputStream());
            List<String> qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            if (name != "" && file != null && qualityFactors.size() > 0) {
                Strategic_Indicator editSI = siRep.getOne(id);
                //TOdo: the equals is not working
                //boolean same_factors = editSI.getQuality_factors().equals(qualityFactors);
                List<String> si_quality_factors=editSI.getQuality_factors();
                boolean same_factors = (si_quality_factors.size()==qualityFactors.size());
                int i = 0;
                while (i<si_quality_factors.size() && same_factors) {
                    if (qualityFactors.indexOf(si_quality_factors.get(i))==-1)
                        same_factors = false;
                    i++;
                }

                if (file.length > 10) editSI.setNetwork(file);
                editSI.setName(name);
                editSI.setDescription(description);
                editSI.setQuality_factors(qualityFactors);
                siRep.flush();
                if (!same_factors)
                    if (AssessStrategicIndicator(name))
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    else
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (DataIntegrityViolationException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping("/api/fetchSIs")
    public @ResponseBody
    void fetchSIs(HttpServletResponse response) {
        if (siRep.count() == 0) {
            try {
                List<DTODetailedStrategicIndicator> dsi = qmadsi.CurrentEvaluation(null, null);
                for (DTODetailedStrategicIndicator d : dsi) {
                    List<String> factors = new ArrayList<>();
                    for (DTOFactor f : d.getFactors()) {
                        factors.add(f.getId());
                    }
                    Strategic_Indicator newSI = new Strategic_Indicator(d.getName(), "", null, factors);
                    siRep.save(newSI);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null) response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } else {
            if (response != null) response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private enum TrainType {
        NONE, ONE, ALL
    }

    @RequestMapping("/api/assessStrategicIndicators")
    public @ResponseBody
    void assesStrategicIndicators(@RequestParam(value = "prj", required=false) String prj,
                                  @RequestParam(value = "from", required=false) String from,
                                  @RequestParam(value = "train", required = false, defaultValue = "ONE") TrainType trainType,
                                  HttpServletRequest request, HttpServletResponse response) {
        boolean correct = true;

        try {

            if (from != null && !from.isEmpty()) {
                LocalDate dateFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                LocalDate dateTo= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                while (correct && dateFrom.compareTo(dateTo)<=0) {
                    correct = AssessDateStrategicIndicators(prj, dateFrom);
                    dateFrom = dateFrom.plusDays(1);
                }
            }
            else
                correct = AssessDateStrategicIndicators(prj, null);

            // Train forecast models
            if (trainType != TrainType.NONE) {
                String technique = null;
                if (trainType == TrainType.ONE) {
                    technique = forecastTechnique;
                }
                if (prj == null)
                    trainForecastModelsAllProjects(technique);
                else
                    trainForecastModelsSingleProject(prj, technique);
            }

            if (correct)
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            else
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void trainForecastModelsAllProjects(String technique) throws IOException, CategoriesException{
        List<String> projects = qmaPrj.getAssessedProjects();
        for (String prj: projects) {
            trainForecastModelsSingleProject(prj, technique);
        }
    }

    private void trainForecastModelsSingleProject(String project, String technique) throws IOException {
        List<DTOMetric> metrics = qmam.CurrentEvaluation(null, project);
        forecast.trainMetricForecast(metrics, "7", project, technique);

        List<DTOQualityFactor> factors = qmaqf.CurrentEvaluation(null, project);
        forecast.trainFactorForecast(factors, "7", project, technique);
    }


    private boolean AssessDateStrategicIndicators(String project, LocalDate evaluationDate) throws IOException, CategoriesException {
        boolean correct = true;


        // if there is no specific project as a parameter, all the projects are assessed
        if (project == null) {
            List<String> projects = qmaPrj.getAssessedProjects();
            int i=0;
            while (i<projects.size() && correct) {
                correct = AssessDateProjectStrategicIndicators(projects.get(i), evaluationDate);
                i++;
            }
        }
        else {
            correct = AssessDateProjectStrategicIndicators(project, evaluationDate);
        }
        return correct;
    }

    private boolean AssessDateProjectStrategicIndicators(String project, LocalDate evaluationDate) throws IOException, CategoriesException {
        Factors factors_qma= new Factors(); //factors list, each of them includes list of SI in which is involved
        List<DTOFactor> list_of_factors;

        // If we receive an evaluationData is because we are recomputing historical data. We need the factors for an
        // specific day, not the last evaluation
        if (evaluationDate == null) {
            evaluationDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            list_of_factors = qmaqf.getAllFactors(project);
        }
        else
            list_of_factors = qmaqf.getAllFactorsHistoricalData(project, evaluationDate, evaluationDate);
        factors_qma.setFactors(list_of_factors);

        return AssessProjectStrategicIndicators(evaluationDate, project, factors_qma);
    }

    // Function assessing the strategic indicators for a concrete Project, it returns a boolean indicating if the
    // assessment is computed correctly
    private boolean AssessProjectStrategicIndicators(LocalDate evaluationDate, String  project, Factors factorsQMA) throws IOException {
        // List of ALL the strategic indicators in the local database
        List<Strategic_Indicator> listSI = siRep.findAll();

/*        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluation_date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
*/
        boolean correct = true;

        // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
        factorsQMA.clearStrategicIndicatorsRelations(evaluationDate);

        // 2.- We will compute the evaluation values for the SIs, adding the corresponding relations to the factors
        //      used for these computation
        for (Strategic_Indicator si : listSI) {
            correct = AssessStrategicIndicator(evaluationDate, project, si, factorsQMA);
        }

        // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
        // the strategic indicators using them
        qmaqf.setFactorStrategicIndicatorRelation(factorsQMA.getFactors(), project);

        return correct;
    }

    // Current assessment for this SI in all the projects
    private boolean AssessStrategicIndicator(String name) throws IOException, CategoriesException {
        boolean correct = false;
        // Local date to be used as evaluation date
        Date input = new Date();
        LocalDate evaluation_date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Strategic Indicator
        Strategic_Indicator si = siRep.findByName(name);

        // All the factors' assessment from QMA external service
        Factors factors_qma= new Factors();

        // List of component, the SI is assessed for all the components
        List <String> projects = qmaPrj.getAssessedProjects();

        // We will compute the evaluation values for the SI for all the components
        for (String prj: projects) {
            // 1.- We need to remove old data from factor evaluations in the strategic_indicators relationship attribute
            factors_qma.setFactors(qmaqf.getAllFactors(prj));
            factors_qma.clearStrategicIndicatorsRelations(evaluation_date, name);

            correct = AssessStrategicIndicator(evaluation_date, prj, si, factors_qma);

            // 3. When all the strategic indicators is calculated, we need to update the factors with the information of
            // the strategic indicators using them
            qmaqf.setFactorStrategicIndicatorRelation(factors_qma.getFactors(), prj);
        }

        return correct;
    }

    private boolean AssessStrategicIndicator(LocalDate evaluationDate, String project, Strategic_Indicator strategicIndicator, Factors factorsQMA)
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
                    mapSIFactors.put(factor.getId(), getQFLabelFromValue(factor.getValue()));
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
            List<DTOSIAssesment> assessment = AssesSI.AssesSI(strategicIndicator.getExternalId(), mapSIFactors, tempFile);
            Pair<Float, String> valueAndLabel = getValueAndLabelFromCategories(assessment);
            if (!valueAndLabel.getFirst().isNaN()) {
                assessmentValueOrLabel = valueAndLabel.getSecond();
                // saving the SI's assessment
                if (!qmasi.setStrategicIndicatorValue(
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
                float value = AssesSI.AssesSI(listFactors_assessment_values, si_factors.size());
                assessmentValueOrLabel = String.valueOf(value);
                // saving the SI's assessment
                if (!qmasi.setStrategicIndicatorValue(
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
                labels.add(getQFLabelFromValue(dtoFactor.getValue()));
            }
            correct = saveFactorSIRelation(project, factorIds, strategicIndicator.getExternalId(), evaluationDate, weights, values, labels, assessmentValueOrLabel);
        }

        return correct;
    }

    private boolean saveFactorSIRelation (String prj, List<String> factorIds, String si, LocalDate evaluationDate, List<Float> weights, List<Float> factorValues, List<String> factorLabels, String siValueOrLabel) throws IOException {
        return qmaRelations.setStrategicIndicatorFactorRelation(prj, factorIds, si, evaluationDate, weights, factorValues, factorLabels, siValueOrLabel);
    }

    @RequestMapping("/api/Simulate")
    public @ResponseBody
    List<DTOStrategicIndicatorEvaluation> Simulate(@RequestParam(value = "prj", required=false) String prj, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<DTOFactor> factors = qmaqf.getAllFactors(prj);
            JsonParser parser = new JsonParser();
            JsonArray simFactors = parser.parse(request.getParameter("factors")).getAsJsonArray();
            for (DTOFactor factor : factors) {
                int i = 0;
                boolean found = false;
                while (i < simFactors.size() && !found) {
                    if (factor.getId().equals(simFactors.get(i).getAsJsonObject().getAsJsonPrimitive("id").getAsString())) {
                        factor.setValue(simFactors.get(i).getAsJsonObject().getAsJsonPrimitive("value").getAsFloat());
                        simFactors.remove(i);
                        found = true;
                    }
                    ++i;
                }
            }
            List<Strategic_Indicator> listSI = siRep.findAll();
            List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
            for (Strategic_Indicator si : listSI) {
                Map<String,String> mapSIFactors = new HashMap<>();
                List<DTOFactor> listSIFactors = new ArrayList<>();
                for (String qfId : si.getQuality_factors()) {
                    for (DTOFactor factor : factors) {
                        if (factor.getId().equals(qfId)) {
                            mapSIFactors.put(factor.getId(), getQFLabelFromValue(factor.getValue()));
                            listSIFactors.add(factor);
                        }
                    }
                }
                if (si.getNetwork() != null && si.getNetwork().length > 10) {
                    File tempFile = File.createTempFile("network", ".dne", null);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(si.getNetwork());
                    List<DTOSIAssesment> assessment = AssesSI.AssesSI(si.getName().replaceAll("\\s+","").toLowerCase(), mapSIFactors, tempFile);
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
                    float value = assesSI(listSIFactors);
                    result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+","").toLowerCase(),
                            si.getName(),
                            si.getDescription(),
                            Pair.of(value, getLabel(value)), getCategories(),
                            null,
                            "Simulation",
                            si.getId(),
                            "",
                            si.getNetwork() != null));
                }
            }
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/deleteCategories")
    public @ResponseBody
    void deleteCategories(HttpServletRequest request, HttpServletResponse response) {
        SICatRep.deleteAll();
        QFCatRep.deleteAll();
    }

    @RequestMapping("/api/rawdataDashboard")
    public @ResponseBody
    String RawDataDashboard(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return rawdataDashboard;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/serverUrl")
    public String serverUrl() {
        return "{\"serverUrl\":\""+serverUrl+"\"}";
    }

    public String getLabel(Float f) {
        allCats = SICatRep.findAll();
        if (f != null && allCats.size() > 0) {
            if (f < 1.0f)
                return allCats.get(allCats.size() - 1 - (int) (f * (float) allCats.size())).getName();
            else
                return allCats.get(0).getName();
        } else return "No Category";
    }

    public List<DTOSIAssesment> getCategories() {
        allCats = SICatRep.findAll();
        List<DTOSIAssesment> result = new ArrayList<>();
        float thresholds_interval = 1.0f/(float)allCats.size();
        float upperThreshold=1;
        for (SICategory c : allCats) {
            upperThreshold -=  thresholds_interval;
            result.add(new DTOSIAssesment(c.getId(), c.getName(), null, c.getColor(), abs((float)upperThreshold)));
        }
        return result;
    }

    public Pair<Float,String> getValueAndLabelFromCategories(final List<DTOSIAssesment> assessments) {
        Float max = -1.0f;
        Float maxIndex = -1.f;
        for (Float i = 0.f; i < assessments.size(); i++) {
            DTOSIAssesment assesment = assessments.get(i.intValue());
            if (max < assesment.getValue()) {
                max = assesment.getValue();
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
        List<SICategory> categories = SICatRep.findAll();
        Collections.reverse(categories);
        Float index = -1.f;
        for (Float i = 0.f; i < categories.size(); i++) {
            if (categories.get(i.intValue()).getName().equals(label))
                index = i;
        }
        return (index/categories.size() + (index+1)/categories.size())/2.0f;
    }

    public String getQFLabelFromValue(Float f) {
        List <QFCategory> QFCats = QFCatRep.findAllByOrderByUpperThresholdAsc();
        if (f != null) {
            for (QFCategory qfcat : QFCats) {
                if (f <= qfcat.getUpperThreshold())
                    return qfcat.getName();
            }
        }
        return "No Category";
    }

    public static float assesSI(List<DTOFactor> factors) {
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

    public static String buildDescriptiveLabelAndValue (Pair<Float, String> value) {
        String labelAndValue;

        String numeric_value;
        if (value.getFirst()==null)
            numeric_value="";
        else
            numeric_value = String.format("%.2f", value.getFirst());

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
        SICategory category = SICatRep.findByName(label);
        return category.getColor();
    }

    @RequestMapping("/api/addToBacklog")
    public String addToBacklogUrl() {
        return "{\"issue_url\":\"https://essi.upc.edu/jira/issue/999\"," +
                "\"issue_id\":\"ID-999\"}";
    }

    @RequestMapping("/api/ForecastTechniques")
    public List<String> getForecastTechniques() {
        return forecast.getForecastTechniques();
    }

    @GetMapping("/api/qualityModel")
    public List<DTORelationsSI> getQualityModel(@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) throws IOException {
        if (date == null)
            return qmaRelations.getRelations(prj, null);
        else
            return qmaRelations.getRelations(prj, LocalDate.parse(date));
    }

    @RequestMapping("api/me")
    public String getUserName (HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "{}";
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            return "{\"userName\":\"" + authentication.getName() + "\"}";
        }
    }
}
