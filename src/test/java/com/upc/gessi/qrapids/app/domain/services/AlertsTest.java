package com.upc.gessi.qrapids.app.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import qr.QRGenerator;
import qr.models.FixedPart;
import qr.models.Form;
import qr.models.QualityRequirementPattern;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
public class AlertsTest {

    private MockMvc mockMvc;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private QRGeneratorFactory qrGeneratorFactory;

    @Mock
    private QRRepository qrRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private Backlog backlog;

    @Mock
    private SimpMessagingTemplate simpleMessagingTemplate;

    @InjectMocks
    private Alerts alertsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(alertsController)
                .build();
    }

    @Test
    public void getAllAlerts() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Alerts setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, project);
        alert.setId(alertId);

        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert);
        when(alertRepository.findByProject_IdOrderByDateDesc(projectId)).thenReturn(alertList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(alertId.intValue())))
                .andExpect(jsonPath("$[0].id_element", is(idElement)))
                .andExpect(jsonPath("$[0].name", is(name)))
                .andExpect(jsonPath("$[0].type", is(alertType.toString())))
                .andExpect(jsonPath("$[0].value", is(value)))
                .andExpect(jsonPath("$[0].threshold", is(threshold)))
                .andExpect(jsonPath("$[0].category", is(category)))
                .andExpect(jsonPath("$[0].date", is(date.getTime())))
                .andExpect(jsonPath("$[0].status", is(alertStatus.toString())))
                .andExpect(jsonPath("$[0].reqAssociat", is(hasReq)))
                .andExpect(jsonPath("$[0].artefacts", is(nullValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(alertRepository, times(1)).findByProject_IdOrderByDateDesc(projectId);
        List<Long> alertIdsList = new ArrayList<>();
        alertIdsList.add(alertId);
        verify(alertRepository, times(1)).setViewedStatusFor(alertIdsList);
    }

    @Test
    public void countNewAlerts() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);
        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // alerts setup
        Long newAlerts = 2L;
        Long newAlertWithQR = 1L;
        when(alertRepository.countByProject_IdAndStatus(projectId, AlertStatus.NEW)).thenReturn(newAlerts);
        when(alertRepository.countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW)).thenReturn(newAlertWithQR);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/new")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAlerts", is(newAlerts.intValue())))
                .andExpect(jsonPath("$.newAlertsWithQR", is(newAlertWithQR.intValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(alertRepository, times(1)).countByProject_IdAndStatus(projectId, AlertStatus.NEW);
        verify(alertRepository, times(1)).countByProject_IdAndReqAssociatIsTrueAndStatusEquals(projectId, AlertStatus.NEW);
        verifyNoMoreInteractions(alertRepository);
    }

    @Test
    public void getQR() throws Exception {
        // Alert setup
        Long alertId = 1L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);
        when(alertRepository.findAlertById(alertId)).thenReturn(alert);

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
        when(qrGenerator.generateQRs(ArgumentMatchers.any(qr.models.Alert.class))).thenReturn(qualityRequirementPatternList);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/" + alertId + "/qrPatterns");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requirementId)))
                .andExpect(jsonPath("$[0].name", is(requirementName)))
                .andExpect(jsonPath("$[0].comments", is(requirementComments)))
                .andExpect(jsonPath("$[0].description", is(requirementDescription)))
                .andExpect(jsonPath("$[0].goal", is(requirementGoal)))
                .andExpect(jsonPath("$[0].forms[0].name", is(formName)))
                .andExpect(jsonPath("$[0].forms[0].description", is(formDescription)))
                .andExpect(jsonPath("$[0].forms[0].comments", is(formComments)))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.formText", is(formText)))
                .andExpect(jsonPath("$[0].costFunction", is(requirementCostFunction)));


        // Verify mock interactions
        verify(alertRepository, times(1)).findAlertById(alertId);
        verifyNoMoreInteractions(alertRepository);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).generateQRs(ArgumentMatchers.any(qr.models.Alert.class));
        verifyNoMoreInteractions(qrGenerator);
    }


    @Test
    public void getAlertDecision() throws Exception {
        // Alert setup
        Long alertId = 1L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);

        Long decisionId = 2L;
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Very important";
        int patternId = 100;
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);
        decision.setId(decisionId);
        alert.setDecision(decision);

        when(alertRepository.findAlertById(alertId)).thenReturn(alert);

        // Requirement setup
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, null);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);

        when(qrRepository.findByDecisionId(decisionId)).thenReturn(qualityRequirement);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/alerts/" + alertId + "/decision");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrGoal", is(goal)))
                .andExpect(jsonPath("$.qrRequirement", is(requirement)))
                .andExpect(jsonPath("$.qrDescription", is(description)))
                .andExpect(jsonPath("$.qrBacklogUrl", is(qrBacklogUrl)))
                .andExpect(jsonPath("$.decisionType", is(decisionType.toString())))
                .andExpect(jsonPath("$.decisionRationale", is(rationale)));

        // Verify mock interactions
        verify(alertRepository, times(1)).findAlertById(alertId);
        verifyNoMoreInteractions(alertRepository);

        verify(qrRepository, times(1)).findByDecisionId(decisionId);
        verifyNoMoreInteractions(qrRepository);
    }

    @Test
    public void ignoreAlert() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.IGNORE;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Alert setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);

        when(alertRepository.findAlertById(alertId)).thenReturn(alert);
        when(alertRepository.save(ArgumentMatchers.any(Alert.class))).thenReturn(alert);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts/" + alertId + "/ignore")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(alertRepository, times(1)).findAlertById(alertId);
        verify(alertRepository, times(1)).save(ArgumentMatchers.any(Alert.class));
        verifyNoMoreInteractions(alertRepository);
    }

    @Test
    public void ignoreQR() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.IGNORE;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr/ignore")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);
    }

    @Test
    public void newQRFromAlert() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Alert setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);

        when(alertRepository.findAlertById(alertId)).thenReturn(alert);
        when(alertRepository.save(ArgumentMatchers.any(Alert.class))).thenReturn(alert);

        // Requirement setup
        Long requirementId = 3L;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qualityRequirement.setId(requirementId);
        qualityRequirement.setBacklogId(qrBacklogId);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);

        when(qrRepository.save(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);
        when(backlog.postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts/" + alertId + "/qr")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId))
                .param("requirement", requirement)
                .param("description", description)
                .param("goal", goal);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requirementId.intValue())))
                .andExpect(jsonPath("$.date", is(date.getTime())))
                .andExpect(jsonPath("$.requirement", is(requirement)))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.goal", is(goal)))
                .andExpect(jsonPath("$.backlogId", is(qrBacklogId)))
                .andExpect(jsonPath("$.backlogUrl", is(qrBacklogUrl)))
                .andExpect(jsonPath("$.backlogProjectId", is(nullValue())))
                .andExpect(jsonPath("$.alert", is(nullValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(alertRepository, times(1)).findAlertById(alertId);
        verify(alertRepository, times(1)).save(ArgumentMatchers.any(Alert.class));

        verify(qrRepository, times(2)).save(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);
    }

    @Test
    public void newQRFromAlertErrorOnBacklog() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Alert setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);

        when(alertRepository.findAlertById(alertId)).thenReturn(alert);
        when(alertRepository.save(ArgumentMatchers.any(Alert.class))).thenReturn(alert);

        // Requirement setup
        Long requirementId = 3L;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, alert, decision, project);
        qualityRequirement.setId(requirementId);
        qualityRequirement.setBacklogId(qrBacklogId);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);

        when(qrRepository.save(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);
        when(backlog.postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/alerts/" + alertId + "/qr")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId))
                .param("requirement", requirement)
                .param("description", description)
                .param("goal", goal);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError());

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(alertRepository, times(1)).findAlertById(alertId);
        verify(alertRepository, times(1)).save(ArgumentMatchers.any(Alert.class));

        verify(qrRepository, times(1)).save(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);
    }

    @Test
    public void newQR() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Requirement setup
        Long requirementId = 3L;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, null, decision, project);
        qualityRequirement.setId(requirementId);
        qualityRequirement.setBacklogId(qrBacklogId);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);

        when(qrRepository.save(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);
        when(backlog.postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId))
                .param("requirement", requirement)
                .param("description", description)
                .param("goal", goal);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requirementId.intValue())))
                .andExpect(jsonPath("$.date", is(date.getTime())))
                .andExpect(jsonPath("$.requirement", is(requirement)))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.goal", is(goal)))
                .andExpect(jsonPath("$.backlogId", is(qrBacklogId)))
                .andExpect(jsonPath("$.backlogUrl", is(qrBacklogUrl)))
                .andExpect(jsonPath("$.backlogProjectId", is(nullValue())))
                .andExpect(jsonPath("$.alert", is(nullValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(qrRepository, times(2)).save(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);
    }

    @Test
    public void newQRErrorOnBacklog() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Decision setup
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        int patternId = 100;
        Date date = new Date();
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        when(decisionRepository.save(ArgumentMatchers.any(Decision.class))).thenReturn(decision);

        // Requirement setup
        Long requirementId = 3L;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        String qrBacklogUrl =  "https://backlog.example/issue/999";
        String qrBacklogId = "ID-999";
        QualityRequirement qualityRequirement = new QualityRequirement(requirement, description, goal, null, decision, project);
        qualityRequirement.setId(requirementId);
        qualityRequirement.setBacklogId(qrBacklogId);
        qualityRequirement.setBacklogUrl(qrBacklogUrl);

        when(qrRepository.save(ArgumentMatchers.any(QualityRequirement.class))).thenReturn(qualityRequirement);
        when(backlog.postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qr")
                .param("prj", projectExternalId)
                .param("rationale", rationale)
                .param("patternId", String.valueOf(patternId))
                .param("requirement", requirement)
                .param("description", description)
                .param("goal", goal);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError());

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(decisionRepository, times(1)).save(ArgumentMatchers.any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(qrRepository, times(1)).save(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(ArgumentMatchers.any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);
    }

    @Test
    public void getQRs() throws Exception {
        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        String backlogId = "1";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setBacklogId(backlogId);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Alert setup
        Long alertId = 2L;
        String idElement = "id";
        String name = "Duplication";
        AlertType alertType = AlertType.METRIC;
        Double value = 0.4;
        Double threshold = 0.5;
        String category = "category";
        Date date = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        boolean hasReq = true;
        Alert alert = new Alert(idElement, name, alertType, value.floatValue(), threshold.floatValue(), category, date, alertStatus, hasReq, null);
        alert.setId(alertId);

        // Decision setup
        DecisionType decisionType = DecisionType.ADD;
        String rationale = "Not important";
        int patternId = 100;
        Decision decision = new Decision(decisionType, date, null, rationale, patternId, null);

        // Requirement setup
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

        List<QualityRequirement> qualityRequirementList = new ArrayList<>();
        qualityRequirementList.add(qualityRequirement);

        when(qrRepository.findByProjectIdOrderByDecision_DateDesc(projectId)).thenReturn(qualityRequirementList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qr")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requirementId.intValue())))
                .andExpect(jsonPath("$[0].date", is(date.getTime())))
                .andExpect(jsonPath("$[0].requirement", is(requirement)))
                .andExpect(jsonPath("$[0].description", is(description)))
                .andExpect(jsonPath("$[0].goal", is(goal)))
                .andExpect(jsonPath("$[0].backlogId", is(qrBacklogId)))
                .andExpect(jsonPath("$[0].backlogUrl", is(qrBacklogUrl)))
                .andExpect(jsonPath("$[0].backlogProjectId", is(backlogId)))
                .andExpect(jsonPath("$[0].alert.id", is(alertId.intValue())))
                .andExpect(jsonPath("$[0].alert.id_element", is(idElement)))
                .andExpect(jsonPath("$[0].alert.name", is(name)))
                .andExpect(jsonPath("$[0].alert.type", is(alertType.toString())))
                .andExpect(jsonPath("$[0].alert.value", is(value)))
                .andExpect(jsonPath("$[0].alert.threshold", is(threshold)))
                .andExpect(jsonPath("$[0].alert.category", is(category)))
                .andExpect(jsonPath("$[0].alert.date", is(date.getTime())))
                .andExpect(jsonPath("$[0].alert.status", is(alertStatus.toString())))
                .andExpect(jsonPath("$[0].alert.reqAssociat", is(hasReq)))
                .andExpect(jsonPath("$[0].alert.artefacts", is(nullValue())));

        // Verify mock interactions
        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(qrRepository, times(1)).findByProjectIdOrderByDecision_DateDesc(projectId);
        verifyNoMoreInteractions(qrRepository);
    }

    @Test
    public void notifyAlert() throws Exception {
        // QRGeneration setup
        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.existsQRPattern(ArgumentMatchers.any(qr.models.Alert.class))).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // project setup
        Long projectId = 1L;
        String projectExternalId = "test";
        String backlogId = "1";
        Project project = new Project(projectExternalId, "Test", "", null, true);
        project.setBacklogId(backlogId);
        project.setId(projectId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        // Alert setup
        when(alertRepository.save(ArgumentMatchers.any(Alert.class))).thenReturn(null);

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
                .andExpect(status().isOk());

        // Verify mock interactions
        verify(qrGenerator, times(1)).existsQRPattern(ArgumentMatchers.any(qr.models.Alert.class));
        verifyNoMoreInteractions(qrGenerator);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(projectRepository, times(1)).findByExternalId(projectExternalId);
        verifyNoMoreInteractions(projectRepository);

        verify(alertRepository, times(1)).save(ArgumentMatchers.any(Alert.class));
        verifyNoMoreInteractions(alertRepository);

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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAllQRPatterns() throws Exception {
        // Requirement setup
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
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatterns");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requirementId)))
                .andExpect(jsonPath("$[0].name", is(requirementName)))
                .andExpect(jsonPath("$[0].comments", is(requirementComments)))
                .andExpect(jsonPath("$[0].description", is(requirementDescription)))
                .andExpect(jsonPath("$[0].goal", is(requirementGoal)))
                .andExpect(jsonPath("$[0].forms[0].name", is(formName)))
                .andExpect(jsonPath("$[0].forms[0].description", is(formDescription)))
                .andExpect(jsonPath("$[0].forms[0].comments", is(formComments)))
                .andExpect(jsonPath("$[0].forms[0].fixedPart.formText", is(formText)))
                .andExpect(jsonPath("$[0].costFunction", is(requirementCostFunction)));

        // Verify mock interactions
        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getAllQRPatterns();
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getQRPattern() throws Exception {
        // Requirement setup
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

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getQRPattern(requirementId)).thenReturn(qualityRequirementPattern);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatterns/" + requirementId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requirementId)))
                .andExpect(jsonPath("$.name", is(requirementName)))
                .andExpect(jsonPath("$.comments", is(requirementComments)))
                .andExpect(jsonPath("$.description", is(requirementDescription)))
                .andExpect(jsonPath("$.goal", is(requirementGoal)))
                .andExpect(jsonPath("$.forms[0].name", is(formName)))
                .andExpect(jsonPath("$.forms[0].description", is(formDescription)))
                .andExpect(jsonPath("$.forms[0].comments", is(formComments)))
                .andExpect(jsonPath("$.forms[0].fixedPart.formText", is(formText)))
                .andExpect(jsonPath("$.costFunction", is(requirementCostFunction)));

        // Verify mock interactions
        verify(qrGenerator, times(1)).getQRPattern(requirementId);
        verifyNoMoreInteractions(qrGenerator);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);
    }

    @Test
    public void getMetricsForQRPattern() throws Exception {
        Integer patternId = 1;
        List<Integer> patternIdList = new ArrayList<>();
        patternIdList.add(patternId);

        String metric = "comments";
        Map<Integer, String> metrics = new HashMap<>();
        metrics.put(patternId, metric);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getMetricsForPatterns(patternIdList)).thenReturn(metrics);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qrPatterns/" + patternId + "/metric");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(metric)));

        // Verify mock interactions
        verify(qrGenerator, times(1)).getMetricsForPatterns(patternIdList);
        verifyNoMoreInteractions(qrGenerator);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);
    }
}