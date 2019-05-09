package com.upc.gessi.qrapids.app.domain.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.models.Product;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Product.ProductRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.dto.DTOProduct;
import com.upc.gessi.qrapids.app.dto.DTOProject;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;


@Service
public class ProductsController {

	@Autowired
    private ProjectRepository projectRep;
	@Autowired
    private ProductRepository productRep;
    @Autowired
    private QMAStrategicIndicators qmasi;

		
	public List<DTOProject> getProjects() throws Exception {
		List<DTOProject> projects = new Vector<DTOProject>();
		List<Project> projectsBD = projectRep.findAll();
		for (Project p : projectsBD) {
			DTOProject project = new DTOProject(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive(), p.getBacklogId());
			projects.add(project);
		}
		Collections.sort(projects, new Comparator<DTOProject>() {
	        @Override
	        public int compare(DTOProject o1, DTOProject o2) {
	            return o1.getName().compareTo(o2.getName());
	        }
	    });
        return projects;
    }
	
	public List<DTOProduct> getProducts() throws Exception {
		List<Product> productsBD = productRep.findAll();
		List<DTOProduct> products = new Vector<DTOProduct>();
		for (Product p : productsBD) {
			List<DTOProject> relatedProjects = new Vector<DTOProject>();
			for (Project proj : p.getProjects()) {
				DTOProject project = new DTOProject(proj.getId(), proj.getExternalId(), proj.getName(), proj.getDescription(), proj.getLogo(), proj.getActive(), proj.getBacklogId());
				relatedProjects.add(project);
			}
			Collections.sort(relatedProjects, new Comparator<DTOProject>() {
		        @Override
		        public int compare(DTOProject o1, DTOProject o2) {
		            return o1.getName().compareTo(o2.getName());
		        }
		    });
			DTOProduct product = new DTOProduct(p.getId(), p.getName(), p.getDescription(), p.getLogo(), relatedProjects);
			products.add(product);
		}
		Collections.sort(products, new Comparator<DTOProduct>() {
	        @Override
	        public int compare(DTOProduct o1, DTOProduct o2) {
	            return o1.getName().compareTo(o2.getName());
	        }
	    });
        return products;
    }
	
	public DTOProduct getProductById(String id) throws Exception {
		Product p = productRep.findOne(Long.parseLong(id));
		List<DTOProject> relatedProjects = new Vector<DTOProject>();
		for (Project proj : p.getProjects()) {
			DTOProject project = new DTOProject(proj.getId(), proj.getExternalId(), proj.getName(), proj.getDescription(), proj.getLogo(), proj.getActive(), proj.getBacklogId());
			relatedProjects.add(project);
		}
		Collections.sort(relatedProjects, new Comparator<DTOProject>() {
	        @Override
	        public int compare(DTOProject o1, DTOProject o2) {
	            return o1.getName().compareTo(o2.getName());
	        }
	    });
		DTOProduct product = new DTOProduct(p.getId(), p.getName(), p.getDescription(), p.getLogo(), relatedProjects);
        return product;
    }
	
	public DTOProject getProjectById(String id) throws Exception {
		Project p = projectRep.findOne(Long.parseLong(id));
		DTOProject project = new DTOProject(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive(), p.getBacklogId());
        return project;
    }
	
	public DTOProject getProjectByExternalId(String externalId) throws Exception {
		Project p = projectRep.findByExternalId(externalId);
		DTOProject project = new DTOProject(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive(), p.getBacklogId());
        return project;
    }
	
	public boolean checkProjectByName(Long id, String name) throws Exception {
		Project p = projectRep.findByName(name);
        return (p == null || p.getId() == id);
    }
	
	public void updateProject(DTOProject p) {
		Project project = new Project(p.getExternalId(), p.getName(), p.getDescription(), p.getLogo(), p.getActive());
		project.setId(p.getId());
		project.setBacklogId(p.getBacklogId());
		projectRep.save(project);
	}
	
	public boolean checkProductByName(Long id, String name) throws Exception {
		Product p = productRep.findByName(name);
        return (p == null || p.getId() == id);
    }
	
	public boolean checkNewProductByName(String name) throws Exception {
		Product p = productRep.findByName(name);
        return (p == null);
    }
	
	public void updateProduct(Long id, String name, String description, byte[] logo, List<String> projectIds) {
		List<Project> projects = new Vector<Project>();
		for (int i=0; i<projectIds.size(); i++) {
			Project p = projectRep.findOne(Long.parseLong(projectIds.get(i)));
			projects.add(p);
		}
		Product product = new Product(name, description, logo, projects);
		product.setId(id);
		productRep.save(product);
	}
	
	public void newProduct(String name, String description, byte[] logo, List<String> projectIds) {
		List<Project> projects = new Vector<Project>();
		for (int i=0; i<projectIds.size(); i++) {
			Project p = projectRep.findOne(Long.parseLong(projectIds.get(i)));
			projects.add(p);
		}
		Product product = new Product(name, description, logo, projects);
		productRep.save(product);
	}
	
	public void deleteProduct(Long id) {
		productRep.delete(id);
	}
	
	public List<DTOStrategicIndicatorEvaluation> getProductEvaluation(Long id) throws IOException, CategoriesException {
		Product p = productRep.findOne(id);
		List<List<DTOStrategicIndicatorEvaluation>> evaluations = new Vector<List<DTOStrategicIndicatorEvaluation>>();
		for (int i=0; i<p.getProjects().size(); i++) {
			/*if (qmafake.usingFakeData()) {
	            return kpirep.CurrentEvaluation();
	        } else {
	            
	        }*/
			evaluations.add(qmasi.CurrentEvaluation(p.getProjects().get(i).getExternalId()));
		}
		List<DTOStrategicIndicatorEvaluation> average = evaluations.get(0);
		for (int i=1; i<evaluations.size(); i++) {
			for (int j=0; j<evaluations.get(i).size(); j++) {
				boolean found = false;
				for (int k=0; k<average.size(); k++) {
					if (average.get(k).getId().equals(evaluations.get(i).get(j).getId())) {
						Float value1 = average.get(k).getValue().getFirst();
						Float value2 = evaluations.get(i).get(j).getValue().getFirst();
						average.get(k).setValue(Pair.of(value1 + value2, ""));
						found = true;
					}
				}
				if (!found) {
					average.add(evaluations.get(i).get(j));
				}
			}
		}
		for (int i=0; i<average.size(); i++) {
			Pair<Float, String> pair = average.get(i).getValue();
			average.get(i).setValue(Pair.of(pair.getFirst()/evaluations.size(), ""));
		}
		return average;
	}
	
	public List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> getDetailedProductEvaluation(Long id) throws IOException, CategoriesException {
		Product p = productRep.findOne(id);
		List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> evaluations = new Vector<Pair<String, List<DTOStrategicIndicatorEvaluation>>>();
		
		for (int i=0; i<p.getProjects().size(); i++) {
			evaluations.add(Pair.of(p.getProjects().get(i).getName(), qmasi.CurrentEvaluation(p.getProjects().get(i).getExternalId())));
		}
		return evaluations;
	}
}
