package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FeedbackValueRepository extends JpaRepository<FeedbackValues, Long>, PagingAndSortingRepository<FeedbackValues, Long>, CustomFeedbackValueRepository {
}
