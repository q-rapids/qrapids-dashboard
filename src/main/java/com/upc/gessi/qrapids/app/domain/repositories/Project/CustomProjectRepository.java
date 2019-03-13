package com.upc.gessi.qrapids.app.domain.repositories.Project;

import java.io.Serializable;
import com.upc.gessi.qrapids.app.domain.models.Project;


public interface CustomProjectRepository extends Serializable {
	Project findByExternalId(String externalId);
	Project findByName(String name);
}
