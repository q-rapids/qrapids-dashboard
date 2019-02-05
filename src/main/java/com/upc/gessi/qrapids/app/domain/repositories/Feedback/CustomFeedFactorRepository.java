package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface CustomFeedFactorRepository extends Serializable {
    List<FeedbackFactors> getFeedbackReport(Long id, String prj) throws IOException, Exception;
}
