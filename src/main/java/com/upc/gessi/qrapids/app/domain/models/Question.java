package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "question")
public class Question implements Serializable{

    // SerialVersion UID
    private static final long serialVersionUID = 18L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="question", unique = true)
    private String question;

    public Question() { } // !important

    public Question(String question) {
        this.question = question;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                '}';
    }
}
