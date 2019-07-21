package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class Projects {

    @Autowired
    private QMAProjects qmaPrj;

    @Autowired
	private ProjectRepository projectRep;

    @GetMapping("/api/projects/import")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getPrj() {
        List<String> projectsES;
    	try {
            projectsES = qmaPrj.getAssessedProjects();
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on ElasticSearch connection");
        }

        //Update DB
		for (int i=0; i < projectsES.size(); ++i) {
			Project p = projectRep.findByExternalId(projectsES.get(i));
			if (p == null) {
                byte[] bytes = null;
				p = new Project(projectsES.get(i), projectsES.get(i), "No description specified", bytes, true);
				projectRep.save(p);
			}
		}
		
		return projectsES;
    }
}
