package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMARelations;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.*;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.exceptions.StrategicIndicatorNotFoundException;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private ProjectsController projectsController;

    @Mock
    private MetricsController metricsController;

    @Mock
    private QualityFactorsController qualityFactorsController;

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
    public void saveStrategicIndicator() throws IOException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");

        // When
        strategicIndicatorsController.saveStrategicIndicator(strategicIndicator.getName(), strategicIndicator.getDescription(), Files.readAllBytes(networkFile.toPath()), strategicIndicator.getQuality_factors(), project);

        // Then
        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicator.getName(), strategicIndicatorSaved.getName());
        assertEquals(strategicIndicator.getDescription(), strategicIndicatorSaved.getDescription());
        assertEquals(strategicIndicator.getQuality_factors(), strategicIndicatorSaved.getQuality_factors());
    }

    @Test
    public void editStrategicIndicator() throws IOException, StrategicIndicatorNotFoundException {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        when(strategicIndicatorRepository.findById(strategicIndicator.getId())).thenReturn(Optional.of(strategicIndicator));

        // When
        strategicIndicatorsController.editStrategicIndicator(strategicIndicator.getId(), strategicIndicator.getName(), strategicIndicator.getDescription(), Files.readAllBytes(networkFile.toPath()), strategicIndicator.getQuality_factors());

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

    @Test
    public void trainForecastModelsAllProjects() throws IOException, CategoriesException {
        // Given
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        when(projectsController.getAllProjects()).thenReturn(projectsList);

        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        when(metricsController.getAllMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoMetricList);

        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        when(qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoQualityFactorList);

        String technique = "PROPHET";

        // When
        strategicIndicatorsController.trainForecastModelsAllProjects(technique);

        // Then
        verify(qmaForecast, times(1)).trainMetricForecast(dtoMetricList, "7", projectExternalId, technique);
        verify(qmaForecast, times(1)).trainFactorForecast(dtoQualityFactorList, "7", projectExternalId, technique);
    }

    @Test
    public void trainForecastModelsSingleProject() throws IOException {
        // Given
        String projectExternalId = "test";

        DTOMetric dtoMetric = domainObjectsBuilder.buildDTOMetric();
        List<DTOMetric> dtoMetricList = new ArrayList<>();
        dtoMetricList.add(dtoMetric);

        when(metricsController.getAllMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoMetricList);

        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);

        when(qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId)).thenReturn(dtoQualityFactorList);

        String technique = "PROPHET";

        // When
        strategicIndicatorsController.trainForecastModelsSingleProject(projectExternalId, technique);

        // Then
        verify(qmaForecast, times(1)).trainMetricForecast(dtoMetricList, "7", projectExternalId, technique);
        verify(qmaForecast, times(1)).trainFactorForecast(dtoQualityFactorList, "7", projectExternalId, technique);
    }

    @Test
    public void assessStrategicIndicators() throws IOException, CategoriesException {
        Project project = domainObjectsBuilder.buildProject();

        DTOFactor dtoFactor1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        float factor2Value = 0.7f;
        DTOFactor dtoFactor2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor2.setId(factor2Id);
        dtoFactor2.setName(factor2Name);
        dtoFactor2.setDescription(factor2Description);
        dtoFactor2.setValue(factor2Value);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        float factor3Value = 0.6f;
        DTOFactor dtoFactor3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor3.setId(factor3Id);
        dtoFactor3.setName(factor3Name);
        dtoFactor3.setDescription(factor3Description);
        dtoFactor3.setValue(factor3Value);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qualityFactorsController.getAllFactorsEvaluation(project.getExternalId())).thenReturn(dtoFactorList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "testingperformance";
        qualityFactors.add(factor1);
        String factor2 = "developmentspeed";
        qualityFactors.add(factor2);
        String factor3 = "externalquality";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);
        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);

        when(strategicIndicatorRepository.findAll()).thenReturn(strategic_indicatorList);

        when(qualityFactorsController.getFactorLabelFromValue(dtoFactor1.getValue())).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(true);

        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(qualityFactors), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()))).thenReturn(true);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicators(project.getExternalId(), null);

        // Then
        assertTrue(correct);

        verify(qualityFactorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, project.getExternalId());
        verify(qualityFactorsController, times(1)).getAllFactorsEvaluation(project.getExternalId());
        verify(qualityFactorsController, times(6)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(qualityFactorsController);

        verify(strategicIndicatorRepository, times(1)).findAll();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(qualityFactors), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicatorsNotCorrect() throws IOException, CategoriesException {
        Project project = domainObjectsBuilder.buildProject();

        DTOFactor dtoFactor1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        float factor2Value = 0.7f;
        DTOFactor dtoFactor2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor2.setId(factor2Id);
        dtoFactor2.setName(factor2Name);
        dtoFactor2.setDescription(factor2Description);
        dtoFactor2.setValue(factor2Value);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        float factor3Value = 0.6f;
        DTOFactor dtoFactor3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor3.setId(factor3Id);
        dtoFactor3.setName(factor3Name);
        dtoFactor3.setDescription(factor3Description);
        dtoFactor3.setValue(factor3Value);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qualityFactorsController.getAllFactorsEvaluation(project.getExternalId())).thenReturn(dtoFactorList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "testingperformance";
        qualityFactors.add(factor1);
        String factor2 = "developmentspeed";
        qualityFactors.add(factor2);
        String factor3 = "externalquality";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);
        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);

        when(strategicIndicatorRepository.findAll()).thenReturn(strategic_indicatorList);

        when(qualityFactorsController.getFactorLabelFromValue(dtoFactor1.getValue())).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(false);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicators(project.getExternalId(), null);

        // Then
        assertFalse(correct);

        verify(qualityFactorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, project.getExternalId());
        verify(qualityFactorsController, times(1)).getAllFactorsEvaluation(project.getExternalId());
        verify(qualityFactorsController, times(3)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(qualityFactorsController);

        verify(strategicIndicatorRepository, times(1)).findAll();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyZeroInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicator () throws IOException, CategoriesException {
        Project project = domainObjectsBuilder.buildProject();
        List<String> projectList = new ArrayList<>();
        projectList.add(project.getExternalId());
        when(projectsController.getAllProjects()).thenReturn(projectList);

        DTOFactor dtoFactor1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        float factor2Value = 0.7f;
        DTOFactor dtoFactor2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor2.setId(factor2Id);
        dtoFactor2.setName(factor2Name);
        dtoFactor2.setDescription(factor2Description);
        dtoFactor2.setValue(factor2Value);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        float factor3Value = 0.6f;
        DTOFactor dtoFactor3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor3.setId(factor3Id);
        dtoFactor3.setName(factor3Name);
        dtoFactor3.setDescription(factor3Description);
        dtoFactor3.setValue(factor3Value);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qualityFactorsController.getAllFactorsEvaluation(project.getExternalId())).thenReturn(dtoFactorList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "testingperformance";
        qualityFactors.add(factor1);
        String factor2 = "developmentspeed";
        qualityFactors.add(factor2);
        String factor3 = "externalquality";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        when(qualityFactorsController.getFactorLabelFromValue(dtoFactor1.getValue())).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(true);

        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(qualityFactors), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()))).thenReturn(true);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicator(strategicIndicator.getName());

        // Then
        assertTrue(correct);

        verify(qualityFactorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, project.getExternalId());
        verify(qualityFactorsController, times(1)).getAllFactorsEvaluation(project.getExternalId());
        verify(qualityFactorsController, times(6)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(qualityFactorsController);

        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicator.getName());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(project.getExternalId()), eq(qualityFactors), eq(strategicIndicator.getExternalId()), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assessStrategicIndicatorNotCorrect () throws IOException, CategoriesException {
        Project project = domainObjectsBuilder.buildProject();
        List<String> projectList = new ArrayList<>();
        projectList.add(project.getExternalId());
        when(projectsController.getAllProjects()).thenReturn(projectList);

        DTOFactor dtoFactor1 = domainObjectsBuilder.buildDTOFactor();

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        float factor2Value = 0.7f;
        DTOFactor dtoFactor2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor2.setId(factor2Id);
        dtoFactor2.setName(factor2Name);
        dtoFactor2.setDescription(factor2Description);
        dtoFactor2.setValue(factor2Value);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        float factor3Value = 0.6f;
        DTOFactor dtoFactor3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor3.setId(factor3Id);
        dtoFactor3.setName(factor3Name);
        dtoFactor3.setDescription(factor3Description);
        dtoFactor3.setValue(factor3Value);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qualityFactorsController.getAllFactorsEvaluation(project.getExternalId())).thenReturn(dtoFactorList);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "testingperformance";
        qualityFactors.add(factor1);
        String factor2 = "developmentspeed";
        qualityFactors.add(factor2);
        String factor3 = "externalquality";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        when(qualityFactorsController.getFactorLabelFromValue(dtoFactor1.getValue())).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor2Value)).thenReturn("Good");
        when(qualityFactorsController.getFactorLabelFromValue(factor3Value)).thenReturn("Neutral");

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(factor2Value);
        factorValuesList.add(factor3Value);

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(false);

        // When
        boolean correct = strategicIndicatorsController.assessStrategicIndicator(strategicIndicator.getName());

        // Then
        assertFalse(correct);

        verify(qualityFactorsController, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, project.getExternalId());
        verify(qualityFactorsController, times(1)).getAllFactorsEvaluation(project.getExternalId());
        verify(qualityFactorsController, times(3)).getFactorLabelFromValue(anyFloat());
        verifyNoMoreInteractions(qualityFactorsController);

        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicator.getName());
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(project.getExternalId()), eq(strategicIndicator.getExternalId()), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyZeroInteractions(qmaRelations);
    }

    @Test
    public void getValueAndLabelFromCategories() {
        // Given
        List<DTOSIAssessment> dtoSIAssessmentList = domainObjectsBuilder.buildDTOSIAssessmentList();

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
    public void fetchStrategicIndicators() throws IOException, CategoriesException, ProjectNotFoundException {
        Project project = domainObjectsBuilder.buildProject();
        List<String> projectsList = new ArrayList<>();
        projectsList.add(project.getExternalId());

        when(projectsController.importProjectsAndUpdateDatabase()).thenReturn(projectsList);
        when(projectsController.findProjectByExternalId(project.getExternalId())).thenReturn(project);

        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = domainObjectsBuilder.buildDTOStrategicIndicatorEvaluation();
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(dtoStrategicIndicatorEvaluation.getId(), dtoStrategicIndicatorEvaluation.getName(), dtoFactorList);
        dtoDetailedStrategicIndicator.setDate(dtoStrategicIndicatorEvaluation.getDate());
        dtoDetailedStrategicIndicator.setValue(Pair.of(dtoFactor.getValue(), "Good"));
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, project.getExternalId())).thenReturn(dtoDetailedStrategicIndicatorList);

        when(strategicIndicatorRepository.existsByExternalIdAndProject_Id(dtoStrategicIndicatorEvaluation.getId(), project.getId())).thenReturn(false);

        // When
        strategicIndicatorsController.fetchStrategicIndicators();

        // Then
        verify(projectsController, times(1)).importProjectsAndUpdateDatabase();
        verify(projectsController, times(1)).findProjectByExternalId(project.getExternalId());
        verify(strategicIndicatorRepository, times(1)).existsByExternalIdAndProject_Id(dtoStrategicIndicatorEvaluation.getId(), project.getId());

        ArgumentCaptor<Strategic_Indicator> argumentSI = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argumentSI.capture());
        Strategic_Indicator strategicIndicatorSaved = argumentSI.getValue();
        assertEquals(dtoStrategicIndicatorEvaluation.getName(), strategicIndicatorSaved.getName());
        assertEquals("", strategicIndicatorSaved.getDescription());
        List<String> factorIds = new ArrayList<>();
        factorIds.add(dtoFactor.getId());
        assertEquals(factorIds, strategicIndicatorSaved.getQuality_factors());

        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void simulateStrategicIndicatorsAssessment() throws IOException {
        // Given
        Project project = domainObjectsBuilder.buildProject();

        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        when(qualityFactorsController.getAllFactorsEvaluation(project.getExternalId())).thenReturn(dtoFactorList);

        Map<String, Float> factorSimulatedMap = new HashMap<>();
        Float factorSimulatedValue = 0.9f;
        factorSimulatedMap.put(dtoFactor.getId(), factorSimulatedValue);
        when(qualityFactorsController.getFactorLabelFromValue(factorSimulatedValue)).thenReturn("Good");

        Strategic_Indicator strategicIndicator = domainObjectsBuilder.buildStrategicIndicator(project);
        strategicIndicator.getQuality_factors().add(dtoFactor.getId());
        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);
        when(strategicIndicatorRepository.findAll()).thenReturn(strategic_indicatorList);

        List<SICategory> siCategoryList = domainObjectsBuilder.buildSICategoryList();
        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        // When
        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = strategicIndicatorsController.simulateStrategicIndicatorsAssessment(factorSimulatedMap, project.getExternalId());

        // Verify mock interactions
        verify(qualityFactorsController, times(1)).getAllFactorsEvaluation(project.getExternalId());
        verify(qualityFactorsController, times(1)).getFactorLabelFromValue(factorSimulatedValue);
        verifyNoMoreInteractions(qualityFactorsController);

        verify(strategicIndicatorRepository, times(1)).findAll();
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
        DTOFactor dtoFactor1 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor1.setValue(0.7f);
        DTOFactor dtoFactor2 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor2.setValue(0.8f);
        DTOFactor dtoFactor3 = domainObjectsBuilder.buildDTOFactor();
        dtoFactor3.setValue(0.9f);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        // When
        float value = strategicIndicatorsController.computeStrategicIndicatorValue(dtoFactorList);

        // Then
        float expectedValue = (dtoFactor1.getValue() + dtoFactor2.getValue() + dtoFactor3.getValue()) / dtoFactorList.size();
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
        List<DTOSIAssessment> dtoSIAssessmentList = strategicIndicatorsController.getCategories();

        // Then
        int expectedNumberOfElements = 3;
        assertEquals(expectedNumberOfElements, dtoSIAssessmentList.size());

        DTOSIAssessment dtoSIAssessment1 = dtoSIAssessmentList.get(0);
        assertEquals(siCategoryList.get(0).getId(), dtoSIAssessment1.getId());
        assertEquals(siCategoryList.get(0).getName(), dtoSIAssessment1.getLabel());
        assertEquals(siCategoryList.get(0).getColor(), dtoSIAssessment1.getColor());
        assertEquals(1f, dtoSIAssessment1.getUpperThreshold(), 0f);

        DTOSIAssessment dtoSIAssessment2 = dtoSIAssessmentList.get(1);
        assertEquals(siCategoryList.get(1).getId(), dtoSIAssessment2.getId());
        assertEquals(siCategoryList.get(1).getName(), dtoSIAssessment2.getLabel());
        assertEquals(siCategoryList.get(1).getColor(), dtoSIAssessment2.getColor());
        assertEquals(0.66f, dtoSIAssessment2.getUpperThreshold(), 0.01f);

        DTOSIAssessment dtoSIAssessment3 = dtoSIAssessmentList.get(2);
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
        assertEquals("Good (0,80)", descriptiveLabel);
    }

    @Test
    public void buildDescriptiveLabelAndValueNoLabel() {
        // Given
        Float value = 0.8f;
        String label = "";

        // When
        String descriptiveLabel = StrategicIndicatorsController.buildDescriptiveLabelAndValue(Pair.of(value, label));

        // Then
        assertEquals("0,80", descriptiveLabel);
    }

}