package com.upc.gessi.qrapids.app.domain.repositories.QFCategory;

import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class QFCategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QFCategoryRepository qfCategoryRepository;

    @Test
    public void findAllByOrderByUpperThresholdAsc() {
        // Given
        QFCategory qfCategoryBad = new QFCategory("Bad", "#ff0000", 0.33f);
        entityManager.persist(qfCategoryBad);

        QFCategory qfCategoryGood = new QFCategory("Good", "#00ff00", 1f);
        entityManager.persist(qfCategoryGood);

        QFCategory qfCategoryNeutral = new QFCategory("Neutral", "#ff8000", 0.67f);
        entityManager.persistAndFlush(qfCategoryNeutral);

        // When
        List<QFCategory> categoryList = qfCategoryRepository.findAllByOrderByUpperThresholdAsc();

        // Then
        assertEquals(qfCategoryBad, categoryList.get(0));
        assertEquals(qfCategoryNeutral, categoryList.get(1));
        assertEquals(qfCategoryGood, categoryList.get(2));
    }
}