package com.upc.gessi.qrapids.app.domain.controllers;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Product;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Product.ProductRepository;
import com.upc.gessi.qrapids.app.domain.repositories.Project.ProjectRepository;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProduct;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOStrategicIndicatorEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


@Service
public class ProductsController {

	@Autowired
    private ProjectRepository projectRep;
	@Autowired
    private ProductRepository productRep;
    @Autowired
    private QMAStrategicIndicators qmasi;
	
	public List<DTOProduct> getProducts() throws Exception {
		Iterable<Product> productIterable = productRep.findAll();
		List<Product> productsBD = new ArrayList<>();
		productIterable.forEach(productsBD::add);
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
		Optional<Product> productOptional = productRep.findById(Long.parseLong(id));
		if (productOptional.isPresent()) {
			Product product = productOptional.get();
			List<DTOProject> relatedProjects = new Vector<DTOProject>();
			for (Project proj : product.getProjects()) {
				DTOProject project = new DTOProject(proj.getId(), proj.getExternalId(), proj.getName(), proj.getDescription(), proj.getLogo(), proj.getActive(), proj.getBacklogId());
				relatedProjects.add(project);
			}
			Collections.sort(relatedProjects, new Comparator<DTOProject>() {
				@Override
				public int compare(DTOProject o1, DTOProject o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return new DTOProduct(product.getId(), product.getName(), product.getDescription(), product.getLogo(), relatedProjects);
		}
		return null;
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
			Optional<Project> projectOptional = projectRep.findById(Long.parseLong(projectIds.get(i)));
			projectOptional.ifPresent(projects::add);
		}
		Product product = new Product(name, description, logo, projects);
		product.setId(id);
		productRep.save(product);
	}
	
	public void newProduct(String name, String description, byte[] logo, List<String> projectIds) {
		List<Project> projects = new Vector<Project>();
		for (int i=0; i<projectIds.size(); i++) {
			Optional<Project> projectOptional = projectRep.findById(Long.parseLong(projectIds.get(i)));
			projectOptional.ifPresent(projects::add);
		}
		Product product = new Product(name, description, logo, projects);
		productRep.save(product);
	}
	
	public void deleteProduct(Long id) {
		productRep.deleteById(id);
	}
	
	public List<DTOStrategicIndicatorEvaluation> getProductEvaluation(Long id) throws IOException, CategoriesException, ProjectNotFoundException {
		List<DTOStrategicIndicatorEvaluation> average = new ArrayList<>();
		Optional<Product> productOptional = productRep.findById(id);
		if (productOptional.isPresent()) {
			Product product = productOptional.get();
			List<List<DTOStrategicIndicatorEvaluation>> evaluations = new Vector<List<DTOStrategicIndicatorEvaluation>>();
			for (int i = 0; i < product.getProjects().size(); i++) {
				// in case of product we need to see all strategic indicators independent of profile
				evaluations.add(qmasi.CurrentEvaluation(product.getProjects().get(i).getExternalId(),null));
			}
			average = evaluations.get(0);
			buildAverageEvaluations(average, evaluations);
			for (int i = 0; i < average.size(); i++) {
				Pair<Float, String> pair = average.get(i).getValue();
				average.get(i).setValue(Pair.of(pair.getFirst() / evaluations.size(), ""));
			}
		}
		return average;
	}

	private void buildAverageEvaluations(List<DTOStrategicIndicatorEvaluation> average, List<List<DTOStrategicIndicatorEvaluation>> evaluations) {
		for (int i = 1; i < evaluations.size(); i++) {
			for (int j = 0; j < evaluations.get(i).size(); j++) {
				boolean found = isFound(average, evaluations.get(i).get(j));
				if (!found) {
					average.add(evaluations.get(i).get(j));
				}
			}
		}
	}

	private boolean isFound(List<DTOStrategicIndicatorEvaluation> average, DTOStrategicIndicatorEvaluation evaluation) {
		for (DTOStrategicIndicatorEvaluation dtoStrategicIndicatorEvaluation : average) {
			if (dtoStrategicIndicatorEvaluation.getId().equals(evaluation.getId())) {
				Float value1 = dtoStrategicIndicatorEvaluation.getValue().getFirst();
				Float value2 = evaluation.getValue().getFirst();
				dtoStrategicIndicatorEvaluation.setValue(Pair.of(value1 + value2, ""));
				return true;
			}
		}
		return false;
	}

	public List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> getDetailedProductEvaluation(Long id) throws IOException, CategoriesException, ProjectNotFoundException {
		Optional<Product> productOptional = productRep.findById(id);
		List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> evaluations = new Vector<Pair<String, List<DTOStrategicIndicatorEvaluation>>>();

		if (productOptional.isPresent()) {
			Product product = productOptional.get();
			for (int i = 0; i < product.getProjects().size(); i++) {
				// in case of product we need to see all strategic indicators independent of profile
				evaluations.add(Pair.of(product.getProjects().get(i).getName(), qmasi.CurrentEvaluation(product.getProjects().get(i).getExternalId(), null)));
			}
		}
		return evaluations;
	}
}
