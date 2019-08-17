package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QualityFactorsController {

    @Autowired
    private QMAQualityFactors qmaQualityFactors;

    public List<DTOQualityFactor> getAllFactorsWithMetricsCurrentEvaluation(String projectExternalId) throws IOException {
        return qmaQualityFactors.CurrentEvaluation(null, projectExternalId);
    }

}
