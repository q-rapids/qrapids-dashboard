package com.upc.gessi.qrapids.app.presentation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.upc.gessi.qrapids.app.domain.controllers.ProductsController;
import com.upc.gessi.qrapids.app.dto.DTOProduct;
import com.upc.gessi.qrapids.app.dto.DTOProject;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;


@Controller("/Products")
public class ProductController {
	
	@Autowired
    private ProductsController productCont;
	
	@RequestMapping("/Products")
    public String Products(){
        return "Product/Products";
    }
	
	@RequestMapping("/api/projects")
    public @ResponseBody List<DTOProject> getProjects() throws Exception {
        return productCont.getProjects();
    }
	
	@RequestMapping("/api/products")
    public @ResponseBody List<DTOProduct> getProducts() throws Exception {
        return productCont.getProducts();
    }
	
	@RequestMapping("/api/products/{id}")
	public @ResponseBody DTOProduct getProductById(@PathVariable String id) throws Exception {
        return productCont.getProductById(id);
    }
	
	@RequestMapping("/api/products/project/{id}")
    /*public @ResponseBody DTOProject getProjectByExternalId(@PathVariable String externalId) throws Exception {
        return productCont.getProjectByExternalId(externalId);
    }*/
	public @ResponseBody DTOProject getProjectById(@PathVariable String id) throws Exception {
        return productCont.getProjectById(id);
    }
	
	@RequestMapping(value = "/api/updateProject", method = RequestMethod.POST)
    public @ResponseBody void updateProject(HttpServletRequest request, HttpServletResponse response) {
        try {
        	Long id = Long.parseLong(request.getParameter("id"));
        	String externalId = request.getParameter("externalId");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] logo = IOUtils.toByteArray(request.getPart("logo").getInputStream());
            if (logo.length < 10) {
            	DTOProject p = productCont.getProjectById(Long.toString(id));
            	logo = p.getLogo();
            }
            if (productCont.checkProjectByName(id, name)) {
            	DTOProject p = new DTOProject(id, externalId, name, description, logo, true);
            	productCont.updateProject(p);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
        	}
            //response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
	
	@RequestMapping(value = "/api/updateProduct", method = RequestMethod.POST)
    public @ResponseBody void updateProduct(HttpServletRequest request, HttpServletResponse response) {
        try {
        	Long id = Long.parseLong(request.getParameter("id"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] logo = IOUtils.toByteArray(request.getPart("logo").getInputStream());
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (logo.length < 10) {
            	DTOProduct p = productCont.getProductById(Long.toString(id));
            	logo = p.getLogo();
            }
            if (productCont.checkProductByName(id, name)) {
            	productCont.updateProduct(id, name, description, logo, projectIds);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
        	}
            //response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
        	System.err.println(e);
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
	
	@RequestMapping(value = "/api/newProduct", method = RequestMethod.POST)
    public @ResponseBody void newProduct(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            byte[] logo = IOUtils.toByteArray(request.getPart("logo").getInputStream());
            List<String> projectIds = Arrays.asList(request.getParameter("projects").split(","));
            if (logo.length < 10) {
            	File f = new File("src/main/resources/static/icons/projectDefault.jpg");
				BufferedImage img = ImageIO.read(f);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				logo = baos.toByteArray();
            }
            if (productCont.checkNewProductByName(name)) {
            	productCont.newProduct(name, description, logo, projectIds);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
        	}
            //response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
        	System.err.println(e);
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
	
	@RequestMapping(value = "/api/deleteProduct", method = RequestMethod.POST)
    public @ResponseBody void deleteProduct(HttpServletRequest request, HttpServletResponse response) {
        try {
        	Long id = Long.parseLong(request.getParameter("id"));
        	productCont.deleteProduct(id);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Exception e) {
        	System.err.println(e);
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
	
	@RequestMapping("/api/products/currentEvaluation/{id}")
    public @ResponseBody List<DTOStrategicIndicatorEvaluation> getProductEvaluation(@PathVariable String id) {
		List<DTOStrategicIndicatorEvaluation> evaluations = new Vector<DTOStrategicIndicatorEvaluation>();
		try {
			evaluations =  productCont.getProductEvaluation(Long.parseLong(id));
        } catch (Exception e) {
        	System.err.println(e);
        }
		return evaluations;
    }
	
	@RequestMapping("/api/products/detailedCurrentEvaluation/{id}")
    public @ResponseBody List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> getDetailedCurrentEvaluation(@PathVariable String id) {
		List<Pair<String, List<DTOStrategicIndicatorEvaluation>>> evaluations = new Vector<Pair<String, List<DTOStrategicIndicatorEvaluation>>>();
		try {
			evaluations =  productCont.getDetailedProductEvaluation(Long.parseLong(id));
        } catch (Exception e) {
        	System.err.println(e);
        }
		return evaluations;
    }
	
	@RequestMapping("/Products/Evaluation")
    public String ProductEvaluation(){
		return "Product/ProductEvaluation";
    }
	
	@RequestMapping("/Products/DetailedEvaluation")
    public String ProductDetailedEvaluation(){
		return "Product/detailedProductEvaluation";
    }

}
