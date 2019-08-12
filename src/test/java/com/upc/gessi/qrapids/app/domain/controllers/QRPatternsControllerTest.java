package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.testHelpers.DomainObjectsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import qr.QRGenerator;
import qr.models.QualityRequirementPattern;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QRPatternsControllerTest {

    private DomainObjectsBuilder domainObjectsBuilder;

    @Mock
    private QRGeneratorFactory qrGeneratorFactory;

    @InjectMocks
    private QRPatternsController qrPatternsController;

    @Before
    public void setUp() {
        domainObjectsBuilder = new DomainObjectsBuilder();
    }

    @Test
    public void getPatternsForAlert() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);

        // Requirement pattern setup
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.generateQRs(ArgumentMatchers.any(qr.models.Alert.class))).thenReturn(qualityRequirementPatternList);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        List<QualityRequirementPattern> qualityRequirementPatternsFound = qrPatternsController.getPatternsForAlert(alert);

        // Then
        int expectedQRPatternsFound = 1;
        assertEquals(expectedQRPatternsFound, qualityRequirementPatternsFound.size());
        assertEquals(qualityRequirementPatternList.get(0), qualityRequirementPatternsFound.get(0));

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).generateQRs(ArgumentMatchers.any(qr.models.Alert.class));
        verifyNoMoreInteractions(qrGenerator);
    }
}