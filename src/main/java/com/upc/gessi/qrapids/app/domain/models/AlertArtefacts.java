package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;

@Entity
@Table(name="alert_artefacts", uniqueConstraints=
@UniqueConstraint(columnNames={"id_alert", "artefact"}))
public class AlertArtefacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_alert")
    private String id_alert;
    @Column(name = "artefact")
    private String artefact;

    public AlertArtefacts() {
    }

    public AlertArtefacts(String id_alert, String artefact) {
        this.id_alert = id_alert;
        this.artefact = artefact;
    }

    public String getId_alert() {
        return id_alert;
    }

    public void setId_alert(String id_alert) {
        this.id_alert = id_alert;
    }

    public String getArtefact() {
        return artefact;
    }

    public void setArtefact(String artefact) {
        this.artefact = artefact;
    }
}
