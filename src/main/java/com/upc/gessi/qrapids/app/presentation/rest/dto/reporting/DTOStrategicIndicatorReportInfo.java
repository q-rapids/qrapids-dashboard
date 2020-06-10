package com.upc.gessi.qrapids.app.presentation.rest.dto.reporting;

import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOSIAssessment;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.List;

public class DTOStrategicIndicatorReportInfo {
    //class attributes
    private String id;
    private Long dbId;
    private String prj_name;
    private String name;
    private String description;
    private Pair<Float, String> current_value;
    private String current_value_description;
    private String current_rationale;
    private LocalDate current_date;
    private List<DTOSIAssessment> probabilities;
    private List<DTOHistoricalData> historicalDataList;

    public static class DTOHistoricalData {
        private Pair<Float, String> value;
        private String value_description;
        private String rationale;
        private LocalDate date;

        public DTOHistoricalData(Pair<Float, String> value, String rationale, LocalDate date) {
            setValue(value);
            this.value_description = StrategicIndicatorsController.buildDescriptiveLabelAndValue(value);
            setRationale(rationale);
            setDate(date);
        }

        public void setValue(Pair<Float, String> value) {
            this.value = value;
        }

        public Pair<Float, String> getValue() {
            return value;
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

    public DTOStrategicIndicatorReportInfo(String id, String prj_name, String name, String description, Pair<Float, String> value, Long dbId,
                                            String rationale, List<DTOSIAssessment> probabilities, LocalDate date) {
        setId(id);
        setPrj_name(prj_name);
        setName(name);
        setDescription(description);
        setCurrent_value(value);
        setCurrent_rationale(rationale);
        setProbabilities(probabilities);
        setCurrent_date(date);
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

    public String getPrj_name() {
        return prj_name;
    }

    public void setPrj_name(String prj_name) {
        this.prj_name = prj_name;
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

    public Pair<Float, String> getCurrent_value() {
        return current_value;
    }

    public String getCurrent_value_description() { return current_value_description;}

    private void setCurrent_value_description(Pair<Float, String> value) {
        this.current_value_description = StrategicIndicatorsController.buildDescriptiveLabelAndValue(value);
    }

    public void setCurrent_value(Pair<Float, String> value) {
        this.current_value = value;
        setCurrent_value_description(value);
    }

    public void setCurrent_rationale(String rationale) {
        this.current_rationale = rationale;
    }

    public String getCurrent_rationale() {
        return current_rationale;
    }

    public List<DTOSIAssessment> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<DTOSIAssessment> probabilities) {
        this.probabilities = probabilities;
    }

    public LocalDate getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(LocalDate date) {
        this.current_date = date;
    }

    public List<DTOHistoricalData> getHistoricalDataList() {
        return historicalDataList;
    }

    public void setHistoricalDataList(List<DTOHistoricalData> historicalData) {
        this.historicalDataList = historicalData;
    }

}
