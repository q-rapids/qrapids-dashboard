package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StrategicIndicatorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @InjectMocks
    private StrategicIndicatorsController strategicIndicatorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getAllStrategicIndicatorsCurrentEvaluation() throws IOException, CategoriesException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);
        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getSingleStrategicIndicatorsCurrentEvaluation() throws IOException, CategoriesException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        when(qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, dtoStrategicIndicatorEvaluation.getId())).thenReturn(dtoStrategicIndicatorEvaluation);

        // When
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluationFound = strategicIndicatorsController.getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationFound);
    }
}