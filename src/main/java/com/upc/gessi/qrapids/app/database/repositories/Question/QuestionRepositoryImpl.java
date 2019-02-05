package com.upc.gessi.qrapids.app.database.repositories.Question;

import com.upc.gessi.qrapids.app.domain.models.Question;
import com.upc.gessi.qrapids.app.domain.repositories.Question.CustomQuestionRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class QuestionRepositoryImpl implements CustomQuestionRepository {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Question findByQuestion(String question) {
        Question result = null;

        List<Question> routes = this.entityManager.createQuery("FROM Question AS u WHERE u.question = :question", Question.class)
                .setParameter("question", question)
                .getResultList();

        if( routes.size() > 0 ) {
            result = routes.get(0);
        }

        return result;
    }
}
