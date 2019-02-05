package com.upc.gessi.qrapids.app.database.repositories.Alert;

import com.upc.gessi.qrapids.app.domain.repositories.Alert.CustomAlertArtefactsRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AlertArtefactsRepositoryImpl implements CustomAlertArtefactsRepository {

    @PersistenceContext
    private EntityManager entityManager;
}
