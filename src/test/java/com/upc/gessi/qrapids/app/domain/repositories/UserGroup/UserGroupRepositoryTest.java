package com.upc.gessi.qrapids.app.domain.repositories.UserGroup;

import com.upc.gessi.qrapids.app.domain.models.Route;
import com.upc.gessi.qrapids.app.domain.models.UserGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ComponentScan("com.upc.gessi.qrapids.app.database.repositories")
@DataJpaTest
public class UserGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Test
    public void findName() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String userGroup1Name = "group1";
        Set<Route> routes1 = new HashSet<>();
        routes1.add(route1);
        UserGroup userGroup1 = new UserGroup(userGroup1Name, routes1);
        entityManager.persist(userGroup1);

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persist(route2);

        String userGroup2Name = "group2";
        Set<Route> routes2 = new HashSet<>();
        routes2.add(route2);
        UserGroup userGroup2 = new UserGroup(userGroup2Name, routes2);
        entityManager.persistAndFlush(userGroup2);

        // When
        UserGroup userGroupFound = userGroupRepository.findByName(userGroup1Name);

        // Then
        assertEquals(userGroup1, userGroupFound);
    }

    @Test
    public void findDefaultUserGroup() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String userGroup1Name = "group1";
        Set<Route> routes1 = new HashSet<>();
        routes1.add(route1);
        UserGroup userGroup1 = new UserGroup(userGroup1Name, routes1);
        userGroup1.setDefaultGroup(true);
        entityManager.persist(userGroup1);

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persist(route2);

        String userGroup2Name = "group2";
        Set<Route> routes2 = new HashSet<>();
        routes2.add(route2);
        UserGroup userGroup2 = new UserGroup(userGroup2Name, routes2);
        userGroup2.setDefaultGroup(false);
        entityManager.persistAndFlush(userGroup2);

        // When
        UserGroup userGroupFound = userGroupRepository.findByDefaultGroupIsTrue();

        // Then
        assertEquals(userGroup1, userGroupFound);
    }

    @Test
    public void hasDefaultGroup() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String userGroup1Name = "group1";
        Set<Route> routes1 = new HashSet<>();
        routes1.add(route1);
        UserGroup userGroup1 = new UserGroup(userGroup1Name, routes1);
        userGroup1.setDefaultGroup(true);
        entityManager.persist(userGroup1);

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persist(route2);

        String userGroup2Name = "group2";
        Set<Route> routes2 = new HashSet<>();
        routes2.add(route2);
        UserGroup userGroup2 = new UserGroup(userGroup2Name, routes2);
        userGroup2.setDefaultGroup(false);
        entityManager.persistAndFlush(userGroup2);

        // When
        boolean found = userGroupRepository.existsByDefaultGroupIsTrue();

        // Then
        assertTrue(found);
    }

    @Test
    public void hasNotDefaultGroup() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String userGroup1Name = "group1";
        Set<Route> routes1 = new HashSet<>();
        routes1.add(route1);
        UserGroup userGroup1 = new UserGroup(userGroup1Name, routes1);
        entityManager.persist(userGroup1);

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persist(route2);

        String userGroup2Name = "group2";
        Set<Route> routes2 = new HashSet<>();
        routes2.add(route2);
        UserGroup userGroup2 = new UserGroup(userGroup2Name, routes2);
        userGroup2.setDefaultGroup(false);
        entityManager.persistAndFlush(userGroup2);

        // When
        boolean found = userGroupRepository.existsByDefaultGroupIsTrue();

        // Then
        assertFalse(found);
    }

    @Test
    public void updateUserGroupDefault() {
        // Given
        String route1Name = "REST : Strategic Indicators - Current Evaluation";
        String route1Path = "/CurrentEvaluation";
        Route route1 = new Route(route1Name, route1Path);
        entityManager.persist(route1);

        String userGroup1Name = "group1";
        Set<Route> routes1 = new HashSet<>();
        routes1.add(route1);
        UserGroup userGroup1 = new UserGroup(userGroup1Name, routes1);
        userGroup1.setDefaultGroup(true);
        entityManager.persist(userGroup1);
        System.out.println(userGroup1.getDefaultGroup());

        String route2Name = "REST : Strategic Indicators - Historical Data";
        String route2Path = "/HistoricalData";
        Route route2 = new Route(route2Name, route2Path);
        entityManager.persist(route2);

        String userGroup2Name = "group2";
        Set<Route> routes2 = new HashSet<>();
        routes2.add(route2);
        UserGroup userGroup2 = new UserGroup(userGroup2Name, routes2);
        userGroup2.setDefaultGroup(false);
        entityManager.persistAndFlush(userGroup2);
        System.out.println(userGroup2.getDefaultGroup());

        // When
        userGroupRepository.updateUserGroupDefault(userGroup2.getId());

        // Then
        UserGroup userGroupFound1 = entityManager.find(UserGroup.class, userGroup1.getId());
        entityManager.refresh(userGroupFound1);
        assertFalse(userGroupFound1.getDefaultGroup());

        UserGroup userGroupFound2 = entityManager.find(UserGroup.class, userGroup2.getId());
        entityManager.refresh(userGroupFound2);
        assertTrue(userGroupFound2.getDefaultGroup());
    }
}