package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAProjects;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Product.ProductRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class Projects {
    @Autowired
    private QMAProjects qmaPrj;
    @Autowired
	private ProjectRepository projectRep;
    @Autowired
	private ProductRepository productRep;
    
    private List<String> projectsES;
    private List<Project> projectsBD;


    @RequestMapping(value = "/api/assessedProjects", method = RequestMethod.GET)
    public @ResponseBody
    List<String> getPrj(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	try {
            projectsES = qmaPrj.getAssessedProjects();
        } catch (CategoriesException e) {
            System.err.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        //Update DB
		for (int i=0; i<projectsES.size(); ++i) {
			Project p = projectRep.findByExternalId(projectsES.get(i));
			if (p == null) {
				File f = new File("src/main/resources/static/icons/projectDefault.jpg");
				BufferedImage img = ImageIO.read(f);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				byte[] bytes = baos.toByteArray();
				p = new Project(projectsES.get(i), projectsES.get(i), "No description specified", bytes, true);
				projectRep.save(p);
			}
		}
		
		//In case one of the projects is removed from the ElasticSearch
		/*
		projectsBD = projectRep.findAll();
		for (int i=0; i<projectsBD.size(); i++) {
			if(!(projectsES.contains(projectsBD.get(i).getExternalId()))) {
				projectsBD.get(i).setActive(false);
				// What happens to the project stored in the DB in case someone creates a new 
				// project with the same name that a previously removed one?
			}
		}
		*/
		
		return projectsES;
    }
}
