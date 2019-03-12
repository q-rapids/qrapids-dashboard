package com.upc.gessi.qrapids.app.database.repositories.Project;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.repositories.Project.CustomProjectRepository;


public class ProjectRepositoryImpl implements CustomProjectRepository {
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
    public Project findByExternalId(String externalId) {
        Project result = null;

        List<Project> results = this.entityManager.createQuery("FROM Project AS u WHERE u.externalId = :externalId", Project.class)
                .setParameter("externalId", externalId)
                .getResultList();

        if( results.size() > 0 ) {
            result = results.get(0);
        }

        return result;
    }
	
	public Project findByName(String name) {
        Project result = null;

        List<Project> results = this.entityManager.createQuery("FROM Project AS u WHERE u.name = :name", Project.class)
                .setParameter("name", name)
                .getResultList();

        if( results.size() > 0 ) {
            result = results.get(0);
        }

        return result;
    }
}
