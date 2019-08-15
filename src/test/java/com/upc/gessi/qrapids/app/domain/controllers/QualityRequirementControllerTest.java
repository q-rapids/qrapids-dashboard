package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QualityRequirementControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QRRepository qrRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private Backlog backlog;

    @InjectMocks
    private QualityRequirementController qualityRequirementController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getQualityRequirementForDecision() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        alert.setDecision(decision);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision, project);
        when(qrRepository.findByDecisionId(decision.getId())).thenReturn(qualityRequirement);

        // When
        QualityRequirement qualityRequirementFound = qualityRequirementController.getQualityRequirementForDecision(decision);

        // Then
        assertEquals(qualityRequirement, qualityRequirementFound);
        verify(qrRepository, times(1)).findByDecisionId(decision.getId());
    }

    @Test
    public void ignoreQualityRequirement() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        String rationale = "Very important";
        int patternId = 100;

        // When
        qualityRequirementController.ignoreQualityRequirement(project, rationale, patternId);

        // Then
        ArgumentCaptor<Decision> decisionArgumentCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository, times(1)).save(decisionArgumentCaptor.capture());
        Decision decisionSaved = decisionArgumentCaptor.getValue();
        assertEquals(DecisionType.IGNORE, decisionSaved.getType());
        assertEquals(rationale, decisionSaved.getRationale());
        assertEquals(patternId, decisionSaved.getPatternId());

        verifyZeroInteractions(alertRepository);
    }

    @Test
    public void ignoreQualityRequirementForAlert() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        String rationale = "User comments";
        int patternId = 100;
        when(decisionRepository.save(any(Decision.class))).then(returnsFirstArg());

        // When
        qualityRequirementController.ignoreQualityRequirementForAlert(project, alert, rationale, patternId);

        // Then
        ArgumentCaptor<Decision> decisionArgumentCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository, times(1)).save(decisionArgumentCaptor.capture());
        Decision decisionSaved = decisionArgumentCaptor.getValue();
        assertEquals(DecisionType.IGNORE, decisionSaved.getType());
        assertEquals(rationale, decisionSaved.getRationale());
        assertEquals(patternId, decisionSaved.getPatternId());

        ArgumentCaptor<Alert> alertArgumentCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(alertArgumentCaptor.capture());
        Alert alertSaved = alertArgumentCaptor.getValue();
        assertEquals(AlertStatus.RESOLVED, alertSaved.getStatus());
        assertEquals(decisionSaved, alertSaved.getDecision());
    }

    @Test
    public void addQualityRequirement() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        String rationale = "User comments";
        int patternId = 100;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        when(decisionRepository.save(any(Decision.class))).then(returnsFirstArg());
        when(backlog.postNewQualityRequirement(any(QualityRequirement.class))).then(returnsFirstArg());

        // When
        QualityRequirement qualityRequirement = qualityRequirementController.addQualityRequirement(requirement, description, goal, rationale, patternId, null, project);

        // Then
        assertEquals(requirement, qualityRequirement.getRequirement());
        assertEquals(description, qualityRequirement.getDescription());
        assertEquals(goal, qualityRequirement.getGoal());
        assertEquals(DecisionType.ADD, qualityRequirement.getDecision().getType());
        assertEquals(rationale, qualityRequirement.getDecision().getRationale());
        assertEquals(patternId, qualityRequirement.getDecision().getPatternId());

        verify(decisionRepository, times(1)).save(any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(qrRepository, times(2)).save(any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);
    }

    @Test(expected = HttpClientErrorException.class)
    public void addQualityRequirementBacklogError () {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        String rationale = "User comments";
        int patternId = 100;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        when(decisionRepository.save(any(Decision.class))).then(returnsFirstArg());
        when(backlog.postNewQualityRequirement(any(QualityRequirement.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Throw
        qualityRequirementController.addQualityRequirement(requirement, description, goal, rationale, patternId, null, project);
    }

    @Test
    public void addQualityRequirementForAlert() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        String rationale = "User comments";
        int patternId = 100;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        when(decisionRepository.save(any(Decision.class))).then(returnsFirstArg());
        when(backlog.postNewQualityRequirement(any(QualityRequirement.class))).then(returnsFirstArg());

        // When
        QualityRequirement qualityRequirement = qualityRequirementController.addQualityRequirementForAlert(requirement, description, goal, rationale, patternId, alert, null, project);

        // Then
        assertEquals(requirement, qualityRequirement.getRequirement());
        assertEquals(description, qualityRequirement.getDescription());
        assertEquals(goal, qualityRequirement.getGoal());
        assertEquals(DecisionType.ADD, qualityRequirement.getDecision().getType());
        assertEquals(rationale, qualityRequirement.getDecision().getRationale());
        assertEquals(patternId, qualityRequirement.getDecision().getPatternId());

        verify(decisionRepository, times(1)).save(any(Decision.class));
        verifyNoMoreInteractions(decisionRepository);

        verify(qrRepository, times(2)).save(any(QualityRequirement.class));
        verifyNoMoreInteractions(qrRepository);

        verify(backlog, times(1)).postNewQualityRequirement(any(QualityRequirement.class));
        verifyNoMoreInteractions(backlog);

        verify(alertRepository, times(1)).save(any(Alert.class));
        verifyNoMoreInteractions(alertRepository);
    }

    @Test(expected = HttpClientErrorException.class)
    public void addQualityRequirementForAlertBacklogError() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);
        String rationale = "User comments";
        int patternId = 100;
        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        when(decisionRepository.save(any(Decision.class))).then(returnsFirstArg());
        when(backlog.postNewQualityRequirement(any(QualityRequirement.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        qualityRequirementController.addQualityRequirementForAlert(requirement, description, goal, rationale, patternId, alert, null, project);
    }
}