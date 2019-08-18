package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upc.gessi.qrapids.app.domain.controllers.FeedbackController;
import com.upc.gessi.qrapids.app.domain.controllers.UsersController;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.List;
import java.util.Map;


@RestController
public class Feedback {

    @Autowired
    private FeedbackRepository fRep;

    @Autowired
    private FeedbackController feedbackController;

    @Autowired
    private UsersController usersController;

    @PostMapping("/api/strategicIndicators/{id}/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void newFeedbackForStrategicIndicator(@PathVariable Long id, @RequestBody Map<String, String> requestBody, Authentication authentication) {
        try {
            java.util.Date dateAux = new java.util.Date();
            Date date = new Date(dateAux.getTime());
            String author = "-1";
            AppUser user = null;
            if (authentication != null) {
                author = authentication.getName();
                user = usersController.findUserByName(author);
            }
            float value = Float.parseFloat(requestBody.get("newvalue"));
            float oldValue = Float.parseFloat(requestBody.get("oldvalue"));
            Type stringListType = new TypeToken<List<String>>() {}.getType();
            List<String> factorIds = new Gson().fromJson(requestBody.get("factorIds"), stringListType);
            List<String> factorNames = new Gson().fromJson(requestBody.get("factorNames"), stringListType);
            Type floatListType = new TypeToken<List<Float>>() {}.getType();
            List<Float> factorValues = new Gson().fromJson(requestBody.get("factorValues"), floatListType);
            List<String> factorEvaluationDates = new Gson().fromJson(requestBody.get("factorEvaluationDates"), stringListType);

            com.upc.gessi.qrapids.app.domain.models.Feedback feedback = feedbackController.buildFeedback(id, date, author, user, value, oldValue);
            feedbackController.saveFeedbackForStrategicIndicator(feedback, factorIds, factorNames, factorValues, factorEvaluationDates);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more attributes are missing in the request body");
        }
    }

    @GetMapping("/api/strategicIndicator/{id}/feedback")
    @ResponseStatus(HttpStatus.OK)
    public List<com.upc.gessi.qrapids.app.domain.models.Feedback> getFeedback(@PathVariable Long id) {
            return fRep.findAllBySiId(id);
    }

    @RequestMapping("/api/strategicIndicator/{id}/feedbackReport")
    @ResponseStatus(HttpStatus.OK)
    public List<FeedbackFactors> getFeedbackReport(@PathVariable Long id) throws IOException, CategoriesException {
        return feedbackController.getFeedbackReport(id);
    }

}
