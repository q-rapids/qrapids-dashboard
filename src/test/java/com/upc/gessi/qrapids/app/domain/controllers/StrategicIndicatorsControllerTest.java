package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StrategicIndicatorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

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

    @Test
    public void getAllDetailedStrategicIndicatorsCurrentEvaluation() throws IOException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));

        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getSingleDetailedStrategicIndicatorCurrentEvaluation() throws IOException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));

        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getAllStrategicIndicatorsHistoricalEvaluation() throws IOException, CategoriesException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaStrategicIndicators.HistoricalData(fromDate, toDate, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getAllDetailedStrategicIndicatorsHistoricalEvaluation() throws IOException {
        // Given
        String projectExternalId = "test";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));

        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(null, fromDate, toDate, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(projectExternalId, fromDate, toDate);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }
}