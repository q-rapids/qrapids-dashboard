package com.upc.gessi.qrapids.app.domain.repositories.Feedback;

import com.upc.gessi.qrapids.app.domain.models.Feedback;

import java.io.Serializable;
import java.util.List;

public interface CustomFeedbackRepository extends Serializable {
    List<Feedback> getFeedback(Long id);
}