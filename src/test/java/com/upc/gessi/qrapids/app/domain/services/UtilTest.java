package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.*;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOMilestone;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsFactor;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsMetric;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UtilTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private QMAStrategicIndicators qmaStrategicIndicators;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @Mock
    private SICategoryRepository siCategoryRepository;

    @Mock
    private QFCategoryRepository qfCategoryRepository;

    @Mock
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Mock
    private QMADetailedStrategicIndicators qmaDetailedStrategicIndicators;

    @Mock
    private Forecast forecast;

    @Mock
    private QMARelations qmaRelations;

    @Mock
    private AssesSI assesSI;

    @Mock
    private QMAProjects qmaProjects;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Backlog backlog;

    @Mock
    private StrategicIndicatorsController strategicIndicatorsController;

    @Mock
    private QualityFactorsController qualityFactorsController;

    @InjectMocks
    private Util utilController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(utilController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void editStrategicIndicator() throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "codequality";
        qualityFactors.add(factor1);
        String factor2 = "softwarestability";
        qualityFactors.add(factor2);
        String factor3 = "testingstatus";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, IOUtils.toByteArray(networkFile.toURI()), qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));

        when(strategicIndicatorsController.assessStrategicIndicator(strategicIndicatorName)).thenReturn(true);

        MockMultipartFile network = new MockMultipartFile("network", "network.dne", "text/plain", Files.readAllBytes(networkFile.toPath()));

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators/{id}", strategicIndicatorId)
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", qualityFactors))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("si/update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Product name"),
                                parameterWithName("description")
                                        .description("Product description"),
                                parameterWithName("quality_factors")
                                        .description("Comma separated values of the quality factors identifiers which belong to the strategic indicator")),
                        requestParts(
                                partWithName("network")
                                        .description("Bayesian network file")
                        )
                ));

        // Verify mock interactions
        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).save(ArgumentMatchers.any(Strategic_Indicator.class));
        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void editStrategicIndicatorAssessment() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        //when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        // Strategic Indicator setup
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";

        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        MockMultipartFile network = new MockMultipartFile("network", "network.dne", "text/plain", Files.readAllBytes(networkFile.toPath()));

        List<String> qualityFactors = new ArrayList<>();
        qualityFactors.add("codequality");
        qualityFactors.add("softwarestability");
        qualityFactors.add("testingstatus");

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, Files.readAllBytes(networkFile.toPath()), qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));
//        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);
//
        List<String> newQualityFactors = new ArrayList<>();
        newQualityFactors.add("codequality");
        newQualityFactors.add("softwarestability");
        newQualityFactors.add("testingperformance");

        when(strategicIndicatorsController.assessStrategicIndicator(strategicIndicatorName)).thenReturn(true);
