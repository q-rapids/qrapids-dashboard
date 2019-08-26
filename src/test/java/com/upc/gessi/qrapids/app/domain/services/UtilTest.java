package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.*;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsFactor;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsMetric;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UtilTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @Mock
    private SICategoryRepository siCategoryRepository;

    @Mock
    private QFCategoryRepository qfCategoryRepository;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Mock
    private Forecast forecast;

    @Mock
    private QMARelations qmaRelations;

    @Mock
    private AssesSI assesSI;

    @Mock
    private QMAProjects qmaProjects;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Backlog backlog;

    @Mock
    private StrategicIndicatorsController strategicIndicatorsController;

    @Mock
    private QualityFactorsController qualityFactorsController;

    @InjectMocks
    private Util utilController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(utilController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void getForecastTechniques() throws Exception {
        List<String> forecastingTechniques = new ArrayList<>();
        String technique1 = "PROPHET";
        forecastingTechniques.add(technique1);
        String technique2 = "ETS";
        forecastingTechniques.add(technique2);
        String technique3 = "NN";
        forecastingTechniques.add(technique3);

        when(forecast.getForecastTechniques()).thenReturn(forecastingTechniques);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/forecastTechniques");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is(technique1)))
                .andExpect(jsonPath("$[1]", is(technique2)))
                .andExpect(jsonPath("$[2]", is(technique3)))
                .andDo(document("forecast/techniques",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]")
                                        .description("Array with the forecasting techniques names"))
                ));

        // Verify mock interactions
        verify(forecast, times(1)).getForecastTechniques();
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getQualityModel() throws Exception {
        String metricId = "nonblockingfiles";
        String metricValue = "0.8";
        String metricWeight = "1";
        DTORelationsMetric dtoRelationsMetric = new DTORelationsMetric(metricId);
        dtoRelationsMetric.setValue(metricValue);
        dtoRelationsMetric.setWeight(metricWeight);
        List<DTORelationsMetric> dtoRelationsMetricList = new ArrayList<>();
        dtoRelationsMetricList.add(dtoRelationsMetric);

        String factorId = "blockingcode";
        String factorValue = "0.8";
        String factorWeight = "1";
        DTORelationsFactor dtoRelationsFactor = new DTORelationsFactor(factorId);
        dtoRelationsFactor.setValue(factorValue);
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

        String projectExternalId = "test";

        when(qmaRelations.getRelations(projectExternalId, null)).thenReturn(dtoRelationsSIList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityModel")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorId)))
                .andExpect(jsonPath("$[0].value", is(strategicIndicatorValue)))
                .andExpect(jsonPath("$[0].valueDescription", is(strategicIndicatorValueDescription)))
                .andExpect(jsonPath("$[0].color", is(strategicIndicatorColor)))
                .andExpect(jsonPath("$[0].factors", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].factors[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].factors[0].weight", is(factorWeight)))
                .andExpect(jsonPath("$[0].factors[0].metrics", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].weight", is(metricWeight)));

        // Verify mock interactions
        verify(qmaRelations, times(1)).getRelations(projectExternalId, null);
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void getQualityModelForDate() throws Exception {
        String metricId = "nonblockingfiles";
        String metricValue = "0.8";
        String metricWeight = "1";
        DTORelationsMetric dtoRelationsMetric = new DTORelationsMetric(metricId);
        dtoRelationsMetric.setValue(metricValue);
        dtoRelationsMetric.setWeight(metricWeight);
        List<DTORelationsMetric> dtoRelationsMetricList = new ArrayList<>();
        dtoRelationsMetricList.add(dtoRelationsMetric);

        String factorId = "blockingcode";
        String factorValue = "0.8";
        String factorWeight = "1";
        DTORelationsFactor dtoRelationsFactor = new DTORelationsFactor(factorId);
        dtoRelationsFactor.setValue(factorValue);
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

        String projectExternalId = "test";
        String date = "2019-07-07";

        when(qmaRelations.getRelations(projectExternalId, LocalDate.parse(date))).thenReturn(dtoRelationsSIList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityModel")
                .param("prj", projectExternalId)
                .param("date", date);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorId)))
                .andExpect(jsonPath("$[0].value", is(strategicIndicatorValue)))
                .andExpect(jsonPath("$[0].valueDescription", is(strategicIndicatorValueDescription)))
                .andExpect(jsonPath("$[0].color", is(strategicIndicatorColor)))
                .andExpect(jsonPath("$[0].factors", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].factors[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].factors[0].weight", is(factorWeight)))
                .andExpect(jsonPath("$[0].factors[0].metrics", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].weight", is(metricWeight)))
                .andDo(document("si/quality-model",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Date (yyyy-mm-dd) of the quality model evaluation")
                        ),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].value")
                                        .description("Strategic indicator assessment value"),
                                fieldWithPath("[].valueDescription")
                                        .description("Strategic indicator assessment value and category"),
                                fieldWithPath("[].color")
                                        .description("Strategic indicator category color"),
                                fieldWithPath("[].factors")
                                        .description("List with all the quality factors composing the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].weight")
                                        .description("Quality factor weight in the strategic indicator assessment"),
                                fieldWithPath("[].factors[].metrics")
                                        .description("List with all the metrics composing the quality factor"),
                                fieldWithPath("[].factors[].metrics[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].factors[].metrics[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].factors[].metrics[].weight")
                                        .description("Metric weight in the computation of the quality factor"))
                ));

        // Verify mock interactions
        verify(qmaRelations, times(1)).getRelations(projectExternalId, LocalDate.parse(date));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void getNextMilestones () throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        String projectBacklogId = "prj-1";
        project.setBacklogId(projectBacklogId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        String milestoneName = "Version 1.3";
        String milestoneDescription = "Version 1.3 adding new features";
        String milestoneType = "Release";
        DTOMilestone milestone = new DTOMilestone(date.toString(), milestoneName, milestoneDescription, milestoneType);

        List<DTOMilestone> milestoneList = new ArrayList<>();
        milestoneList.add(milestone);

        LocalDate now = LocalDate.now();

        when(backlog.getMilestones(project.getBacklogId(), now)).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId)
                .param("date", now.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(date.toString())))
                .andExpect(jsonPath("$[0].name", is(milestoneName)))
                .andExpect(jsonPath("$[0].description", is(milestoneDescription)))
                .andExpect(jsonPath("$[0].type", is(milestoneType)))
                .andDo(document("milestones/get-from-date",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Minimum milestone date (yyyy-mm-dd)")
                        ),
                        responseFields(
                                fieldWithPath("[].date")
                                        .description("Milestone date"),
                                fieldWithPath("[].name")
                                        .description("Milestone name"),
                                fieldWithPath("[].description")
                                        .description("Milestone description"),
                                fieldWithPath("[].type")
                                        .description("Milestone type"))
                ));
    }

    @Test
    public void getAllMilestones () throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        String projectBacklogId = "prj-1";
        project.setBacklogId(projectBacklogId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        String milestoneName = "Version 1.3";
        String milestoneDescription = "Version 1.3 adding new features";
        String milestoneType = "Release";
        List<DTOMilestone> milestoneList = new ArrayList<>();
        milestoneList.add(new DTOMilestone(date.toString(), milestoneName, milestoneDescription, milestoneType));

        when(backlog.getMilestones(project.getBacklogId(), null)).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(date.toString())))
                .andExpect(jsonPath("$[0].name", is(milestoneName)))
                .andExpect(jsonPath("$[0].description", is(milestoneDescription)))
                .andExpect(jsonPath("$[0].type", is(milestoneType)));
    }
}