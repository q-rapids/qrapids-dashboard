package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import DTOs.Relations.RelationDTO;
import DTOs.Relations.SourceRelationDTO;
import DTOs.Relations.TargetRelationDTO;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.services.Util;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsFactor;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsMetric;
import com.upc.gessi.qrapids.app.dto.relations.DTORelationsSI;
import evaluation.Relations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QMARelations {

    @Autowired
    private QMAConnection qmacon;

    @Autowired
    private Util util;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    private static final String SI_TYPE = "strategic_indicators";
    private static final String FACTORS_TYPE = "factors";
    private static final String METRICS_TYPE = "metrics";

    public boolean setStrategicIndicatorFactorRelation(String projectId,
                                                       List<String> factorIds,
                                                       String strategicIndicatorId,
                                                       LocalDate evaluationDate,
                                                       List<Float> weights,
                                                       List<Float> factorValues,
                                                       List<String> factorLabels,
                                                       String strategicIndicatorValue) throws IOException {
        return Relations.setStrategicIndicatorFactorRelation(
                projectId,
                factorIds.toArray(new String[0]),
                strategicIndicatorId,
                evaluationDate,
                convertFloatListToDoubleArray(weights),
                convertFloatListToDoubleArray(factorValues),
                factorLabels.toArray(new String[0]),
                strategicIndicatorValue);
    }

    private double[] convertFloatListToDoubleArray(List<Float> floatList) {
        double[] doubleArray = new double[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            doubleArray[i] = floatList.get(i).doubleValue();
        }
        return doubleArray;
    }

    public List<DTORelationsSI> getRelations (String prj, LocalDate date) throws IOException {
        qmacon.initConnexion();
        List<RelationDTO> relationDTOS;
        if (date == null)
            relationDTOS = Relations.getRelations(prj);
        else
            relationDTOS = Relations.getRelations(prj, date);

        return RelationDTOToDTORelationSI(relationDTOS);
    }

    private List<DTORelationsSI> RelationDTOToDTORelationSI (List<RelationDTO> relationDTOS) {
        Map<String, DTORelationsSI> strategicIndicatorsMap = new HashMap<>();
        Map<String, DTORelationsFactor> factorsMap = new HashMap<>();
        Map<String, DTORelationsMetric> metricsMap = new HashMap<>();

        for (RelationDTO relation : relationDTOS) {
            String weight = relation.getWeight();
            SourceRelationDTO source = relation.getSource();
            TargetRelationDTO target = relation.getTarget();
            if (target.getType().equals(FACTORS_TYPE) && source.getType().equals(METRICS_TYPE)) {
                DTORelationsFactor factor;
                if (factorsMap.containsKey(target.getID())) {
                    factor = factorsMap.get(target.getID());
                } else {
                    factor = new DTORelationsFactor(target.getID());
                    factorsMap.put(target.getID(), factor);
                }

                DTORelationsMetric metric;
                if (metricsMap.containsKey(source.getID())) {
                    metric = metricsMap.get(source.getID());
                } else {
                    metric = new DTORelationsMetric(source.getID());
                    metricsMap.put(source.getID(), metric);
                }
                metric.setWeight(weight);
                metric.setValue(source.getValue());

                factor.setMetric(new DTORelationsMetric(metric));
            }
            else if (target.getType().equals(SI_TYPE) && source.getType().equals(FACTORS_TYPE)) {
                DTORelationsSI strategicIndicator;
                if (strategicIndicatorsMap.containsKey(target.getID())) {
                    strategicIndicator = strategicIndicatorsMap.get(target.getID());
                } else {
                    strategicIndicator = new DTORelationsSI(target.getID());
                    strategicIndicatorsMap.put(target.getID(), strategicIndicator);
                }
                strategicIndicator.setValue(target.getValue());
                try {
                    Float value = Float.parseFloat(strategicIndicator.getValue());
                    String label = strategicIndicatorsController.getLabel(value);
                    String valueDescription = StrategicIndicatorsController.buildDescriptiveLabelAndValue(Pair.of(value, label));
                    strategicIndicator.setValueDescription(valueDescription);
                    strategicIndicator.setColor(strategicIndicatorsController.getColorFromLabel(label));
                }
                catch (NumberFormatException nfe) {
                    String label = strategicIndicator.getValue();
                    Float value = strategicIndicatorsController.getValueFromLabel(label);
                    String valueDescription = StrategicIndicatorsController.buildDescriptiveLabelAndValue(Pair.of(value, label));
                    strategicIndicator.setValueDescription(valueDescription);
                    strategicIndicator.setColor(strategicIndicatorsController.getColorFromLabel(label));
                }

                DTORelationsFactor factor;
                if (factorsMap.containsKey(source.getID())) {
                    factor = factorsMap.get(source.getID());
                } else {
                    factor = new DTORelationsFactor(source.getID());
                    factorsMap.put(source.getID(), factor);
                }
                factor.setWeight(weight);
                factor.setValue(source.getValue());

                strategicIndicator.setFactor(new DTORelationsFactor(factor));
            }
        }

        return new ArrayList<>(strategicIndicatorsMap.values());
    }
}
