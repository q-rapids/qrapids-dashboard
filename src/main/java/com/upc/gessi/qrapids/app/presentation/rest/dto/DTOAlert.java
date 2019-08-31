package com.upc.gessi.qrapids.app.presentation.rest.dto;

import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import com.upc.gessi.qrapids.app.domain.models.AlertType;

import java.sql.Date;
import java.util.List;

public class DTOAlert {

    private Long id;
    private String id_element;
    private String name;
    private AlertType type;
    private float value;
    private float threshold;
    private String category;
    private Date date;
    private AlertStatus status;
    private boolean reqAssociat;

    private List<String> artefacts;

    public DTOAlert(Long id, String id_element, String name, AlertType type, float value, float threshold, String category, Date date, AlertStatus status, boolean reqAssociat, List<String> artefacts) {
        this.id = id;
        this.id_element = id_element;
        this.name = name;
        this.type = type;
        this.value = value;
        this.threshold = threshold;
        this.category = category;
        this.date = date;
        this.status = status;
        this.reqAssociat = reqAssociat;
        this.artefacts = artefacts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getId_element() {
        return id_element;
    }

    public void setId_element(String id_element) {
        this.id_element = id_element;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public boolean isReqAssociat() {
        return reqAssociat;
    }

    public void setReqAssociat(boolean reqAssociat) {
        this.reqAssociat = reqAssociat;
    }

    public List<String> getArtefacts() {
        return artefacts;
    }

    public void setArtefacts(List<String> artefacts) {
        this.artefacts = artefacts;
    }
}
