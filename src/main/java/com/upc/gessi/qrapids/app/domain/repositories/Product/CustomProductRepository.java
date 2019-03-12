package com.upc.gessi.qrapids.app.domain.repositories.Product;

import java.io.Serializable;
import com.upc.gessi.qrapids.app.domain.models.Product;

public interface CustomProductRepository extends Serializable {
	Product findByName(String name);
}
