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