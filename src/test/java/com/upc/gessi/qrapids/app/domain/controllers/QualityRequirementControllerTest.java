package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QualityRequirementControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QRRepository qrRepository;

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
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(alert, decision);
        when(qrRepository.findByDecisionId(decision.getId())).thenReturn(qualityRequirement);

        // When
        QualityRequirement qualityRequirementFound = qualityRequirementController.getQualityRequirementForDecision(decision);

        // Then
        assertEquals(qualityRequirement, qualityRequirementFound);
        verify(qrRepository, times(1)).findByDecisionId(decision.getId());
    }
}