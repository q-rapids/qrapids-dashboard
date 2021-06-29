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

    /**
     * Create a new quality requirement pattern and add it to a classifier.
     * @param newPattern Pattern that will be created
     * @param classifierId Identifier of classifier where the new pattern will be added
     * @param classifierName Name of the classifier
     * @param classifierPos Position of the classifier
     * @param classifierPatterns Identifiers of patterns inside the classifier, excluding the new one
     * @return Pattern created successfully
     */
    public boolean createPattern(QualityRequirementPattern newPattern, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        int newId = gen.createQRPattern(newPattern);
        if (newId > -1) {
            List<Integer> classifierPatternsWithNewId = new ArrayList<>(classifierPatterns);
            classifierPatternsWithNewId.add(newId);
            gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatternsWithNewId);
            return true;
        }
        return false;
    }

    /**
     * Edit a quality requirement pattern and add it to a classifier.
     * @param id Pattern identifier
     * @param qrPattern Edited pattern
     * @param classifierId Identifier of classifier where the pattern will be added
     * @param classifierName Name of the classifier
     * @param classifierPos Position of the classifier
     * @param classifierPatterns Identifiers of patterns inside the classifier, including the edited one
     * @return Pattern edited successfully
     */
    public boolean editPattern(Integer id, QualityRequirementPattern qrPattern, Integer classifierId, String classifierName, Integer classifierPos, List<Integer> classifierPatterns) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        if (gen.updateQRPattern(id, qrPattern)) {
            gen.updateClassifierWithPatterns(classifierId, classifierName, classifierPos, classifierPatterns);
            return true;
        }
        return false;
    }

    /**
     * Delete a quality requirement pattern.
     * @param id Pattern identifier
     */
    public void deletePattern(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.deleteQRPattern(id);
    }

    /**
     * Get all the classifiers.
     * @return List containing all the classifiers
     */
    public List<Classifier> getAllClassifiers() {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllClassifiers();
    }

    /**
     * Get one classifier.
     * @param id Classifier identifier
     * @return Classifier with the given identifier
     */
    public Classifier getOneClassifier(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getClassifier(id.longValue());
    }

    /**
     * Create a new classifier.
     * @param name New classifier name
     * @param parentId Identifier of the classifier where the new classifier will be added
     */
    public void createClassifier(String name, Integer parentId) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.createClassifier(name, parentId.longValue());
    }

    /**
     * Edit a classifier.
     * @param id Classifier identifier
     * @param name New name of edited classifier
     * @param oldParentId Old parent classifier identifier
     * @param newParentId New parent classifier identifier
     */
    public void updateClassifier(Integer id, String name, Integer oldParentId, Integer newParentId) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.updateAndMoveClassifier(id.longValue(), name, oldParentId.longValue(), newParentId.longValue());
    }

    /**
     * Delete a classifier.
     * @param id Classifier identifier
     */
    public void deleteClassifier(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        gen.deleteClassifier(id.longValue());
    }

    /**
     * Get all the metrics
     * @return List containing all the metrics
     */
    public List<Metric> getAllMetrics() {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getAllMetrics();
    }

    /**
     * Get one metric.
     * @param id Metric identifier
     * @return Metric with the given identifier
     */
    public Metric getOneMetric(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.getMetric(id);
    }

    /**
     * Create a new metric.
     * @param m Metric that will be created
     * @return Metric created successfully
     */
    public boolean createMetric(Metric m) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.createMetric(m);
    }

    /**
     * Edit a metric.
     * @param id Metric identifier
     * @param m Edited metric
     * @return Metric edited successfully
     */
    public boolean updateMetric(Integer id, Metric m) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.updateMetric(id, m);
    }

    /**
     * Delete a metric.
     * @param id Metric identifier
     * @return Metric deleted successfully
     */
    public boolean deleteMetric(Integer id) {
        QRGenerator gen = qrGeneratorFactory.getQRGenerator();
        return gen.deleteMetric(id);
    }
}
