package com.upc.gessi.qrapids.app.config.security.seeds;

import com.upc.gessi.qrapids.app.domain.models.Question;
import com.upc.gessi.qrapids.app.domain.repositories.Question.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionsFirstLoad {

    @Autowired
    QuestionRepository questionRepository;

    @PostConstruct
    public void init() throws Exception {

        // Set base dataset container
        List<Question> questions = new ArrayList<>();

        // has elements?
        if ( this.questionRepository.count() != 0 )
            return; // dons seed data

        // Default question security
        questions.add( new Question("What is the name of your high school?") );
        questions.add( new Question("Which phone number do you remember most from your childhood?") );
        questions.add( new Question("What was your favorite place to visit as a child?") );
        questions.add( new Question("Who is your favorite actor, musician, or artist?") );
        questions.add( new Question("What is the name of your favorite pet?") );
        questions.add( new Question("In what city were you born?") );
        questions.add( new Question("What high school did you attend?") );
        questions.add( new Question("What is the name of your first school?") );
        questions.add( new Question("What is your favorite movie?") );
        questions.add( new Question("What is your mother's maiden name?") );
        questions.add( new Question("What street did you grow up on?") );
        questions.add( new Question("When is your anniversary?") );
        questions.add( new Question("What is your favorite color?") );
        questions.add( new Question("What is your father's middle name?") );
        questions.add( new Question("What is the name of your first grade teacher?") );
        questions.add( new Question("What was your high school mascot?") );
        questions.add( new Question("Which is your favorite web browser?") );


        // Validation
        for( Question question : questions ) {

            Question curr = this.questionRepository.findByQuestion( question.getQuestion() );

            if( curr == null )
                this.questionRepository.save( question );
        }

    }
}
