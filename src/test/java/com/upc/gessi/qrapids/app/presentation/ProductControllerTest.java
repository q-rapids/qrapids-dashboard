package com.upc.gessi.qrapids.app.presentation;

import com.upc.gessi.qrapids.QrapidsApplication;
import com.upc.gessi.qrapids.app.domain.controllers.ProductsController;
import com.upc.gessi.qrapids.app.dto.DTOProduct;
import com.upc.gessi.qrapids.app.dto.DTOProject;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductsController productsController;

    @InjectMocks
    private ProductController productController;

    private Double getFloatAsDouble(Float fValue) {
        return Double.valueOf(fValue.toString());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .build();
    }

    @Test
    public void getProjects() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        when(productsController.getProjects()).thenReturn(dtoProjectList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/projects");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$[0].name", is(projectName)))
                .andExpect(jsonPath("$[0].description", is(projectDescription)))
                .andExpect(jsonPath("$[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].active", is(active)))
                .andExpect(jsonPath("$[0].backlogId", is(projectBacklogId)));

        // Verify mock interactions
        verify(productsController, times(1)).getProjects();
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void getProducts() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long productId = 1L;
        String productName = "Test";
        String productDescription = "Test product";

        DTOProduct dtoProduct = new DTOProduct(productId, productName, productDescription, null, dtoProjectList);
        List<DTOProduct> dtoProductList = new ArrayList<>();
        dtoProductList.add(dtoProduct);

        when(productsController.getProducts()).thenReturn(dtoProductList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/products");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(productId.intValue())))
                .andExpect(jsonPath("$[0].name", is(productName)))
                .andExpect(jsonPath("$[0].description", is(productDescription)))
                .andExpect(jsonPath("$[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].projects", hasSize(1)))
                .andExpect(jsonPath("$[0].projects[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].projects[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$[0].projects[0].name", is(projectName)))
                .andExpect(jsonPath("$[0].projects[0].description", is(projectDescription)))
                .andExpect(jsonPath("$[0].projects[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].projects[0].active", is(active)))
                .andExpect(jsonPath("$[0].projects[0].backlogId", is(projectBacklogId)));

        // Verify mock interactions
        verify(productsController, times(1)).getProducts();
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void getProductById() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long productId = 1L;
        String productName = "Test";
        String productDescription = "Test product";

        DTOProduct dtoProduct = new DTOProduct(productId, productName, productDescription, null, dtoProjectList);

        when(productsController.getProductById(productId.toString())).thenReturn(dtoProduct);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/products/" + productId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productId.intValue())))
                .andExpect(jsonPath("$.name", is(productName)))
                .andExpect(jsonPath("$.description", is(productDescription)))
                .andExpect(jsonPath("$.logo", is(nullValue())))
                .andExpect(jsonPath("$.projects", hasSize(1)))
                .andExpect(jsonPath("$.projects[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$.projects[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$.projects[0].name", is(projectName)))
                .andExpect(jsonPath("$.projects[0].description", is(projectDescription)))
                .andExpect(jsonPath("$.projects[0].logo", is(nullValue())))
                .andExpect(jsonPath("$.projects[0].active", is(active)))
                .andExpect(jsonPath("$.projects[0].backlogId", is(projectBacklogId)));

        // Verify mock interactions
        verify(productsController, times(1)).getProductById(productId.toString());
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void getProjectById() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);

        when(productsController.getProjectById(projectId.toString())).thenReturn(dtoProject);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/products/project/" + projectId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(projectId.intValue())))
                .andExpect(jsonPath("$.externalId", is(projectExternalId)))
                .andExpect(jsonPath("$.name", is(projectName)))
                .andExpect(jsonPath("$.description", is(projectDescription)))
                .andExpect(jsonPath("$.logo", is(nullValue())))
                .andExpect(jsonPath("$.active", is(active)))
                .andExpect(jsonPath("$.backlogId", is(projectBacklogId)));

        // Verify mock interactions
        verify(productsController, times(1)).getProjectById(projectId.toString());
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void updateProject() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "999";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, logoMultipartFile.getBytes(), true, projectBacklogId);

        when(productsController.checkProjectByName(projectId, projectName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/updateProject")
                .file(logoMultipartFile)
                .param("id", projectId.toString())
                .param("externalId", projectExternalId)
                .param("name", projectName)
                .param("description", projectDescription)
                .param("backlogId", projectBacklogId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        // Verify mock interactions
        verify(productsController, times(1)).checkProjectByName(projectId, projectName);

        ArgumentCaptor<DTOProject> argument = ArgumentCaptor.forClass(DTOProject.class);
        verify(productsController, times(1)).updateProject(argument.capture());
        assertEquals(dtoProject.getId(), argument.getValue().getId());
        assertEquals(dtoProject.getExternalId(), argument.getValue().getExternalId());
        assertEquals(dtoProject.getName(), argument.getValue().getName());
        assertEquals(dtoProject.getDescription(), argument.getValue().getDescription());
        assertEquals(dtoProject.getActive(), argument.getValue().getActive());
        assertEquals(dtoProject.getExternalId(), argument.getValue().getExternalId());

        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void updateProjectNameAlreadyExists() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        String projectBacklogId = "999";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(productsController.checkProjectByName(projectId, projectName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/updateProject")
                .file(logoMultipartFile)
                .param("id", projectId.toString())
                .param("externalId", projectExternalId)
                .param("name", projectName)
                .param("description", projectDescription)
                .param("backlogId", projectBacklogId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());

        // Verify mock interactions
        verify(productsController, times(1)).checkProjectByName(projectId, projectName);

        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void updateProduct() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long productId = 1L;
        String productName = "Test";
        String productDescription = "Test product";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(productsController.checkProductByName(productId, productName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/updateProduct")
                .file(logoMultipartFile)
                .param("id", productId.toString())
                .param("name", productName)
                .param("description", productDescription)
                .param("projects", projectId.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        // Verify mock interactions
        verify(productsController, times(1)).checkProductByName(productId, productName);

        List<String> projectIds = new ArrayList<>();
        projectIds.add(projectId.toString());
        verify(productsController, times(1)).updateProduct(productId, productName, productDescription, logoMultipartFile.getBytes(), projectIds);

        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void updateProductNameAlreadyExists() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long productId = 1L;
        String productName = "Test";
        String productDescription = "Test product";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(productsController.checkProductByName(productId, productName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/updateProduct")
                .file(logoMultipartFile)
                .param("id", productId.toString())
                .param("name", productName)
                .param("description", productDescription)
                .param("projects", projectId.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());

        // Verify mock interactions
        verify(productsController, times(1)).checkProductByName(productId, productName);
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void newProduct() throws Exception {
        String productName = "Test";
        String productDescription = "Test product";
        String projectId = "1";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(productsController.checkNewProductByName(productName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/newProduct")
                .file(logoMultipartFile)
                .param("name", productName)
                .param("description", productDescription)
                .param("projects", projectId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        // Verify mock interactions
        verify(productsController, times(1)).checkNewProductByName(productName);

        List<String> projectIds = new ArrayList<>();
        projectIds.add(projectId);
        verify(productsController, times(1)).newProduct(productName, productDescription, logoMultipartFile.getBytes(), projectIds);

        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void newProductNameAlreadyExists() throws Exception {
        String productName = "Test";
        String productDescription = "Test product";
        String projectId = "1";

        URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
        File file = new File(projectImageUrl.getPath());
        MockMultipartFile logoMultipartFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", Files.readAllBytes(file.toPath()));

        when(productsController.checkNewProductByName(productName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/newProduct")
                .file(logoMultipartFile)
                .param("name", productName)
                .param("description", productDescription)
                .param("projects", projectId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());

        // Verify mock interactions
        verify(productsController, times(1)).checkNewProductByName(productName);
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void deleteProduct() throws Exception {
        Long productId = 1L;

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/deleteProduct")
                .param("id", productId.toString());

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        // Verify mock interactions
        verify(productsController, times(1)).deleteProduct(productId);
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void deleteProductBadId() throws Exception {
        String productId = "test";

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/deleteProduct")
                .param("id", productId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isMethodNotAllowed());

        // Verify mock interactions
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void getProductEvaluation() throws Exception {
        Long productId = 1L;

        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        String strategicIndicatorId = "blocking";
        Long strategicIndicatorDbId = 1L;
        String strategicIndicatorName = "Blocking";
        String strategicIndicatorDescription = "Blocking elements";
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicatorId, strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, dtoSIAssesmentList, date, datasource, strategicIndicatorDbId, categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);

        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        when(productsController.getProductEvaluation(productId)).thenReturn(dtoStrategicIndicatorEvaluationList);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/products/currentEvaluation/" + productId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$[0].dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$[0].name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[0].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue())))
                .andExpect(jsonPath("$[0].probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$[0].probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[1].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue())))
                .andExpect(jsonPath("$[0].probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$[0].probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$[0].probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$[0].probabilities[2].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue())))
                .andExpect(jsonPath("$[0].probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$[0].probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$[0].hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$[0].hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));

        // Verify mock interactions
        verify(productsController, times(1)).getProductEvaluation(productId);
        verifyNoMoreInteractions(productsController);
    }

    @Test
    public void getDetailedCurrentEvaluation() throws Exception {
        Long productId = 1L;
        String productName = "test";

        List<DTOSIAssesment> dtoSIAssesmentList = new ArrayList<>();

        Long assessment1Id = 10L;
        String assessment1Label = "Good";
        Float assessment1Value = null;
        String assessment1Color = "#00ff00";
        Float assessment1UpperThreshold = 0.66f;
        DTOSIAssesment dtoSIAssesment1 = new DTOSIAssesment(assessment1Id, assessment1Label, assessment1Value, assessment1Color, assessment1UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment1);

        Long assessment2Id = 11L;
        String assessment2Label = "Neutral";
        Float assessment2Value = null;
        String assessment2Color = "#ff8000";
        Float assessment2UpperThreshold = 0.33f;
        DTOSIAssesment dtoSIAssesment2 = new DTOSIAssesment(assessment2Id, assessment2Label, assessment2Value, assessment2Color, assessment2UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment2);

        Long assessment3Id = 11L;
        String assessment3Label = "Bad";
        Float assessment3Value = null;
        String assessment3Color = "#ff0000";
        Float assessment3UpperThreshold = 0f;
        DTOSIAssesment dtoSIAssesment3 = new DTOSIAssesment(assessment3Id, assessment3Label, assessment3Value, assessment3Color, assessment3UpperThreshold);
        dtoSIAssesmentList.add(dtoSIAssesment3);

        String strategicIndicatorId = "blocking";
        Long strategicIndicatorDbId = 1L;
        String strategicIndicatorName = "Blocking";
        String strategicIndicatorDescription = "Blocking elements";
        Float strategicIndicatorValue = 0.8f;
        String strategicIndicatorCategory = "Good";
        Pair<Float, String> strategicIndicatorValuePair = Pair.of(strategicIndicatorValue, strategicIndicatorCategory);
        String dateString = "2019-07-07";
        LocalDate date = LocalDate.parse(dateString);
        String datasource = "Q-Rapdis Dashboard";
        String categoriesDescription = "[Good (0,67), Neutral (0,33), Bad (0,00)]";
        DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation = new DTOStrategicIndicatorEvaluation(strategicIndicatorId, strategicIndicatorName, strategicIndicatorDescription, strategicIndicatorValuePair, dtoSIAssesmentList, date, datasource, strategicIndicatorDbId, categoriesDescription, false);
        dtoStrategicIndicatorEvaluation.setHasFeedback(false);
        dtoStrategicIndicatorEvaluation.setForecastingError(null);

        List<DTOStrategicIndicatorEvaluation> dtoStrategicIndicatorEvaluationList = new ArrayList<>();
        dtoStrategicIndicatorEvaluationList.add(dtoStrategicIndicatorEvaluation);

        Pair<String, List<DTOStrategicIndicatorEvaluation>> productStrategicIndicatorsEvaluation = Pair.of(productName, dtoStrategicIndicatorEvaluationList);
        List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> productsEvaluations = new ArrayList<>();
        productsEvaluations.add(productStrategicIndicatorsEvaluation);

        when(productsController.getDetailedProductEvaluation(productId)).thenReturn(productsEvaluations);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/products/detailedCurrentEvaluation/" + productId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].first", is(productName)))
                .andExpect(jsonPath("$[0].second", hasSize(1)))
                .andExpect(jsonPath("$[0].second[0].id", is(dtoStrategicIndicatorEvaluation.getId())))
                .andExpect(jsonPath("$[0].second[0].dbId", is(dtoStrategicIndicatorEvaluation.getDbId().intValue())))
                .andExpect(jsonPath("$[0].second[0].name", is(dtoStrategicIndicatorEvaluation.getName())))
                .andExpect(jsonPath("$[0].second[0].description", is(dtoStrategicIndicatorEvaluation.getDescription())))
                .andExpect(jsonPath("$[0].second[0].value.first", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getValue().getFirst()))))
                .andExpect(jsonPath("$[0].second[0].value.second", is(dtoStrategicIndicatorEvaluation.getValue().getSecond())))
                .andExpect(jsonPath("$[0].second[0].value_description", is(dtoStrategicIndicatorEvaluation.getValue_description())))
                .andExpect(jsonPath("$[0].second[0].probabilities", hasSize(3)))
                .andExpect(jsonPath("$[0].second[0].probabilities[0].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[0].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getLabel())))
                .andExpect(jsonPath("$[0].second[0].probabilities[0].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[0].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getColor())))
                .andExpect(jsonPath("$[0].second[0].probabilities[0].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(0).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].second[0].probabilities[1].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getId().intValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[1].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getLabel())))
                .andExpect(jsonPath("$[0].second[0].probabilities[1].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[1].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getColor())))
                .andExpect(jsonPath("$[0].second[0].probabilities[1].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(1).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].second[0].probabilities[2].id", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getId().intValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[2].label", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getLabel())))
                .andExpect(jsonPath("$[0].second[0].probabilities[2].value", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getValue())))
                .andExpect(jsonPath("$[0].second[0].probabilities[2].color", is(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getColor())))
                .andExpect(jsonPath("$[0].second[0].probabilities[2].upperThreshold", is(getFloatAsDouble(dtoStrategicIndicatorEvaluation.getProbabilities().get(2).getUpperThreshold()))))
                .andExpect(jsonPath("$[0].second[0].date[0]", is(dtoStrategicIndicatorEvaluation.getDate().getYear())))
                .andExpect(jsonPath("$[0].second[0].date[1]", is(dtoStrategicIndicatorEvaluation.getDate().getMonthValue())))
                .andExpect(jsonPath("$[0].second[0].date[2]", is(dtoStrategicIndicatorEvaluation.getDate().getDayOfMonth())))
                .andExpect(jsonPath("$[0].second[0].datasource", is(dtoStrategicIndicatorEvaluation.getDatasource())))
                .andExpect(jsonPath("$[0].second[0].categories_description", is(dtoStrategicIndicatorEvaluation.getCategories_description())))
                .andExpect(jsonPath("$[0].second[0].hasBN", is(dtoStrategicIndicatorEvaluation.isHasBN())))
                .andExpect(jsonPath("$[0].second[0].hasFeedback", is(dtoStrategicIndicatorEvaluation.isHasFeedback())))
                .andExpect(jsonPath("$[0].second[0].forecastingError", is(dtoStrategicIndicatorEvaluation.getForecastingError())));

        // Verify mock interactions
        verify(productsController, times(1)).getDetailedProductEvaluation(productId);
        verifyNoMoreInteractions(productsController);
    }
}