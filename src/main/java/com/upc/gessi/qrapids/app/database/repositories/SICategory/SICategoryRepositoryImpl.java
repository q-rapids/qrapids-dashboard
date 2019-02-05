package com.upc.gessi.qrapids.app.database.repositories.SICategory;

import com.upc.gessi.qrapids.app.domain.repositories.SICategory.CustomSICategoryRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class SICategoryRepositoryImpl implements CustomSICategoryRepository {

	@PersistenceContext
	private EntityManager entityManager;

}
