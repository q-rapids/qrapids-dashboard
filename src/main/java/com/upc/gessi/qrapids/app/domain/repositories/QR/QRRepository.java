package com.upc.gessi.qrapids.app.domain.repositories.QR;

import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface QRRepository extends JpaRepository<QualityRequirement, Long>, PagingAndSortingRepository<QualityRequirement,Long>, CustomQRRepository {

    QualityRequirement findByDecisionId (Long id);

    @Query("SELECT new com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement(d.id, d.type, d.date, a.username, d.rationale, d.patternId, q.requirement, q.description, q.goal, q.backlogId, q.backlogUrl) FROM QualityRequirement q RIGHT JOIN q.decision d LEFT JOIN d.author a ORDER BY d.date DESC")
    List<DTODecisionQualityRequirement> getAllDecisionsAndQRs();

}
