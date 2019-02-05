package com.upc.gessi.qrapids.app.domain.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.FeedbackValues;
import com.upc.gessi.qrapids.app.domain.models.FeedbackFactors;
import com.upc.gessi.qrapids.app.database.repositories.Feedback.FeedFactorRepositoryImpl;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Feedback.FeedbackValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;


@RestController
public class Feedback {

    @Autowired
    private FeedbackRepository fRep;

    @Autowired
    private FeedbackValueRepository fvRep;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private FeedFactorRepositoryImpl ffRep;

    @RequestMapping(value="/api/feedback", method= RequestMethod.POST)
    public void newSI(HttpServletRequest request, HttpServletResponse response) throws UnknownHostException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            java.util.Date dateAux = new java.util.Date();
            Date date = new Date(dateAux.getTime());
            String author = "-1"; //TODO: Remove when user authentiacation enabled
            float value = Float.parseFloat(request.getParameter("newvalue"));
            float oldvalue = Float.parseFloat(request.getParameter("oldvalue"));
            Type stringListType = new TypeToken<List<String>>() {}.getType();
            List<String> factorIds = new Gson().fromJson(request.getParameter("factorIds"), stringListType);
            List<String> factorNames = new Gson().fromJson(request.getParameter("factorNames"), stringListType);
            Type floatListType = new TypeToken<List<Float>>() {}.getType();
            List<Float> factorValues = new Gson().fromJson(request.getParameter("factorValues"), floatListType);
            List<String> factorEvaluationDates = new Gson().fromJson(request.getParameter("factorEvaluationDates"), stringListType);

            if (!id.equals("")) {
                com.upc.gessi.qrapids.app.domain.models.Feedback feed = new com.upc.gessi.qrapids.app.domain.models.Feedback(id, date, author, value, oldvalue);
                fRep.save(feed);
                for (int i = 0; i < factorIds.size(); i++) {
                    java.util.Date evaluationDateAux = new SimpleDateFormat("yyyy-MM-dd").parse(factorEvaluationDates.get(i));
                    Date evaluationDate = new Date(evaluationDateAux.getTime());
                    FeedbackValues feedbackValue = new FeedbackValues(factorIds.get(i), factorNames.get(i), factorValues.get(i), evaluationDate, id, date);
                    fvRep.save(feedbackValue);
                }
            }
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
            System.err.println(e);
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping("/api/Feedback/{id}")
    public List<com.upc.gessi.qrapids.app.domain.models.Feedback> getFeedback(@PathVariable Long id) throws UnknownHostException {
            return fRep.getFeedback(id);
    }



    @RequestMapping("/api/FeedbackReport/{id}")
    public List<FeedbackFactors> getFeedbackReport(@RequestParam(value = "prj", required=false) String prj, @PathVariable Long id) throws Exception {
        return ffRep.getFeedbackReport(id, prj);
    }

}
