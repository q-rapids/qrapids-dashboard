package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProductsController;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProduct;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ElementAlreadyPresentException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@RestController
public class Products {
	
	@Autowired
    private ProductsController productCont;
	
	@GetMapping("/api/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProject> getProjects() {
	    try {
            return productCont.getProjects();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@GetMapping("/api/products")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProduct> getProducts() {
	    try {
            return productCont.getProducts();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@GetMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.OK)
	public DTOProduct getProductById(@PathVariable String id) {
        try {
            return productCont.getProductById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@GetMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.OK)
	public DTOProject getProjectById(@PathVariable String id) throws Exception {
        try {
            return productCont.getProjectById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@PutMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProject(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
        	String externalId = request.getParameter("externalId");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String backlogId = request.getParameter("backlogId");
            byte[] logoBytes = null;
            if (logo != null) {
                logoBytes = IOUtils.toByteArray(logo.getInputStream());
            }
            if (logoBytes != null && logoBytes.length < 10) {
            	DTOProject p = productCont.getProjectById(Long.toString(id));
            	logoBytes = p.getLogo();
            }
            if (productCont.checkProjectByName(id, name)) {
            	DTOProject p = new DTOProject(id, externalId, name, description, logoBytes, true, backlogId);
            	productCont.updateProject(p);
            } else {
                throw new ElementAlreadyPresentException();
        	}
        } catch (ElementAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@PutMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProduct(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] logoBytes = null;
            if (logo != null) {
                logoBytes = IOUtils.toByteArray(logo.getInputStream());
            }
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (logoBytes != null && logoBytes.length < 10) {
            	DTOProduct p = productCont.getProductById(Long.toString(id));
            	logoBytes = p.getLogo();
            }
            if (productCont.checkProductByName(id, name)) {
            	productCont.updateProduct(id, name, description, logoBytes, projectIds);
            } else {
                throw new ElementAlreadyPresentException();
        	}
        } catch (ElementAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    public void newProduct(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] logoBytes = null;
            if (logo != null) {
                logoBytes = IOUtils.toByteArray(logo.getInputStream());
            }
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (logoBytes != null && logoBytes.length < 10) {
                //URL projectImageUrl = QrapidsApplication.class.getClassLoader().getResource("static" + File.separator + "icons" + File.separator + "projectDefault.jpg");
                //File f = new File(projectImageUrl.getPath());
				//BufferedImage img = ImageIO.read(f);
				//ByteArrayOutputStream baos = new ByteArrayOutputStream();
				//ImageIO.write(img, "jpg", baos);
				//logo = baos.toByteArray();
                logo = null;
            }
            if (productCont.checkNewProductByName(name)) {
            	productCont.newProduct(name, description, logoBytes, projectIds);
            } else {
                throw new ElementAlreadyPresentException();
            }
        } catch (ElementAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@DeleteMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProduct(@PathVariable Long id) {
	    productCont.deleteProduct(id);
    }
	
	@GetMapping("/api/products/{id}/current")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorEvaluation> getProductEvaluation(@PathVariable String id) {
		try {
			return productCont.getProductEvaluation(Long.parseLong(id));
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
	
	@GetMapping("/api/products/{id}/projects/current")
    @ResponseStatus(HttpStatus.OK)
    public List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> getDetailedCurrentEvaluation(@PathVariable String id) {
		try {
			return productCont.getDetailedProductEvaluation(Long.parseLong(id));
        } catch (CategoriesException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The categories do not match");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
}
