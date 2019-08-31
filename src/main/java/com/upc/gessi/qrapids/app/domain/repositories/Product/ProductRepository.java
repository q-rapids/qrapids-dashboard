package com.upc.gessi.qrapids.app.domain.repositories.Product;

import com.upc.gessi.qrapids.app.domain.models.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

    Product findByName (String name);

}
