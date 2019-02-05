package com.upc.gessi.qrapids.app.domain.repositories.QFCategory;

import com.upc.gessi.qrapids.app.domain.models.QFCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface QFCategoryRepository extends JpaRepository<QFCategory, Long>, PagingAndSortingRepository<QFCategory,Long>, CustomQFCategoryRepository {
    List<QFCategory> findAllByOrderByUpperThresholdAsc();
}
