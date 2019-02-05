package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, PagingAndSortingRepository<Feedback, Long>, CustomFeedbackRepository{
}
