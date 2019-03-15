package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import evaluation.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class QMAProjects {
    @Autowired
    private QMAConnection qmacon;

    public List<String> getAssessedProjects() throws IOException, CategoriesException {
        List<String> projects;

        // Data coming from QMA API
        qmacon.initConnexion();
        projects = Project.getProjects();
        return projects;

    }

}
