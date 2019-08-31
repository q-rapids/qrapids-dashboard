package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.models.Decision;
import com.upc.gessi.qrapids.app.domain.models.DecisionType;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QR.QRRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODecisionQualityRequirement;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import qr.models.QualityRequirementPattern;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecisionsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private QRRepository qrRepository;

    @Mock
    private QRPatternsController qrPatternsController;

    @InjectMocks
    private DecisionsController decisionsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getAllDecisionsByProject() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        List<Decision> decisionList = new ArrayList<>();
        decisionList.add(decision);
        when(decisionRepository.findByProject_Id(project.getId())).thenReturn(decisionList);

        // When
        List<Decision> decisionListFound = decisionsController.getAllDecisionsByProject(project);

        // Then
        assertEquals(decisionList.size(), decisionListFound.size());
        assertEquals(decision, decisionListFound.get(0));

        verify(decisionRepository, times(1)).findByProject_Id(project.getId());
    }

    @Test
    public void getAllDecisionsWithQRByProjectAndDates() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);
        when(qrPatternsController.getAllPatterns()).thenReturn(qualityRequirementPatternList);

        Map<Integer, String> metricForPatternMap = new HashMap<>();
        String metric = "duplication";
        metricForPatternMap.put(qualityRequirementPattern.getId(), metric);
        when(qrPatternsController.getMetricsForPatterns(anyList())).thenReturn(metricForPatternMap);

        Project project = domainObjectsBuilder.buildProject();
        Decision decision = domainObjectsBuilder.buildDecision(project, DecisionType.ADD);
        QualityRequirement qualityRequirement = domainObjectsBuilder.buildQualityRequirement(null, decision, project);
        DTODecisionQualityRequirement dtoDecisionQualityRequirement = domainObjectsBuilder.buildDecisionWithQualityRequirement(qualityRequirement);
        DTODecisionQualityRequirement dtoDecisionNoQualityRequirement = domainObjectsBuilder.buildDecisionWithoutQualityRequirement(decision);
        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirementList = new ArrayList<>();
        dtoDecisionQualityRequirementList.add(dtoDecisionQualityRequirement);
        dtoDecisionQualityRequirementList.add(dtoDecisionNoQualityRequirement);
        Date dateFrom = Date.valueOf("2019-07-15");
        Date dateTo = Date.valueOf("2019-08-01");
        when(qrRepository.getAllDecisionsAndQRsByProject_Id(project.getId(), dateFrom, dateTo)).thenReturn(dtoDecisionQualityRequirementList);

        // When
        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirementListFound = decisionsController.getAllDecisionsWithQRByProjectAndDates(project, dateFrom, dateTo);

        // Then
        assertEquals(dtoDecisionQualityRequirementList.size(), dtoDecisionQualityRequirementListFound.size());

        assertEquals(metric, dtoDecisionQualityRequirementListFound.get(0).getElementId());
        assertEquals(qualityRequirement.getRequirement(), dtoDecisionQualityRequirementListFound.get(0).getRequirement());
        assertEquals(qualityRequirement.getDescription(), dtoDecisionQualityRequirementListFound.get(0).getDescription());
        assertEquals(qualityRequirement.getGoal(), dtoDecisionQualityRequirementListFound.get(0).getGoal());

        assertEquals(metric, dtoDecisionQualityRequirementListFound.get(1).getElementId());
        assertEquals(qualityRequirementPattern.getForms().get(0).getFixedPart().getFormText(), dtoDecisionQualityRequirementListFound.get(1).getRequirement());
        assertEquals(qualityRequirementPattern.getForms().get(0).getDescription(), dtoDecisionQualityRequirementListFound.get(1).getDescription());
        assertEquals(qualityRequirementPattern.getGoal(), dtoDecisionQualityRequirementListFound.get(1).getGoal());
    }
}