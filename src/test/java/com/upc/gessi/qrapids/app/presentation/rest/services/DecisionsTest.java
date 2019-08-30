package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.DecisionsController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.DecisionType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODecisionQualityRequirement;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
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

public class DecisionsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ProjectsController projectsDomainController;

    @Mock
    private DecisionsController decisionsDomainController;

    @InjectMocks
    private Decisions decisionsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(decisionsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getDecisions() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(null, decision, project);
        DTODecisionQualityRequirement dtoDecisionQualityRequirement = domainObjectsBuilder.buildDecisionWithQualityRequirement(qualityRequirement);
        String metric = "duplications";
        dtoDecisionQualityRequirement.setElementId(metric);
        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirementList = new ArrayList<>();
        dtoDecisionQualityRequirementList.add(dtoDecisionQualityRequirement);
        when(decisionsDomainController.getAllDecisionsWithQRByProjectAndDates(eq(project), any(), any())).thenReturn(dtoDecisionQualityRequirementList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/decisions")
                .param("prj", project.getExternalId())
                .param("qrs", "true")
                .param("from", "2019-07-15")
                .param("to", "2019-08-01");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(decision.getId().intValue())))
                .andExpect(jsonPath("$[0].type", is(decision.getType().toString())))
                .andExpect(jsonPath("$[0].date", is(decision.getDate().getTime())))
                .andExpect(jsonPath("$[0].author", is(nullValue())))
                .andExpect(jsonPath("$[0].rationale", is(decision.getRationale())))
                .andExpect(jsonPath("$[0].patternId", is(decision.getPatternId())))
                .andExpect(jsonPath("$[0].elementId", is(metric)))
                .andExpect(jsonPath("$[0].requirement", is(qualityRequirement.getRequirement())))
                .andExpect(jsonPath("$[0].description", is(qualityRequirement.getDescription())))
                .andExpect(jsonPath("$[0].goal", is(qualityRequirement.getGoal())))
                .andExpect(jsonPath("$[0].backlogId", is(qualityRequirement.getBacklogId())))
                .andExpect(jsonPath("$[0].backlogUrl", is(qualityRequirement.getBacklogUrl())))
                .andDo(document("decisions/get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("qrs")
                                        .optional()
                                        .description("Indicates if the result must include the information about the quality requirements associated to the decisions"),
                                parameterWithName("from")
                                        .optional()
                                        .description("Starting date (yyyy-mm-dd) for the requested the period"),
                                parameterWithName("to")
                                        .optional()
                                        .description("Ending date (yyyy-mm-dd) for the requested the period")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Decision identifier"),
                                fieldWithPath("[].type")
                                        .description("Decision type (ADD or IGNORE)"),
                                fieldWithPath("[].date")
                                        .description("Decision creation date"),
                                fieldWithPath("[].author")
                                        .description("Name of the decision creator"),
                                fieldWithPath("[].rationale")
                                        .description("User rationale behind the decision"),
                                fieldWithPath("[].patternId")
                                        .description("Identifier of the quality requirement pattern being added or ignored"),
                                fieldWithPath("[].elementId")
                                        .description("Identifier of the element impacted by the quality requirement"),
                                fieldWithPath("[].requirement")
                                        .description("Text of the added quality requirement"),
                                fieldWithPath("[].description")
                                        .description("Description of the added quality requirement"),
                                fieldWithPath("[].goal")
                                        .description("Goal of the added quality requirement"),
                                fieldWithPath("[].backlogId")
                                        .description("Quality requirement identifier inside the backlog"),
                                fieldWithPath("[].backlogUrl")
                                        .description("Link to the backlog issue containing the quality requirement")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(decisionsDomainController, times(1)).getAllDecisionsWithQRByProjectAndDates(eq(project), any(), any());
        verifyNoMoreInteractions(decisionsDomainController);
    }

    @Test
    public void getDecisionsWithoutQRs() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        List<Decision> decisionList = new ArrayList<>();
        decisionList.add(decision);
        when(decisionsDomainController.getAllDecisionsByProject(project)).thenReturn(decisionList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/decisions")
                .param("prj", project.getExternalId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(decision.getId().intValue())))
                .andExpect(jsonPath("$[0].type", is(decision.getType().toString())))
                .andExpect(jsonPath("$[0].date", is(decision.getDate().getTime())))
                .andExpect(jsonPath("$[0].author", is("")))
                .andExpect(jsonPath("$[0].rationale", is(decision.getRationale())))
                .andExpect(jsonPath("$[0].patternId", is(decision.getPatternId())))
                .andExpect(jsonPath("$[0].elementId", is(nullValue())));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(decisionsDomainController, times(1)).getAllDecisionsByProject(project);
        verifyNoMoreInteractions(decisionsDomainController);
    }

    @Test
    public void getDecisionsProjectNotFound () throws Exception {
        // Given
        String projectExternalId = "missingProject";
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/decisions")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("decisions/get-all-project-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(decisionsDomainController);
    }
}