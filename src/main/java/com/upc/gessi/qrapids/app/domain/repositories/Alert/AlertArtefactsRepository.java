package com.upc.gessi.qrapids.app.domain.repositories.Alert;

import com.upc.gessi.qrapids.app.domain.models.AlertArtefacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AlertArtefactsRepository extends JpaRepository<AlertArtefacts, Long>, PagingAndSortingRepository<AlertArtefacts,Long>, CustomAlertArtefactsRepository{
}
