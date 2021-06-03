package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.exceptions.QRPatternNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qr.QRGenerator;
import qr.models.Classifier;
import qr.models.Metric;
import qr.models.QualityRequirementPattern;
import qr.models.enumerations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QRPatternsController {

    @Autowired
    QRGeneratorFactory qrGeneratorFactory;

    public List<QualityRequirementPattern> getPatternsForAlert(Alert alert) {
        qr.models.Alert alertModel = new qr.models.Alert(alert.getId_element(), alert.getName(), Type.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), null);
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.generateQRs(alertModel);
    }

    public boolean existsPatternForAlert (Alert alert) {
        qr.models.Alert alertModel = new qr.models.Alert(alert.getId_element(), alert.getName(), Type.valueOf(alert.getType().toString()), alert.getValue(), alert.getThreshold(), alert.getCategory(), null);
        QRGenerator qrGenerator = qrGeneratorFactory.getQRGenerator();
        return qrGenerator.existsQRPattern(alertModel);
    }

    public List<QualityRequirementPattern> getAllPatterns () {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllQRPatterns();
    }

    public QualityRequirementPattern getOnePattern (Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getQRPattern(id.longValue());
    }

    public String getMetricForPattern (Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        return gen.getMetricsForPatterns(ids).get(id);
    }

    public Map<Integer, String> getMetricsForPatterns (List<Integer> ids) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getMetricsForPatterns(ids);
    }

    public void createPattern(QualityRequirementPattern newPattern, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        int newId = gen.createQRPattern(newPattern);
        List<Integer> classifierPatternsWithNewId = new ArrayList<>(classifierPatterns);
        classifierPatternsWithNewId.add(newId);
        gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatternsWithNewId);
    }

    public void editPattern(Integer id, QualityRequirementPattern qrPattern, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.updateQRPattern(id, qrPattern);
        gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatterns);
    }

    public void deletePattern(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.deleteQRPattern(id);
    }

    public List<Classifier> getAllClassifiers() {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllClassifiers();
    }

    public Classifier getOneClassifier(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getClassifier(id.longValue());
    }

    public void createClassifier(String name, Integer parentId) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.createClassifier(name, parentId.longValue());
    }

    public void updateClassifier(Integer id, String name, Integer oldParentId, Integer newParentId) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.updateAndMoveClassifier(id.longValue(), name, oldParentId.longValue(), newParentId.longValue());
    }

    public void deleteClassifier(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.deleteClassifier(id.longValue());
    }

    public List<Metric> getAllMetrics() {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllMetrics();
    }

    public Metric getOneMetric(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getMetric(id);
    }

    public boolean createMetric(Metric m) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.createMetric(m);
    }

    public boolean updateMetric(Integer id, Metric m) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.updateMetric(id, m);
    }

    public boolean deleteMetric(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.deleteMetric(id);
    }
}
