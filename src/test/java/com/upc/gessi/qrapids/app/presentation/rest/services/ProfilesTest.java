package com.upc.gessi.qrapids.app.presentation.rest.services;


import com.upc.gessi.qrapids.QrapidsApplication;
import com.upc.gessi.qrapids.app.domain.controllers.ProfilesController;
import com.upc.gessi.qrapids.app.domain.models.Profile;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProduct;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.data.util.Pair;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProfile;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProfilesTest {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ProfilesController profilesController;

    @InjectMocks
    private Profiles profileController;

    private Double getFloatAsDouble(Float fValue) {
        return Double.valueOf(fValue.toString());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(profileController)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void getProfiles() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long profileId = 1L;
        String profileName = "Test";
        String profileDescription = "Test profile";
        String qualityLevel = "ALL";

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        DTOProfile dtoProfile = new DTOProfile(profileId, profileName, profileDescription, qualityLevel, dtoProjectList, allSIs);
        List<DTOProfile> dtoProfileList = new ArrayList<>();
        dtoProfileList.add(dtoProfile);

        when(profilesController.getProfiles()).thenReturn(dtoProfileList);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/profiles");

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(profileId.intValue())))
                .andExpect(jsonPath("$[0].name", is(profileName)))
                .andExpect(jsonPath("$[0].description", is(profileDescription)))
                .andExpect(jsonPath("$[0].qualityLevel", is(qualityLevel)))
                .andExpect(jsonPath("$[0].projects", hasSize(1)))
                .andExpect(jsonPath("$[0].projects[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].projects[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$[0].projects[0].name", is(projectName)))
                .andExpect(jsonPath("$[0].projects[0].description", is(projectDescription)))
                .andExpect(jsonPath("$[0].projects[0].logo", is(nullValue())))
                .andExpect(jsonPath("$[0].projects[0].active", is(active)))
                .andExpect(jsonPath("$[0].projects[0].backlogId", is(projectBacklogId)))
                .andExpect(jsonPath("$[0].allSIs", hasSize(1)))
                .andExpect(jsonPath("$[0].allSIs[0].first", is(projectId.intValue())))
                .andExpect(jsonPath("$[0].allSIs[0].second", is(true)))
                .andDo(document("profiles/all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Profile identifier"),
                                fieldWithPath("[].name")
                                        .description("Profile name"),
                                fieldWithPath("[].description")
                                        .description("Profile description"),
                                fieldWithPath("[].qualityLevel")
                                        .description("Profile quality level"),
                                fieldWithPath("[].projects")
                                        .description("List of all the projects which compose the profile"),
                                fieldWithPath("[].projects[].id")
                                        .description("Project identifier"),
                                fieldWithPath("[].projects[].externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("[].projects[].name")
                                        .description("Project name"),
                                fieldWithPath("[].projects[].description")
                                        .description("Project description"),
                                fieldWithPath("[].projects[].logo")
                                        .description("Project logo file"),
                                fieldWithPath("[].projects[].active")
                                        .description("Is an active project?"),
                                fieldWithPath("[].projects[].backlogId")
                                        .description("Project identifier in the backlog"),
                                fieldWithPath("[].allSIs")
                                        .description("List of pairs which specify for each project of profile, if it show all strategic indicators or not"),
                                fieldWithPath("[].allSIs[].first")
                                        .description("Project identifier"),
                                fieldWithPath("[].allSIs[].second")
                                        .description("Are all strategic indicators shown?"))
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).getProfiles();
        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void getProfileById() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long profileId = 1L;
        String profileName = "Test";
        String profileDescription = "Test profile";
        String qualityLevel = "ALL";

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        DTOProfile dtoProfile = new DTOProfile(profileId, profileName, profileDescription, qualityLevel, dtoProjectList, allSIs);

        when(profilesController.getProfileById(profileId.toString())).thenReturn(dtoProfile);

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .get("/api/profiles/{id}", profileId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(profileId.intValue())))
                .andExpect(jsonPath("$.name", is(profileName)))
                .andExpect(jsonPath("$.description", is(profileDescription)))
                .andExpect(jsonPath("$.qualityLevel", is(qualityLevel)))
                .andExpect(jsonPath("$.projects", hasSize(1)))
                .andExpect(jsonPath("$.projects[0].id", is(projectId.intValue())))
                .andExpect(jsonPath("$.projects[0].externalId", is(projectExternalId)))
                .andExpect(jsonPath("$.projects[0].name", is(projectName)))
                .andExpect(jsonPath("$.projects[0].description", is(projectDescription)))
                .andExpect(jsonPath("$.projects[0].logo", is(nullValue())))
                .andExpect(jsonPath("$.projects[0].active", is(active)))
                .andExpect(jsonPath("$.projects[0].backlogId", is(projectBacklogId)))
                .andExpect(jsonPath("$.allSIs", hasSize(1)))
                .andExpect(jsonPath("$.allSIs[0].first", is(projectId.intValue())))
                .andExpect(jsonPath("$.allSIs[0].second", is(true)))
                .andDo(document("profiles/single",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Profile identifier")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("Profile identifier"),
                                fieldWithPath("name")
                                        .description("Profile name"),
                                fieldWithPath("description")
                                        .description("Profile description"),
                                fieldWithPath("qualityLevel")
                                        .description("Profile quality level"),
                                fieldWithPath("projects")
                                        .description("List of all the projects which compose the profile"),
                                fieldWithPath("projects[].id")
                                        .description("Project identifier"),
                                fieldWithPath("projects[].externalId")
                                        .description("Project external identifier"),
                                fieldWithPath("projects[].name")
                                        .description("Project name"),
                                fieldWithPath("projects[].description")
                                        .description("Project description"),
                                fieldWithPath("projects[].logo")
                                        .description("Project logo file"),
                                fieldWithPath("projects[].active")
                                        .description("Is an active project?"),
                                fieldWithPath("projects[].backlogId")
                                        .description("Project identifier in the backlog"),
                                fieldWithPath("allSIs")
                                        .description("List of pairs which specify for each project of profile, if it show all strategic indicators or not"),
                                fieldWithPath("allSIs[].first")
                                        .description("Project identifier"),
                                fieldWithPath("allSIs[].second")
                                        .description("Are all strategic indicators shown?"))
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).getProfileById(profileId.toString());
        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void updateProfile() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long profileId = 1L;
        String profileName = "Test";
        String profileDescription = "Test profile";
        Profile.QualityLevel qualityLevel = Profile.QualityLevel.valueOf("ALL");

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        String projects_info;
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("prj", projectId);
        item.put("all_si", true);
        item.put("si", new ArrayList<>());
        array.add(item);
        projects_info = array.toString();

        Map<String, Pair<Boolean, List<String>>> projectsInfoMap = new HashMap<>();
        projectsInfoMap.put(projectId.toString(), Pair.of(true, new ArrayList<>()));

        when(profilesController.checkProfileByName(profileId, profileName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/profiles/{id}", profileId)
                .param("name", profileName)
                .param("description", profileDescription)
                .param("quality_level", qualityLevel.toString())
                .param("projects_info", projects_info)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("profiles/update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Profile name"),
                                parameterWithName("description")
                                        .description("Profile description"),
                                parameterWithName("quality_level")
                                        .description("One of three possible options: ALL, FACTOR_METRIC, METRICS"),
                                parameterWithName("projects_info")
                                        .description("Array of JSON object { prj: project identifier, all_si: are all strategic indicators shown? , si: list of selected strategic indicators }"))
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).checkProfileByName(profileId, profileName);
        verify(profilesController, times(1)).updateProfile(profileId, profileName, profileDescription, qualityLevel, projectsInfoMap);

        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void updateProfileNameAlreadyExists() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        Long profileId = 1L;
        String profileName = "Test";
        String profileDescription = "Test profile";
        Profile.QualityLevel qualityLevel = Profile.QualityLevel.valueOf("ALL");

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        String projects_info;
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("prj", projectId);
        item.put("all_si", true);
        item.put("si", new ArrayList<>());
        array.add(item);
        projects_info = array.toString();

        when(profilesController.checkProfileByName(profileId, profileName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/profiles/{id}", profileId)
                .param("name", profileName)
                .param("description", profileDescription)
                .param("quality_level", qualityLevel.toString())
                .param("projects_info", projects_info)
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("profiles/update-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Profile name"),
                                parameterWithName("description")
                                        .description("Profile description"),
                                parameterWithName("quality_level")
                                        .description("One of three possible options: ALL, FACTOR_METRIC, METRICS"),
                                parameterWithName("projects_info")
                                        .description("Array of JSON object { prj: project identifier, all_si: are all strategic indicators shown? , si: list of selected strategic indicators }"))
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).checkProfileByName(profileId, profileName);
        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void newProfile() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        String profileName = "Test";
        String profileDescription = "Test profile";
        Profile.QualityLevel qualityLevel = Profile.QualityLevel.valueOf("ALL");

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        String projects_info;
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("prj", projectId);
        item.put("all_si", true);
        item.put("si", new ArrayList<>());
        array.add(item);
        projects_info = array.toString();

        Map<String, Pair<Boolean, List<String>>> projectsInfoMap = new HashMap<>();
        projectsInfoMap.put(projectId.toString(), Pair.of(true, new ArrayList<>()));

        when(profilesController.checkNewProfileByName(profileName)).thenReturn(true);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/profiles")
                .param("name", profileName)
                .param("description", profileDescription)
                .param("quality_level", qualityLevel.toString())
                .param("projects_info", projects_info);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andDo(document("profiles/add",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name")
                                        .description("Profile name"),
                                parameterWithName("description")
                                        .description("Profile description"),
                                parameterWithName("quality_level")
                                        .description("One of three possible options: ALL, FACTOR_METRIC, METRICS"),
                                parameterWithName("projects_info")
                                        .description("Array of JSON object { prj: project identifier, all_si: are all strategic indicators shown? , si: list of selected strategic indicators }"))
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).checkNewProfileByName(profileName);
        verify(profilesController, times(1)).newProfile(profileName, profileDescription, qualityLevel, projectsInfoMap);

        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void newProductNameAlreadyExists() throws Exception {
        Long projectId = 1L;
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        boolean active = true;
        String projectBacklogId = "999";
        DTOProject dtoProject = new DTOProject(projectId, projectExternalId, projectName, projectDescription, null, active, projectBacklogId);
        List<DTOProject> dtoProjectList = new ArrayList<>();
        dtoProjectList.add(dtoProject);

        String profileName = "Test";
        String profileDescription = "Test profile";
        Profile.QualityLevel qualityLevel = Profile.QualityLevel.valueOf("ALL");

        Pair<Long, Boolean> pair = Pair.of(projectId, true);
        List<Pair<Long, Boolean>> allSIs = new ArrayList<>();
        allSIs.add(pair);

        String projects_info;
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("prj", projectId);
        item.put("all_si", true);
        item.put("si", new ArrayList<>());
        array.add(item);
        projects_info = array.toString();

        when(profilesController.checkNewProfileByName(profileName)).thenReturn(false);

        // Perform request
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/profiles")
                .param("name", profileName)
                .param("description", profileDescription)
                .param("quality_level", qualityLevel.toString())
                .param("projects_info", projects_info);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andDo(document("profiles/add-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).checkNewProfileByName(profileName);
        verifyNoMoreInteractions(profilesController);
    }

    @Test
    public void deleteProduct() throws Exception {
        Long profileId = 1L;

        // Perform request
        RequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .delete("/api/profiles/{id}", profileId);

        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andDo(document("profiles/delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id")
                                        .description("Profile identifier")
                        )
                ));

        // Verify mock interactions
        verify(profilesController, times(1)).deleteProfile(profileId);
        verifyNoMoreInteractions(profilesController);
    }

}