//
//        // Factors setup
//        String factor1Id = "codequality";
//        String factor1Name = "Code Quality";
//        String factor1Description = "Quality of the system code";
//        Double factor1Value = 0.8;
//        LocalDate evaluationDate = LocalDate.now();
//        String factorRationale = "parameters: {...}, formula: ...";
//        String strategicIndicatorExternalId = "productquality";
//        List<String> strategicIndicatorsList = new ArrayList<>();
//        strategicIndicatorsList.add(strategicIndicatorExternalId);
//        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        String factor2Id = "softwarestability";
//        String factor2Name = "Software Stability";
//        String factor2Description = "Critical issues in the system";
//        Double factor2Value = 0.7;
//        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        String factor3Id = "testingperformance";
//        String factor3Name = "Testing Performance";
//        String factor3Description = "Performance of the tests";
//        Double factor3Value = 0.6;
//        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        List<DTOFactor> dtoFactorList = new ArrayList<>();
//        dtoFactorList.add(dtoFactor1);
//        dtoFactorList.add(dtoFactor2);
//        dtoFactorList.add(dtoFactor3);
//
//        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);
//
//        // Assessments setup
//        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();
//
//        Long assessment1Id = 10L;
//        String assessment1Label = "Good";
//        Float assessment1Value = 0.5f;
//        String assessment1Color = "#00ff00";
//        Float assessment1UpperThreshold = 0.66f;
//        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment1);
//
//        Long assessment2Id = 11L;
//        String assessment2Label = "Neutral";
//        Float assessment2Value = 0.3f;
//        String assessment2Color = "#ff8000";
//        Float assessment2UpperThreshold = 0.33f;
//        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment2);
//
//        Long assessment3Id = 11L;
//        String assessment3Label = "Bad";
//        Float assessment3Value = 0.2f;
//        String assessment3Color = "#ff0000";
//        Float assessment3UpperThreshold = 0f;
//        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment3);
//
//        Map<String, String> mapFactors = new HashMap<>();
//        mapFactors.put(factor1Id, "Good");
//        mapFactors.put(factor2Id, "Good");
//        mapFactors.put(factor3Id, "Neutral");
//
//        // Factors categories setup
//        Long factorGoodCategoryId = 10L;
//        String factorGoodCategoryName = "Good";
//        String factorGoodCategoryColor = "#00ff00";
//        float factorGoodCategoryUpperThreshold = 1f;
//        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
//        factorGoodCategory.setId(factorGoodCategoryId);
//
//        Long factorNeutralCategoryId = 11L;
//        String factorNeutralCategoryName = "Neutral";
//        String factorNeutralCategoryColor = "#ff8000";
//        float factorNeutralCategoryUpperThreshold = 0.67f;
//        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
//        factorNeutralCategory.setId(factorNeutralCategoryId);
//
//        Long factorBadCategoryId = 12L;
//        String factorBadCategoryName = "Bad";
//        String factorBadCategoryColor = "#ff0000";
//        float factorBadCategoryUpperThreshold = 0.33f;
//        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
//        factorBadCategory.setId(factorBadCategoryId);
//
//        List<QFCategory> factorCategoryList = new ArrayList<>();
//        factorCategoryList.add(factorBadCategory);
//        factorCategoryList.add(factorNeutralCategory);
//        factorCategoryList.add(factorGoodCategory);
//
//        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);
//
//        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);
//
//        // SI Categories setup
//        Long strategicIndicatorGoodCategoryId = 10L;
//        String strategicIndicatorGoodCategoryName = "Good";
//        String strategicIndicatorGoodCategoryColor = "#00ff00";
//        SICategory siGoodCategory = new SICategory(strategicIndicatorGoodCategoryName, strategicIndicatorGoodCategoryColor);
//        siGoodCategory.setId(strategicIndicatorGoodCategoryId);
//
//        Long strategicIndicatorNeutralCategoryId = 11L;
//        String strategicIndicatorNeutralCategoryName = "Neutral";
//        String strategicIndicatorNeutralCategoryColor = "#ff8000";
//        SICategory siNeutralCategory = new SICategory(strategicIndicatorNeutralCategoryName, strategicIndicatorNeutralCategoryColor);
//        siNeutralCategory.setId(strategicIndicatorNeutralCategoryId);
//
//        Long strategicIndicatorBadCategoryId = 12L;
//        String strategicIndicatorBadCategoryName = "Bad";
//        String strategicIndicatorBadCategoryColor = "#ff0000";
//        SICategory siBadCategory = new SICategory(strategicIndicatorBadCategoryName, strategicIndicatorBadCategoryColor);
//        siBadCategory.setId(strategicIndicatorBadCategoryId);
//
//        List<SICategory> siCategoryList = new ArrayList<>();
//        siCategoryList.add(siGoodCategory);
//        siCategoryList.add(siNeutralCategory);
//        siCategoryList.add(siBadCategory);
//
//        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);
//
//        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(true);
//
//        List<Float> factorValuesList = new ArrayList<>();
//        factorValuesList.add(dtoFactor1.getValue());
//        factorValuesList.add(dtoFactor2.getValue());
//        factorValuesList.add(dtoFactor3.getValue());
//        List<Float> factorWeightsList = new ArrayList<>();
//        factorWeightsList.add(0f);
//        factorWeightsList.add(0f);
//        factorWeightsList.add(0f);
//        List<String> factorCategoryNamesList = new ArrayList<>();
//        factorCategoryNamesList.add("Good");
//        factorCategoryNamesList.add("Good");
//        factorCategoryNamesList.add("Neutral");
//
//        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(newQualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators/{id}", strategicIndicatorId)
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", newQualityFactors))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("si/update-assessment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);

        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicatorName, strategicIndicatorSaved.getName());
        assertEquals(strategicIndicatorDescription, strategicIndicatorSaved.getDescription());
        assertEquals(newQualityFactors, strategicIndicatorSaved.getQuality_factors());

        verify(strategicIndicatorsController, times(1)).assessStrategicIndicator(strategicIndicatorName);


//        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
//        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
//        verifyNoMoreInteractions(qmaQualityFactors);
//
//        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicatorName);
//        verify(strategicIndicatorRepository, times(1)).save(ArgumentMatchers.any(Strategic_Indicator.class));
//        verifyNoMoreInteractions(strategicIndicatorRepository);
//
//        verify(qmaProjects, times(1)).getAssessedProjects();
//        verifyNoMoreInteractions(qmaProjects);
//
//        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
//        verifyNoMoreInteractions(assesSI);
//
//        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
//        verifyNoMoreInteractions(qmaStrategicIndicators);
//
//        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(newQualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"));
//        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void editStrategicIndicatorAssessmentError() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        //when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        // Strategic Indicator setup
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";

        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        MockMultipartFile network = new MockMultipartFile("network", "network.dne", "text/plain", Files.readAllBytes(networkFile.toPath()));

        List<String> qualityFactors = new ArrayList<>();
        qualityFactors.add("codequality");
        qualityFactors.add("softwarestability");
        qualityFactors.add("testingstatus");

        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, Files.readAllBytes(networkFile.toPath()), qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));
