package com.upc.gessi.qrapids.app.database.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.CustomFeedbackValueRepository;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.Date;
import java.util.List;

@Component("feedbackValueRepositoryImpl")
public class FeedbackValueRepositoryImpl implements CustomFeedbackValueRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveFeedv(FeedbackValues feedv) {

    }

    @Override
    public List<FeedbackValues> getFeedbackValues(Long id, Date feeDate) {
        TypedQuery<FeedbackValues> feedquery = this.entityManager.createQuery("SELECT f FROM FeedbackValues f WHERE f.siId = :id AND f.feedbackDate = :date", FeedbackValues.class)
                .setParameter("id", id)
                .setParameter("date", feeDate);

        List<FeedbackValues> feeds = feedquery.getResultList();

        return feeds;
    }
}
