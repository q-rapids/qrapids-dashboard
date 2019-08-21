package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.StrategicIndicatorNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StrategicIndicatorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Mock
    private Forecast qmaForecast;

    @Mock
    private SICategoryRepository siCategoryRepository;

    @InjectMocks
    private StrategicIndicatorsController strategicIndicatorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getStrategicIndicatorsByProject() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        List<Strategic_Indicator> strategicIndicatorList = new ArrayList<>();
        strategicIndicatorList.add(strategicIndicator);
        when(strategicIndicatorRepository.findByProject_Id(project.getId())).thenReturn(strategicIndicatorList);

        // When
        List<Strategic_Indicator> strategicIndicatorListFound = strategicIndicatorsController.getStrategicIndicatorsByProject(project);

        // Then
        assertEquals(strategicIndicatorList.size(), strategicIndicatorListFound.size());
        assertEquals(strategicIndicator, strategicIndicatorListFound.get(0));
    }

    @Test
    public void deleteStrategicIndicator() throws StrategicIndicatorNotFoundException {
        // Given
        Long strategicIndicatorId = 1L;
        when(strategicIndicatorRepository.existsById(strategicIndicatorId)).thenReturn(true);

        // When
        strategicIndicatorsController.deleteStrategicIndicator(strategicIndicatorId);

        // Then
        verify(strategicIndicatorRepository, times(1)).existsById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).deleteById(strategicIndicatorId);
        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test(expected = StrategicIndicatorNotFoundException.class)
    public void deleteStrategicIndicatorNotFound() throws StrategicIndicatorNotFoundException {
        // Given
        Long strategicIndicatorId = 1L;
        when(strategicIndicatorRepository.existsById(strategicIndicatorId)).thenReturn(false);

        // Throw
        strategicIndicatorsController.deleteStrategicIndicator(strategicIndicatorId);
    }

    @Test
    public void getStrategicIndicatorCategories() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        List<SICategory> siCategoryListFound = strategicIndicatorsController.getStrategicIndicatorCategories();

        // Then
        assertEquals(siCategoryList.size(), siCategoryListFound.size());
        assertEquals(siCategoryList.get(0), siCategoryListFound.get(0));
        assertEquals(siCategoryList.get(1), siCategoryListFound.get(1));
        assertEquals(siCategoryList.get(2), siCategoryListFound.get(2));
    }

    @Test
    public void newStrategicIndicatorCategories() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawSICategoryList();

        // When
        strategicIndicatorsController.newStrategicIndicatorCategories(categories);

        // Then
        verify(siCategoryRepository, times(1)).deleteAll();

        ArgumentCaptor<SICategory> siCategoryArgumentCaptor = ArgumentCaptor.forClass(SICategory.class);
        verify(siCategoryRepository, times(3)).save(siCategoryArgumentCaptor.capture());
        List<SICategory> siCategoryListSaved = siCategoryArgumentCaptor.getAllValues();
        assertEquals(categories.get(0).get("name"), siCategoryListSaved.get(0).getName());
        assertEquals(categories.get(0).get("color"), siCategoryListSaved.get(0).getColor());
        assertEquals(categories.get(1).get("name"), siCategoryListSaved.get(1).getName());
        assertEquals(categories.get(1).get("color"), siCategoryListSaved.get(1).getColor());
        assertEquals(categories.get(2).get("name"), siCategoryListSaved.get(2).getName());
        assertEquals(categories.get(2).get("color"), siCategoryListSaved.get(2).getColor());
    }

    @Test(expected = CategoriesException.class)
    public void newStrategicIndicatorCategoriesNotEnough() throws CategoriesException {
        // Given
        List<Map<String, String>> categories = domainObjectsBuilder.buildRawSICategoryList();
        categories.remove(2);
        categories.remove(1);

        // Throw
        strategicIndicatorsController.newStrategicIndicatorCategories(categories);
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

    @Test
    public void getSingleDetailedStrategicIndicatorsHistoricalEvaluation() throws IOException {
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
        when(qmaDetailedStrategicIndicators.HistoricalData(dtoDetailedStrategicIndicator.getId(), fromDate, toDate, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, fromDate, toDate);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getStrategicIndicatorsPrediction() throws IOException {
        // Given
        String projectExternalId = "test";
        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);
        when(qmaForecast.ForecastSI(technique, freq, horizon, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getStrategicIndicatorsPrediction(technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getDetailedStrategicIndicatorsPrediction() throws IOException {
        // Given
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));

        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        String projectExternalId = "test";
        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";

        when(qmaForecast.ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(dtoDetailedStrategicIndicatorList, technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }
}