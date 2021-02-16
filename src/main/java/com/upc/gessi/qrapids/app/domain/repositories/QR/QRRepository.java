package com.upc.gessi.qrapids.app.domain.repositories.QR;

import com.upc.gessi.qrapids.app.domain.models.QualityRequirement;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTODecisionQualityRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface QRRepository extends JpaRepository<QualityRequirement, Long> {

    QualityRequirement findByDecisionId (Long id);

    List<QualityRequirement> findByProjectIdOrderByDecision_DateDesc (Long projectId);

    @Query("SELECT new com.upc.gessi.qrapids.app.presentation.rest.dto.DTODecisionQualityRequirement(d.id, d.type, d.date, a.username, d.rationale, d.patternId, q.requirement, q.description, q.goal, q.backlogId, q.backlogUrl)" +
            "FROM QualityRequirement q RIGHT JOIN q.decision d LEFT JOIN d.author a " +
            "WHERE d.project.id = :projectId AND d.date <= :to AND d.date >= :from " +
            "ORDER BY d.date DESC")
    List<DTODecisionQualityRequirement> getAllDecisionsAndQRsByProject_Id(@Param("projectId") Long projectId, @Param("from") Date from, @Param("to") Date to);

}
