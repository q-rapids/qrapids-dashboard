package com.upc.gessi.qrapids.app.domain.repositories.QFCategory;

import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface QFCategoryRepository extends CrudRepository<QFCategory, Long> {
    List<QFCategory> findAllByOrderByUpperThresholdAsc();
}
