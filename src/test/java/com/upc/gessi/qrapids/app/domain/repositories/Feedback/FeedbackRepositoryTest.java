package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.Feedback;
import com.upc.gessi.qrapids.app.domain.repositories.Decision.DecisionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ComponentScan("com.upc.gessi.qrapids.app.database.repositories")
@DataJpaTest
public class FeedbackRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Test
    public void getFeedbackByStrategicIndicatorId () {
        // Given
        Feedback feedback1 = new Feedback();
        Long strategicIndicator1Id = 1L;
        Date date1 = Date.valueOf("2019-08-01");
        String author1 = "Jack";
        feedback1.setSiId(strategicIndicator1Id);
        feedback1.setDate(date1);
        feedback1.setAuthor(author1);
        entityManager.persist(feedback1);

        Feedback feedback2 = new Feedback();
        Long strategicIndicator2Id = 2L;
        Date date2 = Date.valueOf("2019-08-02");
        String author2 = "Nick";
        feedback2.setSiId(strategicIndicator2Id);
        feedback2.setDate(date2);
        feedback2.setAuthor(author2);
        entityManager.persistAndFlush(feedback2);

        // When
        List<Feedback> feedbackFoundList = feedbackRepository.getFeedback(strategicIndicator1Id);

        // Then
        int expectedNumberFeedback = 1;
        assertEquals(expectedNumberFeedback, feedbackFoundList.size());
        assertEquals(feedback1, feedbackFoundList.get(0));
    }
}