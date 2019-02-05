package com.upc.gessi.qrapids.app.database.repositories.QFCategory;

import com.upc.gessi.qrapids.app.domain.repositories.QFCategory.CustomQFCategoryRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class QFCategoryRepositoryImpl implements CustomQFCategoryRepository {

	@PersistenceContext
	private EntityManager entityManager;

}
