package com.upc.gessi.qrapids.app.database.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.Feedback;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.CustomFeedbackRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class FeedbackRepositoryImpl implements CustomFeedbackRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Feedback> getFeedback(Long id) {

        TypedQuery<Feedback> feedquery = this.entityManager.createQuery("SELECT f FROM Feedback f WHERE f.siId = :id", Feedback.class)
            .setParameter("id", id);

        List<Feedback> feeds = feedquery.getResultList();

        return feeds;
    }
}
