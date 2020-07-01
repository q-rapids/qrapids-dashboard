package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProductsController;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProduct;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOProject;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ElementAlreadyPresentException;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Autowired
    private ProductsController productCont;

	private Logger logger = LoggerFactory.getLogger(Products.class);

	@GetMapping("/api/products")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOProduct> getProducts() {
	    try {
            return productCont.getProducts();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
	
	@GetMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.OK)
	public DTOProduct getProductById(@PathVariable String id) {
        try {
            return productCont.getProductById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
	
	@PutMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProduct(@PathVariable Long id, HttpServletRequest request, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String name = request.getParameter(NAME);
            String description = request.getParameter(DESCRIPTION);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
	
	@PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    public void newProduct(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String name = request.getParameter(NAME);
            String description = request.getParameter(DESCRIPTION);
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
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
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
	
	@GetMapping("/api/products/{id}/projects/current")
    @ResponseStatus(HttpStatus.OK)
    public List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> getDetailedCurrentEvaluation(@PathVariable String id) {
		try {
			return productCont.getDetailedProductEvaluation(Long.parseLong(id));
        } catch (CategoriesException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
}