//        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);
//
        List<String> newQualityFactors = new ArrayList<>();
        newQualityFactors.add("codequality");
        newQualityFactors.add("softwarestability");
        newQualityFactors.add("testingperformance");

        when(strategicIndicatorsController.assessStrategicIndicator(strategicIndicatorName)).thenReturn(false);
//
//        // Factors setup
//        String factor1Id = "codequality";
//        String factor1Name = "Code Quality";
//        String factor1Description = "Quality of the system code";
//        Double factor1Value = 0.8;
//        LocalDate evaluationDate = LocalDate.now();
//        String factorRationale = "parameters: {...}, formula: ...";
//        String strategicIndicatorExternalId = "productquality";
//        List<String> strategicIndicatorsList = new ArrayList<>();
//        strategicIndicatorsList.add(strategicIndicatorExternalId);
//        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        String factor2Id = "softwarestability";
//        String factor2Name = "Software Stability";
//        String factor2Description = "Critical issues in the system";
//        Double factor2Value = 0.7;
//        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        String factor3Id = "testingperformance";
//        String factor3Name = "Testing Performance";
//        String factor3Description = "Performance of the tests";
//        Double factor3Value = 0.6;
//        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
//
//        List<DTOFactor> dtoFactorList = new ArrayList<>();
//        dtoFactorList.add(dtoFactor1);
//        dtoFactorList.add(dtoFactor2);
//        dtoFactorList.add(dtoFactor3);
//
//        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);
//
//        // Assessments setup
//        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();
//
//        Long assessment1Id = 10L;
//        String assessment1Label = "Good";
//        Float assessment1Value = 0.5f;
//        String assessment1Color = "#00ff00";
//        Float assessment1UpperThreshold = 0.66f;
//        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment1);
//
//        Long assessment2Id = 11L;
//        String assessment2Label = "Neutral";
//        Float assessment2Value = 0.3f;
//        String assessment2Color = "#ff8000";
//        Float assessment2UpperThreshold = 0.33f;
//        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment2);
//
//        Long assessment3Id = 11L;
//        String assessment3Label = "Bad";
//        Float assessment3Value = 0.2f;
//        String assessment3Color = "#ff0000";
//        Float assessment3UpperThreshold = 0f;
//        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
//        dtoSIAssesmentList.add(dtoSIAssesment3);
//
//        Map<String, String> mapFactors = new HashMap<>();
//        mapFactors.put(factor1Id, "Good");
//        mapFactors.put(factor2Id, "Good");
//        mapFactors.put(factor3Id, "Neutral");
//
//        // Factors categories setup
//        Long factorGoodCategoryId = 10L;
//        String factorGoodCategoryName = "Good";
//        String factorGoodCategoryColor = "#00ff00";
//        float factorGoodCategoryUpperThreshold = 1f;
//        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
//        factorGoodCategory.setId(factorGoodCategoryId);
//
//        Long factorNeutralCategoryId = 11L;
//        String factorNeutralCategoryName = "Neutral";
//        String factorNeutralCategoryColor = "#ff8000";
//        float factorNeutralCategoryUpperThreshold = 0.67f;
//        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
//        factorNeutralCategory.setId(factorNeutralCategoryId);
//
//        Long factorBadCategoryId = 12L;
//        String factorBadCategoryName = "Bad";
//        String factorBadCategoryColor = "#ff0000";
//        float factorBadCategoryUpperThreshold = 0.33f;
//        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
//        factorBadCategory.setId(factorBadCategoryId);
//
//        List<QFCategory> factorCategoryList = new ArrayList<>();
//        factorCategoryList.add(factorBadCategory);
//        factorCategoryList.add(factorNeutralCategory);
//        factorCategoryList.add(factorGoodCategory);
//
//        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);
//
//        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);
//
//        // SI Categories setup
//        Long strategicIndicatorGoodCategoryId = 10L;
//        String strategicIndicatorGoodCategoryName = "Good";
//        String strategicIndicatorGoodCategoryColor = "#00ff00";
//        SICategory siGoodCategory = new SICategory(strategicIndicatorGoodCategoryName, strategicIndicatorGoodCategoryColor);
//        siGoodCategory.setId(strategicIndicatorGoodCategoryId);
//
//        Long strategicIndicatorNeutralCategoryId = 11L;
//        String strategicIndicatorNeutralCategoryName = "Neutral";
//        String strategicIndicatorNeutralCategoryColor = "#ff8000";
//        SICategory siNeutralCategory = new SICategory(strategicIndicatorNeutralCategoryName, strategicIndicatorNeutralCategoryColor);
//        siNeutralCategory.setId(strategicIndicatorNeutralCategoryId);
//
//        Long strategicIndicatorBadCategoryId = 12L;
//        String strategicIndicatorBadCategoryName = "Bad";
//        String strategicIndicatorBadCategoryColor = "#ff0000";
//        SICategory siBadCategory = new SICategory(strategicIndicatorBadCategoryName, strategicIndicatorBadCategoryColor);
//        siBadCategory.setId(strategicIndicatorBadCategoryId);
//
//        List<SICategory> siCategoryList = new ArrayList<>();
//        siCategoryList.add(siGoodCategory);
//        siCategoryList.add(siNeutralCategory);
//        siCategoryList.add(siBadCategory);
//
//        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);
//
//        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators/{id}", strategicIndicatorId)
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", newQualityFactors))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/update-assessment-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);

        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicatorName, strategicIndicatorSaved.getName());
        assertEquals(strategicIndicatorDescription, strategicIndicatorSaved.getDescription());
        assertEquals(newQualityFactors, strategicIndicatorSaved.getQuality_factors());

        verify(strategicIndicatorsController, times(1)).assessStrategicIndicator(strategicIndicatorName);
