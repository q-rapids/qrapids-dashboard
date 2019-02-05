package com.upc.gessi.qrapids.app.database.repositories.QR;

import com.upc.gessi.qrapids.app.domain.repositories.QR.CustomQRRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class QRRepositoryImpl implements CustomQRRepository {

    @PersistenceContext
    private EntityManager entityManager;



}
