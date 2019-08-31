package com.upc.gessi.qrapids.app.domain.repositories.Question;

import com.upc.gessi.qrapids.app.domain.models.Question;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<Question, Long> {

    Question findByQuestion(String question);

}
