package com.upc.gessi.qrapids.app.domain.repositories.AppUser;

import com.upc.gessi.qrapids.app.domain.models.AppUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
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
        AppUser appUserFound = userRepository.findByEmail(email);

        // Then
        assertEquals(appUser, appUserFound);
    }
}