//        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
//        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
//        verifyNoMoreInteractions(qmaQualityFactors);
//
//        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);
//        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicatorName);
//        verify(strategicIndicatorRepository, times(1)).save(ArgumentMatchers.any(Strategic_Indicator.class));
//        verifyNoMoreInteractions(strategicIndicatorRepository);
//
//        verify(qmaProjects, times(1)).getAssessedProjects();
//        verifyNoMoreInteractions(qmaProjects);
//
//        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
//        verifyNoMoreInteractions(assesSI);
//
//        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
//        verifyNoMoreInteractions(qmaStrategicIndicators);
//
//        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void editStrategicIndicatorMissingParam() throws Exception {
        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");

        MockMultipartFile network = new MockMultipartFile("network", "network.dne", "text/plain", Files.readAllBytes(networkFile.toPath()));

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators/{id}", strategicIndicatorId)
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("si/update-missing-params",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void editStrategicIndicatorIntegrityViolation() throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Product Quality";
        String strategicIndicatorDescription = "Quality of the product built";
        File networkFile = new File("src/test/java/com/upc/gessi/qrapids/app/testHelpers/WSA_ProductQuality.dne");
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "codequality";
        qualityFactors.add(factor1);
        String factor2 = "softwarestability";
        qualityFactors.add(factor2);
        String factor3 = "testingstatus";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, IOUtils.toByteArray(networkFile.toURI()), qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);

        when(strategicIndicatorRepository.findById(strategicIndicatorId)).thenReturn(Optional.of(strategicIndicator));

        doThrow(new DataIntegrityViolationException("")).when(strategicIndicatorRepository).save(ArgumentMatchers.any(Strategic_Indicator.class));

        MockMultipartFile network = new MockMultipartFile("network", "network.dne", "text/plain", Files.readAllBytes(networkFile.toPath()));

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators/{id}", strategicIndicatorId)
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", qualityFactors))
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("si/update-data-integrity-violation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(strategicIndicatorRepository, times(1)).findById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).save(ArgumentMatchers.any(Strategic_Indicator.class));
        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void fetchStrategicIndicators() throws Exception {
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);
        Long projectId = 1L;

        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(null);

        String strategicIndicatorId = "blocking";
        String strategicIndicatorName = "Blocking";
        String factorId = "blockingcode";
        String factorName = "Blocking code";
        String factorDescription = "Technical debt in software code in terms of rule violations";
        Float factorValue = 0.8f;
        String dateString = "2019-07-07";
        LocalDate evaluationDate = LocalDate.parse(dateString);
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicator = "blocking";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicator);
        DTOFactor dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue, evaluationDate, null, factorRationale, strategicIndicatorsList);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        DTODetailedStrategicIndicator dtoDetailedStrategicIndicator = new DTODetailedStrategicIndicator(strategicIndicatorId, strategicIndicatorName, dtoFactorList);
        List<DTODetailedStrategicIndicator> dtoDetailedStrategicIndicatorList = new ArrayList<>();
        dtoDetailedStrategicIndicatorList.add(dtoDetailedStrategicIndicator);

        when(qmaDetailedStrategicIndicators.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoDetailedStrategicIndicatorList);

        when(strategicIndicatorRepository.existsByExternalIdAndProject_Id(strategicIndicatorId, projectId)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/fetch");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("si/fetch",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        ArgumentCaptor<Project> argumentPrj = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(argumentPrj.capture());
        Project projectSaved = argumentPrj.getValue();
        assertEquals(projectExternalId, projectSaved.getExternalId());
        assertEquals(projectExternalId, projectSaved.getName());
        assertEquals("No description specified", projectSaved.getDescription());
        assertNull(projectSaved.getLogo());
        assertTrue(projectSaved.getActive());

        ArgumentCaptor<Strategic_Indicator> argumentSI = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argumentSI.capture());
        Strategic_Indicator strategicIndicatorSaved = argumentSI.getValue();
        assertEquals(strategicIndicatorName, strategicIndicatorSaved.getName());
        assertEquals("", strategicIndicatorSaved.getDescription());
        List<String> factorIds = new ArrayList<>();
        factorIds.add(factorId);
        assertEquals(factorIds, strategicIndicatorSaved.getQuality_factors());
    }

    @Test
    public void simulate() throws Exception {
        String factorId = "testingperformance";
        String factorName = "Testing Performance";
        String factorDescription = "Performance of the tests";
        Double factorValue = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor = new DTOFactor(factorId, factorName, factorDescription, factorValue.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);

        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        Map<String, String> factorSimulated = new HashMap<>();
        factorSimulated.put("id", factorId);
        Double factorSimulatedValue = 0.9;
        factorSimulated.put("value", factorSimulatedValue.toString());
        List<Map<String, String>> factorSimulatedList = new ArrayList<>();
        factorSimulatedList.add(factorSimulated);
        when(qualityFactorsController.getFactorLabelFromValue(factorSimulatedValue.floatValue())).thenReturn("Good");

        Long strategicIndicatorId = 1L;
        String strategicIndicatorName = "Process Performance";
        String strategicIndicatorDescription = "Performance levels of the processes involved in the project";
        List<String> qualityFactors = new ArrayList<>();
        String factor1 = "developmentspeed";
        qualityFactors.add(factor1);
        String factor2 = "externalquality";
        qualityFactors.add(factor2);
        String factor3 = "testingperformance";
        qualityFactors.add(factor3);
        Strategic_Indicator strategicIndicator = new Strategic_Indicator(strategicIndicatorName, strategicIndicatorDescription, null, qualityFactors, project);
        strategicIndicator.setId(strategicIndicatorId);
        List<Strategic_Indicator> strategic_indicatorList = new ArrayList<>();
        strategic_indicatorList.add(strategicIndicator);

        when(strategicIndicatorRepository.findAll()).thenReturn(strategic_indicatorList);

        // Categories setup
        Long strategicIndicatorGoodCategoryId = 10L;
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        SICategory siGoodCategory = new SICategory(strategicIndicatorGoodCategoryName, strategicIndicatorGoodCategoryColor);
        siGoodCategory.setId(strategicIndicatorGoodCategoryId);

        Long strategicIndicatorNeutralCategoryId = 11L;
        String strategicIndicatorNeutralCategoryName = "Neutral";
        String strategicIndicatorNeutralCategoryColor = "#ff8000";
        SICategory siNeutralCategory = new SICategory(strategicIndicatorNeutralCategoryName, strategicIndicatorNeutralCategoryColor);
        siNeutralCategory.setId(strategicIndicatorNeutralCategoryId);

        Long strategicIndicatorBadCategoryId = 12L;
        String strategicIndicatorBadCategoryName = "Bad";
        String strategicIndicatorBadCategoryColor = "#ff0000";
        SICategory siBadCategory = new SICategory(strategicIndicatorBadCategoryName, strategicIndicatorBadCategoryColor);
        siBadCategory.setId(strategicIndicatorBadCategoryId);

        List<SICategory> siCategoryList = new ArrayList<>();
        siCategoryList.add(siGoodCategory);
        siCategoryList.add(siNeutralCategory);
        siCategoryList.add(siBadCategory);

        when(siCategoryRepository.findAll()).thenReturn(siCategoryList);

        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/strategicIndicators/simulate")
                .param("prj", projectExternalId)
                .param("factors", gson.toJson(factorSimulatedList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorExternalId)))
                .andExpect(jsonPath("$[0].dbId", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$[0].name", is(strategicIndicatorName)))
                .andExpect(jsonPath("$[0].description", is(strategicIndicatorDescription)))
                .andExpect(jsonPath("$[0].value.first", is(factorSimulatedValue)))
                .andExpect(jsonPath("$[0].value.second", is(strategicIndicatorGoodCategoryName)))
                .andExpect(jsonPath("$[0].value_description", is(strategicIndicatorGoodCategoryName + " (" + String.format("%.2f", factorSimulatedValue) + ")")))
                .andExpect(jsonPath("$[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].probabilities[0].id", is(strategicIndicatorGoodCategoryId.intValue())))
                .andExpect(jsonPath("$[0].probabilities[0].label", is(strategicIndicatorGoodCategoryName)))
                .andExpect(jsonPath("$[0].probabilities[0].value", is(nullValue())))
                .andExpect(jsonPath("$[0].probabilities[0].color", is(strategicIndicatorGoodCategoryColor)))
                .andExpect(jsonPath("$[0].probabilities[0].upperThreshold", is(closeTo(1, 0.01))))
                .andExpect(jsonPath("$[0].probabilities[1].id", is(strategicIndicatorNeutralCategoryId.intValue())))
                .andExpect(jsonPath("$[0].probabilities[1].label", is(strategicIndicatorNeutralCategoryName)))
                .andExpect(jsonPath("$[0].probabilities[1].value", is(nullValue())))
                .andExpect(jsonPath("$[0].probabilities[1].color", is(strategicIndicatorNeutralCategoryColor)))
                .andExpect(jsonPath("$[0].probabilities[1].upperThreshold", is(closeTo(0.66, 0.01))))
                .andExpect(jsonPath("$[0].probabilities[2].id", is(strategicIndicatorBadCategoryId.intValue())))
                .andExpect(jsonPath("$[0].probabilities[2].label", is(strategicIndicatorBadCategoryName)))
                .andExpect(jsonPath("$[0].probabilities[2].value", is(nullValue())))
                .andExpect(jsonPath("$[0].probabilities[2].color", is(strategicIndicatorBadCategoryColor)))
                .andExpect(jsonPath("$[0].probabilities[2].upperThreshold", is(closeTo(0.33, 0.01))))
                .andExpect(jsonPath("$[0].date", is(nullValue())))
                .andExpect(jsonPath("$[0].datasource", is("Simulation")))
                .andExpect(jsonPath("$[0].categories_description", is("")))
                .andExpect(jsonPath("$[0].hasBN", is(false)))
                .andExpect(jsonPath("$[0].hasFeedback", is(false)))
                .andExpect(jsonPath("$[0].forecastingError", is(nullValue())))
                .andExpect(jsonPath("$[0].mismatchDays", is(0)))
                .andExpect(jsonPath("$[0].missingFactors", is(nullValue())))
                .andDo(document("si/simulation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("factors")
                                        .description("List of the names and new values of the quality factors")),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].dbId")
                                        .description("Strategic indicator database identifier"),
                                fieldWithPath("[].name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("[].description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("[].value.first")
                                        .description("Strategic indicator numerical value"),
                                fieldWithPath("[].value.second")
                                        .description("Strategic indicator category"),
                                fieldWithPath("[].value_description")
                                        .description("Readable strategic indicator value and category"),
                                fieldWithPath("[].probabilities")
                                        .description("Strategic indicator categories list"),
                                fieldWithPath("[].probabilities[].id")
                                        .description("Strategic indicator category identifier"),
                                fieldWithPath("[].probabilities[].label")
                                        .description("Strategic indicator category label"),
                                fieldWithPath("[].probabilities[].value")
                                        .description("Strategic indicator category probability"),
                                fieldWithPath("[].probabilities[].color")
                                        .description("Strategic indicator category hexadecimal color"),
                                fieldWithPath("[].probabilities[].upperThreshold")
                                        .description("Strategic indicator category upper threshold"),
                                fieldWithPath("[].date")
                                        .description("Strategic indicator assessment date"),
                                fieldWithPath("[].datasource")
                                        .description("Strategic indicator source of data"),
                                fieldWithPath("[].categories_description")
                                        .description("Array with the strategic indicator categories and thresholds"),
                                fieldWithPath("[].hasBN")
                                        .description("Does the strategic indicator have a Bayesian Network?"),
                                fieldWithPath("[].hasFeedback")
                                        .description("Does the strategic indicator have any feedback"),
                                fieldWithPath("[].forecastingError")
                                        .description("Errors in the forecasting"),
                                fieldWithPath("[].mismatchDays")
                                        .description("Maximum difference (in days) when there is difference in the evaluation dates between the strategic indicator and some quality factors"),
                                fieldWithPath("[].missingFactors")
                                        .description("Factors without assessment"))
                ));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(strategicIndicatorRepository, times(1)).findAll();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(siCategoryRepository, times(2)).findAll();
        verifyNoMoreInteractions(siCategoryRepository);
    }

    @Test
    public void simulateError() throws Exception {
        String projectExternalId = "test";

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenThrow(new IOException());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/strategicIndicators/simulate")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/simulation-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getForecastTechniques() throws Exception {
        List<String> forecastingTechniques = new ArrayList<>();
        String technique1 = "PROPHET";
        forecastingTechniques.add(technique1);
        String technique2 = "ETS";
        forecastingTechniques.add(technique2);
        String technique3 = "NN";
        forecastingTechniques.add(technique3);

        when(forecast.getForecastTechniques()).thenReturn(forecastingTechniques);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/forecastTechniques");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is(technique1)))
                .andExpect(jsonPath("$[1]", is(technique2)))
                .andExpect(jsonPath("$[2]", is(technique3)))
                .andDo(document("forecast/techniques",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]")
                                        .description("Array with the forecasting techniques names"))
                ));

        // Verify mock interactions
        verify(forecast, times(1)).getForecastTechniques();
        verifyNoMoreInteractions(forecast);
    }

    @Test
    public void getQualityModel() throws Exception {
        String metricId = "nonblockingfiles";
        String metricValue = "0.8";
        String metricWeight = "1";
        DTORelationsMetric dtoRelationsMetric = new DTORelationsMetric(metricId);
        dtoRelationsMetric.setValue(metricValue);
        dtoRelationsMetric.setWeight(metricWeight);
        List<DTORelationsMetric> dtoRelationsMetricList = new ArrayList<>();
        dtoRelationsMetricList.add(dtoRelationsMetric);

        String factorId = "blockingcode";
        String factorValue = "0.8";
        String factorWeight = "1";
        DTORelationsFactor dtoRelationsFactor = new DTORelationsFactor(factorId);
        dtoRelationsFactor.setValue(factorValue);
        dtoRelationsFactor.setWeight(factorWeight);
        dtoRelationsFactor.setMetrics(dtoRelationsMetricList);
        List<DTORelationsFactor> dtoRelationsFactorList = new ArrayList<>();
        dtoRelationsFactorList.add(dtoRelationsFactor);

        String strategicIndicatorId = "blocking";
        String strategicIndicatorValue = "0.8";
        String strategicIndicatorValueDescription = "Good (0.8)";
        String strategicIndicatorColor = "#00ff00";
        DTORelationsSI dtoRelationsSI = new DTORelationsSI(strategicIndicatorId);
        dtoRelationsSI.setValue(strategicIndicatorValue);
        dtoRelationsSI.setValueDescription(strategicIndicatorValueDescription);
        dtoRelationsSI.setColor(strategicIndicatorColor);
        dtoRelationsSI.setFactors(dtoRelationsFactorList);
        List<DTORelationsSI> dtoRelationsSIList = new ArrayList<>();
        dtoRelationsSIList.add(dtoRelationsSI);

        String projectExternalId = "test";

        when(qmaRelations.getRelations(projectExternalId, null)).thenReturn(dtoRelationsSIList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityModel")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorId)))
                .andExpect(jsonPath("$[0].value", is(strategicIndicatorValue)))
                .andExpect(jsonPath("$[0].valueDescription", is(strategicIndicatorValueDescription)))
                .andExpect(jsonPath("$[0].color", is(strategicIndicatorColor)))
                .andExpect(jsonPath("$[0].factors", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].factors[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].factors[0].weight", is(factorWeight)))
                .andExpect(jsonPath("$[0].factors[0].metrics", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].weight", is(metricWeight)));

        // Verify mock interactions
        verify(qmaRelations, times(1)).getRelations(projectExternalId, null);
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void getQualityModelForDate() throws Exception {
        String metricId = "nonblockingfiles";
        String metricValue = "0.8";
        String metricWeight = "1";
        DTORelationsMetric dtoRelationsMetric = new DTORelationsMetric(metricId);
        dtoRelationsMetric.setValue(metricValue);
        dtoRelationsMetric.setWeight(metricWeight);
        List<DTORelationsMetric> dtoRelationsMetricList = new ArrayList<>();
        dtoRelationsMetricList.add(dtoRelationsMetric);

        String factorId = "blockingcode";
        String factorValue = "0.8";
        String factorWeight = "1";
        DTORelationsFactor dtoRelationsFactor = new DTORelationsFactor(factorId);
        dtoRelationsFactor.setValue(factorValue);
        dtoRelationsFactor.setWeight(factorWeight);
        dtoRelationsFactor.setMetrics(dtoRelationsMetricList);
        List<DTORelationsFactor> dtoRelationsFactorList = new ArrayList<>();
        dtoRelationsFactorList.add(dtoRelationsFactor);

        String strategicIndicatorId = "blocking";
        String strategicIndicatorValue = "0.8";
        String strategicIndicatorValueDescription = "Good (0.8)";
        String strategicIndicatorColor = "#00ff00";
        DTORelationsSI dtoRelationsSI = new DTORelationsSI(strategicIndicatorId);
        dtoRelationsSI.setValue(strategicIndicatorValue);
        dtoRelationsSI.setValueDescription(strategicIndicatorValueDescription);
        dtoRelationsSI.setColor(strategicIndicatorColor);
        dtoRelationsSI.setFactors(dtoRelationsFactorList);
        List<DTORelationsSI> dtoRelationsSIList = new ArrayList<>();
        dtoRelationsSIList.add(dtoRelationsSI);

        String projectExternalId = "test";
        String date = "2019-07-07";

        when(qmaRelations.getRelations(projectExternalId, LocalDate.parse(date))).thenReturn(dtoRelationsSIList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/qualityModel")
                .param("prj", projectExternalId)
                .param("date", date);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorId)))
                .andExpect(jsonPath("$[0].value", is(strategicIndicatorValue)))
                .andExpect(jsonPath("$[0].valueDescription", is(strategicIndicatorValueDescription)))
                .andExpect(jsonPath("$[0].color", is(strategicIndicatorColor)))
                .andExpect(jsonPath("$[0].factors", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].id", is(factorId)))
                .andExpect(jsonPath("$[0].factors[0].value", is(factorValue)))
                .andExpect(jsonPath("$[0].factors[0].weight", is(factorWeight)))
                .andExpect(jsonPath("$[0].factors[0].metrics", hasSize(1)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].id", is(metricId)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].value", is(metricValue)))
                .andExpect(jsonPath("$[0].factors[0].metrics[0].weight", is(metricWeight)))
                .andDo(document("si/quality-model",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Date (yyyy-mm-dd) of the quality model evaluation")
                        ),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("[].value")
                                        .description("Strategic indicator assessment value"),
                                fieldWithPath("[].valueDescription")
                                        .description("Strategic indicator assessment value and category"),
                                fieldWithPath("[].color")
                                        .description("Strategic indicator category color"),
                                fieldWithPath("[].factors")
                                        .description("List with all the quality factors composing the strategic indicator"),
                                fieldWithPath("[].factors[].id")
                                        .description("Quality factor identifier"),
                                fieldWithPath("[].factors[].value")
                                        .description("Quality factor value"),
                                fieldWithPath("[].factors[].weight")
                                        .description("Quality factor weight in the strategic indicator assessment"),
                                fieldWithPath("[].factors[].metrics")
                                        .description("List with all the metrics composing the quality factor"),
                                fieldWithPath("[].factors[].metrics[].id")
                                        .description("Metric identifier"),
                                fieldWithPath("[].factors[].metrics[].value")
                                        .description("Metric value"),
                                fieldWithPath("[].factors[].metrics[].weight")
                                        .description("Metric weight in the computation of the quality factor"))
                ));

        // Verify mock interactions
        verify(qmaRelations, times(1)).getRelations(projectExternalId, LocalDate.parse(date));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void getNextMilestones () throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        String projectBacklogId = "prj-1";
        project.setBacklogId(projectBacklogId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        String milestoneName = "Version 1.3";
        String milestoneDescription = "Version 1.3 adding new features";
        String milestoneType = "Release";
        DTOMilestone milestone = new DTOMilestone(date.toString(), milestoneName, milestoneDescription, milestoneType);

        List<DTOMilestone> milestoneList = new ArrayList<>();
        milestoneList.add(milestone);

        LocalDate now = LocalDate.now();

        when(backlog.getMilestones(project.getBacklogId(), now)).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId)
                .param("date", now.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(date.toString())))
                .andExpect(jsonPath("$[0].name", is(milestoneName)))
                .andExpect(jsonPath("$[0].description", is(milestoneDescription)))
                .andExpect(jsonPath("$[0].type", is(milestoneType)))
                .andDo(document("milestones/get-from-date",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("date")
                                        .optional()
                                        .description("Minimum milestone date (yyyy-mm-dd)")
                        ),
                        responseFields(
                                fieldWithPath("[].date")
                                        .description("Milestone date"),
                                fieldWithPath("[].name")
                                        .description("Milestone name"),
                                fieldWithPath("[].description")
                                        .description("Milestone description"),
                                fieldWithPath("[].type")
                                        .description("Milestone type"))
                ));
    }

    @Test
    public void getAllMilestones () throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        String projectBacklogId = "prj-1";
        project.setBacklogId(projectBacklogId);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

        LocalDate date = LocalDate.now();
        date = date.plusDays(3);
        String milestoneName = "Version 1.3";
        String milestoneDescription = "Version 1.3 adding new features";
        String milestoneType = "Release";
        List<DTOMilestone> milestoneList = new ArrayList<>();
        milestoneList.add(new DTOMilestone(date.toString(), milestoneName, milestoneDescription, milestoneType));

        when(backlog.getMilestones(project.getBacklogId(), null)).thenReturn(milestoneList);

        //Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/milestones")
                .param("prj", projectExternalId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date", is(date.toString())))
                .andExpect(jsonPath("$[0].name", is(milestoneName)))
                .andExpect(jsonPath("$[0].description", is(milestoneDescription)))
                .andExpect(jsonPath("$[0].type", is(milestoneType)));
    }
}