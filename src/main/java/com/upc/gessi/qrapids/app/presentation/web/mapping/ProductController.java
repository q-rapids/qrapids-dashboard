package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/Products")
public class ProductController {

    @RequestMapping("/Products/Configuration")
    public String Products(){
        return "Product/Products";
    }

    @RequestMapping("/Products/Evaluation")
    public String ProductEvaluation(){
        return "Product/ProductEvaluation";
    }

    @RequestMapping("/Products/DetailedEvaluation")
    public String ProductDetailedEvaluation(){
        return "Product/DetailedProductEvaluation";
    }
}
