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
import org.mockito.ArgumentMatchers;
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
import qr.models.Classifier;
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
                                fieldWithPath("[].alert.valueDescription")
                                        .description("Category and value of the element causing the alert"),
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
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].id", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getId())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].name", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].description", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].correctnessCondition", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].metricId", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId())))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.parameters[0].metricName", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricName())))
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
                                fieldWithPath("[].forms[].fixedPart.parameters[].id")
                                        .description("Suggested quality requirement parameter id"),
                                fieldWithPath("[].forms[].fixedPart.parameters[].name")
                                        .description("Suggested quality requirement parameter name"),
                                fieldWithPath("[].forms[].fixedPart.parameters[].description")
                                        .description("Suggested quality requirement parameter description"),
                                fieldWithPath("[].forms[].fixedPart.parameters[].correctnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                fieldWithPath("[].forms[].fixedPart.parameters[].metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                fieldWithPath("[].forms[].fixedPart.parameters[].metricName")
                                        .description("Suggested quality requirement parameter metric name"),
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
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].id", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getId())))
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].name", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())))
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].description", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())))
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].correctnessCondition", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())))
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].metricId", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId())))
                .andExpect(jsonPath("$.forms[0].fixedPart.parameters[0].metricName", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricName())))
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
                                fieldWithPath("forms[].fixedPart.parameters[].id")
                                        .description("Suggested quality requirement parameter id"),
                                fieldWithPath("forms[].fixedPart.parameters[].name")
                                        .description("Suggested quality requirement parameter name"),
                                fieldWithPath("forms[].fixedPart.parameters[].description")
                                        .description("Suggested quality requirement parameter description"),
                                fieldWithPath("forms[].fixedPart.parameters[].correctnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                fieldWithPath("forms[].fixedPart.parameters[].metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                fieldWithPath("forms[].fixedPart.parameters[].metricName")
                                        .description("Suggested quality requirement parameter metric name"),
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

    @Test
    public void createQRPattern() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierPos = 0;
        List<Integer> classifierPatternsId = new ArrayList<>();
        when(qrPatternsDomainController.createPattern(ArgumentMatchers.any(QualityRequirementPattern.class), eq(classifier.getId()), eq(classifier.getName()), eq(classifierPos), eq(classifierPatternsId))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qrPatterns")
                .param("name", qualityRequirementPattern.getName())
                .param("goal", qualityRequirementPattern.getGoal())
                .param("description", qualityRequirementPattern.getForms().get(0).getDescription())
                .param("requirement", qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())
                .param("parameterName", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())
                .param("parameterDescription", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())
                .param("parameterCorrectnessCondition", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())
                .param("metricId", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId().toString())
                .param("classifierId", classifier.getId().toString())
                .param("classifierName", classifier.getName())
                .param("classifierPos", classifierPos.toString())
                .param("classifierPatterns", "");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qrs/add-qr-pattern",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Quality requirement pattern identifier"),
                                parameterWithName("goal")
                                        .description("Quality requirement goal"),
                                parameterWithName("description")
                                        .description("Suggested quality requirement description"),
                                parameterWithName("requirement")
                                        .description("Suggested quality requirement text"),
                                parameterWithName("parameterName")
                                        .description("Suggested quality requirement parameter name"),
                                parameterWithName("parameterDescription")
                                        .description("Suggested quality requirement parameter description"),
                                parameterWithName("parameterCorrectnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                parameterWithName("metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                parameterWithName("classifierId")
                                        .description("Identifier of classifier where new pattern will be included"),
                                parameterWithName("classifierName")
                                        .description("Name of classifier where new pattern will be included"),
                                parameterWithName("classifierPos")
                                        .description("Quality requirement pattern position inside classifier"),
                                parameterWithName("classifierPatterns")
                                        .description("Classifier requirement patterns identifiers excluding the new pattern")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).createPattern(ArgumentMatchers.any(QualityRequirementPattern.class), eq(classifier.getId()), eq(classifier.getName()), eq(classifierPos), eq(classifierPatternsId));
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void updateQRPattern() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierPos = 0;
        List<Integer> classifierPatternsId = new ArrayList<>();
        classifierPatternsId.add(qualityRequirementPattern.getId());
        when(qrPatternsDomainController.editPattern(eq(qualityRequirementPattern.getId()), ArgumentMatchers.any(QualityRequirementPattern.class), eq(classifier.getId()), eq(classifier.getName()), eq(classifierPos), eq(classifierPatternsId))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .put("/api/qrPatterns/{id}", qualityRequirementPattern.getId())
                .param("name", qualityRequirementPattern.getName())
                .param("goal", qualityRequirementPattern.getGoal())
                .param("description", qualityRequirementPattern.getForms().get(0).getDescription())
                .param("requirement", qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())
                .param("parameterName", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())
                .param("parameterDescription", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())
                .param("parameterCorrectnessCondition", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())
                .param("metricId", qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId().toString())
                .param("classifierId", classifier.getId().toString())
                .param("classifierName", classifier.getName())
                .param("classifierPos", classifierPos.toString())
                .param("classifierPatterns", qualityRequirementPattern.getId().toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/update-qr-pattern",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality requirement pattern identifier")
                        ),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Quality requirement pattern identifier"),
                                parameterWithName("goal")
                                        .description("Quality requirement goal"),
                                parameterWithName("description")
                                        .description("Suggested quality requirement description"),
                                parameterWithName("requirement")
                                        .description("Suggested quality requirement text"),
                                parameterWithName("parameterName")
                                        .description("Suggested quality requirement parameter name"),
                                parameterWithName("parameterDescription")
                                        .description("Suggested quality requirement parameter description"),
                                parameterWithName("parameterCorrectnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                parameterWithName("metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                parameterWithName("classifierId")
                                        .description("Identifier of classifier where pattern will be moved"),
                                parameterWithName("classifierName")
                                        .description("Name of classifier where pattern will be moved"),
                                parameterWithName("classifierPos")
                                        .description("Quality requirement pattern position inside classifier"),
                                parameterWithName("classifierPatterns")
                                        .description("Classifier requirement patterns identifiers, including the current pattern")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).editPattern(eq(qualityRequirementPattern.getId()), ArgumentMatchers.any(QualityRequirementPattern.class), eq(classifier.getId()), eq(classifier.getName()), eq(classifierPos), eq(classifierPatternsId));
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void deleteQRPattern() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/qrPatterns/{id}", qualityRequirementPattern.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/delete-qr-pattern",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality requirement pattern identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).deletePattern(qualityRequirementPattern.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getAllQRPatternsClassifiers() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);
        Classifier classifier2 = domainObjectsBuilder.buildClassifier();
        classifier2.setRequirementPatterns(qualityRequirementPatternList);
        List<Classifier> classifierList2 = new ArrayList<>();
        classifierList2.add(classifier2);
        Classifier classifier1 = domainObjectsBuilder.buildClassifier();
        classifier1.setInternalClassifiers(classifierList2);
        List<Classifier> classifierList1 = new ArrayList<>();
        classifierList1.add(classifier1);
        when(qrPatternsDomainController.getAllClassifiers()).thenReturn(classifierList1);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatternsClassifiers");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(classifier1.getId())))
                .andExpect(jsonPath("$[0].name", is(classifier1.getName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].id", is(classifier2.getId())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].name", is(classifier2.getName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].internalClassifiers", is(classifier2.getInternalClassifiers())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].id", is(qualityRequirementPattern.getId())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].name", is(qualityRequirementPattern.getName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].comments", is(qualityRequirementPattern.getComments())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].description", is(qualityRequirementPattern.getDescription())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].goal", is(qualityRequirementPattern.getGoal())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].name", is(qualityRequirementPattern.getForms().get(0).getName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].description", is(qualityRequirementPattern.getForms().get(0).getDescription())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].comments", is(qualityRequirementPattern.getForms().get(0).getComments())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.formText", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].id", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getId())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].name", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].description", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].correctnessCondition", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].metricId", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].metricName", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricName())))
                .andExpect(jsonPath("$[0].internalClassifiers[0].requirementPatterns[0].costFunction", is(qualityRequirementPattern.getCostFunction())))
                .andExpect(jsonPath("$[0].requirementPatterns", is(classifier1.getRequirementPatterns())))
                .andDo(document("qrs/get-all-qr-patterns-classifiers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Classifier identifier"),
                                fieldWithPath("[].name")
                                        .description("Classifier name"),
                                fieldWithPath("[].internalClassifiers[].id")
                                        .description("Internal classifier identifier"),
                                fieldWithPath("[].internalClassifiers[].name")
                                        .description("Internal classifier name"),
                                fieldWithPath("[].internalClassifiers[].internalClassifiers")
                                        .description("Internal classifiers of internal classifier"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].id")
                                        .description("Quality requirement identifier"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].name")
                                        .description("Quality requirement name"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].comments")
                                        .description("Quality requirement comments"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].description")
                                        .description("Quality requirement description"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].goal")
                                        .description("Quality requirement goal"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].name")
                                        .description("Suggested quality requirement name"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].description")
                                        .description("Suggested quality requirement description"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].comments")
                                        .description("Suggested quality requirement comments"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.formText")
                                        .description("Suggested quality requirement text"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].id")
                                        .description("Suggested quality requirement parameter id"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].name")
                                        .description("Suggested quality requirement parameter name"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].description")
                                        .description("Suggested quality requirement parameter description"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].correctnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].metricName")
                                        .description("Suggested quality requirement parameter metric name"),
                                fieldWithPath("[].internalClassifiers[].requirementPatterns[].costFunction")
                                        .description("Suggested quality requirement cost function"),
                                fieldWithPath("[].requirementPatterns")
                                        .description("Requirement patterns of classifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getAllClassifiers();
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getQRPatternsClassifier() throws Exception {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);
        Classifier classifier2 = domainObjectsBuilder.buildClassifier();
        classifier2.setRequirementPatterns(qualityRequirementPatternList);
        List<Classifier> classifierList2 = new ArrayList<>();
        classifierList2.add(classifier2);
        Classifier classifier1 = domainObjectsBuilder.buildClassifier();
        classifier1.setInternalClassifiers(classifierList2);
        when(qrPatternsDomainController.getOneClassifier(classifier1.getId())).thenReturn(classifier1);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qrPatternsClassifiers/{id}", classifier1.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(classifier1.getId())))
                .andExpect(jsonPath("$.name", is(classifier1.getName())))
                .andExpect(jsonPath("$.internalClassifiers[0].id", is(classifier2.getId())))
                .andExpect(jsonPath("$.internalClassifiers[0].name", is(classifier2.getName())))
                .andExpect(jsonPath("$.internalClassifiers[0].internalClassifiers", is(classifier2.getInternalClassifiers())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].id", is(qualityRequirementPattern.getId())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].name", is(qualityRequirementPattern.getName())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].comments", is(qualityRequirementPattern.getComments())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].description", is(qualityRequirementPattern.getDescription())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].goal", is(qualityRequirementPattern.getGoal())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].name", is(qualityRequirementPattern.getForms().get(0).getName())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].description", is(qualityRequirementPattern.getForms().get(0).getDescription())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].comments", is(qualityRequirementPattern.getForms().get(0).getComments())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.formText", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].id", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getId())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].name", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getName())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].description", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getDescription())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].correctnessCondition", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getCorrectnessCondition())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].metricId", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricId())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].forms[0].fixedPart.parameters[0].metricName", is(qualityRequirementPattern.getForms().get(0).getFixedPart().getParameters().get(0).getMetricName())))
                .andExpect(jsonPath("$.internalClassifiers[0].requirementPatterns[0].costFunction", is(qualityRequirementPattern.getCostFunction())))
                .andExpect(jsonPath("$.requirementPatterns", is(classifier1.getRequirementPatterns())))
                .andDo(document("qrs/get-single-qr-patterns-classifier",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Classifier identifier")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Classifier identifier"),
                                fieldWithPath("name")
                                        .description("Classifier name"),
                                fieldWithPath("internalClassifiers[].id")
                                        .description("Internal classifier identifier"),
                                fieldWithPath("internalClassifiers[].name")
                                        .description("Internal classifier name"),
                                fieldWithPath("internalClassifiers[].internalClassifiers")
                                        .description("Internal classifiers of internal classifier"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].id")
                                        .description("Quality requirement identifier"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].name")
                                        .description("Quality requirement name"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].comments")
                                        .description("Quality requirement comments"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].description")
                                        .description("Quality requirement description"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].goal")
                                        .description("Quality requirement goal"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].name")
                                        .description("Suggested quality requirement name"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].description")
                                        .description("Suggested quality requirement description"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].comments")
                                        .description("Suggested quality requirement comments"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.formText")
                                        .description("Suggested quality requirement text"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].id")
                                        .description("Suggested quality requirement parameter id"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].name")
                                        .description("Suggested quality requirement parameter name"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].description")
                                        .description("Suggested quality requirement parameter description"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].correctnessCondition")
                                        .description("Suggested quality requirement parameter correctness condition"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].metricId")
                                        .description("Suggested quality requirement parameter metric id"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].forms[].fixedPart.parameters[].metricName")
                                        .description("Suggested quality requirement parameter metric name"),
                                fieldWithPath("internalClassifiers[].requirementPatterns[].costFunction")
                                        .description("Suggested quality requirement cost function"),
                                fieldWithPath("requirementPatterns")
                                        .description("Requirement patterns of classifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getOneClassifier(classifier1.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void createQRPatternsClassifier() throws Exception {
        // Given
        String classifierName = "commitresponsetime";
        Integer parentClassifierId = 129;

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qrPatternsClassifiers")
                .param("name", classifierName)
                .param("parentClassifier", parentClassifierId.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qrs/add-qr-patterns-classifier",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Classifier name"),
                                parameterWithName("parentClassifier")
                                        .description("Parent classifier identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).createClassifier(classifierName, parentClassifierId);
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void updateQRPatternsClassifier() throws Exception {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierOldParentId = 123;
        Integer classifierNewParentId = 129;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .put("/api/qrPatternsClassifiers/{id}", classifier.getId())
                .param("name", classifier.getName())
                .param("oldParentClassifier", classifierOldParentId.toString())
                .param("parentClassifier", classifierNewParentId.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/update-qr-patterns-classifier",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Classifier identifier")
                        ),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Classifier name"),
                                parameterWithName("oldParentClassifier")
                                        .description("Old parent classifier identifier"),
                                parameterWithName("parentClassifier")
                                        .description("New parent classifier identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).updateClassifier(classifier.getId(), classifier.getName(), classifierOldParentId, classifierNewParentId);
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void deleteQRPatternsClassifier() throws Exception {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/qrPatternsClassifiers/{id}", classifier.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/delete-qr-patterns-classifier",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Classifier identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).deleteClassifier(classifier.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getAllQRPatternsMetrics() throws Exception {
        // Given
        qr.models.Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        List<qr.models.Metric> metricList = new ArrayList<>();
        metricList.add(metric);
        when(qrPatternsDomainController.getAllMetrics()).thenReturn(metricList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatternsMetrics");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(metric.getId())))
                .andExpect(jsonPath("$[0].name", is(metric.getName())))
                .andExpect(jsonPath("$[0].description", is(metric.getDescription())))
                .andExpect(jsonPath("$[0].type", is(metric.getType())))
                .andExpect(jsonPath("$[0].minValue", is(metric.getMinValue().doubleValue())))
                .andExpect(jsonPath("$[0].maxValue", is(metric.getMaxValue().doubleValue())))
                .andExpect(jsonPath("$[0].possibleValues", is(nullValue())))
                .andDo(document("qrs/get-all-qr-patterns-metrics",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].name")
                                        .description("Metric name"),
                                fieldWithPath("[].description")
                                        .description("Metric description"),
                                fieldWithPath("[].type")
                                        .description("Metric type"),
                                fieldWithPath("[].minValue")
                                        .description("Metric minimum value, if type is integer or float"),
                                fieldWithPath("[].maxValue")
                                        .description("Metric maximum value, if type is integer or float"),
                                fieldWithPath("[].possibleValues")
                                        .description("Metric possible values, if type is domain")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getAllMetrics();
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getQRPatternsMetric() throws Exception {
        // Given
        qr.models.Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        when(qrPatternsDomainController.getOneMetric(metric.getId())).thenReturn(metric);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/qrPatternsMetrics/{id}", metric.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(metric.getId())))
                .andExpect(jsonPath("$.name", is(metric.getName())))
                .andExpect(jsonPath("$.description", is(metric.getDescription())))
                .andExpect(jsonPath("$.type", is(metric.getType())))
                .andExpect(jsonPath("$.minValue", is(metric.getMinValue().doubleValue())))
                .andExpect(jsonPath("$.maxValue", is(metric.getMaxValue().doubleValue())))
                .andExpect(jsonPath("$.possibleValues", is(nullValue())))
                .andDo(document("qrs/get-single-qr-patterns-metric",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Quality requirement pattern identifier")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Metric identifier"),
                                fieldWithPath("name")
                                        .description("Metric name"),
                                fieldWithPath("description")
                                        .description("Metric description"),
                                fieldWithPath("type")
                                        .description("Metric type"),
                                fieldWithPath("minValue")
                                        .description("Metric minimum value, if type is integer or float"),
                                fieldWithPath("maxValue")
                                        .description("Metric maximum value, if type is integer or float"),
                                fieldWithPath("possibleValues")
                                        .description("Metric possible values, if type is domain")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).getOneMetric(metric.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void createQRPatternsMetric() throws Exception {
        // Given
        qr.models.Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        when(qrPatternsDomainController.createMetric(ArgumentMatchers.any(qr.models.Metric.class))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qrPatternsMetrics")
                .param("name", metric.getName())
                .param("description", metric.getDescription())
                .param("type", metric.getType())
                .param("minValue", metric.getMinValue().toString())
                .param("maxValue", metric.getMaxValue().toString())
                .param("possibleValues", "");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qrs/add-qr-patterns-metric",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Metric name"),
                                parameterWithName("description")
                                        .description("Metric description"),
                                parameterWithName("type")
                                        .description("Metric type (integer, float, string, time or domain)"),
                                parameterWithName("minValue")
                                        .description("Metric minimum value, if type is integer or float"),
                                parameterWithName("maxValue")
                                        .description("Metric maximum value, if type is integer or float"),
                                parameterWithName("possibleValues")
                                        .description("Metric possible values, if type is domain")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).createMetric(ArgumentMatchers.any(qr.models.Metric.class));
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void updateQRPatternsMetric() throws Exception {
        // Given
        qr.models.Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        when(qrPatternsDomainController.updateMetric(eq(metric.getId()), ArgumentMatchers.any(qr.models.Metric.class))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .put("/api/qrPatternsMetrics/{id}", metric.getId())
                .param("name", metric.getName())
                .param("description", metric.getDescription())
                .param("type", metric.getType())
                .param("minValue", metric.getMinValue().toString())
                .param("maxValue", metric.getMaxValue().toString())
                .param("possibleValues", "");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/update-qr-patterns-metric",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Metric identifier")
                        ),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Metric name"),
                                parameterWithName("description")
                                        .description("Metric description"),
                                parameterWithName("type")
                                        .description("Metric type (integer, float, string, time or domain)"),
                                parameterWithName("minValue")
                                        .description("Metric minimum value, if type is integer or float"),
                                parameterWithName("maxValue")
                                        .description("Metric maximum value, if type is integer or float"),
                                parameterWithName("possibleValues")
                                        .description("Metric possible values, if type is domain")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).updateMetric(eq(metric.getId()), ArgumentMatchers.any(qr.models.Metric.class));
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void deleteQRPatternsMetric() throws Exception {
        // Given
        qr.models.Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        when(qrPatternsDomainController.deleteMetric(metric.getId())).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/qrPatternsMetrics/{id}", metric.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("qrs/delete-qr-patterns-metric",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Metric identifier")
                        )
                ));

        // Verify mock interactions
        verify(qrPatternsDomainController, times(1)).deleteMetric(metric.getId());
        verifyNoMoreInteractions(qrPatternsDomainController);
    }
}