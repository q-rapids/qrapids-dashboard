package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class Projects {
    @Autowired
    private QMAProjects qmaPrj;


    @RequestMapping(value = "/api/assessedProjects", method = RequestMethod.GET)
    public @ResponseBody
    List<String> getPrj(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<String> projects= qmaPrj.getAssessedProjects();
            System.err.println(projects.toString());
            return projects;
        } catch (CategoriesException e) {
            System.err.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }
}
