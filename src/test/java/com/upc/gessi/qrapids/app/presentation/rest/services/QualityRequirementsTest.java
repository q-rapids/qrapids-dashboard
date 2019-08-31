package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.QRPatternsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityRequirementController;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import com.upc.gessi.qrapids.app.testHelpers.HelperFunctions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import qr.models.QualityRequirementPattern;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class QualityRequirementsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ProjectsController projectsDomainController;

    @Mock
    private QRPatternsController qrPatternsDomainController;

    @Mock
    private QualityRequirementController qualityRequirementDomainController;

    @InjectMocks
    private QualityRequirements qualityRequirements;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(qualityRequirements)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void ignoreQR() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        String rationale = "Not important";
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr/ignore")
                .param("prj", project.getExternalId())
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qrs/ignore-qr",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("rationale")
                                        .description("User rationale of the decision"),
                                parameterWithName("patternId")
                                        .description("Identifier of the ignored quality requirement pattern")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(qualityRequirementDomainController, times(1)).ignoreQualityRequirement(project, rationale, patternId);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void ignoreQRWrongProject() throws Exception {
        // Given
        String projectExternalId = "test";
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        String rationale = "Not important";
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/qr/ignore")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("qrs/ignore-qr-wrong-project",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQR() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(null, decision, project);
        int patternId = 100;
        when(qualityRequirementDomainController.addQualityRequirement(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, null, project)).thenReturn(qualityRequirement);


        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr")
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(qualityRequirement.getId().intValue())))
                .andExpect(jsonPath("$.date", is(decision.getDate().getTime())))
                .andExpect(jsonPath("$.requirement", is(qualityRequirement.getRequirement())))
                .andExpect(jsonPath("$.description", is(qualityRequirement.getDescription())))
                .andExpect(jsonPath("$.goal", is(qualityRequirement.getGoal())))
                .andExpect(jsonPath("$.backlogId", is(qualityRequirement.getBacklogId())))
                .andExpect(jsonPath("$.backlogUrl", is(qualityRequirement.getBacklogUrl())))
                .andExpect(jsonPath("$.backlogProjectId", is(nullValue())))
                .andExpect(jsonPath("$.alert", is(nullValue())))
                .andDo(document("qrs/add-qr",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("rationale")
                                        .description("User rationale of the decision"),
                                parameterWithName("patternId")
                                        .description("Identifier of the added quality requirement pattern"),
                                parameterWithName("requirement")
                                        .description("Text of the added quality requirement"),
                                parameterWithName("description")
                                        .description("Description of the added quality requirement"),
                                parameterWithName("goal")
                                        .description("Goal of the added quality requirement")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Identifier of the added quality requirement"),
                                fieldWithPath("date")
                                        .description("Quality requirement creation date"),
                                fieldWithPath("requirement")
                                        .description("Text of the added quality requirement"),
                                fieldWithPath("description")
                                        .description("Description of the added quality requirement"),
                                fieldWithPath("goal")
                                        .description("Goal of the added quality requirement"),
                                fieldWithPath("backlogId")
                                        .description("Quality requirement identifier inside the backlog"),
                                fieldWithPath("backlogUrl")
                                        .description("Link to the backlog issue containing the quality requirement"),
                                fieldWithPath("backlogProjectId")
                                        .description("Backlog identifier of the project containing the quality requirement"),
                                fieldWithPath("alert")
                                        .description("Alert object which caused the quality requirement addition")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(qualityRequirementDomainController, times(1)).addQualityRequirement(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, null, project);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRErrorOnBacklog() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(null, decision, project);
        int patternId = 100;
        when(qualityRequirementDomainController.addQualityRequirement(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, null, project)).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr")
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason(is("Error when saving the quality requirement in the backlog")))
                .andDo(document("qrs/add-qr-backlog-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(qualityRequirementDomainController, times(1)).addQualityRequirement(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, null, project);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRProjectNotFound () throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenThrow(new ProjectNotFoundException());

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(null, decision, project);
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/qr")
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("alerts/add-qr-project-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void getQRs() throws Exception {
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);

        List<QualityRequirement> qualityRequirementList = new ArrayList<>();
        qualityRequirementList.add(qualityRequirement);

        when(qualityRequirementDomainController.getAllQualityRequirementsForProject(project)).thenReturn(qualityRequirementList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qr")
                .param("prj", project.getExternalId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(qualityRequirement.getId().intValue())))
                .andExpect(jsonPath("$[0].date", is(decision.getDate().getTime())))
                .andExpect(jsonPath("$[0].requirement", is(qualityRequirement.getRequirement())))
                .andExpect(jsonPath("$[0].description", is(qualityRequirement.getDescription())))
                .andExpect(jsonPath("$[0].goal", is(qualityRequirement.getGoal())))
                .andExpect(jsonPath("$[0].backlogId", is(qualityRequirement.getBacklogId())))
                .andExpect(jsonPath("$[0].backlogUrl", is(qualityRequirement.getBacklogUrl())))
                .andExpect(jsonPath("$[0].backlogProjectId", is(project.getBacklogId())))
                .andExpect(jsonPath("$[0].alert.id", is(alert.getId().intValue())))
                .andExpect(jsonPath("$[0].alert.id_element", is(alert.getId_element())))
                .andExpect(jsonPath("$[0].alert.name", is(alert.getName())))
                .andExpect(jsonPath("$[0].alert.type", is(alert.getType().toString())))
                .andExpect(jsonPath("$[0].alert.value", is(HelperFunctions.getFloatAsDouble(alert.getValue()))))
                .andExpect(jsonPath("$[0].alert.threshold", is(HelperFunctions.getFloatAsDouble(alert.getThreshold()))))
                .andExpect(jsonPath("$[0].alert.category", is(alert.getCategory())))
                .andExpect(jsonPath("$[0].alert.date", is(alert.getDate().getTime())))
                .andExpect(jsonPath("$[0].alert.status", is(alert.getStatus().toString())))
                .andExpect(jsonPath("$[0].alert.reqAssociat", is(alert.isReqAssociat())))
                .andExpect(jsonPath("$[0].alert.artefacts", is(nullValue())))
                .andDo(document("qrs/get-all-qrs",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")
                        ),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Identifier of the added quality requirement"),
                                fieldWithPath("[].date")
                                        .description("Quality requirement creation date"),
                                fieldWithPath("[].requirement")
                                        .description("Text of the added quality requirement"),
                                fieldWithPath("[].description")
                                        .description("Description of the added quality requirement"),
                                fieldWithPath("[].goal")
                                        .description("Goal of the added quality requirement"),
                                fieldWithPath("[].backlogId")
                                        .description("Quality requirement identifier inside the backlog"),
                                fieldWithPath("[].backlogUrl")
                                        .description("Link to the backlog issue containing the quality requirement"),
                                fieldWithPath("[].backlogProjectId")
                                        .description("Backlog identifier of the project containing the quality requirement"),
                                fieldWithPath("[].alert.id")
                                        .description("Alert identifier"),
                                fieldWithPath("[].alert.id_element")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("[].alert.name")
                                        .description("Name of the element causing the alert"),
                                fieldWithPath("[].alert.type")
                                        .description("Type of element causing the alert (METRIC or FACTOR)"),
                                fieldWithPath("[].alert.value")
                                        .description("Current value of the element causing the alert"),
                                fieldWithPath("[].alert.threshold")
                                        .description("Minimum acceptable value for the element"),
                                fieldWithPath("[].alert.category")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("[].alert.date")
                                        .description("Generation date of the alert"),
                                fieldWithPath("[].alert.status")
                                        .description("Status of the alert (NEW, VIEWED or RESOLVED)"),
                                fieldWithPath("[].alert.reqAssociat")
                                        .description("The alert has or hasn't an associated quality requirement"),
                                fieldWithPath("[].alert.artefacts")
                                        .description("Alert artefacts")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(qualityRequirementDomainController, times(1)).getAllQualityRequirementsForProject(project);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void getQRsWrongProject () throws Exception {
        // Given
        String projectExternalId = "test";
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qr")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("qrs/get-all-qrs-wrong-project",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectsDomainController);

        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void getAllQRPatterns() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);
        when(qrPatternsDomainController.getAllPatterns()).thenReturn(qualityRequirementPatternList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatterns");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(qualityRequirementPattern.getId())))
                .andExpect(jsonPath("$[0].name", is(qualityRequirementPattern.getName())))
                .andExpect(jsonPath("$[0].comments", is(qualityRequirementPattern.getComments())))
                .andExpect(jsonPath("$[0].description", is(qualityRequirementPattern.getDescription())))
                .andExpect(jsonPath("$[0].goal", is(qualityRequirementPattern.getGoal())))
                .andExpect(jsonPath("$[0].forms[0].name", is(qualityRequirementPattern.getForms().get(0).getName())))
                .andExpect(jsonPath("$[0].forms[0].description", is(qualityRequirementPattern.getForms().get(0).getDescription())))
                .andExpect(jsonPath("$[0].forms[0].comments", is(qualityRequirementPattern.getForms().get(0).getComments())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.formText", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())))
                .andExpect(jsonPath("$[0].costFunction", is(qualityRequirementPattern.getCostFunction())))
                .andDo(document("qrs/get-all-qr-patterns",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Quality requirement identifier"),
                                fieldWithPath("[].name")
                                        .description("Quality requirement name"),
                                fieldWithPath("[].comments")
                                        .description("Quality requirement comments"),
                                fieldWithPath("[].description")
                                        .description("Quality requirement description"),
                                fieldWithPath("[].goal")
                                        .description("Quality requirement goal"),
                                fieldWithPath("[].forms[].name")
                                        .description("Suggested quality requirement name"),
                                fieldWithPath("[].forms[].description")
                                        .description("Suggested quality requirement description"),
                                fieldWithPath("[].forms[].comments")
                                        .description("Suggested quality requirement comments"),
                                fieldWithPath("[].forms[].fixedPart.formText")
                                        .description("Suggested quality requirement text"),
                                fieldWithPath("[].costFunction")
                                        .description("Suggested quality requirement cost function")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getAllPatterns();
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getQRPattern() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        when(qrPatternsDomainController.getOnePattern(qualityRequirementPattern.getId())).thenReturn(qualityRequirementPattern);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qrPatterns/{id}", qualityRequirementPattern.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(qualityRequirementPattern.getId())))
                .andExpect(jsonPath("$.name", is(qualityRequirementPattern.getName())))
                .andExpect(jsonPath("$.comments", is(qualityRequirementPattern.getComments())))
                .andExpect(jsonPath("$.description", is(qualityRequirementPattern.getDescription())))
                .andExpect(jsonPath("$.goal", is(qualityRequirementPattern.getGoal())))
                .andExpect(jsonPath("$.forms[0].name", is(qualityRequirementPattern.getForms().get(0).getName())))
                .andExpect(jsonPath("$.forms[0].description", is(qualityRequirementPattern.getForms().get(0).getDescription())))
                .andExpect(jsonPath("$.forms[0].comments", is(qualityRequirementPattern.getForms().get(0).getComments())))
                .andExpect(jsonPath("$.forms[0].fixedPart.formText", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())))
                .andExpect(jsonPath("$.costFunction", is(qualityRequirementPattern.getCostFunction())))
                .andDo(document("qrs/get-single-qr-pattern",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality requirement pattern identifier")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Quality requirement identifier"),
                                fieldWithPath("name")
                                        .description("Quality requirement name"),
                                fieldWithPath("comments")
                                        .description("Quality requirement comments"),
                                fieldWithPath("description")
                                        .description("Quality requirement description"),
                                fieldWithPath("goal")
                                        .description("Quality requirement goal"),
                                fieldWithPath("forms[].name")
                                        .description("Suggested quality requirement name"),
                                fieldWithPath("forms[].description")
                                        .description("Suggested quality requirement description"),
                                fieldWithPath("forms[].comments")
                                        .description("Suggested quality requirement comments"),
                                fieldWithPath("forms[].fixedPart.formText")
                                        .description("Suggested quality requirement text"),
                                fieldWithPath("costFunction")
                                        .description("Suggested quality requirement cost function")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getOnePattern(qualityRequirementPattern.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getMetricsForQRPattern() throws Exception {
        Integer patternId = 1;
        String metric = "comments";
        when(qrPatternsDomainController.getMetricForPattern(patternId)).thenReturn(metric);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qrPatterns/{id}/metric", patternId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metric", is(metric)))
                .andDo(document("qrs/get-pattern-metric",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality requirement pattern identifier")
                        ),
                        responseFields(
                                fieldWithPath("metric")
                                        .description("Metric identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getMetricForPattern(patternId);
        verifyNoMoreInteractions(qrPatternsDomainController);
    }
}