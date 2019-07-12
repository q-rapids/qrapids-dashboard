package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.DecisionType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import qr.QRGenerator;
import qr.models.FixedPart;
import qr.models.Form;
import qr.models.QualityRequirementPattern;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DecisionsTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private QRGeneratorFactory qrGeneratorFactory;

    @Mock
    private QRRepository qrRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @InjectMocks
    private Decisions decisionsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(decisionsController)
                .build();
    }

    @Test
    public void getDecisions() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Requirement pattern setup
        String formText = "The ratio of files without duplications should be at least %value%";
        FixedPart fixedPart = new FixedPart(formText);
        String formName = "Duplications";
        String formDescription = "The ratio of files without duplications should be at least the given value";
        String formComments = "No comments";
        Form form = new Form(formName, formDescription, formComments, fixedPart);
        List<Form> formList = new ArrayList<>();
        formList.add(form);
        Integer requirementId = 1;
        String requirementName = "Duplications";
        String requirementComments = "No comments";
        String requirementDescription = "No description";
        String requirementGoal = "Improve the quality of the source code";
        String requirementCostFunction = "No cost function";
        QualityRequirementPattern qualityRequirementPattern = new QualityRequirementPattern(requirementId, requirementName, requirementComments, requirementDescription, requirementGoal, formList, requirementCostFunction);
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getAllQRPatterns()).thenReturn(qualityRequirementPatternList);

        Integer patternId = 1;
        List<Integer> patternIdList = new ArrayList<>();
        patternIdList.add(patternId);

        String metric = "duplication";
        Map<Integer, String> metrics = new HashMap<>();
        metrics.put(patternId, metric);

        when(qrGenerator.getMetricsForPatterns(patternIdList)).thenReturn(metrics);

        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // QR setup
        Long decisionId = 2L;
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        Date date = new Date();
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        DTODecisionQualityRequirement dtoDecisionQualityRequirement = new DTODecisionQualityRequirement(decisionId, decisionType, date, null, rationale, patternId, requirement, description, goal, qrBacklogId, qrBacklogUrl);
        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirementList = new ArrayList<>();
        dtoDecisionQualityRequirementList.add(dtoDecisionQualityRequirement);

        when(qrRepository.getAllDecisionsAndQRsByProject_Id(eq(projectId), any(Date.class), any(Date.class))).thenReturn(dtoDecisionQualityRequirementList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/decisions")
                .param("prj", projectExternalId)
                .param("qrs", "true");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(decisionId.intValue())))
                .andExpect(jsonPath("$[0].type", is(decisionType.toString())))
                .andExpect(jsonPath("$[0].date", is(date.getTime())))
                .andExpect(jsonPath("$[0].author", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(rationale)))
                .andExpect(jsonPath("$[0].patternId", is(patternId)))
                .andExpect(jsonPath("$[0].elementId", is(metric)))
                .andExpect(jsonPath("$[0].requirement", is(requirement)))
                .andExpect(jsonPath("$[0].description", is(description)))
                .andExpect(jsonPath("$[0].goal", is(goal)))
                .andExpect(jsonPath("$[0].backlogId", is(qrBacklogId)))
                .andExpect(jsonPath("$[0].backlogUrl", is(qrBacklogUrl)));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getAllQRPatterns();
        verify(qrGenerator, times(1)).getMetricsForPatterns(patternIdList);
        verifyNoMoreInteractions(qrGenerator);

        verify(qrRepository, times(1)).getAllDecisionsAndQRsByProject_Id(eq(projectId), any(Date.class), any(Date.class));
        verifyNoMoreInteractions(qrRepository);
    }

    @Test
    public void getDecisionsWithoutQRs() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        Long decisionId = 2L;
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        Date date = new Date();
        int patternId = 1;
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, project);
        decision.setId(decisionId);
        List<Decision> decisionList = new ArrayList<>();
        decisionList.add(decision);

        when(decisionRepository.findByProject_Id(projectId)).thenReturn(decisionList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/decisions")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(decisionId.intValue())))
                .andExpect(jsonPath("$[0].type", is(decisionType.toString())))
                .andExpect(jsonPath("$[0].date", is(date.getTime())))
                .andExpect(jsonPath("$[0].author", is("")))
                .andExpect(jsonPath("$[0].rationale", is(rationale)))
                .andExpect(jsonPath("$[0].patternId", is(patternId)))
                .andExpect(jsonPath("$[0].elementId", is(nullValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).findByProject_Id(projectId);
        verifyNoMoreInteractions(decisionRepository);
    }
}