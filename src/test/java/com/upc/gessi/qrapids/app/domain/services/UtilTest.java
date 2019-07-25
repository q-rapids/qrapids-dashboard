package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.*;
import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.domain.repositories.MetricCategory.MetricRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTODetailedStrategicIndicator;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsFactor;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsMetric;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import com.upc.gessi.qrapids.app.testHelpers.HelperFunctions;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
    private MetricRepository metricRepository;

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
    public void getStrategicIndicatorsCategories () throws Exception {
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

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(strategicIndicatorGoodCategoryId.intValue())))
                .andExpect(jsonPath("$[0].name", is(strategicIndicatorGoodCategoryName)))
                .andExpect(jsonPath("$[0].color", is(strategicIndicatorGoodCategoryColor)))
                .andExpect(jsonPath("$[1].id", is(strategicIndicatorNeutralCategoryId.intValue())))
                .andExpect(jsonPath("$[1].name", is(strategicIndicatorNeutralCategoryName)))
                .andExpect(jsonPath("$[1].color", is(strategicIndicatorNeutralCategoryColor)))
                .andExpect(jsonPath("$[2].id", is(strategicIndicatorBadCategoryId.intValue())))
                .andExpect(jsonPath("$[2].name", is(strategicIndicatorBadCategoryName)))
                .andExpect(jsonPath("$[2].color", is(strategicIndicatorBadCategoryColor)))
                .andDo(document("si/categories",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Category identifier"),
                                fieldWithPath("[].name")
                                        .description("Category name"),
                                fieldWithPath("[].color")
                                        .description("Category hexadecimal color")
                        )
                ));
    }

    @Test
    public void newStrategicIndicatorsCategories () throws Exception {
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        Map<String, String> strategicIndicatorGoodCategory = new HashMap<>();
        strategicIndicatorGoodCategory.put("name", strategicIndicatorGoodCategoryName);
        strategicIndicatorGoodCategory.put("color", strategicIndicatorGoodCategoryColor);

        String strategicIndicatorNeutralCategoryName = "Neutral";
        String strategicIndicatorNeutralCategoryColor = "#ff8000";
        Map<String, String> strategicIndicatorNeutralCategory = new HashMap<>();
        strategicIndicatorNeutralCategory.put("name", strategicIndicatorNeutralCategoryName);
        strategicIndicatorNeutralCategory.put("color", strategicIndicatorNeutralCategoryColor);

        String strategicIndicatorBadCategoryName = "Bad";
        String strategicIndicatorBadCategoryColor = "#ff0000";
        Map<String, String> strategicIndicatorBadCategory = new HashMap<>();
        strategicIndicatorBadCategory.put("name", strategicIndicatorBadCategoryName);
        strategicIndicatorBadCategory.put("color", strategicIndicatorBadCategoryColor);

        List<Map<String, String>> strategicIndicatorCategoriesList = new ArrayList<>();
        strategicIndicatorCategoriesList.add(strategicIndicatorGoodCategory);
        strategicIndicatorCategoriesList.add(strategicIndicatorNeutralCategory);
        strategicIndicatorCategoriesList.add(strategicIndicatorBadCategory);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/strategicIndicators/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(strategicIndicatorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("si/categories-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].name")
                                        .description("Strategic indicator category name"),
                                fieldWithPath("[].color")
                                        .description("Strategic indicator category color"))
                ));

        // Verify mock interactions
        verify(qmaStrategicIndicators, times(1)).deleteAllCategories();
        verify(qmaStrategicIndicators, times(1)).newCategories(strategicIndicatorCategoriesList);
        verifyNoMoreInteractions(qmaStrategicIndicators);
    }

    @Test
    public void newStrategicIndicatorsCategoriesNotEnough () throws Exception {
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        Map<String, String> strategicIndicatorGoodCategory = new HashMap<>();
        strategicIndicatorGoodCategory.put("name", strategicIndicatorGoodCategoryName);
        strategicIndicatorGoodCategory.put("color", strategicIndicatorGoodCategoryColor);

        List<Map<String, String>> strategicIndicatorCategoriesList = new ArrayList<>();
        strategicIndicatorCategoriesList.add(strategicIndicatorGoodCategory);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/strategicIndicators/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(strategicIndicatorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Not enough categories"))
                .andDo(document("si/categories-new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyNoMoreInteractions(qmaStrategicIndicators);
    }

    @Test
    public void getFactorsCategories () throws Exception {
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorGoodCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorBadCategory);

        when(qfCategoryRepository.findAll()).thenReturn(factorCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/qualityFactors/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(factorGoodCategoryId.intValue())))
                .andExpect(jsonPath("$[0].name", is(factorGoodCategoryName)))
                .andExpect(jsonPath("$[0].color", is(factorGoodCategoryColor)))
                .andExpect(jsonPath("$[0].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorGoodCategoryUpperThreshold))))
                .andExpect(jsonPath("$[1].id", is(factorNeutralCategoryId.intValue())))
                .andExpect(jsonPath("$[1].name", is(factorNeutralCategoryName)))
                .andExpect(jsonPath("$[1].color", is(factorNeutralCategoryColor)))
                .andExpect(jsonPath("$[1].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorNeutralCategoryUpperThreshold))))
                .andExpect(jsonPath("$[2].id", is(factorBadCategoryId.intValue())))
                .andExpect(jsonPath("$[2].name", is(factorBadCategoryName)))
                .andExpect(jsonPath("$[2].color", is(factorBadCategoryColor)))
                .andExpect(jsonPath("$[2].upperThreshold", is(HelperFunctions.getFloatAsDouble(factorBadCategoryUpperThreshold))))
                .andDo(document("qf/categories",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Category identifier"),
                                fieldWithPath("[].name")
                                        .description("Category name"),
                                fieldWithPath("[].color")
                                        .description("Category hexadecimal color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Category upper threshold")
                        )
                ));
    }

    @Test
    public void newFactorsCategories () throws Exception {
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        Float factorGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> factorGoodCategory = new HashMap<>();
        factorGoodCategory.put("name", factorGoodCategoryName);
        factorGoodCategory.put("color", factorGoodCategoryColor);
        factorGoodCategory.put("upperThreshold", factorGoodCategoryUpperThreshold.toString());

        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        Float factorNeutralCategoryUpperThreshold = 0.67f;
        Map<String, String> factorNeutralCategory = new HashMap<>();
        factorNeutralCategory.put("name", factorNeutralCategoryName);
        factorNeutralCategory.put("color", factorNeutralCategoryColor);
        factorNeutralCategory.put("upperThreshold", factorNeutralCategoryUpperThreshold.toString());

        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        Float factorBadCategoryUpperThreshold = 0.33f;
        Map<String, String> factorBadCategory = new HashMap<>();
        factorBadCategory.put("name", factorBadCategoryName);
        factorBadCategory.put("color", factorBadCategoryColor);
        factorBadCategory.put("upperThreshold", factorBadCategoryUpperThreshold.toString());

        List<Map<String, String>> factorCategoriesList = new ArrayList<>();
        factorCategoriesList.add(factorGoodCategory);
        factorCategoriesList.add(factorNeutralCategory);
        factorCategoriesList.add(factorBadCategory);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qualityFactors/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(factorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("qf/categories-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].name")
                                        .description("Quality factors category name"),
                                fieldWithPath("[].color")
                                        .description("Quality factors category color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Quality factors category upper threshold"))
                ));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).deleteAllCategories();
        verify(qmaQualityFactors, times(1)).newCategories(factorCategoriesList);
        verifyNoMoreInteractions(qmaQualityFactors);
    }

    @Test
    public void newFactorsCategoriesNotEnough () throws Exception {
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        Float factorGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> factorGoodCategory = new HashMap<>();
        factorGoodCategory.put("name", factorGoodCategoryName);
        factorGoodCategory.put("color", factorGoodCategoryColor);
        factorGoodCategory.put("upperThreshold", factorGoodCategoryUpperThreshold.toString());

        List<Map<String, String>> factorCategoriesList = new ArrayList<>();
        factorCategoriesList.add(factorGoodCategory);

        //Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/qualityFactors/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(factorCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Not enough categories"))
                .andDo(document("qf/categories-new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    public void getMetricsCategories () throws Exception {
        Long metricGoodCategoryId = 10L;
        String metricGoodCategoryName = "Good";
        String metricGoodCategoryColor = "#00ff00";
        float metricGoodCategoryUpperThreshold = 1f;
        MetricCategory metricGoodCategory = new MetricCategory(metricGoodCategoryName, metricGoodCategoryColor, metricGoodCategoryUpperThreshold);
        metricGoodCategory.setId(metricGoodCategoryId);

        Long metricNeutralCategoryId = 11L;
        String metricNeutralCategoryName = "Neutral";
        String metricNeutralCategoryColor = "#ff8000";
        float metricNeutralCategoryUpperThreshold = 0.67f;
        MetricCategory metricNeutralCategory = new MetricCategory(metricNeutralCategoryName, metricNeutralCategoryColor, metricNeutralCategoryUpperThreshold);
        metricNeutralCategory.setId(metricNeutralCategoryId);

        Long metricBadCategoryId = 12L;
        String metricBadCategoryName = "Bad";
        String metricBadCategoryColor = "#ff0000";
        float metricBadCategoryUpperThreshold = 0.33f;
        MetricCategory metricBadCategory = new MetricCategory(metricBadCategoryName, metricBadCategoryColor, metricBadCategoryUpperThreshold);
        metricBadCategory.setId(metricBadCategoryId);

        List<MetricCategory> metricCategoryList = new ArrayList<>();
        metricCategoryList.add(metricGoodCategory);
        metricCategoryList.add(metricNeutralCategory);
        metricCategoryList.add(metricBadCategory);

        when(metricRepository.findAll()).thenReturn(metricCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/metrics/categories");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(metricGoodCategoryId.intValue())))
                .andExpect(jsonPath("$[0].name", is(metricGoodCategoryName)))
                .andExpect(jsonPath("$[0].color", is(metricGoodCategoryColor)))
                .andExpect(jsonPath("$[0].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricGoodCategoryUpperThreshold))))
                .andExpect(jsonPath("$[1].id", is(metricNeutralCategoryId.intValue())))
                .andExpect(jsonPath("$[1].name", is(metricNeutralCategoryName)))
                .andExpect(jsonPath("$[1].color", is(metricNeutralCategoryColor)))
                .andExpect(jsonPath("$[1].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricNeutralCategoryUpperThreshold))))
                .andExpect(jsonPath("$[2].id", is(metricBadCategoryId.intValue())))
                .andExpect(jsonPath("$[2].name", is(metricBadCategoryName)))
                .andExpect(jsonPath("$[2].color", is(metricBadCategoryColor)))
                .andExpect(jsonPath("$[2].upperThreshold", is(HelperFunctions.getFloatAsDouble(metricBadCategoryUpperThreshold))))
                .andDo(document("metrics/categories",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Category identifier"),
                                fieldWithPath("[].name")
                                        .description("Category name"),
                                fieldWithPath("[].color")
                                        .description("Category hexadecimal color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Category upper threshold")
                        )
                ));
    }

    @Test
    public void newMetricsCategories () throws Exception {
        String metricGoodCategoryName = "Good";
        String metricGoodCategoryColor = "#00ff00";
        Float metricGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> metricGoodCategory = new HashMap<>();
        metricGoodCategory.put("name", metricGoodCategoryName);
        metricGoodCategory.put("color", metricGoodCategoryColor);
        metricGoodCategory.put("upperThreshold", metricGoodCategoryUpperThreshold.toString());

        String metricNeutralCategoryName = "Neutral";
        String metricNeutralCategoryColor = "#ff8000";
        Float metricNeutralCategoryUpperThreshold = 0.67f;
        Map<String, String> metricNeutralCategory = new HashMap<>();
        metricNeutralCategory.put("name", metricNeutralCategoryName);
        metricNeutralCategory.put("color", metricNeutralCategoryColor);
        metricNeutralCategory.put("upperThreshold", metricNeutralCategoryUpperThreshold.toString());

        String metricBadCategoryName = "Bad";
        String metricBadCategoryColor = "#ff0000";
        Float metricBadCategoryUpperThreshold = 0.33f;
        Map<String, String> metricBadCategory = new HashMap<>();
        metricBadCategory.put("name", metricBadCategoryName);
        metricBadCategory.put("color", metricBadCategoryColor);
        metricBadCategory.put("upperThreshold", metricBadCategoryUpperThreshold.toString());

        List<Map<String, String>> metricCategoriesList = new ArrayList<>();
        metricCategoriesList.add(metricGoodCategory);
        metricCategoriesList.add(metricNeutralCategory);
        metricCategoriesList.add(metricBadCategory);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/metrics/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(metricCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("metrics/categories-new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].name")
                                        .description("Metrics category name"),
                                fieldWithPath("[].color")
                                        .description("Metrics category color"),
                                fieldWithPath("[].upperThreshold")
                                        .description("Metrics category upper threshold"))
                ));

        // Verify mock interactions
        verify(metricRepository, times(1)).deleteAll();
        verify(metricRepository, times(3)).save(ArgumentMatchers.any(MetricCategory.class));
        verifyNoMoreInteractions(metricRepository);
    }

    @Test
    public void newMetricsCategoriesNotEnough () throws Exception {
        String metricGoodCategoryName = "Good";
        String metricGoodCategoryColor = "#00ff00";
        Float metricGoodCategoryUpperThreshold = 1.0f;
        Map<String, String> metricGoodCategory = new HashMap<>();
        metricGoodCategory.put("name", metricGoodCategoryName);
        metricGoodCategory.put("color", metricGoodCategoryColor);
        metricGoodCategory.put("upperThreshold", metricGoodCategoryUpperThreshold.toString());

        List<Map<String, String>> metricCategoriesList = new ArrayList<>();
        metricCategoriesList.add(metricGoodCategory);

        // Perform request
        Gson gson = new Gson();
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/metrics/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(metricCategoriesList));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(is("Not enough categories")))
                .andDo(document("metrics/categories-new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verifyNoMoreInteractions(metricRepository);
    }

    @Test
    public void newStrategicIndicator() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

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

        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        // Factors setup
        String factor1Id = "codequality";
        String factor1Name = "Code Quality";
        String factor1Description = "Quality of the system code";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "productquality";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "softwarestability";
        String factor2Name = "Software Stability";
        String factor2Description = "Critical issues in the system";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "testingstatus";
        String factor3Name = "Testing status";
        String factor3Description = "Status of the tests";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // Assessments setup
        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = 0.5f;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = 0.3f;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = 0.2f;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        Map<String, String> mapFactors = new HashMap<>();
        mapFactors.put(factor1Id, "Good");
        mapFactors.put(factor2Id, "Good");
        mapFactors.put(factor3Id, "Neutral");

        // Factors categories setup
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);

        // SI Categories setup
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

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(true);

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(dtoFactor2.getValue());
        factorValuesList.add(dtoFactor3.getValue());
        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(0f);
        factorWeightsList.add(0f);
        factorWeightsList.add(0f);
        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(qualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"))).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators")
                .file(network)
                .param("prj", projectExternalId)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", qualityFactors));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("si/new",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
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
        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicatorName, strategicIndicatorSaved.getName());
        assertEquals(strategicIndicatorDescription, strategicIndicatorSaved.getDescription());
        assertEquals(qualityFactors, strategicIndicatorSaved.getQuality_factors());

        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(qualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void newStrategicIndicatorAssessmentError() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        when(projectRepository.findByExternalId(projectExternalId)).thenReturn(project);

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

        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        // Factors setup
        String factor1Id = "codequality";
        String factor1Name = "Code Quality";
        String factor1Description = "Quality of the system code";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "productquality";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "softwarestability";
        String factor2Name = "Software Stability";
        String factor2Description = "Critical issues in the system";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "testingstatus";
        String factor3Name = "Testing status";
        String factor3Description = "Status of the tests";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // Assessments setup
        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = 0.5f;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = 0.3f;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = 0.2f;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        Map<String, String> mapFactors = new HashMap<>();
        mapFactors.put(factor1Id, "Good");
        mapFactors.put(factor2Id, "Good");
        mapFactors.put(factor3Id, "Neutral");

        // Factors categories setup
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);

        // SI Categories setup
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

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/strategicIndicators")
                .file(network)
                .param("name", strategicIndicatorName)
                .param("description", strategicIndicatorDescription)
                .param("quality_factors", String.join(",", qualityFactors));

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/new-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        ArgumentCaptor<Strategic_Indicator> argument = ArgumentCaptor.forClass(Strategic_Indicator.class);
        verify(strategicIndicatorRepository, times(1)).save(argument.capture());
        Strategic_Indicator strategicIndicatorSaved = argument.getValue();
        assertEquals(strategicIndicatorName, strategicIndicatorSaved.getName());
        assertEquals(strategicIndicatorDescription, strategicIndicatorSaved.getDescription());
        assertEquals(qualityFactors, strategicIndicatorSaved.getQuality_factors());

        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void getStrategicIndicator() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "prj-1";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        project.setId(projectId);
        project.setBacklogId(projectBacklogId);

        Long strategicIndicatorId = 1L;
        String strategicIndicatorExternalId = "productquality";
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

        when(strategicIndicatorRepository.existsById(strategicIndicatorId)).thenReturn(true);
        when(strategicIndicatorRepository.getOne(strategicIndicatorId)).thenReturn(strategicIndicator);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/strategicIndicators/{id}", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(strategicIndicatorId.intValue())))
                .andExpect(jsonPath("$.externalId", is(strategicIndicatorExternalId)))
                .andExpect(jsonPath("$.name", is(strategicIndicatorName)))
                .andExpect(jsonPath("$.description", is(strategicIndicatorDescription)))
                .andExpect(jsonPath("$.network", is(notNullValue())))
                .andExpect(jsonPath("$.quality_factors", hasSize(3)))
                .andExpect(jsonPath("$.quality_factors[0]", is(factor1)))
                .andExpect(jsonPath("$.quality_factors[1]", is(factor2)))
                .andExpect(jsonPath("$.quality_factors[2]", is(factor3)))
                .andExpect(jsonPath("$.project.id", is(projectId.intValue())))
                .andExpect(jsonPath("$.project.externalId", is(projectExternalId)))
                .andExpect(jsonPath("$.project.name", is(projectName)))
                .andExpect(jsonPath("$.project.description", is(projectDescription)))
                .andExpect(jsonPath("$.project.logo", is(nullValue())))
                .andExpect(jsonPath("$.project.active", is(true)))
                .andExpect(jsonPath("$.project.backlogId", is(projectBacklogId)))
                .andDo(document("si/get-one",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Strategic indicator identifier")),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Strategic indicator identifier"),
                                fieldWithPath("externalId")
                                        .description("Strategic indicator external identifier"),
                                fieldWithPath("name")
                                        .description("Strategic indicator name"),
                                fieldWithPath("description")
                                        .description("Strategic indicator description"),
                                fieldWithPath("network")
                                        .description("Strategic indicator bayesian network"),
                                fieldWithPath("quality_factors")
                                        .description("Strategic indicator quality factors identifiers list"),
                                fieldWithPath("project.id")
                                        .description("Project identifier"),
                                fieldWithPath("project.externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("project.name")
                                        .description("Project name"),
                                fieldWithPath("project.description")
                                        .description("Project description"),
                                fieldWithPath("project.logo")
                                        .description("Project logo"),
                                fieldWithPath("project.active")
                                        .description("Is an active project?"),
                                fieldWithPath("project.backlogId")
                                        .description("Project identifier in the backlog"))
                ));

        // Verify mock interactions
        verify(strategicIndicatorRepository, times(1)).existsById(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).getOne(strategicIndicatorId);
        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void getMissingStrategicIndicator() throws Exception {
        Long strategicIndicatorId = 2L;
        when(strategicIndicatorRepository.existsById(strategicIndicatorId)).thenReturn(false);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/{id}", strategicIndicatorId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andDo(document("si/get-one-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
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

        when(strategicIndicatorRepository.getOne(strategicIndicatorId)).thenReturn(strategicIndicator);

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
        verify(strategicIndicatorRepository, times(1)).getOne(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).flush();
        verifyNoMoreInteractions(strategicIndicatorRepository);
    }

    @Test
    public void editStrategicIndicatorAssessment() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

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

        when(strategicIndicatorRepository.getOne(strategicIndicatorId)).thenReturn(strategicIndicator);
        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        List<String> newQualityFactors = new ArrayList<>();
        newQualityFactors.add("codequality");
        newQualityFactors.add("softwarestability");
        newQualityFactors.add("testingperformance");

        // Factors setup
        String factor1Id = "codequality";
        String factor1Name = "Code Quality";
        String factor1Description = "Quality of the system code";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "productquality";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "softwarestability";
        String factor2Name = "Software Stability";
        String factor2Description = "Critical issues in the system";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "testingperformance";
        String factor3Name = "Testing Performance";
        String factor3Description = "Performance of the tests";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // Assessments setup
        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = 0.5f;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = 0.3f;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = 0.2f;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        Map<String, String> mapFactors = new HashMap<>();
        mapFactors.put(factor1Id, "Good");
        mapFactors.put(factor2Id, "Good");
        mapFactors.put(factor3Id, "Neutral");

        // Factors categories setup
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);

        // SI Categories setup
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

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(true);

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(dtoFactor1.getValue());
        factorValuesList.add(dtoFactor2.getValue());
        factorValuesList.add(dtoFactor3.getValue());
        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(0f);
        factorWeightsList.add(0f);
        factorWeightsList.add(0f);
        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(newQualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"))).thenReturn(true);

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
        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(strategicIndicatorRepository, times(1)).getOne(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicatorName);
        verify(strategicIndicatorRepository, times(1)).flush();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(newQualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq("Good"));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void editStrategicIndicatorAssessmentError() throws Exception {
        // Project setup
        List<String> projectsList = new ArrayList<>();
        String projectExternalId = "test";
        projectsList.add(projectExternalId);

        when(qmaProjects.getAssessedProjects()).thenReturn(projectsList);

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

        when(strategicIndicatorRepository.getOne(strategicIndicatorId)).thenReturn(strategicIndicator);
        when(strategicIndicatorRepository.findByName(strategicIndicatorName)).thenReturn(strategicIndicator);

        List<String> newQualityFactors = new ArrayList<>();
        newQualityFactors.add("codequality");
        newQualityFactors.add("softwarestability");
        newQualityFactors.add("testingperformance");

        // Factors setup
        String factor1Id = "codequality";
        String factor1Name = "Code Quality";
        String factor1Description = "Quality of the system code";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "productquality";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "softwarestability";
        String factor2Name = "Software Stability";
        String factor2Description = "Critical issues in the system";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "testingperformance";
        String factor3Name = "Testing Performance";
        String factor3Description = "Performance of the tests";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // Assessments setup
        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = 0.5f;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = 0.3f;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = 0.2f;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        Map<String, String> mapFactors = new HashMap<>();
        mapFactors.put(factor1Id, "Good");
        mapFactors.put(factor2Id, "Good");
        mapFactors.put(factor3Id, "Neutral");

        // Factors categories setup
        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        when(assesSI.AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class))).thenReturn(dtoSIAssesmentList);

        // SI Categories setup
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

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L))).thenReturn(false);

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
        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(anyList(), eq(projectExternalId));
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(strategicIndicatorRepository, times(1)).getOne(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).findByName(strategicIndicatorName);
        verify(strategicIndicatorRepository, times(1)).flush();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(qmaProjects, times(1)).getAssessedProjects();
        verifyNoMoreInteractions(qmaProjects);

        verify(assesSI, times(1)).AssesSI(eq(strategicIndicatorExternalId), eq(mapFactors), ArgumentMatchers.any(File.class));
        verifyNoMoreInteractions(assesSI);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(0.8333334f), ArgumentMatchers.any(LocalDate.class), anyList(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyNoMoreInteractions(qmaRelations);
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

        when(strategicIndicatorRepository.getOne(strategicIndicatorId)).thenReturn(strategicIndicator);

        doThrow(new DataIntegrityViolationException("")).when(strategicIndicatorRepository).flush();

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
        verify(strategicIndicatorRepository, times(1)).getOne(strategicIndicatorId);
        verify(strategicIndicatorRepository, times(1)).flush();
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
    public void assesStrategicIndicators() throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        String factor1Id = "testingperformance";
        String factor1Name = "Testing Performance";
        String factor1Description = "Performance of the tests";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

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

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(factor1Value.floatValue());
        factorValuesList.add(factor2Value.floatValue());
        factorValuesList.add(factor3Value.floatValue());

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(true);

        List<Float> factorWeightsList = new ArrayList<>();
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        factorWeightsList.add(1f);
        List<String> factorCategoryNamesList = new ArrayList<>();
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Good");
        factorCategoryNamesList.add("Neutral");

        when(qmaRelations.setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(qualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()))).thenReturn(true);

        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/assess")
                .param("prj", projectExternalId)
                .param("train", "NONE");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("si/assess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("prj")
                                        .description("Project external identifier"),
                                parameterWithName("train").description("Indicates if the forecasting models should be trained: " +
                                        "NONE for no training, ONE for one method training and ALL for all methods training"))
                ));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, projectExternalId);
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(strategicIndicatorRepository, times(1)).findAll();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qfCategoryRepository, times(6)).findAllByOrderByUpperThresholdAsc();
        verifyNoMoreInteractions(qfCategoryRepository);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verify(qmaRelations, times(1)).setStrategicIndicatorFactorRelation(eq(projectExternalId), eq(qualityFactors), eq(strategicIndicatorExternalId), ArgumentMatchers.any(LocalDate.class), eq(factorWeightsList), eq(factorValuesList), eq(factorCategoryNamesList), eq(factorsAverageValue.toString()));
        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assesStrategicIndicatorsNotCorrect() throws Exception {
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);

        String factor1Id = "testingperformance";
        String factor1Name = "Testing Performance";
        String factor1Description = "Performance of the tests";
        Double factor1Value = 0.8;
        LocalDate evaluationDate = LocalDate.now();
        String factorRationale = "parameters: {...}, formula: ...";
        String strategicIndicatorExternalId = "processperformance";
        List<String> strategicIndicatorsList = new ArrayList<>();
        strategicIndicatorsList.add(strategicIndicatorExternalId);
        DTOFactor dtoFactor1 = new DTOFactor(factor1Id, factor1Name, factor1Description, factor1Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor2Id = "developmentspeed";
        String factor2Name = "Development Speed";
        String factor2Description = "Time spent to add new value to the product";
        Double factor2Value = 0.7;
        DTOFactor dtoFactor2 = new DTOFactor(factor2Id, factor2Name, factor2Description, factor2Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        String factor3Id = "externalquality";
        String factor3Name = "External Quality";
        String factor3Description = "Quality perceived by the users";
        Double factor3Value = 0.6;
        DTOFactor dtoFactor3 = new DTOFactor(factor3Id, factor3Name, factor3Description, factor3Value.floatValue(), evaluationDate, null, factorRationale, strategicIndicatorsList);

        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor1);
        dtoFactorList.add(dtoFactor2);
        dtoFactorList.add(dtoFactor3);

        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

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

        List<Float> factorValuesList = new ArrayList<>();
        factorValuesList.add(factor1Value.floatValue());
        factorValuesList.add(factor2Value.floatValue());
        factorValuesList.add(factor3Value.floatValue());

        int factorsNumber = factorValuesList.size();
        Float factorsValuesSum = 0f;
        for (Float factorValue : factorValuesList) {
            factorsValuesSum += factorValue;
        }
        Float factorsAverageValue = factorsValuesSum / factorsNumber;

        when(assesSI.AssesSI(factorValuesList, 3)).thenReturn(factorsAverageValue);

        when(qmaStrategicIndicators.setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L))).thenReturn(false);

        Long factorGoodCategoryId = 10L;
        String factorGoodCategoryName = "Good";
        String factorGoodCategoryColor = "#00ff00";
        float factorGoodCategoryUpperThreshold = 1f;
        QFCategory factorGoodCategory = new QFCategory(factorGoodCategoryName, factorGoodCategoryColor, factorGoodCategoryUpperThreshold);
        factorGoodCategory.setId(factorGoodCategoryId);

        Long factorNeutralCategoryId = 11L;
        String factorNeutralCategoryName = "Neutral";
        String factorNeutralCategoryColor = "#ff8000";
        float factorNeutralCategoryUpperThreshold = 0.67f;
        QFCategory factorNeutralCategory = new QFCategory(factorNeutralCategoryName, factorNeutralCategoryColor, factorNeutralCategoryUpperThreshold);
        factorNeutralCategory.setId(factorNeutralCategoryId);

        Long factorBadCategoryId = 12L;
        String factorBadCategoryName = "Bad";
        String factorBadCategoryColor = "#ff0000";
        float factorBadCategoryUpperThreshold = 0.33f;
        QFCategory factorBadCategory = new QFCategory(factorBadCategoryName, factorBadCategoryColor, factorBadCategoryUpperThreshold);
        factorBadCategory.setId(factorBadCategoryId);

        List<QFCategory> factorCategoryList = new ArrayList<>();
        factorCategoryList.add(factorBadCategory);
        factorCategoryList.add(factorNeutralCategory);
        factorCategoryList.add(factorGoodCategory);

        when(qfCategoryRepository.findAllByOrderByUpperThresholdAsc()).thenReturn(factorCategoryList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/assess")
                .param("prj", projectExternalId)
                .param("train", "NONE");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError())
                .andDo(document("si/assess-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(qmaQualityFactors, times(1)).setFactorStrategicIndicatorRelation(dtoFactorList, projectExternalId);
        verify(qmaQualityFactors, times(1)).getAllFactors(projectExternalId);
        verifyNoMoreInteractions(qmaQualityFactors);

        verify(strategicIndicatorRepository, times(1)).findAll();
        verifyNoMoreInteractions(strategicIndicatorRepository);

        verify(assesSI, times(1)).AssesSI(factorValuesList, 3);
        verifyNoMoreInteractions(assesSI);

        verify(qfCategoryRepository, times(3)).findAllByOrderByUpperThresholdAsc();
        verifyNoMoreInteractions(qfCategoryRepository);

        verify(qmaStrategicIndicators, times(1)).setStrategicIndicatorValue(eq(projectExternalId), eq(strategicIndicatorExternalId), eq(strategicIndicatorName), eq(strategicIndicatorDescription), eq(factorsAverageValue.floatValue()), ArgumentMatchers.any(LocalDate.class), isNull(), anyList(), eq(0L));
        verifyNoMoreInteractions(qmaStrategicIndicators);

        verifyNoMoreInteractions(qmaRelations);
    }

    @Test
    public void assesStrategicIndicatorsBadParam() throws Exception {
        String projectExternalId = "test";

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/strategicIndicators/assess")
                .param("prj", projectExternalId)
                .param("train", "NONE")
                .param("from", "2019-15-03");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andDo(document("si/assess-param-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
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
                                        .description("Errors in the forecasting"))
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
}