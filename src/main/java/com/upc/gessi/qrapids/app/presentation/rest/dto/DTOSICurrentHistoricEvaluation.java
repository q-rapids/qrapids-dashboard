package com.upc.gessi.qrapids.app.presentation.rest.dto;

import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.List;

public class DTOSICurrentHistoricEvaluation {
    //class attributes
    private String id;
    private Long dbId;
    private String prjName;
    private String name;
    private String description;
    private Pair<Float, String> currentValue;
    private String currentValueDescription;
    private String currentRationale;
    private LocalDate currentDate;
    private List<DTOSIAssessment> probabilities;
    private List<DTOHistoricalData> historicalDataList;

    public static class DTOHistoricalData {
        private Pair<Float, String> value;
        private String valueDescription;
        private String rationale;
        private LocalDate date;

        public DTOHistoricalData(Pair<Float, String> value, String rationale, LocalDate date) {
            setValue(value);
            this.valueDescription = StrategicIndicatorsController.buildDescriptiveLabelAndValue(value);
            setRationale(rationale);
            setDate(date);
        }

        public void setValue(Pair<Float, String> value) {
            this.value = value;
        }

        public Pair<Float, String> getValue() {
            return value;
        }

        public String getValueDescription() {
            return valueDescription;
        }

        public void setValueDescription(String valueDescription) {
            this.valueDescription = valueDescription;
        }

        public void setRationale(String rationale) {
            this.rationale = rationale;
        }

        public String getRationale() {
            return rationale;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    public DTOSICurrentHistoricEvaluation(String id, String prj_name, String name, String description, Pair<Float, String> value, Long dbId,
                                          String rationale, List<DTOSIAssessment> probabilities, LocalDate date) {
        setId(id);
        setPrjName(prj_name);
        setName(name);
        setDescription(description);
        setCurrentValue(value);
        setCurrentRationale(rationale);
        setProbabilities(probabilities);
        setCurrentDate(date);
        setDbId(dbId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
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

    public Pair<Float, String> getCurrentValue() {
        return currentValue;
    }

    public String getCurrentValueDescription() { return currentValueDescription;}

    private void setCurrentValueDescription(Pair<Float, String> value) {
        this.currentValueDescription = StrategicIndicatorsController.buildDescriptiveLabelAndValue(value);
    }

    public void setCurrentValue(Pair<Float, String> value) {
        this.currentValue = value;
        setCurrentValueDescription(value);
    }

    public void setCurrentRationale(String rationale) {
        this.currentRationale = rationale;
    }

    public String getCurrentRationale() {
        return currentRationale;
    }

    public List<DTOSIAssessment> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<DTOSIAssessment> probabilities) {
        this.probabilities = probabilities;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public List<DTOHistoricalData> getHistoricalDataList() {
        return historicalDataList;
    }

    public void setHistoricalDataList(List<DTOHistoricalData> historicalData) {
        this.historicalDataList = historicalData;
    }

}
