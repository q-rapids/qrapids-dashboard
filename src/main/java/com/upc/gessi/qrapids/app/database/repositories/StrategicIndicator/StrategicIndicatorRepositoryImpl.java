package com.upc.gessi.qrapids.app.database.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.CustomStrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class StrategicIndicatorRepositoryImpl implements CustomStrategicIndicatorRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Strategic_Indicator findByName (String name) {
		Strategic_Indicator result = null;

		result = this.entityManager.createQuery("FROM Strategic_Indicator AS si WHERE si.name = :name", Strategic_Indicator.class)
				.setParameter("name", name)
				.getSingleResult();

		return result;

	}

	@Override
	public Strategic_Indicator findById(Long id) {
		Strategic_Indicator result = null;

		result = this.entityManager.createQuery("FROM Strategic_Indicator AS si WHERE si.id = :id", Strategic_Indicator.class)
				.setParameter("id", id)
				.getSingleResult();

		return result;
	}

}
