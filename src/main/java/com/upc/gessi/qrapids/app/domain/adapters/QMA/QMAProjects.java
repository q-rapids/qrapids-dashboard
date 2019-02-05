package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class QMAProjects {
    @Autowired
    private QMAConnection qmacon;

    public List<String> getAssessedProjects() throws IOException, CategoriesException {
        List<String> projects = new ArrayList();

        // Data coming from QMA API
        qmacon.initConnexion();
        projects = util.Project.getProjects();

        // TO BE REMOVED, only for the temporary version
        //projects.add("default");
        return projects;

    }

}
