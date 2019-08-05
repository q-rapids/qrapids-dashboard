package com.upc.gessi.qrapids.app.domain.repositories.Question;

import com.upc.gessi.qrapids.app.domain.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuestionRepository extends CrudRepository<Question, Long> {

    Question findByQuestion(String question);

}
