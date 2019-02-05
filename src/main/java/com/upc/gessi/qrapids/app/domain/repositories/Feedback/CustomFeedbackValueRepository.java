package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

public interface CustomFeedbackValueRepository extends Serializable {
    void saveFeedv(FeedbackValues feedv);

    List<FeedbackValues> getFeedbackValues(Long id, Date feeDate);
}
