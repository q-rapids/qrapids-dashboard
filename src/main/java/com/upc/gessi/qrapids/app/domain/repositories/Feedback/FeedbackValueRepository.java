package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;
import java.util.List;

public interface FeedbackValueRepository extends CrudRepository<FeedbackValues, Long> {

    List<FeedbackValues> findAllBySiIdAndFeedbackDate (Long id, Date feeDate);

}
