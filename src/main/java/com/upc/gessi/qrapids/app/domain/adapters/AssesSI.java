package com.upc.gessi.qrapids.app.domain.adapters;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.services.Util;
import com.upc.gessi.qrapids.app.dto.DTOSIAssesment;
import com.upc.gessi.qrapids.app.domain.repositories.SICategory.SICategoryRepository;
import com.upc.gessi.qrapids.app.domain.models.SICategory;
import com.upc.gessi.qrapids.app.dto.assessmentSI.DTOAssessmentSI;
import com.upc.gessi.qrapids.app.dto.assessmentSI.DTOCategorySI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.*;

@Component
public class AssesSI {

    @Autowired
    private SICategoryRepository SICatRep;

    @Autowired
    private Util util;

    @Value("${assessSI.url}")
    private String url;

    public List<DTOSIAssesment> AssesSI(String SIid, Map<String, String> mapFactors, File network) {

        mapFactors = new LinkedHashMap<>(mapFactors);

        try {
            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/api/si/assessment");

            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("SIid", SIid);
            List<String> factorNames = new ArrayList<>(mapFactors.keySet());
            for (String name : factorNames) {
                params.add("factorNames", name);
            }
            List<String> factorValues = new ArrayList<>(mapFactors.values());
            for (String value: factorValues) {
                params.add("factorValues", value);
            }
            params.add("network", new FileSystemResource(network));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(params, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), requestEntity, String.class);

            HttpStatus statusCode = responseEntity.getStatusCode();
            List<DTOSIAssesment> dtoSiAssesment;
            if (statusCode == HttpStatus.OK) {
                Gson gson = new Gson();
                DTOAssessmentSI assessmentSI = gson.fromJson(responseEntity.getBody(), DTOAssessmentSI.class);
                dtoSiAssesment = DTOAssessmentSItoDTOSIAssesment(assessmentSI.getProbsSICategories());
            }
            else {
                dtoSiAssesment = new ArrayList<>();
            }
            return dtoSiAssesment;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // If there is no BN, the assessment is the factors average
    public float AssesSI(List<Float> factors_assessment, int n_factors) {
        try {
            float total = 0.f;
//            int n_factors = 0;
            float result =0.f;

            for (Float factor : factors_assessment) {
                total += factor;
//              n_factors++;
            }
            if (total>0)
                result = total/n_factors;

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.f;
        }
    }

    public List<DTOSIAssesment> DTOAssessmentSItoDTOSIAssesment(ArrayList<DTOCategorySI> catsEstimation) {
        List<DTOSIAssesment> categories = util.getCategories();
        if (catsEstimation.size() == categories.size()) {
            int i = 0;
            for (DTOSIAssesment assesment : categories) {
                if (assesment.getLabel().equals(catsEstimation.get(catsEstimation.size() - 1 - i).getIdSICategory())) {
                    assesment.setValue(catsEstimation.get(catsEstimation.size() - 1 - i).getProbSICategory());
                }
                ++i;
            }
        }
        return categories;
    }
}
