package com.upc.gessi.qrapids.app.domain.repositories.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.upc.gessi.qrapids.app.domain.models.Product;
import com.upc.gessi.qrapids.app.domain.repositories.Product.CustomProductRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, PagingAndSortingRepository<Product,Long>, CustomProductRepository{

}
