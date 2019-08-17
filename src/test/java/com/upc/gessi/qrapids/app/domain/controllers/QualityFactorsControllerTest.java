package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAQualityFactors;
import com.upc.gessi.qrapids.app.dto.DTOFactor;
import com.upc.gessi.qrapids.app.dto.DTOQualityFactor;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QualityFactorsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QMAQualityFactors qmaQualityFactors;

    @InjectMocks
    private QualityFactorsController qualityFactorsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getSingleFactorEvaluation() throws IOException {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        String projectExternalId = "test";
        when(qmaQualityFactors.SingleCurrentEvaluation(dtoFactor.getId(), projectExternalId)).thenReturn(dtoFactor);

        // When
        DTOFactor dtoFactorFound = qualityFactorsController.getSingleFactorEvaluation(dtoFactor.getId(), projectExternalId);

        // Then
        assertEquals(dtoFactor, dtoFactorFound);
    }

    @Test
    public void getAllFactorsEvaluation() throws IOException {
        // Given
        DTOFactor dtoFactor = domainObjectsBuilder.buildDTOFactor();
        List<DTOFactor> dtoFactorList = new ArrayList<>();
        dtoFactorList.add(dtoFactor);
        String projectExternalId = "test";
        when(qmaQualityFactors.getAllFactors(projectExternalId)).thenReturn(dtoFactorList);

        // When
        List<DTOFactor> dtoFactorListFound = qualityFactorsController.getAllFactorsEvaluation(projectExternalId);

        // Then
        assertEquals(dtoFactorList.size(), dtoFactorListFound.size());
        assertEquals(dtoFactor, dtoFactorListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsCurrentEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String projectExternalId = "test";
        when(qmaQualityFactors.CurrentEvaluation(null, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getAllFactorsWithMetricsCurrentEvaluation(projectExternalId);

        // Then
        assertEquals(dtoQualityFactorList.size(), dtoQualityFactorListFound.size());
        assertEquals(dtoQualityFactor, dtoQualityFactorListFound.get(0));
    }

    @Test
    public void getAllFactorsWithMetricsHistoricalEvaluation() throws IOException {
        // Given
        DTOQualityFactor dtoQualityFactor = domainObjectsBuilder.buildDTOQualityFactor();
        List<DTOQualityFactor> dtoQualityFactorList = new ArrayList<>();
        dtoQualityFactorList.add(dtoQualityFactor);
        String projectExternalId = "test";
        LocalDate from = dtoQualityFactor.getMetrics().get(0).getDate().minusDays(7);
        LocalDate to = dtoQualityFactor.getMetrics().get(0).getDate();
        when(qmaQualityFactors.HistoricalData(null, from, to, projectExternalId)).thenReturn(dtoQualityFactorList);

        // When
        List<DTOQualityFactor> dtoQualityFactorListFound = qualityFactorsController.getAllFactorsWithMetricsHistoricalEvaluation(projectExternalId, from, to);

        // Then
        assertEquals(dtoQualityFactorList.size(), dtoQualityFactorListFound.size());
        assertEquals(dtoQualityFactor, dtoQualityFactorListFound.get(0));
    }
}