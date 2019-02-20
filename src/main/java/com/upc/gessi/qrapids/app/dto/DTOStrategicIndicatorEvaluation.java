package com.upc.gessi.qrapids.app.dto;

import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.List;

/**
 * This class define objects with all the information about a Strategic Indicator, which includes the Factor id, name and a
 * set of Metrics
 *
 * @author Oriol M./Guillem B./Lidia L.
 */
public class DTOStrategicIndicatorEvaluation {
    //class attributes
    private String id;
    private Long dbId;
    private String name;
    private String description;
    private Pair<Float, String> value;
    private String value_description;
    private List<DTOSIAssesment> probabilities;
    private LocalDate date;
    private String datasource;
    private String categories_description;
    private boolean hasBN;
    private boolean hasFeedback;
    private String forecastingError;

    /**
     * Constructor of the DTO of Strategic Indicators Evaluation
     *
     * @param id The parameter defines the ID of the Strategic Indicator
     * @param name The parameter defines the name of the Strategic Indicator
     * @param description The parameter defines the description of the Strategic Indicator
     * @param probabilities The parameter defines the target of the KPI
     * @param value The parameter defines the value of the Strategic Indicator evaluation
     * @param date The parameter defines the date of the Strategic Indicator evaluation
     * @param datasource The parameter defines the data source of the Strategic Indicator evaluation
     * @param dbId The parameter defines the database id of the Strategic Indicator
     * @param categories The parameter include the list of categories associated to the strategic indicator
     */
    public DTOStrategicIndicatorEvaluation(String id, String name, String description, Pair<Float, String> value, List<DTOSIAssesment> probabilities, LocalDate date, String datasource, Long dbId, String categories, boolean hasBN) {
        setId(id);
        setName(name);
        setDescription(description);
        setValue(value);
        setProbabilities(probabilities);
        setDate(date);
        setDbId(dbId);
        setDatasource(datasource);
        setCategories_description(categories);
        setHasBN(hasBN);
    }

    public DTOStrategicIndicatorEvaluation(String id, String name, String forecastingError) {
        this.id = id;
        this.name = name;
        this.forecastingError = forecastingError;
    }

    /**
     * All the getters from the class DTOStrategicIndicatorsEvaluation that returns the value of the attribute defined
     * in the header of the getter
     *
     * @return the value of the attribute defined in the header of the getter
     */

    /**
     * All the setters from the class DTODetailedStrategicIndicators that set the value of the respective attribute
     * passed as parameter in the value of the class attribute
     *
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null)
            this.description = description;
    }


    public Pair<Float, String> getValue() {
        return value;
    }

    public String getValue_description() { return value_description;}

    private void setValue_description(Pair<Float, String> value) {

        String numeric_value;

        if (value.getFirst()==null)
            numeric_value="";
        else
            numeric_value = String.format("%.2f", value.getFirst());

        if (value.getSecond().isEmpty())
            this.value_description = numeric_value;
        else{
            this.value_description = value.getSecond();
            if (!numeric_value.isEmpty())
                this.value_description += " (" + numeric_value + ')';
        }
    }

    public void setValue(Pair<Float, String> value) {
        this.value = value;
        setValue_description(value);
    }


    public List<DTOSIAssesment> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<DTOSIAssesment> probabilities) {
        this.probabilities = probabilities;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {

        if (datasource !=null )
            this.datasource = datasource;
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getCategories_description() {
        return categories_description;
    }

    public void setCategories_description(String categories_description) {
        if (categories_description!=null)
            this.categories_description = categories_description;
    }

    public boolean isHasBN() {
        return hasBN;
    }

    public void setHasBN(boolean hasBN) {
        this.hasBN = hasBN;
    }

    public boolean isHasFeedback() {
        return hasFeedback;
    }

    public void setHasFeedback(boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
    }

    public String getForecastingError() {
        return forecastingError;
    }

    public void setForecastingError(String forecastingError) {
        this.forecastingError = forecastingError;
    }
}
