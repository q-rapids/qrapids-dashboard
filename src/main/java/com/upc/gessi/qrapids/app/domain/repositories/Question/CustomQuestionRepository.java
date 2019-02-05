package com.upc.gessi.qrapids.app.domain.repositories.Question;

import com.upc.gessi.qrapids.app.domain.models.Question;

import java.io.Serializable;

public interface CustomQuestionRepository extends Serializable {
    Question findByQuestion(String question );
}
