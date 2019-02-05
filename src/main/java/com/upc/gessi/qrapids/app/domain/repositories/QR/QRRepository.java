package com.upc.gessi.qrapids.app.domain.repositories.QR;

import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QRRepository extends JpaRepository<QualityRequirement, Long>, PagingAndSortingRepository<QualityRequirement,Long>, CustomQRRepository {

    QualityRequirement findByDecisionId (Long id);

}
