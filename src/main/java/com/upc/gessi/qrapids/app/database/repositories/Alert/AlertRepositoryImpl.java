package com.upc.gessi.qrapids.app.database.repositories.Alert;

import com.upc.gessi.qrapids.app.domain.repositories.Alert.CustomAlertRepository;
import com.upc.gessi.qrapids.app.dto.DTOAlert;
import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertArtefacts;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AlertRepositoryImpl implements CustomAlertRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<DTOAlert> getAlerts(){
        TypedQuery<Alert> alertquery = this.entityManager.createQuery("SELECT a FROM Alert a", Alert.class);

        List<Alert> alerts = alertquery.getResultList();


        TypedQuery<AlertArtefacts> artequery = this.entityManager.createQuery("SELECT aa FROM AlertArtefacts aa", AlertArtefacts.class);

        List<AlertArtefacts> artefacts = artequery.getResultList();

        List<DTOAlert> dtoAlerts = new ArrayList<>();
        System.out.println(1);
        for (int i = 0; i < alerts.size(); ++i){
            System.out.println(i);
            Alert alert = alerts.get(i);
            System.out.println(alert.getId_element());
            DTOAlert dtoAlert = new DTOAlert(alert.getId(), alert.getId_element(), alert.getName(), alert.getType(), alert.getValue(), alert.getThreshold(), alert.getCategory(), new Date(alert.getDate().getTime()), alert.getStatus(), alert.isReqAssociat(), null);
            for (int j = 0; j < artefacts.size(); ++j){
                AlertArtefacts alertArtefacts = artefacts.get(i);
                List<String> artefactsName = new ArrayList<>();
                if (alertArtefacts.getId_alert().equals(alert.getId())){
                    artefactsName.add(alertArtefacts.getArtefact());
                }
                dtoAlert.setArtefacts(artefactsName);
                System.out.println(dtoAlert.getName());
            }
            dtoAlerts.add(dtoAlert);
        }
        return dtoAlerts;
    }

    @Override
    public List<Alert> getAlertByName(String name) {
        TypedQuery<Alert> alertquery = this.entityManager.createQuery("SELECT a FROM Alert a WHERE a.name = :name", Alert.class)
                .setParameter("name", name);

        List<Alert> alerts = alertquery.getResultList();
        return alerts;
    }


}
