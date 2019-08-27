package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QRGeneratorFactory;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qr.QRGenerator;
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
}
