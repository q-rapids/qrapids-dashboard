package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class FeedbackValueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FeedbackValueRepository feedbackValueRepository;

    @Test
    public void getFeedbackValues() {
        String factor1Id = "blockingcode";
        String factor1Name = "Blocking Code";
        float factor1Value = 0.8f;
        Date factor1EvaluationDate = Date.valueOf("2019-08-01");
        Long strategicIndicator1Id = 1L;
        Date feedback1Date = Date.valueOf("2019-08-02");
        FeedbackValues feedbackValues1 = new FeedbackValues(factor1Id, factor1Name, factor1Value, factor1EvaluationDate, strategicIndicator1Id, feedback1Date);
        entityManager.persist(feedbackValues1);

        String factor2Id = "testingperformance";
        String factor2Name = "Testing Performance";
        float factor2Value = 0.7f;
        Date factor2EvaluationDate = Date.valueOf("2019-08-02");
        Long strategicIndicator2Id = 2L;
        Date feedback2Date = Date.valueOf("2019-08-02");
        FeedbackValues feedbackValues2 = new FeedbackValues(factor2Id, factor2Name, factor2Value, factor2EvaluationDate, strategicIndicator2Id, feedback2Date);
        entityManager.persistAndFlush(feedbackValues2);

        List<FeedbackValues> feedbackValuesList = feedbackValueRepository.findAllBySiIdAndFeedbackDate(strategicIndicator1Id, feedback1Date);

        int expectedNumberFeedbackValues = 1;
        assertEquals(expectedNumberFeedbackValues,feedbackValuesList.size());
        assertEquals(feedbackValues1, feedbackValuesList.get(0));
    }
}