package com.upc.gessi.qrapids.app.domain.repositories.SICategory;

import com.upc.gessi.qrapids.app.domain.models.SICategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SICategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SICategoryRepository siCategoryRepository;

    @Test
    public void findByName() {
        // Given
        String strategicIndicatorGoodCategoryName = "Good";
        String strategicIndicatorGoodCategoryColor = "#00ff00";
        SICategory siGoodCategory = new SICategory(strategicIndicatorGoodCategoryName, strategicIndicatorGoodCategoryColor);
        entityManager.persist(siGoodCategory);

        String strategicIndicatorNeutralCategoryName = "Neutral";
        String strategicIndicatorNeutralCategoryColor = "#ff8000";
        SICategory siNeutralCategory = new SICategory(strategicIndicatorNeutralCategoryName, strategicIndicatorNeutralCategoryColor);
        entityManager.persist(siNeutralCategory);

        String strategicIndicatorBadCategoryName = "Bad";
        String strategicIndicatorBadCategoryColor = "#ff0000";
        SICategory siBadCategory = new SICategory(strategicIndicatorBadCategoryName, strategicIndicatorBadCategoryColor);
        entityManager.persistAndFlush(siBadCategory);

        // When
        SICategory siCategoryFound = siCategoryRepository.findByName(strategicIndicatorGoodCategoryName);

        // Then
        assertEquals(siGoodCategory, siCategoryFound);
    }
}