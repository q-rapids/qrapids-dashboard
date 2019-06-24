package com.upc.gessi.qrapids.app.dto;

import org.springframework.data.util.Pair;

import java.time.LocalDate;

/**
 * This class define objects with all the information about a Metric, which includes the Metric id, name, source,
 * evaluationdate and evaluationValue
 *
 * @author Oriol M./Guillem B/Lidia L.
 */
public class DTOMetric {

    //class attributes
    private String id;
    private String name;
    private String description;
    private Float value;
    private String value_description;
    private LocalDate date;
    private String datasource;
    private String rationale;
    private Pair<Float, Float> confidence80;
    private Pair<Float, Float> confidence95;
    private String forecastingError;

    /**
     * Constructor of the DTO of Metrics
     *
     * @param id The parameter defines the ID of the Metric
     * @param name The parameter defines the name of the Metric
     * @param description The parameter defines the description of the Metric
     * @param value The parameter defines the value of the metric evaluation
     * @param date The parameter defines the date of the metric evaluation
     * @param datasource The parameter defines the datasource of the metric evaluation
     * @param rationale The parameter describes textually the rationale behind the value
     */
    public DTOMetric(String id, String name, String description, String datasource, String rationale, LocalDate date, float value) {
        setId(id);
        setName(name);
        setDescription(description);
        setValue(value);
        setDate(date);
        setDatasource(datasource);
        setRationale(rationale);
    }

    /**
     * Constructor of the DTO of Metrics
     *
     * @param id The parameter defines the ID of the Metric
     * @param name The parameter defines the name of the Metric
     * @param description The parameter defines the description of the Metric
     * @param value The parameter defines the value of the metric evaluation
     * @param date The parameter defines the date of the metric evaluation
     * @param datasource The parameter defines the datasource of the metric evaluation
     * @param rationale The parameter describes textually the rationale behind the value
     * @param confidence80 Upper and lower values respectively for the 80% confidence interval
     * @param confidence95 Upper and lower values respectively for the 95% confidence interval
     */
    public DTOMetric(String id, String name, String description, String datasource, String rationale, LocalDate date, Float value, Pair<Float, Float> confidence80, Pair<Float, Float> confidence95) {
        setId(id);
        setName(name);
        setDescription(description);
        setValue(value);
        setDate(date);
        setDatasource(datasource);
        setRationale(rationale);
        setConfidence80(confidence80);
        setConfidence95(confidence95);
    }

    public DTOMetric(String id, String name, String forecastingError) {
        this.id = id;
        this.name = name;
        this.forecastingError = forecastingError;
    }

    public DTOMetric() {
    }

    /**
     * All the getters from the class DTOMetric that returns the value of the attribute defined
     * in the header of the getter
     *
     * @return the value of the attribute defined in the header of the getter
     */

    public String getId() {
        return id;
    }

    public String getName() {
        if (this.name.isEmpty()) {
            return this.id;
        } else {
            if (this.name.startsWith("\"") && this.name.endsWith("\"")) {
                return this.name.substring(1, this.name.length()-1);
            }
            else return this.name;
        }
    }

    public String getDescription() { return description;}

    public Float getValue() {
        return value;
    }

    public String getValue_description() { return value_description;}

    public LocalDate getDate() {
        return date;
    }

    public String getDatasource() {
        return datasource;
    }

    public String getRationale() {return rationale;}

    public Pair<Float, Float> getConfidence80() {
        return confidence80;
    }

    public void setConfidence80(Pair<Float, Float> confidence80) {
        this.confidence80 = confidence80;
    }

    public Pair<Float, Float> getConfidence95() {
        return confidence95;
    }

    public void setConfidence95(Pair<Float, Float> confidence95) {
        this.confidence95 = confidence95;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        if (description!=null)
            this.description = description;
    }

    public void setValue(Float value) {
        this.value = value;
        setValue_description(value);
    }

    private void setValue_description(Float value) {
        value_description = String.format("%.2f", value);
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDatasource(String datasource) {
        if (datasource!=null)
            this.datasource = datasource;
    }

    public void setRationale(String rationale) {
        if (rationale!=null)
            this.rationale = rationale;
    }

    public String getForecastingError() {
        return forecastingError;
    }

    public void setForecastingError(String forecastingError) {
        this.forecastingError = forecastingError;
    }
}
