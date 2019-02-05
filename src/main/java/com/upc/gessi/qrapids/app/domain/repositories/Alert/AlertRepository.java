package com.upc.gessi.qrapids.app.domain.repositories.Alert;

import com.upc.gessi.qrapids.app.domain.models.Alert;
import com.upc.gessi.qrapids.app.domain.models.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, PagingAndSortingRepository<Alert,Long>, CustomAlertRepository{

    List<Alert> findAllByOrderByDateDesc();

    Alert findAlertById(Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Alert a set a.status = 1 where a.status = 0 and a.id in ?1")
    int setViewedStatusFor(List<Long> alertIds);

    long countByStatus(AlertStatus status);

    long countByReqAssociatIsTrueAndStatusEquals(AlertStatus status);
}
