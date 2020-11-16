package com.upc.gessi.qrapids.app.presentation.rest.dto;

import com.upc.gessi.qrapids.app.domain.controllers.FactorsController;

import java.time.LocalDate;
import java.util.List;

/**
 * This class define objects with all the information about a Quality Factor, which includes the Factor id, name and a
 * set of Metrics
 *
 * @author Oriol M./Guillem B.
 */
public class DTODetailedFactorEvaluation {

    //class attributes
    private String id;
    private String name;
    private LocalDate date;
    private Float value;
    private String value_description;
    private int mismatchDays;
    private List<String> missingMetrics;
    private List<DTOMetricEvaluation> metrics;

    /**
     * Constructor of the DTO of Quality Factors
     *
     * @param id The parameter defines the ID of the Factor
     * @param name The parameter defines the name of the Factor
     * @param metrics The parameter define the set of metrics that compose the Quality Factors
     */
    public DTODetailedFactorEvaluation(String id, String name, List<DTOMetricEvaluation> metrics) {
        this.id = id;
        this.name = name;
        this.metrics = metrics;
    }

    /**
     * All the getters from the class DTOQualityFactor that returns the value of the attribute defined
     * in the header of the getter
     *
     * @return the value of the attribute defined in the header of the getter
     */

    public String getId() {
        return id;
    }

    public String getName() {
        return this.name.isEmpty() ? this.id : this.name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
        setValue_description(value);
    }

    public String getValue_description() { return value_description;}

    private void setValue_description(Float value) {
        this.value_description = FactorsController.buildDescriptiveLabelAndValue(value);
    }

    public int getMismatchDays() {
        return mismatchDays;
    }

    public void setMismatchDays(int mismatchDays) {
        this.mismatchDays = mismatchDays;
    }

    public List<String> getMissingMetrics() {
        return missingMetrics;
    }

    public void setMissingMetrics(List<String> missingMetrics) {
        this.missingMetrics = missingMetrics;
    }

    public List<DTOMetricEvaluation> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<DTOMetricEvaluation> metrics) {
        this.metrics = metrics;
    }
}
