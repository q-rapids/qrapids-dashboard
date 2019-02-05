package com.upc.gessi.qrapids.app.database.repositories.Route;

import com.upc.gessi.qrapids.app.domain.repositories.Route.CustomRouteRepository;
import com.upc.gessi.qrapids.app.domain.models.Route;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


public class RouteRepositoryImpl implements CustomRouteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Route findByName(String name) {
        Route result = null;

        List<Route> routes = this.entityManager.createQuery("FROM Route AS u WHERE u.name = :name", Route.class)
                .setParameter("name", name)
                .getResultList();

        if( routes.size() > 0 ) {
            result = routes.get(0);
        }

        return result;
    }
}
