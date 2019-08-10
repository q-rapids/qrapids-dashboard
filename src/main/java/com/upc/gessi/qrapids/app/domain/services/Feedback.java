package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.AppUser;
import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import com.upc.gessi.qrapids.app.domain.controllers.FeedFactorController;
import com.upc.gessi.qrapids.app.domain.repositories.AppUser.UserRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


@RestController
public class Feedback {

    @Autowired
    private FeedbackRepository fRep;

    @Autowired
    private FeedbackValueRepository fvRep;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private FeedFactorController feedFactorController;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/strategicIndicators/{id}/feedback")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void newSI(@PathVariable Long id, @RequestBody Map<String, String> requestBody, Authentication authentication) throws UnknownHostException {
        try {
            java.util.Date dateAux = new java.util.Date();
            Date date = new Date(dateAux.getTime());
            String author = "-1";
            AppUser user = null;
            if (authentication != null) {
                author = authentication.getName();
                user = userRepository.findByUsername(author);
            }
            float value = Float.parseFloat(requestBody.get("newvalue"));
            float oldvalue = Float.parseFloat(requestBody.get("oldvalue"));
            Type stringListType = new TypeToken<List<String>>() {}.getType();
            List<String> factorIds = new Gson().fromJson(requestBody.get("factorIds"), stringListType);
            List<String> factorNames = new Gson().fromJson(requestBody.get("factorNames"), stringListType);
            Type floatListType = new TypeToken<List<Float>>() {}.getType();
            List<Float> factorValues = new Gson().fromJson(requestBody.get("factorValues"), floatListType);
            List<String> factorEvaluationDates = new Gson().fromJson(requestBody.get("factorEvaluationDates"), stringListType);

            if (!id.equals("")) {
                com.upc.gessi.qrapids.app.domain.models.Feedback feed = new com.upc.gessi.qrapids.app.domain.models.Feedback(id, date, author, user, value, oldvalue);
                fRep.save(feed);
                for (int i = 0; i < factorIds.size(); i++) {
                    java.util.Date evaluationDateAux = new SimpleDateFormat("yyyy-MM-dd").parse(factorEvaluationDates.get(i));
                    Date evaluationDate = new Date(evaluationDateAux.getTime());
                    FeedbackValues feedbackValue = new FeedbackValues(factorIds.get(i), factorNames.get(i), factorValues.get(i), evaluationDate, id, date);
                    fvRep.save(feedbackValue);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
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
        return feedFactorController.getFeedbackReport(id);
    }

}
