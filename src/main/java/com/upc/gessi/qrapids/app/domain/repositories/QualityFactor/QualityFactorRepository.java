package com.upc.gessi.qrapids.app.domain.repositories.QualityFactor;

import com.upc.gessi.qrapids.app.domain.models.Factor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QualityFactorRepository extends CrudRepository<Factor, Long> {

    List<Factor> findByProject_IdOrderByName (Long projectId);

}
