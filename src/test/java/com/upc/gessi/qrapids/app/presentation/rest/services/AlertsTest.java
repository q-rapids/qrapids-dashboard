package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upc.gessi.qrapids.app.domain.controllers.AlertsController;
import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.QRPatternsController;
import com.upc.gessi.qrapids.app.domain.controllers.QualityRequirementController;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.exceptions.AlertNotFoundException;
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
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class AlertsTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private SimpMessagingTemplate simpleMessagingTemplate;

    @Mock
    private ProjectsController projectsDomainController;

    @Mock
    private AlertsController alertsDomainController;

    @Mock
    private QRPatternsController qrPatternsDomainController;

    @Mock
    private QualityRequirementController qualityRequirementDomainController;

    @InjectMocks
    private Alerts alertsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(alertsController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getAllAlerts() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);
        when(alertsDomainController.getAlerts(project)).thenReturn(alertList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts")
                .param("prj", project.getExternalId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(alert.getId().intValue())))
                .andExpect(jsonPath("$[0].id_element", is(alert.getId_element())))
                .andExpect(jsonPath("$[0].name", is(alert.getName())))
                .andExpect(jsonPath("$[0].type", is(alert.getType().toString())))
                .andExpect(jsonPath("$[0].value", is(HelperFunctions.getFloatAsDouble(alert.getValue()))))
                .andExpect(jsonPath("$[0].threshold", is(HelperFunctions.getFloatAsDouble(alert.getThreshold()))))
                .andExpect(jsonPath("$[0].category", is(alert.getCategory())))
                .andExpect(jsonPath("$[0].date", is(alert.getDate().getTime())))
                .andExpect(jsonPath("$[0].status", is(alert.getStatus().toString())))
                .andExpect(jsonPath("$[0].reqAssociat", is(alert.isReqAssociat())))
                .andExpect(jsonPath("$[0].artefacts", is(nullValue())))
                .andDo(document("alerts/get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Alert identifier"),
                                fieldWithPath("[].id_element")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("[].name")
                                        .description("Name of the element causing the alert"),
                                fieldWithPath("[].type")
                                        .description("Type of element causing the alert (METRIC or FACTOR)"),
                                fieldWithPath("[].value")
                                        .description("Current value of the element causing the alert"),
                                fieldWithPath("[].threshold")
                                        .description("Minimum acceptable value for the element"),
                                fieldWithPath("[].category")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("[].date")
                                        .description("Generation date of the alert"),
                                fieldWithPath("[].status")
                                        .description("Status of the alert (NEW, VIEWED or RESOLVED)"),
                                fieldWithPath("[].reqAssociat")
                                        .description("The alert has or hasn't an associated quality requirement"),
                                fieldWithPath("[].artefacts")
                                        .description("Alert artefacts")
                        )
                ));

        // Verify mock interactions
        verify(alertsDomainController, times(1)).getAlerts(project);
        verify(alertsDomainController, times(1)).setViewedStatusForAlerts(alertList);
        verifyNoMoreInteractions(alertsDomainController);
    }

    @Test
    public void getAllAlertsWrongProject() throws Exception {
        // Given
        String projectExternalId = "test";
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("alerts/get-all-wrong-project",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(alertsDomainController);
    }

    @Test
    public void countNewAlerts() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);
        Long newAlerts = 2L;
        Long newAlertWithQR = 1L;
        when(alertsDomainController.countNewAlerts(project)).thenReturn(Pair.of(newAlerts, newAlertWithQR));

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/countNew")
                .param("prj", project.getExternalId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAlerts", is(newAlerts.intValue())))
                .andExpect(jsonPath("$.newAlertsWithQR", is(newAlertWithQR.intValue())))
                .andDo(document("alerts/count-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier")),
                        responseFields(
                                fieldWithPath("newAlerts")
                                        .description("Number of new alerts"),
                                fieldWithPath("newAlertsWithQR")
                                        .description("Number of new alerts with an associated quality requirement")
                        )
                ));

        // Verify mock interactions
        verify(alertsDomainController, times(1)).countNewAlerts(project);
        verifyNoMoreInteractions(alertsDomainController);
    }

    @Test
    public void countNewAlertsWrongProject() throws Exception {
        // Given
        String projectExternalId = "test";
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/countNew")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("alerts/count-new-wrong-project",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectsDomainController);

        verifyZeroInteractions(alertsDomainController);
    }

    @Test
    public void getQRPatternForAlert() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertsDomainController.getAlertById(alert.getId())).thenReturn(alert);

        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);

        when(qrPatternsDomainController.getPatternsForAlert(alert)).thenReturn(qualityRequirementPatternList);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/alerts/{id}/qrPatterns", alert.getId());

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
                .andDo(document("alerts/get-qr-patterns",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Alert identifier")
                        ),
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
        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verify(qrPatternsDomainController, times(1)).getPatternsForAlert(alert);
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getQRPatternForAlertNotFound() throws Exception {
        // Given
        long alertId = 1L;
        when(alertsDomainController.getAlertById(alertId)).thenThrow(new AlertNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/{id}/qrPatterns", alertId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(status().reason(is("Alert not found")))
                .andDo(document("alerts/get-qr-patterns-alert-not-found",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(qrPatternsDomainController);
    }

    @Test
    public void getAlertDecision() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        alert.setDecision(decision);
        when(alertsDomainController.getAlertById(alert.getId())).thenReturn(alert);

        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        when(qualityRequirementDomainController.getQualityRequirementForDecision(decision)).thenReturn(qualityRequirement);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/alerts/{id}/decision", alert.getId());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrGoal", is(qualityRequirement.getGoal())))
                .andExpect(jsonPath("$.qrRequirement", is(qualityRequirement.getRequirement())))
                .andExpect(jsonPath("$.qrDescription", is(qualityRequirement.getDescription())))
                .andExpect(jsonPath("$.qrBacklogUrl", is(qualityRequirement.getBacklogUrl())))
                .andExpect(jsonPath("$.decisionType", is(decision.getType().toString())))
                .andExpect(jsonPath("$.decisionRationale", is(decision.getRationale())))
                .andDo(document("alerts/get-decision",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Alert identifier")
                        ),
                        responseFields(
                                fieldWithPath("qrGoal")
                                        .description("Quality requirement goal"),
                                fieldWithPath("qrRequirement")
                                        .description("Quality requirement text"),
                                fieldWithPath("qrDescription")
                                        .description("Quality requirement description"),
                                fieldWithPath("qrBacklogUrl")
                                        .description("Link to the backlog issue containing the quality requirement"),
                                fieldWithPath("decisionType")
                                        .description("Type of the decision (ADD or IGNORE)"),
                                fieldWithPath("decisionRationale")
                                        .description("User rationale of the decision")
                        )
                ));

        // Verify mock interactions
        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verify(qualityRequirementDomainController, times(1)).getQualityRequirementForDecision(decision);
        verifyNoMoreInteractions(qrPatternsDomainController);
    }

    @Test
    public void getAlertDecisionAlertNotFound() throws Exception {
        long alertId = 1L;
        when(alertsDomainController.getAlertById(alertId)).thenThrow(new AlertNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/{id}/decision", alertId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(status().reason(is("Alert not found")))
                .andDo(document("alerts/get-decision-alert-not-found",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(qualityRequirementDomainController);
        verifyZeroInteractions(qrPatternsDomainController);
    }

    @Test
    public void ignoreAlert() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);
        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertsDomainController.getAlertById(alert.getId())).thenReturn(alert);
        String rationale = "Not important";
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr/ignore", alert.getId())
                .param("prj", project.getExternalId())
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("alerts/ignore-qr",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Alert identifier")
                        ),
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

        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verify(qualityRequirementDomainController, times(1)).ignoreQualityRequirementForAlert(project, alert, rationale, patternId);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void ignoreAlertWrongProject() throws Exception {
        String projectExternalId = "test";
        Long alertId = 2L;
        String rationale = "Not important";
        int patternId = 100;
        when(projectsDomainController.findProjectByExternalId(projectExternalId)).thenThrow(new ProjectNotFoundException());

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr/ignore", alertId)
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("alerts/ignore-qr-wrong-project",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void ignoreAlertNotFound() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        long alertId = 2L;
        when(alertsDomainController.getAlertById(alertId)).thenThrow(new AlertNotFoundException());
        String rationale = "Not important";
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr/ignore", alertId)
                .param("prj", project.getExternalId())
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(status().reason(is("Alert not found")))
                .andDo(document("alerts/ignore-qr-alert-not-found",
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(projectsDomainController);
        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRFromAlert() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertsDomainController.getAlertById(alert.getId())).thenReturn(alert);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        int patternId = 100;
        when(qualityRequirementDomainController.addQualityRequirementForAlert(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, alert, null, project)).thenReturn(qualityRequirement);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr", alert.getId())
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
                .andDo(document("alerts/add-qr-from-alert",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Alert identifier")
                        ),
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

        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verify(qualityRequirementDomainController, times(1)).addQualityRequirementForAlert(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, alert, null, project);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRFromAlertErrorOnBacklog() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertsDomainController.getAlertById(alert.getId())).thenReturn(alert);

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        int patternId = 100;
        when(qualityRequirementDomainController.addQualityRequirementForAlert(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, alert, null, project)).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr", alert.getId())
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason(is("Error when saving the quality requirement in the backlog")))
                .andDo(document("alerts/add-qr-from-alert-backlog-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verify(qualityRequirementDomainController, times(1)).addQualityRequirementForAlert(qualityRequirement.getRequirement(), qualityRequirement.getDescription(), qualityRequirement.getGoal(), decision.getRationale(), patternId, alert, null, project);
        verifyNoMoreInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRFromAlertNotFound() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        Alert alert = domainObjectsBuilder.buildAlert(project);
        when(alertsDomainController.getAlertById(alert.getId())).thenThrow(new AlertNotFoundException());

        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr", alert.getId())
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andDo(document("alerts/add-qr-from-alert-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(alertsDomainController, times(1)).getAlertById(alert.getId());
        verifyNoMoreInteractions(alertsDomainController);

        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void newQRFromAlertProjectNotFound () throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenThrow(new ProjectNotFoundException());

        Alert alert = domainObjectsBuilder.buildAlert(project);
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        int patternId = 100;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .post("/api/alerts/{id}/qr", alert.getId())
                .param("prj", project.getExternalId())
                .param("rationale", decision.getRationale())
                .param("patternId", String.valueOf(patternId))
                .param("requirement", qualityRequirement.getRequirement())
                .param("description", qualityRequirement.getDescription())
                .param("goal", qualityRequirement.getGoal());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("alerts/add-qr-from-alert-project-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verifyZeroInteractions(alertsDomainController);

        verifyZeroInteractions(qualityRequirementDomainController);
    }

    @Test
    public void notifyAlert() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        // Perform request
        String id = "duplication";
        String name = "Duplication";
        String type = "METRIC";
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "duplication";
        Map<String, String> element = new HashMap<>();
        element.put("id", id);
        element.put("name", name);
        element.put("type", type);
        element.put("value", Float.toString(value));
        element.put("threshold", Float.toString(threshold));
        element.put("category", category);
        element.put("project_id", project.getExternalId());
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/notifyAlert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("alerts/notify-alert",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("element.id")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("element.name")
                                        .description("Name of the element causing the alert"),
                                fieldWithPath("element.type")
                                        .description("Type of the element causing the alert (METRIC or FACTOR)"),
                                fieldWithPath("element.value")
                                        .description("Current value of the element causing the alert"),
                                fieldWithPath("element.threshold")
                                        .description("Minimum acceptable value for the element"),
                                fieldWithPath("element.category")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("element.project_id")
                                        .description("Project identifier of the element causing the alert")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(alertsDomainController, times(1)).createAlert(id, name, AlertType.valueOf(type), value, threshold, category, project);
        verifyNoMoreInteractions(alertsDomainController);

        verify(simpleMessagingTemplate, times(1)).convertAndSend(eq("/queue/notify"), ArgumentMatchers.any(Notification.class));
        verifyNoMoreInteractions(simpleMessagingTemplate);
    }

    @Test
    public void createAlert() throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        // Perform request
        String id = "duplication";
        String name = "Duplication";
        String type = "METRIC";
        float value = 0.4f;
        float threshold = 0.5f;
        String category = "duplication";
        Map<String, String> element = new HashMap<>();
        element.put("id", id);
        element.put("name", name);
        element.put("type", type);
        element.put("value", Float.toString(value));
        element.put("threshold", Float.toString(threshold));
        element.put("category", category);
        element.put("project_id", project.getExternalId());
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("alerts/add-alert",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("element.id")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("element.name")
                                        .description("Name of the element causing the alert"),
                                fieldWithPath("element.type")
                                        .description("Type of the element causing the alert (METRIC or FACTOR)"),
                                fieldWithPath("element.value")
                                        .description("Current value of the element causing the alert"),
                                fieldWithPath("element.threshold")
                                        .description("Minimum acceptable value for the element"),
                                fieldWithPath("element.category")
                                        .description("Identifier of the element causing the alert"),
                                fieldWithPath("element.project_id")
                                        .description("Project identifier of the element causing the alert")
                        )
                ));

        // Verify mock interactions
        verify(projectsDomainController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsDomainController);

        verify(alertsDomainController, times(1)).createAlert(id, name, AlertType.valueOf(type), value, threshold, category, project);
        verifyNoMoreInteractions(alertsDomainController);

        verify(simpleMessagingTemplate, times(1)).convertAndSend(eq("/queue/notify"), ArgumentMatchers.any(Notification.class));
        verifyNoMoreInteractions(simpleMessagingTemplate);
    }

    @Test
    public void notifyAlertWrongType() throws Exception {
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "CATEGORY");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        element.put("project_id", "test");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/notifyAlert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("alerts/notify-alert-wrong-type",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void addAlertWrongType() throws Exception {
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "CATEGORY");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        element.put("project_id", "test");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("One or more arguments have the wrong type")))
                .andDo(document("alerts/add-alert-wrong-type",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void notifyAlertMissingParams() throws Exception {
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "METRIC");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/notifyAlert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("alerts/notify-alert-missing-param",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void addAlertMissingParams() throws Exception {
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "METRIC");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("One or more attributes are missing in the request body")))
                .andDo(document("alerts/add-alert-missing-param",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void notifyAlertProjectNotFound () throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenThrow(new ProjectNotFoundException());

        // Perform request
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "METRIC");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        element.put("project_id", "test");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/notifyAlert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("alerts/notify-alert-project-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(alertsDomainController);
    }

    @Test
    public void addAlertProjectNotFound () throws Exception {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsDomainController.findProjectByExternalId(project.getExternalId())).thenThrow(new ProjectNotFoundException());

        // Perform request
        Map<String, String> element = new HashMap<>();
        element.put("id", "duplication");
        element.put("name", "Duplication");
        element.put("type", "METRIC");
        element.put("value", "0.4");
        element.put("threshold", "0.5");
        element.put("category", "duplication");
        element.put("project_id", "test");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("element", element);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String bodyJson = objectWriter.writeValueAsString(body);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("The project identifier does not exist")))
                .andDo(document("alerts/add-alert-project-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyZeroInteractions(alertsDomainController);
    }
}