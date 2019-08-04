package com.upc.gessi.qrapids.app.domain.repositories.AppUser;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.repositories.Alert.AlertRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ComponentScan("com.upc.gessi.qrapids.app.database.repositories")
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsername() {
        // Given
        String userName = "test";
        String email = "test@mail.com";
        String password = "pwd";
        AppUser appUser = new AppUser(userName, email, false, password);
        entityManager.persistAndFlush(appUser);

        // When
        AppUser appUserFound = userRepository.findByUsername(userName);

        // Then
        assertEquals(appUser, appUserFound);
    }

    @Test
    public void findByEmail() {
        // Given
        String userName = "test";
        String email = "test@mail.com";
        String password = "pwd";
        AppUser appUser = new AppUser(userName, email, false, password);
        entityManager.persistAndFlush(appUser);

        // When
        AppUser appUserFound = userRepository.findUserByEmail(email);

        // Then
        assertEquals(appUser, appUserFound);
    }
}