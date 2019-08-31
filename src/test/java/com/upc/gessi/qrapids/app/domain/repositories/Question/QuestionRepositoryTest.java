package com.upc.gessi.qrapids.app.domain.repositories.Question;

import com.upc.gessi.qrapids.app.domain.models.Question;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class QuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void findByQuestion() {
        // Given
        String question1Text = "Where are you from?";
        Question question1 = new Question(question1Text);
        entityManager.persist(question1);

        String question2Text = "What was your first job?";
        Question question2 = new Question(question2Text);
        entityManager.persistAndFlush(question2);

        // When
        Question questionFound = questionRepository.findByQuestion(question1Text);

        // Then
        assertEquals(question1, questionFound);
    }
}