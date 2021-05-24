package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.exceptions.QRPatternNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qr.QRGenerator;
import qr.models.Classifier;
import qr.models.FixedPart;
import qr.models.Form;
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

    public void createPattern(String name, String goal, String description, String requirement, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        FixedPart newFixedPart = new FixedPart(requirement, new ArrayList<>());
        Form newForm = new Form(name ,description, "", newFixedPart);
        List<Form> formList = new ArrayList<>();
        formList.add(newForm);
        QualityRequirementPattern newPattern = new QualityRequirementPattern(null, name, "", "", goal, formList, "");
        int newId = gen.createQRPattern(newPattern);
        List<Integer> classifierPatternsWithNewId = new ArrayList<>(classifierPatterns);
        classifierPatternsWithNewId.add(newId);
        gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatternsWithNewId);
    }

    public QualityRequirementPattern editPattern(Integer id, String name, String goal, String description, String fixedPartFormText, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) throws QRPatternNotFoundException {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        QualityRequirementPattern qrPattern = getOnePattern(id);
        if (qrPattern == null) {
            throw new QRPatternNotFoundException();
        }
        qrPattern.setName(name);
        qrPattern.setGoal(goal);
        qrPattern.getForms().get(0).setName(name);
        qrPattern.getForms().get(0).setDescription(description);
        qrPattern.getForms().get(0).getFixedPart().setFormText(fixedPartFormText);
        gen.updateQRPattern(id, qrPattern);
        gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatterns);
        return qrPattern;
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
}
