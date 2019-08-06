package com.upc.gessi.qrapids.app.domain.repositories.Route;

import com.upc.gessi.qrapids.app.domain.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RouteRepository extends CrudRepository<Route, Long> {

    Route findByName(String name);

}
