package com.upc.gessi.qrapids.app.domain.controllers;


import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.exceptions.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.Profile.ProfileProjectStrategicIndicatorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorQualityFactorsRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StrategicIndicatorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private ProfileProjectStrategicIndicatorsRepository profileProjectStrategicIndicatorsRepository;

    @Mock
    private StrategicIndicatorQualityFactorsRepository strategicIndicatorQualityFactorsRepository;

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Mock
    private Forecast qmaForecast;

    @Mock
    private SICategoryRepository siCategoryRepository;

    @Mock
    private AlertsController alertsController;

    @Mock
    private ProjectsController projectsController;

    @Mock
    private MetricsController metricsController;

    @Mock
    private FactorsController factorsController;

    @Mock
    private StrategicIndicatorQualityFactorsController strategicIndicatorQualityFactorsController;

    @Mock
    private AssesSI assesSI;

    @Mock
    private QMARelations qmaRelations;

    @InjectMocks
    private StrategicIndicatorsController strategicIndicatorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getStrategicIndicatorsByProjectAndProfile() throws ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        List<Strategic_Indicator> strategicIndicatorList = new ArrayList<>();
        strategicIndicatorList.add(strategicIndicator);
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);
        when(strategicIndicatorRepository.findByProject_IdOrderByName(project.getId())).thenReturn(strategicIndicatorList);

        // When
        List<Strategic_Indicator> strategicIndicatorListFound = strategicIndicatorsController.getStrategicIndicatorsByProjectAndProfile(project.getExternalId(),null); // without profile

        // Then
        assertEquals(strategicIndicatorList.size(), strategicIndicatorListFound.size());
        assertEquals(strategicIndicator, strategicIndicatorListFound.get(0));
    }

    @Test
    public void getStrategicIndicatorById() throws StrategicIndicatorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        when(strategicIndicatorRepository.findById(strategicIndicator.getId())).thenReturn(Optional.of(strategicIndicator));

        // When
        Strategic_Indicator strategicIndicatorFound = strategicIndicatorsController.getStrategicIndicatorById(strategicIndicator.getId());

        // Then
        assertEquals(strategicIndicator, strategicIndicatorFound);
    }

    @Test(expected = StrategicIndicatorNotFoundException.class)
    public void getStrategicIndicatorByIdNotFound() throws StrategicIndicatorNotFoundException {
        // Given
        Long strategicIndicatorId = 2L;
        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.empty());

        // Throw
        strategicIndicatorsController.getStrategicIndicatorById(strategicIndicatorId);
    }

    @Test
    public void saveStrategicIndicator() throws IOException, QualityFactorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");

        List<StrategicIndicatorQualityFactors> qualityFactors = domainObjectsBuilder.buildQualityFactors(strategicIndicator);
        for (StrategicIndicatorQualityFactors qf : qualityFactors) {
            when(factorsController.getQualityFactorById(eq(qf.getFactor().getId()))).thenReturn(qf.getFactor());
            when(strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(eq(qf.getFactor()),eq(qf.getWeight()), any(Strategic_Indicator.class))).thenReturn(qf);
        }

        // When
        strategicIndicatorsController.saveStrategicIndicator(strategicIndicator.getName(), strategicIndicator.getDescription(), strategicIndicator.getThreshold().toString(), Files.readAllBytes(networkFile.toPath()), strategicIndicator.getWeights(), project);

        // Then
        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(2)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicator.getName(), strategicIndicatorSaved.getName());
        assertEquals(strategicIndicator.getDescription(), strategicIndicatorSaved.getDescription());
        assertEquals(strategicIndicator.getQuality_factors(), strategicIndicatorSaved.getQuality_factors());
    }

    @Test
    public void editStrategicIndicator() throws IOException, StrategicIndicatorNotFoundException, StrategicIndicatorQualityFactorNotFoundException, QualityFactorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        when(strategicIndicatorRepository.findById(strategicIndicator.getId())).thenReturn(Optional.of(strategicIndicator));

        List<StrategicIndicatorQualityFactors> qualityFactors = domainObjectsBuilder.buildQualityFactors(strategicIndicator);
        when(strategicIndicatorQualityFactorsRepository.findByStrategic_indicator(strategicIndicator)).thenReturn(qualityFactors);
        for (StrategicIndicatorQualityFactors qf : qualityFactors) {
            when(factorsController.getQualityFactorById(eq(qf.getFactor().getId()))).thenReturn(qf.getFactor());
            when(strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(eq(qf.getFactor()),eq(qf.getWeight()), any(Strategic_Indicator.class))).thenReturn(qf);
        }

        // When
        strategicIndicatorsController.editStrategicIndicator(strategicIndicator.getId(), strategicIndicator.getName(), strategicIndicator.getDescription(), strategicIndicator.getThreshold().toString(), Files.readAllBytes(networkFile.toPath()), strategicIndicator.getWeights());

        // Then
        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicator.getName(), strategicIndicatorSaved.getName());
        assertEquals(strategicIndicator.getDescription(), strategicIndicatorSaved.getDescription());
        assertEquals(strategicIndicator.getQuality_factors(), strategicIndicatorSaved.getQuality_factors());
    }

    @Test
    public void deleteStrategicIndicator() throws StrategicIndicatorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.existsById(strategicIndicatorId)).thenReturn(true);
        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));
        when(profileProjectStrategicIndicatorsRepository.findByStrategic_indicator(strategicIndicator)).thenReturn(null);

        // When
        strategicIndicatorsController.deleteStrategicIndicator(strategicIndicatorId);

        // Then
        verify(strategicIndicatorRepository, times(1)).existsById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).deleteById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);
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
    public void getAllStrategicIndicatorsCurrentEvaluation() throws IOException, CategoriesException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);
        when(qmaStrategicIndicators.CurrentEvaluation(projectExternalId, profileId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId, profileId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getSingleStrategicIndicatorsCurrentEvaluation() throws IOException, CategoriesException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        when(qmaStrategicIndicators.SingleCurrentEvaluation(projectExternalId, profileId, dtoStrategicIndicatorEvaluation.getId())).thenReturn(dtoStrategicIndicatorEvaluation);

        // When
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluationFound = strategicIndicatorsController.getSingleStrategicIndicatorsCurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId, profileId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationFound);
    }

    @Test
    public void getAllDetailedStrategicIndicatorsCurrentEvaluation() throws IOException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));

        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId, profileId, true)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getAllDetailedStrategicIndicatorsCurrentEvaluation(projectExternalId, profileId, true);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getSingleDetailedStrategicIndicatorCurrentEvaluation() throws IOException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));

        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(dtoStrategicIndicatorEvaluation.getId(), projectExternalId, profileId, true)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getSingleDetailedStrategicIndicatorCurrentEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, profileId);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getAllStrategicIndicatorsHistoricalEvaluation() throws IOException, CategoriesException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);
        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaStrategicIndicators.HistoricalData(fromDate, toDate, projectExternalId, profileId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(projectExternalId, profileId, fromDate, toDate);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getAllDetailedStrategicIndicatorsHistoricalEvaluation() throws IOException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));

        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(null, fromDate, toDate, projectExternalId, profileId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getAllDetailedStrategicIndicatorsHistoricalEvaluation(projectExternalId, profileId, fromDate, toDate);


        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getSingleDetailedStrategicIndicatorsHistoricalEvaluation() throws IOException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));

        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        String from = "2019-07-07";
        LocalDate fromDate = LocalDate.parse(from);
        String to = "2019-07-15";
        LocalDate toDate = LocalDate.parse(to);
        when(qmaDetailedStrategicIndicators.HistoricalData(dtoDetailedStrategicIndicator.getId(), fromDate, toDate, projectExternalId, profileId)).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getSingleDetailedStrategicIndicatorsHistoricalEvaluation(dtoDetailedStrategicIndicator.getId(), projectExternalId, profileId, fromDate, toDate);


        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void getStrategicIndicatorsPrediction() throws IOException {
        // Given
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        dtoStrategicIndicatorEvaluation.setDatasource("Forecast");
        dtoStrategicIndicatorEvaluation.setRationale("Forecast");
        float first80 = 0.97473043f;
        float second80 = 0.9745246f;
        Pair<Float, Float> confidence80 = Pair.of(first80, second80);
        dtoStrategicIndicatorEvaluation.setConfidence80(confidence80);
        float first95 = 0.9747849f;
        float second95 = 0.97447014f;
        Pair<Float, Float> confidence95 = Pair.of(first95, second95);
        dtoStrategicIndicatorEvaluation.setConfidence95(confidence95);
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        String projectExternalId = "test";
        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";

        when(qmaForecast.ForecastSI(dtoStrategicIndicatorEvaluationList,technique, freq, horizon, projectExternalId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationListFound = strategicIndicatorsController.getStrategicIndicatorsPrediction(dtoStrategicIndicatorEvaluationList, technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(dtoStrategicIndicatorEvaluationList.size(), dtoStrategicIndicatorEvaluationListFound.size());
        assertEquals(dtoStrategicIndicatorEvaluation, dtoStrategicIndicatorEvaluationListFound.get(0));
    }

    @Test
    public void getDetailedStrategicIndicatorsPrediction() throws IOException {
        // Given
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));

        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        String projectExternalId = "test";
        String technique = "PROPHET";
        String horizon = "7";
        String freq = "7";

        when(qmaForecast.ForecastDSI(anyList(), eq(technique), eq(freq), eq(horizon), eq(projectExternalId))).thenReturn(dtoDetailedStrategicIndicatorList);

        // When
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorListFound = strategicIndicatorsController.getDetailedStrategicIndicatorsPrediction(dtoDetailedStrategicIndicatorList, technique, freq, horizon, projectExternalId);

        // Then
        assertEquals(dtoDetailedStrategicIndicatorList.size(), dtoDetailedStrategicIndicatorListFound.size());
        assertEquals(dtoDetailedStrategicIndicator, dtoDetailedStrategicIndicatorListFound.get(0));
    }

    @Test
    public void trainForecastModelsAllProjects() throws IOException, CategoriesException, ProjectNotFoundException {
        // Given
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        String profileId = "null"; // without profile
        projectsList.add(projectExternalId);

        when(projectsController.getAllProjectsExternalID()).thenReturn(projectsList);

        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);

        when(metricsController.getAllMetricsCurrentEvaluation(projectExternalId, profileId)).thenReturn(dtoMetricEvaluationList);

        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);

        when(factorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, profileId, false)).thenReturn(dtoDetailedFactorEvaluationList);

        String technique = "PROPHET";

        // When
        strategicIndicatorsController.trainForecastModelsAllProjects(technique);

        // Then
        verify(qmaForecast, times(1)).trainMetricForecast(dtoMetricEvaluationList, "7", projectExternalId, technique);
        verify(qmaForecast, times(1)).trainFactorForecast(dtoDetailedFactorEvaluationList, "7", projectExternalId, technique);
    }

    @Test
    public void trainForecastModelsSingleProject() throws IOException, CategoriesException, ProjectNotFoundException {
        // Given
        String projectExternalId = "test";
        String profileId = "null"; // without profile

        DTOMetricEvaluation dtoMetricEvaluation = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetricEvaluation> dtoMetricEvaluationList = new ArrayList<>();
        dtoMetricEvaluationList.add(dtoMetricEvaluation);

        when(metricsController.getAllMetricsCurrentEvaluation(projectExternalId, profileId)).thenReturn(dtoMetricEvaluationList);

        DTODetailedFactorEvaluation dtoDetailedFactorEvaluation = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTODetailedFactorEvaluation> dtoDetailedFactorEvaluationList = new ArrayList<>();
        dtoDetailedFactorEvaluationList.add(dtoDetailedFactorEvaluation);

        when(factorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId, profileId, false)).thenReturn(dtoDetailedFactorEvaluationList);

        DTOStrategicIndicatorEvaluation dtoStrategicIndicator = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorList = new ArrayList<>();
        dtoStrategicIndicatorList.add(dtoStrategicIndicator);

        when(strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(projectExternalId, profileId)).thenReturn(dtoStrategicIndicatorList);


        String technique = "PROPHET";

        // When
        strategicIndicatorsController.trainForecastModelsSingleProject(projectExternalId, profileId, technique);

        // Then
        verify(qmaForecast, times(1)).trainMetricForecast(dtoMetricEvaluationList, "7", projectExternalId, technique);
        verify(qmaForecast, times(1)).trainFactorForecast(dtoDetailedFactorEvaluationList, "7", projectExternalId, technique);
        verify(qmaForecast, times(1)).trainStrategicIndicatorForecast(dtoStrategicIndicatorList, "7", projectExternalId, technique);
    }

    @Test
    public void assessStrategicIndicators() throws IOException, CategoriesException, ArithmeticException, ProjectNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOFactorEvaluation dtoFactorEvaluation1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "codequality";
        String factor2Name = "Code Quality";
        String factor2Description = "Quality of the implemented code";
        float factor2Value = 0.7f;
        DTOFactorEvaluation dtoFactorEvaluation2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation2.setId(factor2Id);
        dtoFactorEvaluation2.setName(factor2Name);
        dtoFactorEvaluation2.setDescription(factor2Description);
        dtoFactorEvaluation2.setValue(Pair.of(factor2Value,"Good"));

        String factor3Id = "softwarestability";
        String factor3Name = "Software Stability";
        String factor3Description = "Stability of the software under development";
        float factor3Value = 0.6f;
        DTOFactorEvaluation dtoFactorEvaluation3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation3.setId(factor3Id);
        dtoFactorEvaluation3.setName(factor3Name);
        dtoFactorEvaluation3.setDescription(factor3Description);
        dtoFactorEvaluation3.setValue(Pair.of(factor3Value,"Normal"));

        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation1);
        dtoFactorEvaluationList.add(dtoFactorEvaluation2);
        dtoFactorEvaluationList.add(dtoFactorEvaluation3);

        when(factorsController.getAllFactorsEvaluation(project.getExternalId(), null,false)).thenReturn(dtoFactorEvaluationList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", strategicIndicator.getProject());
        Factor factor3 =  new Factor("testingperformance", "Performance of testing phases", strategicIndicator.getProject());
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);

        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors(factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", strategicIndicator.getProject());
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", strategicIndicator.getProject());
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);

        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", strategicIndicator.getProject());
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", strategicIndicator.getProject());
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);

        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);

        when(strategicIndicatorRepository.findByProject_Id(project.getId())).thenReturn(strategic_indicatorList);

        when(factorsController.getFactorLabelFromValue(dtoFactorEvaluation1.getValue().getFirst())).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactorEvaluation1.getValue().getFirst());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(0.33333334f);
        factorWeightsList.add(0.33333334f);
        factorWeightsList.add(0.33333334f);

        List<Float> factorWeightedValuesList = new ArrayList<>();

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        int i = 0;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
            factorWeightedValuesList.add(factorValue * factorWeightsList.get(i));
            i++;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";

        when(assesSI.assesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(),ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(true);

        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(strategicIndicator.getQuality_factors()), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorWeightedValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()))).thenReturn(true);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicators(project.getExternalId(), null);

        // Then
        assertTrue(correct);

        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsController);

        verify(factorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorEvaluationList, project.getExternalId());
        verify(factorsController, times(1)).getAllFactorsEvaluation(project.getExternalId(), null,false);
        verify(factorsController, times(6)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(factorsController);

        verify(strategicIndicatorRepository, times(1)).findByProject_Id(project.getId());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).assesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verify(qmaStrategicIndicators, times(1)).prepareSIIndex(eq(project.getExternalId()));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(strategicIndicator.getQuality_factors()), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorWeightedValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicatorsNotCorrect() throws IOException, CategoriesException, ProjectNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOFactorEvaluation dtoFactorEvaluation1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "codequality";
        String factor2Name = "Code Quality";
        String factor2Description = "Quality of the implemented code";
        float factor2Value = 0.7f;
        DTOFactorEvaluation dtoFactorEvaluation2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation2.setId(factor2Id);
        dtoFactorEvaluation2.setName(factor2Name);
        dtoFactorEvaluation2.setDescription(factor2Description);
        dtoFactorEvaluation2.setValue(Pair.of(factor2Value,"Good"));

        String factor3Id = "softwarestability";
        String factor3Name = "Software Stability";
        String factor3Description = "Stability of the software under development";
        float factor3Value = 0.6f;
        DTOFactorEvaluation dtoFactorEvaluation3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation3.setId(factor3Id);
        dtoFactorEvaluation3.setName(factor3Name);
        dtoFactorEvaluation3.setDescription(factor3Description);
        dtoFactorEvaluation3.setValue(Pair.of(factor3Value,"Normal"));

        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation1);
        dtoFactorEvaluationList.add(dtoFactorEvaluation2);
        dtoFactorEvaluationList.add(dtoFactorEvaluation3);

        when(factorsController.getAllFactorsEvaluation(project.getExternalId(), null,false)).thenReturn(dtoFactorEvaluationList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", strategicIndicator.getProject());
        Factor factor3 =  new Factor("testingperformance", "Performance of testing phases", strategicIndicator.getProject());
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);

        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors(factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", strategicIndicator.getProject());
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", strategicIndicator.getProject());
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);

        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", strategicIndicator.getProject());
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", strategicIndicator.getProject());
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);

        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);

        when(strategicIndicatorRepository.findByProject_Id(project.getId())).thenReturn(strategic_indicatorList);

        when(factorsController.getFactorLabelFromValue(dtoFactorEvaluation1.getValue().getFirst())).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactorEvaluation1.getValue().getFirst());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";

        when(assesSI.assesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        //when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), eq(strategicIndicatorRationale), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(false);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicators(project.getExternalId(), null);

        // Then
        assertFalse(correct);

        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());
        verifyNoMoreInteractions(projectsController);

        verify(factorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorEvaluationList, project.getExternalId());
        verify(factorsController, times(1)).getAllFactorsEvaluation(project.getExternalId(), null,false);
        verify(factorsController, times(3)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(factorsController);

        verify(strategicIndicatorRepository, times(1)).findByProject_Id(project.getId());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).assesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verify(qmaStrategicIndicators, times(1)).prepareSIIndex(eq(project.getExternalId()));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyZeroInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicator () throws IOException, ProjectNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOFactorEvaluation dtoFactorEvaluation1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "codequality";
        String factor2Name = "Code Quality";
        String factor2Description = "Quality of the implemented code";
        float factor2Value = 0.7f;
        DTOFactorEvaluation dtoFactorEvaluation2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation2.setId(factor2Id);
        dtoFactorEvaluation2.setName(factor2Name);
        dtoFactorEvaluation2.setDescription(factor2Description);
        dtoFactorEvaluation2.setValue(Pair.of(factor2Value,"Good"));

        String factor3Id = "softwarestability";
        String factor3Name = "Software Stability";
        String factor3Description = "Stability of the software under development";
        float factor3Value = 0.6f;
        DTOFactorEvaluation dtoFactorEvaluation3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation3.setId(factor3Id);
        dtoFactorEvaluation3.setName(factor3Name);
        dtoFactorEvaluation3.setDescription(factor3Description);
        dtoFactorEvaluation3.setValue(Pair.of(factor3Value,"Normal"));

        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation1);
        dtoFactorEvaluationList.add(dtoFactorEvaluation2);
        dtoFactorEvaluationList.add(dtoFactorEvaluation3);

        when(factorsController.getAllFactorsEvaluation(project.getExternalId(), null,false)).thenReturn(dtoFactorEvaluationList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", strategicIndicator.getProject());
        Factor factor3 =  new Factor("testingperformance", "Performance of testing phases", strategicIndicator.getProject());
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);

        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors(factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", strategicIndicator.getProject());
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", strategicIndicator.getProject());
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);

        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", strategicIndicator.getProject());
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", strategicIndicator.getProject());
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);

        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        when(strategicIndicatorRepository.findByNameAndProject_Id(strategicIndicatorName, project.getId())).thenReturn(strategicIndicator);

        when(factorsController.getFactorLabelFromValue(dtoFactorEvaluation1.getValue().getFirst())).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactorEvaluation1.getValue().getFirst());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(0.33333334f);
        factorWeightsList.add(0.33333334f);
        factorWeightsList.add(0.33333334f);

        List<Float> factorWeightedValuesList = new ArrayList<>();

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        int i = 0;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
            factorWeightedValuesList.add(factorValue*factorWeightsList.get(i));
            i++;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";

        when(assesSI.assesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(true);

        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(strategicIndicator.getQuality_factors()), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorWeightedValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()))).thenReturn(true);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicator(strategicIndicator.getName(), project.getExternalId());

        // Then
        assertTrue(correct);

        verify(factorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorEvaluationList, project.getExternalId());
        verify(factorsController, times(1)).getAllFactorsEvaluation(project.getExternalId(), null,false);
        verify(factorsController, times(6)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(factorsController);

        verify(strategicIndicatorRepository, times(1)).findByNameAndProject_Id(strategicIndicator.getName(), project.getId());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).assesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(strategicIndicator.getQuality_factors()), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorWeightedValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicatorNotCorrect () throws IOException, ProjectNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOFactorEvaluation dtoFactorEvaluation1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "codequality";
        String factor2Name = "Code Quality";
        String factor2Description = "Quality of the implemented code";
        float factor2Value = 0.7f;
        DTOFactorEvaluation dtoFactorEvaluation2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation2.setId(factor2Id);
        dtoFactorEvaluation2.setName(factor2Name);
        dtoFactorEvaluation2.setDescription(factor2Description);
        dtoFactorEvaluation2.setValue(Pair.of(factor2Value, "Good"));

        String factor3Id = "softwarestability";
        String factor3Name = "Software Stability";
        String factor3Description = "Stability of the software under development";
        float factor3Value = 0.6f;
        DTOFactorEvaluation dtoFactorEvaluation3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation3.setId(factor3Id);
        dtoFactorEvaluation3.setName(factor3Name);
        dtoFactorEvaluation3.setDescription(factor3Description);
        dtoFactorEvaluation3.setValue(Pair.of(factor3Value,"Normal"));

        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation1);
        dtoFactorEvaluationList.add(dtoFactorEvaluation2);
        dtoFactorEvaluationList.add(dtoFactorEvaluation3);

        // without profile & don't filter DB
        when(factorsController.getAllFactorsEvaluation(project.getExternalId(), null,false)).thenReturn(dtoFactorEvaluationList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, project);
        strategicIndicator.setId(strategicIndicatorId);

        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();

        List<QualityFactorMetrics> qualityMetrics3 = new ArrayList<>();
        Metric metric3 = new Metric("fasttests","Fast Tests", "Percentage of tests under the testing duration threshold", strategicIndicator.getProject());
        Factor factor3 =  new Factor("testingperformance", "Performance of testing phases", strategicIndicator.getProject());
        QualityFactorMetrics qfm3 = new QualityFactorMetrics(-1f, metric3, factor3);
        qfm3.setId(3L);
        qualityMetrics3.add(qfm3);
        factor3.setQualityFactorMetricsList(qualityMetrics3);
        factor3.setWeighted(false);

        Long siqf3Id = 3L;
        StrategicIndicatorQualityFactors siqf3 = new StrategicIndicatorQualityFactors(factor3, -1, strategicIndicator);
        siqf3.setId(siqf3Id);
        qualityFactors.add(siqf3);

        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code", strategicIndicator.getProject());
        Factor factor1 =  new Factor("codequality", "Quality of the implemented code", strategicIndicator.getProject());
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);

        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        List<QualityFactorMetrics> qualityMetrics2 = new ArrayList<>();
        Metric metric2 = new Metric("bugdensity","Bugdensity", "Density of files without bugs", strategicIndicator.getProject());
        Factor factor2 =  new Factor("softwarestability", "Stability of the software under development", strategicIndicator.getProject());
        QualityFactorMetrics qfm2 = new QualityFactorMetrics(-1f, metric2, factor2);
        qfm2.setId(2L);
        qualityMetrics2.add(qfm2);
        factor2.setQualityFactorMetricsList(qualityMetrics2);
        factor2.setWeighted(false);

        Long siqf2Id = 2L;
        StrategicIndicatorQualityFactors siqf2 = new StrategicIndicatorQualityFactors( factor2, -1, strategicIndicator);
        siqf2.setId(siqf2Id);
        qualityFactors.add(siqf2);

        strategicIndicator.setStrategicIndicatorQualityFactorsList(qualityFactors);
        strategicIndicator.setWeighted(false);

        when(strategicIndicatorRepository.findByNameAndProject_Id(strategicIndicatorName, project.getId())).thenReturn(strategicIndicator);

        when(factorsController.getFactorLabelFromValue(dtoFactorEvaluation1.getValue().getFirst())).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(factorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactorEvaluation1.getValue().getFirst());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        String strategicIndicatorRationale = "factors: {...}, formula: ..., value: ..., category: ...";

        when(assesSI.assesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        //when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), eq(strategicIndicatorRationale), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(false);

        // When

        boolean correct = strategicIndicatorsController.assessStrategicIndicator(strategicIndicator.getName(), project.getExternalId());

        // Then
        assertFalse(correct);

        verify(factorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorEvaluationList, project.getExternalId());
        verify(factorsController, times(1)).getAllFactorsEvaluation(project.getExternalId(), null,false);
        verify(factorsController, times(3)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(factorsController);

        verify(strategicIndicatorRepository, times(1)).findByNameAndProject_Id(strategicIndicator.getName(), project.getId());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).assesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), anyString(), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyZeroInteractions(qmaRelations);
    }

    @Test
    public void getValueAndLabelFromCategories() {
        // Given
        List<DTOAssessment> dtoSIAssessmentList = domainObjectsBuilder.buildDTOSIAssessmentList();

        List<SICategory> categoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(categoryList);

        // When
        Pair<Float, String> valueLabelPair = strategicIndicatorsController.getValueAndLabelFromCategories(dtoSIAssessmentList);

        // Then
        float expectedValue = 0.83f;
        String expectedLabel = "Good";
        assertEquals(expectedValue, valueLabelPair.getFirst(), 0.01f);
        assertEquals(expectedLabel, valueLabelPair.getSecond());
    }

    @Test
    public void getValueFromLabelGood() {
        // Given
        List<SICategory> categoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(categoryList);

        // When
        float value = strategicIndicatorsController.getValueFromLabel("Good");

        // Then
        int categoryPosition = 3;
        float categorySpan = 1f / categoryList.size();
        float expectedValue = categorySpan * categoryPosition - categorySpan / 2f;
        assertEquals(expectedValue, value, 0.000001f);
    }

    @Test
    public void getValueFromLabelNeutral() {
        // Given
        List<SICategory> categoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(categoryList);

        // When
        float value = strategicIndicatorsController.getValueFromLabel("Neutral");

        // Then
        int categoryPosition = 2;
        float categorySpan = 1f / categoryList.size();
        float expectedValue = categorySpan * categoryPosition - categorySpan / 2f;
        assertEquals(expectedValue, value, 0.000001f);
    }

    @Test
    public void getValueFromLabelBad() {
        // Given
        List<SICategory> categoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(categoryList);

        // When
        float value = strategicIndicatorsController.getValueFromLabel("Bad");

        // Then
        int categoryPosition = 1;
        float categorySpan = 1f / categoryList.size();
        float expectedValue = categorySpan * categoryPosition - categorySpan / 2f;
        assertEquals(expectedValue, value, 0.000001f);
    }

    @Test
    public void fetchStrategicIndicators() throws IOException, CategoriesException, ProjectNotFoundException, QualityFactorNotFoundException, StrategicIndicatorQualityFactorNotFoundException, StrategicIndicatorNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        List<String> projectsList = new ArrayList<>();
        projectsList.add(project.getExternalId());

        when(projectsController.getAllProjectsExternalID()).thenReturn(projectsList);
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        DTODetailedStrategicIndicatorEvaluation dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicatorEvaluation(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorEvaluationList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactorEvaluation.getValue().getFirst(), "Good"));
        List<DTODetailedStrategicIndicatorEvaluation> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, project.getExternalId(), null,false)).thenReturn(dtoDetailedStrategicIndicatorList);
        for (DTOFactorEvaluation qf : dtoFactorEvaluationList) {
            Factor f = new Factor(qf.getId(), qf.getDescription(), project);
            f.setId(1L);
            when(factorsController.findFactorByExternalIdAndProjectId(any(String.class), eq(project.getId()))).thenReturn(f);
        }

        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicatorForSimulation(project);
        List<StrategicIndicatorQualityFactors> qualityFactors = new ArrayList<>();
        // define factor1 with its metric composition
        List<QualityFactorMetrics> qualityMetrics1 = new ArrayList<>();
        Metric metric1 = new Metric("duplication","Duplication", "Density of non-duplicated code",project);
        metric1.setId(1L);
        Factor factor1 =  new Factor("testingperformance", "Performance of the tests", project);
        factor1.setId(1L);
        QualityFactorMetrics qfm1 = new QualityFactorMetrics(-1f, metric1, factor1);
        qfm1.setId(1L);
        qualityMetrics1.add(qfm1);
        factor1.setQualityFactorMetricsList(qualityMetrics1);
        factor1.setWeighted(false);
        // define si with factor1 union
        Long siqf1Id = 1L;
        StrategicIndicatorQualityFactors siqf1 = new StrategicIndicatorQualityFactors(factor1, -1, strategicIndicator);
        siqf1.setId(siqf1Id);
        qualityFactors.add(siqf1);

        for (StrategicIndicatorQualityFactors qf : qualityFactors) {
            when(factorsController.getQualityFactorById(eq(qf.getFactor().getId()))).thenReturn(qf.getFactor());
            when(strategicIndicatorQualityFactorsController.saveStrategicIndicatorQualityFactor(eq(qf.getFactor()),eq(qf.getWeight()), any(Strategic_Indicator.class))).thenReturn(qf);
        }

        // When
        strategicIndicatorsController.fetchStrategicIndicators();

        // Then
        verify(projectsController, times(1)).getAllProjectsExternalID();
        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());

        ArgumentCaptor<Strategic_Indicator> argumentSI = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(2)).save(argumentSI.capture());
        verify(strategicIndicatorRepository, times(1)).findByNameAndProject_Id(anyString(),anyLong());
        Strategic_Indicator strategicIndicatorSaved = argumentSI.getValue();
        assertEquals(dtoStrategicIndicatorEvaluation.getName(), strategicIndicatorSaved.getName());
        assertEquals("", strategicIndicatorSaved.getDescription());
        List<String> factorIds = new ArrayList<>();
        factorIds.add(dtoFactorEvaluation.getId());
        assertEquals(factorIds, strategicIndicatorSaved.getQuality_factors());

        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void simulateStrategicIndicatorsAssessment() throws IOException, ProjectNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOFactorEvaluation dtoFactorEvaluation = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation);
        when(factorsController.getAllFactorsEvaluation(project.getExternalId(), null, true)).thenReturn(dtoFactorEvaluationList);

        Map<String, Float> factorSimulatedMap = new HashMap<>();
        Float factorSimulatedValue = 0.9f;
        factorSimulatedMap.put(dtoFactorEvaluation.getId(), factorSimulatedValue);
        when(factorsController.getFactorLabelFromValue(factorSimulatedValue)).thenReturn("Good");

        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicatorForSimulation(project);

        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);
        when(strategicIndicatorRepository.findByProject_IdOrderByName(project.getId())).thenReturn(strategic_indicatorList);

        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = strategicIndicatorsController.simulateStrategicIndicatorsAssessment(factorSimulatedMap, project.getExternalId(), null); // without profile

        // Verify mock interactions
        verify(factorsController, times(1)).getAllFactorsEvaluation(project.getExternalId(), null,true);
        verify(factorsController, times(2)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(factorsController);
        
        verify(strategicIndicatorRepository, times(1)).findByProject_IdOrderByName(project.getId());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(siCategoryRepository, times(2)).findAll();
        verifyNoMoreInteractions(siCategoryRepository);

        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluationFound = dtoStrategicIndicatorEvaluationList.get(0);
        String strategicIndicatorNameLowerCase = strategicIndicator.getName().replaceAll("\\s+","").toLowerCase();
        assertEquals(strategicIndicatorNameLowerCase, dtoStrategicIndicatorEvaluationFound.getId());
        assertEquals(strategicIndicator.getName(), dtoStrategicIndicatorEvaluationFound.getName());
        assertEquals(strategicIndicator.getDescription(), dtoStrategicIndicatorEvaluationFound.getDescription());
        assertEquals(factorSimulatedValue, dtoStrategicIndicatorEvaluationFound.getValue().getFirst(), 0f);
        assertEquals("Good", dtoStrategicIndicatorEvaluationFound.getValue().getSecond());
        assertEquals("Simulation", dtoStrategicIndicatorEvaluationFound.getDatasource());
        assertEquals(strategicIndicator.getId(), dtoStrategicIndicatorEvaluationFound.getDbId(), 0f);
    }


    @Test
    public void computeStrategicIndicatorValue() {
        // Given
        DTOFactorEvaluation dtoFactorEvaluation1 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation1.setValue(Pair.of(0.7f,"Good"));
        DTOFactorEvaluation dtoFactorEvaluation2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation2.setValue(Pair.of(0.8f,"Good"));
        DTOFactorEvaluation dtoFactorEvaluation3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactorEvaluation3.setValue(Pair.of(0.9f,"Good"));
        List<DTOFactorEvaluation> dtoFactorEvaluationList = new ArrayList<>();
        dtoFactorEvaluationList.add(dtoFactorEvaluation1);
        dtoFactorEvaluationList.add(dtoFactorEvaluation2);
        dtoFactorEvaluationList.add(dtoFactorEvaluation3);

        // When
        float value = strategicIndicatorsController.computeStrategicIndicatorValue(dtoFactorEvaluationList);

        // Then
        float expectedValue = (dtoFactorEvaluation1.getValue().getFirst() + dtoFactorEvaluation2.getValue().getFirst() + dtoFactorEvaluation3.getValue().getFirst()) / dtoFactorEvaluationList.size();
        assertEquals(expectedValue, value, 0f);
    }

    @Test
    public void getLabelGood() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        String label = strategicIndicatorsController.getLabel(0.8f);

        // Then
        assertEquals("Good", label);
    }

    @Test
    public void getLabelNeutral() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        String label = strategicIndicatorsController.getLabel(0.5f);

        // Then
        assertEquals("Neutral", label);
    }

    @Test
    public void getLabelBad() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        String label = strategicIndicatorsController.getLabel(0.2f);

        // Then
        assertEquals("Bad", label);
    }

    @Test
    public void getLabelNoCategory() {
        // Given
        when(siCategoryRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        String label = strategicIndicatorsController.getLabel(0.8f);

        // Then
        assertEquals("No Category", label);
    }

    @Test
    public void getCategories() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        List<DTOAssessment> dtoSIAssessmentList = strategicIndicatorsController.getCategories();

        // Then
        int expectedNumberOfElements = 3;
        assertEquals(expectedNumberOfElements, dtoSIAssessmentList.size());

        DTOAssessment dtoSIAssessment1 = dtoSIAssessmentList.get(0);
        assertEquals(siCategoryList.get(0).getId(), dtoSIAssessment1.getId());
        assertEquals(siCategoryList.get(0).getName(), dtoSIAssessment1.getLabel());
        assertEquals(siCategoryList.get(0).getColor(), dtoSIAssessment1.getColor());
        assertEquals(1f, dtoSIAssessment1.getUpperThreshold(), 0f);

        DTOAssessment dtoSIAssessment2 = dtoSIAssessmentList.get(1);
        assertEquals(siCategoryList.get(1).getId(), dtoSIAssessment2.getId());
        assertEquals(siCategoryList.get(1).getName(), dtoSIAssessment2.getLabel());
        assertEquals(siCategoryList.get(1).getColor(), dtoSIAssessment2.getColor());
        assertEquals(0.66f, dtoSIAssessment2.getUpperThreshold(), 0.01f);

        DTOAssessment dtoSIAssessment3 = dtoSIAssessmentList.get(2);
        assertEquals(siCategoryList.get(2).getId(), dtoSIAssessment3.getId());
        assertEquals(siCategoryList.get(2).getName(), dtoSIAssessment3.getLabel());
        assertEquals(siCategoryList.get(2).getColor(), dtoSIAssessment3.getColor());
        assertEquals(0.33f, dtoSIAssessment3.getUpperThreshold(), 0.01f);
    }

    @Test
    public void buildDescriptiveLabelAndValue() {
        // Given
        Float value = 0.8f;
        String label = "Good";

        // When
        String descriptiveLabel = StrategicIndicatorsController.buildDescriptiveLabelAndValue(Pair.of(value, label));

        // Then
        assertEquals("Good (0.80)", descriptiveLabel);
    }

    @Test
    public void buildDescriptiveLabelAndValueNoLabel() {
        // Given
        Float value = 0.8f;
        String label = "";

        // When
        String descriptiveLabel = StrategicIndicatorsController.buildDescriptiveLabelAndValue(Pair.of(value, label));

        // Then
        assertEquals("0.80", descriptiveLabel);
    }

    @Test
    public void getColorFromLabel() {
        // Given
        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findByName(siCategoryList.get(0).getName())).thenReturn(siCategoryList.get(0));

        // When
        String color = strategicIndicatorsController.getColorFromLabel(siCategoryList.get(0).getName());

        // Then
        assertEquals(siCategoryList.get(0).getColor(), color);
    }
}