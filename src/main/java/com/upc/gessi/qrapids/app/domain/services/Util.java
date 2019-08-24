package com.upc.gessi.qrapids.app.domain.services;


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import com.upc.gessi.qrapids.app.exceptions.AssessmentErrorException;
import com.upc.gessi.qrapids.app.exceptions.MissingParametersException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Math.abs;

@RestController
public class Util {

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private QMAQualityFactors qmaqf;

    @Autowired
    private QMAProjects qmaPrj;

    @Autowired
    private QMARelations qmaRelations;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private SICategoryRepository SICatRep;

    @Autowired
    private AssesSI AssesSI;

    @Value("${rawdata.dashboard}")
    private String rawdataDashboard;

    @Value("${pabre.url}")
    private String pabreUrl;

    @Value("${server.url}")
    private String serverUrl;

    @Autowired
    private Forecast forecast;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private Backlog backlog;

    @Autowired
    StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    QualityFactorsController qualityFactorsController;

    @PostMapping("/api/strategicIndicators")
    @ResponseStatus(HttpStatus.CREATED)
    public void newSI(HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String prj = request.getParameter("prj");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] file = null;
            if (network != null) {
                file = IOUtils.toByteArray(network.getInputStream());
            }
            List<String> qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            if (!name.equals("") && qualityFactors.size() > 0) {
                Project project = projectRepository.findByExternalId(prj);
                Strategic_Indicator newSI = new Strategic_Indicator(name, description, file, qualityFactors, project);
                siRep.save(newSI);
            }
            if (!strategicIndicatorsController.assessStrategicIndicator(name)) {
                throw new AssessmentErrorException();
            }
        } catch (AssessmentErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assessment error: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/api/strategicIndicators/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void editSI(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "network", required = false) MultipartFile network) {
        try {
            String name;
            String description;
            byte[] file = null;
            List<String> qualityFactors;
            try {
                name = request.getParameter("name");
                description = request.getParameter("description");
                if (network != null) {
                    file = IOUtils.toByteArray(network.getInputStream());
                }
                qualityFactors = Arrays.asList(request.getParameter("quality_factors").split(","));
            } catch (Exception e) {
                throw new MissingParametersException();
            }
            if (!name.equals("") && qualityFactors.size() > 0) {
                Optional<Strategic_Indicator> strategicIndicatorOptional = siRep.findById(id);
                if (strategicIndicatorOptional.isPresent()) {
                    Strategic_Indicator strategicIndicator = strategicIndicatorOptional.get();
                    List<String> strategicIndicatorQualityFactors = strategicIndicator.getQuality_factors();
                    boolean sameFactors = (strategicIndicatorQualityFactors.size() == qualityFactors.size());
                    int i = 0;
                    while (i < strategicIndicatorQualityFactors.size() && sameFactors) {
                        if (qualityFactors.indexOf(strategicIndicatorQualityFactors.get(i)) == -1)
                            sameFactors = false;
                        i++;
                    }

                    if (file != null && file.length > 10) strategicIndicator.setNetwork(file);
                    strategicIndicator.setName(name);
                    strategicIndicator.setDescription(description);
                    strategicIndicator.setQuality_factors(qualityFactors);
                    siRep.save(strategicIndicator);
                    if (!sameFactors) {
                        if (!strategicIndicatorsController.assessStrategicIndicator(name)) {
                            throw new AssessmentErrorException();
                        }
                    }
                }
            }
        } catch (MissingParametersException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing parameters in the request");
        } catch (AssessmentErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assessment error: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integrity violation: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/api/strategicIndicators/fetch")
    @ResponseStatus(HttpStatus.OK)
    public void fetchSIs() {
        try {
            List<String> projects = qmaPrj.getAssessedProjects();
            for(String projectName : projects) {
                Project project = projectRepository.findByExternalId(projectName);
                if (project == null) {
                    byte[] bytes = null;
                    project = new Project(projectName, projectName, "No description specified", bytes, true);
                    projectRepository.save(project);
                }
                List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicators = new ArrayList<>();
                try {
                    dtoDetailedStrategicIndicators = qmadsi.CurrentEvaluation(null, projectName);
                } catch (Exception e) {}
                for (DTODetailedStrategicIndicator d : dtoDetailedStrategicIndicators) {
                    List<String> factors = new ArrayList<>();
                    for (DTOFactor f : d.getFactors()) {
                        factors.add(f.getId());
                    }
                    Strategic_Indicator newSI = new Strategic_Indicator(d.getName(), "", null, factors, project);
                    if (!siRep.existsByExternalIdAndProject_Id(newSI.getExternalId(), project.getId())) {
                        siRep.save(newSI);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/api/strategicIndicators/simulate")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> Simulate(@RequestParam(value = "prj", required=false) String prj, HttpServletRequest request) {
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
            Iterable<Strategic_Indicator> listSI = siRep.findAll();
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
                    List<DTOSIAssessment> assessment = AssesSI.AssesSI(si.getName().replaceAll("\\s+","").toLowerCase(), mapSIFactors, tempFile);
                    float value = strategicIndicatorsController.getValueAndLabelFromCategories(assessment).getFirst();
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
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulation error: " + e.getMessage());
        }
    }

    @GetMapping("/api/rawdataDashboard")
    @ResponseStatus(HttpStatus.OK)
    public String RawDataDashboard() {
        return rawdataDashboard;
    }

    @GetMapping("/api/serverUrl")
    @ResponseStatus(HttpStatus.OK)
    public String serverUrl() {
        return "{\"serverUrl\":\""+serverUrl+"\"}";
    }

    public String getLabel(Float f) {
        Iterable<SICategory> siCategoryIterable = SICatRep.findAll();
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
        Iterable<SICategory> siCategoryIterable = SICatRep.findAll();
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

    @PostMapping("/api/createIssueTest")
    @ResponseStatus(HttpStatus.OK)
    public String addToBacklogUrl() {
        return "{\"issue_url\":\"https://essi.upc.edu/jira/issue/999\"," +
                "\"issue_id\":\"ID-999\"}";
    }

    @GetMapping("/api/milestonesTest")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMilestone> getMilestonesTest(@RequestParam("project_id") String projectId, @RequestParam(value = "date_from", required = false) String dateFrom) {
        List<DTOMilestone> milestoneList = new ArrayList<>();

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        milestoneList.add(new DTOMilestone(date.toString(), "Version 1.3", "Version 1.3 adding new features", "Release"));

        LocalDate date2 = LocalDate.now();
        date2 = date2.plusDays(20);
        milestoneList.add(new DTOMilestone(date2.toString(), "Version 1.4", "Version 1.4 adding new features", "Release"));

        LocalDate date3 = LocalDate.now();
        date3 = date3.plusDays(40);
        milestoneList.add(new DTOMilestone(date3.toString(), "Version 1.5", "Version 1.5 adding new features", "Release"));

        return milestoneList;
    }

    @GetMapping("/api/forecastTechniques")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getForecastTechniques() {
        return forecast.getForecastTechniques();
    }

    @GetMapping("/api/strategicIndicators/qualityModel")
    @ResponseStatus(HttpStatus.OK)
    public List<DTORelationsSI> getQualityModel(@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) {
        try {
            if (date == null)
                return qmaRelations.getRelations(prj, null);
            else
                return qmaRelations.getRelations(prj, LocalDate.parse(date));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("api/me")
    @ResponseStatus(HttpStatus.OK)
    public String getUserName (HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "{}";
        } else {
            return "{\"userName\":\"" + authentication.getName() + "\"}";
        }
    }

    @GetMapping("api/milestones")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOMilestone> getMilestones (@RequestParam("prj") String prj, @RequestParam(value = "date", required = false) String date) {
        Project project = projectRepository.findByExternalId(prj);
        if (project != null) {
            LocalDate localDate = null;
            if (date != null) {
                localDate = LocalDate.parse(date);
            }
            return backlog.getMilestones(project.getBacklogId(), localDate);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project does not exist");
        }
    }
}
