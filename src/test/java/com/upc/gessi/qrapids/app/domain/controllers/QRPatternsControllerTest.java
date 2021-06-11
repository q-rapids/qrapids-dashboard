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
import qr.models.Classifier;
import qr.models.Metric;
import qr.models.QualityRequirementPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void existsPatternForAlert() {
        // Given
        Project project = domainObjectsBuilder.buildProject();
        Alert alert = domainObjectsBuilder.buildAlert(project);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.existsQRPattern(any())).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean exists = qrPatternsController.existsPatternForAlert(alert);

        // Then
        assertTrue(exists);
    }

    @Test
    public void getAllPatterns() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        List<QualityRequirementPattern> qualityRequirementPatternList = new ArrayList<>();
        qualityRequirementPatternList.add(qualityRequirementPattern);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getAllQRPatterns()).thenReturn(qualityRequirementPatternList);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        List<QualityRequirementPattern> qualityRequirementPatternsFound = qrPatternsController.getAllPatterns();

        // Then
        int expectedQRPatternsFound = 1;
        assertEquals(expectedQRPatternsFound, qualityRequirementPatternsFound.size());
        assertEquals(qualityRequirementPatternList.get(0), qualityRequirementPatternsFound.get(0));

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getAllQRPatterns();
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getOnePattern() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getQRPattern(qualityRequirementPattern.getId())).thenReturn(qualityRequirementPattern);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        QualityRequirementPattern qualityRequirementPatternFound = qrPatternsController.getOnePattern(qualityRequirementPattern.getId());

        // Then
        assertEquals(qualityRequirementPattern, qualityRequirementPatternFound);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getQRPattern(qualityRequirementPattern.getId().longValue());
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getMetricForPattern() {
        // Given
        Integer patternId = 1;
        List<Integer> patternIdList = new ArrayList<>();
        patternIdList.add(patternId);

        String metric = "comments";
        Map<Integer, String> metrics = new HashMap<>();
        metrics.put(patternId, metric);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getMetricsForPatterns(patternIdList)).thenReturn(metrics);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        String metricFound = qrPatternsController.getMetricForPattern(patternId);

        // Then
        assertEquals(metric, metricFound);
    }

    @Test
    public void createPattern() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierPos = 0;
        List<Integer> classifierPatternsId = new ArrayList<>();
        List<Integer> classifierPatternsWithNewId = new ArrayList<>();
        classifierPatternsWithNewId.add(qualityRequirementPattern.getId());

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.createQRPattern(qualityRequirementPattern)).thenReturn(qualityRequirementPattern.getId());
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean createPatternResult = qrPatternsController.createPattern(qualityRequirementPattern, classifier.getId(), classifier.getName(), classifierPos, classifierPatternsId);

        // Then
        boolean expectedCreatePatternResult = true;
        assertEquals(expectedCreatePatternResult, createPatternResult);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).createQRPattern(qualityRequirementPattern);
        verify(qrGenerator, times(1)).updateClassifierWithPatterns(classifier.getId(), classifier.getName(), classifierPos, classifierPatternsWithNewId);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void editPattern() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierPos = 0;
        List<Integer> classifierPatternsId = new ArrayList<>();
        classifierPatternsId.add(qualityRequirementPattern.getId());

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.updateQRPattern(qualityRequirementPattern.getId(), qualityRequirementPattern)).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean editPatternResult = qrPatternsController.editPattern(qualityRequirementPattern.getId(), qualityRequirementPattern, classifier.getId(), classifier.getName(), classifierPos, classifierPatternsId);

        // Then
        boolean expectedEditPatternResult = true;
        assertEquals(expectedEditPatternResult, editPatternResult);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).updateQRPattern(qualityRequirementPattern.getId(), qualityRequirementPattern);
        verify(qrGenerator, times(1)).updateClassifierWithPatterns(classifier.getId(), classifier.getName(), classifierPos, classifierPatternsId);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void deletePattern() {
        // Given
        QualityRequirementPattern qualityRequirementPattern = domainObjectsBuilder.buildQualityRequirementPattern();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        qrPatternsController.deletePattern(qualityRequirementPattern.getId());

        // Then
        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).deleteQRPattern(qualityRequirementPattern.getId());
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getAllClassifiers() {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        List<Classifier> classifierList = new ArrayList<>();
        classifierList.add(classifier);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getAllClassifiers()).thenReturn(classifierList);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        List<Classifier> classifiersFound = qrPatternsController.getAllClassifiers();

        // Then
        int expectedClassifiersFound = 1;
        assertEquals(expectedClassifiersFound, classifiersFound.size());
        assertEquals(classifierList.get(0), classifiersFound.get(0));

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getAllClassifiers();
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getOneClassifier() {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getClassifier(classifier.getId())).thenReturn(classifier);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        Classifier classifierFound = qrPatternsController.getOneClassifier(classifier.getId());

        // Then
        assertEquals(classifier, classifierFound);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getClassifier(classifier.getId().longValue());
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void createClassifier() {
        // Given
        String classifierName = "commitresponsetime";
        Integer classifierParentId = 129;

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        qrPatternsController.createClassifier(classifierName, classifierParentId);

        // Then
        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).createClassifier(classifierName, classifierParentId);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void updateClassifier() {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();
        Integer classifierOldParentId = 123;
        Integer classifierNewParentId = 129;

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        qrPatternsController.updateClassifier(classifier.getId(), classifier.getName(), classifierOldParentId, classifierNewParentId);

        // Then
        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).updateAndMoveClassifier(classifier.getId(), classifier.getName(), classifierOldParentId, classifierNewParentId);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void deleteClassifier() {
        // Given
        Classifier classifier = domainObjectsBuilder.buildClassifier();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        qrPatternsController.deleteClassifier(classifier.getId());

        // Then
        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).deleteClassifier(classifier.getId());
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getAllMetrics() {
        // Given
        Metric metric = domainObjectsBuilder.buildQRPatternsMetric();
        List<Metric> metricList = new ArrayList<>();
        metricList.add(metric);

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getAllMetrics()).thenReturn(metricList);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        List<Metric> metricsFound = qrPatternsController.getAllMetrics();

        // Then
        int expectedMetricsFound = 1;
        assertEquals(expectedMetricsFound, metricsFound.size());
        assertEquals(metricList.get(0), metricsFound.get(0));

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getAllMetrics();
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void getOneMetric() {
        // Given
        Metric metric = domainObjectsBuilder.buildQRPatternsMetric();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.getMetric(metric.getId())).thenReturn(metric);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        Metric metricFound = qrPatternsController.getOneMetric(metric.getId());

        // Then
        assertEquals(metric, metricFound);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).getMetric(metric.getId().longValue());
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void createMetric() {
        // Given
        Metric metric = domainObjectsBuilder.buildQRPatternsMetric();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.createMetric(metric)).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean createMetricResult = qrPatternsController.createMetric(metric);

        // Then
        boolean expectedCreateMetricResult = true;
        assertEquals(expectedCreateMetricResult, createMetricResult);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).createMetric(metric);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void updateMetric() {
        // Given
        Metric metric = domainObjectsBuilder.buildQRPatternsMetric();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.updateMetric(metric.getId(), metric)).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean updateMetricResult = qrPatternsController.updateMetric(metric.getId(), metric);

        // Then
        boolean expectedUpdateMetricResult = true;
        assertEquals(expectedUpdateMetricResult, updateMetricResult);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).updateMetric(metric.getId(), metric);
        verifyNoMoreInteractions(qrGenerator);
    }

    @Test
    public void deleteMetric() {
        // Given
        Metric metric = domainObjectsBuilder.buildQRPatternsMetric();

        QRGenerator qrGenerator = mock(QRGenerator.class);
        when(qrGenerator.deleteMetric(metric.getId())).thenReturn(true);
        when(qrGeneratorFactory.getQRGenerator()).thenReturn(qrGenerator);

        // When
        boolean deleteMetricResult = qrPatternsController.deleteMetric(metric.getId());

        // Then
        boolean expectedDeleteMetricResult = true;
        assertEquals(expectedDeleteMetricResult, deleteMetricResult);

        verify(qrGeneratorFactory, times(1)).getQRGenerator();
        verifyNoMoreInteractions(qrGeneratorFactory);

        verify(qrGenerator, times(1)).deleteMetric(metric.getId());
        verifyNoMoreInteractions(qrGenerator);
    }
}