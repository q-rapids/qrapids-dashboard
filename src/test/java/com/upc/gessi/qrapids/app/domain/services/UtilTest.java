package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.AssesSI;
import com.upc.gessi.qrapids.app.domain.adapters.Backlog;
import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.*;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.QFCategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
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
}