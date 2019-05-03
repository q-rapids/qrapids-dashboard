package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "alert")
public class Alert implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_element")
    private String id_element;
    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private AlertType type;
    @Column(name = "value")
    private float value;
    @Column(name = "threshold")
    private float threshold;
    @Column(name = "category")
    private String category;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "status")
    private AlertStatus status;
    @Column(name = "reqAssociat")
    private boolean reqAssociat;
    @ManyToOne
    @JoinColumn(name="decisionId", referencedColumnName = "id")
    private Decision decision;
    @ManyToOne
    @JoinColumn(name="projectId", referencedColumnName = "id")
    private Project project;

    public Alert(){

    }

    public Alert(String id_element, String name, AlertType type, float value, float threshold, String category, Date date, AlertStatus status, boolean reqAssociat, Project project) {
        this.id_element = id_element;
        this.name = name;
        this.type = type;
        this.value = value;
        this.threshold = threshold;
        this.category = category;
        this.date = date;
        this.status = status;
        this.reqAssociat = reqAssociat;
        this.project = project;
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

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
