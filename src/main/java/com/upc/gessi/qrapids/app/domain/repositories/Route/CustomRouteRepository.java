package com.upc.gessi.qrapids.app.domain.repositories.Route;

import com.upc.gessi.qrapids.app.domain.models.Route;

import java.io.Serializable;

public interface CustomRouteRepository extends Serializable {
    Route findByName(String name);
}
