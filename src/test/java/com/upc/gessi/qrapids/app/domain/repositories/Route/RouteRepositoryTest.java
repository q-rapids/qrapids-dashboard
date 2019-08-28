package com.upc.gessi.qrapids.app.domain.repositories.Route;

import com.upc.gessi.qrapids.app.domain.models.Route;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RouteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RouteRepository routeRepository;

    @Test
    public void findByName() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persistAndFlush(route2);

        // When
        Route routeFound = routeRepository.findByName(route1Name);

        // Then
        assertEquals(route1, routeFound);
    }
}