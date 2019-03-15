package com.upc.gessi.qrapids.app.database.repositories.Product;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.upc.gessi.qrapids.app.domain.models.Product;
import com.upc.gessi.qrapids.app.domain.repositories.Product.CustomProductRepository;

public class ProductRepositoryImpl implements CustomProductRepository {
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
    public Product findByName(String name) {
        Product result = null;

        List<Product> results = this.entityManager.createQuery("FROM Product AS u WHERE u.name = :name", Product.class)
                .setParameter("name", name)
                .getResultList();

        if( results.size() > 0 ) {
            result = results.get(0);
        }

        return result;
    }
}