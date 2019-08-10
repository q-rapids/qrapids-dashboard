package com.upc.gessi.qrapids.app.domain.repositories.QR;

import com.upc.gessi.qrapids.app.domain.models.*;
import com.upc.gessi.qrapids.app.dto.DTODecisionQualityRequirement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ComponentScan("com.upc.gessi.qrapids.app.database.repositories")
@DataJpaTest
public class QRRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QRRepository qrRepository;

    @Test
    public void findByDecisionId() {
        // Given
        String projectExternalId = "project1";
        String projectName = "Project 1";
        String projectDescription = "test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        entityManager.persist(project);

        String alertIdElement = "id";
        String alertName = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float alertValue = 0.4f;
        float alertThreshold = 0.5f;
        String alertCategory = "category";
        Date alertDate = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert1 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project);
        entityManager.persist(alert1);

        DecisionType decisionType = DecisionType.IGNORE;
        String decisionRationale = "Not important";
        int patternId = 100;
        Date decisionDate = new Date();
        Decision decision1 = new Decision(decisionType, decisionDate, null, decisionRationale, patternId, project);
        entityManager.persist(decision1);

        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        QualityRequirement qualityRequirement1 = new QualityRequirement(requirement, description, goal, alert1, decision1, project);
        entityManager.persist(qualityRequirement1);

        Alert alert2 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project);
        entityManager.persist(alert2);

        Decision decision2 = new Decision(decisionType, decisionDate, null, decisionRationale, patternId, project);
        entityManager.persist(decision2);

        QualityRequirement qualityRequirement2 = new QualityRequirement(requirement, description, goal, alert2, decision2, project);
        entityManager.persistAndFlush(qualityRequirement2);

        // When
        QualityRequirement qualityRequirementFound = qrRepository.findByDecisionId(decision1.getId());

        // Then
        assertEquals(qualityRequirement1, qualityRequirementFound);
    }

    @Test
    public void findByProjectIdOrderByDecision_DateDesc() throws ParseException {
        // Given
        String project1ExternalId = "project1";
        String project1Name = "Project 1";
        String project1Description = "test project";
        Project project1 = new Project(project1ExternalId, project1Name, project1Description, null, true);
        entityManager.persist(project1);

        String project2ExternalId = "project2";
        String project2Name = "Project 2";
        String project2Description = "test project";
        Project project2 = new Project(project2ExternalId, project2Name, project2Description, null, true);
        entityManager.persist(project2);

        String alertIdElement = "id";
        String alertName = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float alertValue = 0.4f;
        float alertThreshold = 0.5f;
        String alertCategory = "category";
        Date alertDate = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert1 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project1);
        entityManager.persist(alert1);

        Alert alert2 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project1);
        entityManager.persist(alert2);

        Alert alert3 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project2);
        entityManager.persist(alert3);

        DecisionType decisionType = DecisionType.IGNORE;
        String decisionRationale = "Not important";
        int patternId = 100;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date decision1Date = simpleDateFormat.parse("2019-08-01");
        Decision decision1 = new Decision(decisionType, decision1Date, null, decisionRationale, patternId, project1);
        entityManager.persist(decision1);

        Date decision2Date = simpleDateFormat.parse("2019-08-02");
        Decision decision2 = new Decision(decisionType, decision2Date, null, decisionRationale, patternId, project1);
        entityManager.persist(decision2);

        Date decision3Date = simpleDateFormat.parse("2019-08-03");
        Decision decision3 = new Decision(decisionType, decision3Date, null, decisionRationale, patternId, project2);
        entityManager.persist(decision3);

        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        QualityRequirement qualityRequirement1 = new QualityRequirement(requirement, description, goal, alert1, decision1, project1);
        entityManager.persist(qualityRequirement1);

        QualityRequirement qualityRequirement2 = new QualityRequirement(requirement, description, goal, alert2, decision2, project1);
        entityManager.persist(qualityRequirement2);

        QualityRequirement qualityRequirement3 = new QualityRequirement(requirement, description, goal, alert2, decision3, project2);
        entityManager.persistAndFlush(qualityRequirement3);

        // When
        List<QualityRequirement> qualityRequirementsList = qrRepository.findByProjectIdOrderByDecision_DateDesc(project1.getId());

        // Then
        int expectedNumberQualityRequirements = 2;
        assertEquals(expectedNumberQualityRequirements, qualityRequirementsList.size());
        assertEquals(qualityRequirement2, qualityRequirementsList.get(0));
        assertEquals(qualityRequirement1, qualityRequirementsList.get(1));
    }

    @Test
    public void getAllDecisionsAndQRsByProject_Id() throws ParseException {
        // Given
        String project1ExternalId = "project1";
        String project1Name = "Project 1";
        String project1Description = "test project";
        Project project1 = new Project(project1ExternalId, project1Name, project1Description, null, true);
        entityManager.persist(project1);

        String project2ExternalId = "project2";
        String project2Name = "Project 2";
        String project2Description = "test project";
        Project project2 = new Project(project2ExternalId, project2Name, project2Description, null, true);
        entityManager.persist(project2);

        String alertIdElement = "id";
        String alertName = "Duplication";
        AlertType alertType = AlertType.METRIC;
        float alertValue = 0.4f;
        float alertThreshold = 0.5f;
        String alertCategory = "category";
        Date alertDate = new Date();
        AlertStatus alertStatus = AlertStatus.NEW;
        Alert alert1 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project1);
        entityManager.persist(alert1);

        Alert alert2 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project1);
        entityManager.persist(alert2);

        Alert alert3 = new Alert(alertIdElement, alertName, alertType, alertValue, alertThreshold, alertCategory, alertDate, alertStatus, true, project2);
        entityManager.persist(alert3);

        DecisionType decisionType = DecisionType.IGNORE;
        String decisionRationale = "Not important";
        int patternId = 100;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date decision1Date = simpleDateFormat.parse("2019-08-01");
        Decision decision1 = new Decision(decisionType, decision1Date, null, decisionRationale, patternId, project1);
        entityManager.persist(decision1);

        Date decision2Date = simpleDateFormat.parse("2019-08-02");
        Decision decision2 = new Decision(decisionType, decision2Date, null, decisionRationale, patternId, project1);
        entityManager.persist(decision2);

        Date decision3Date = simpleDateFormat.parse("2019-08-03");
        Decision decision3 = new Decision(decisionType, decision3Date, null, decisionRationale, patternId, project2);
        entityManager.persist(decision3);

        String requirement = "The ratio of files without duplications should be at least 0.8";
        String description = "The ratio of files without duplications should be at least the given value";
        String goal = "Improve the quality of the source code";
        QualityRequirement qualityRequirement1 = new QualityRequirement(requirement, description, goal, alert1, decision1, project1);
        entityManager.persist(qualityRequirement1);

        QualityRequirement qualityRequirement2 = new QualityRequirement(requirement, description, goal, alert2, decision2, project1);
        entityManager.persist(qualityRequirement2);

        QualityRequirement qualityRequirement3 = new QualityRequirement(requirement, description, goal, alert2, decision3, project2);
        entityManager.persistAndFlush(qualityRequirement3);

        // When
        List<DTODecisionQualityRequirement> dtoDecisionQualityRequirementList = qrRepository.getAllDecisionsAndQRsByProject_Id(project1.getId(), simpleDateFormat.parse("2019-08-01"), simpleDateFormat.parse("2019-08-03"));

        // Then
        int expectedNumberQualityRequirements = 2;
        assertEquals(expectedNumberQualityRequirements, dtoDecisionQualityRequirementList.size());
        assertEquals((long) decision2.getId(), dtoDecisionQualityRequirementList.get(0).getId());
        assertEquals(qualityRequirement2.getRequirement(), dtoDecisionQualityRequirementList.get(0).getRequirement());
        assertEquals((long) decision1.getId(), dtoDecisionQualityRequirementList.get(1).getId());
        assertEquals(qualityRequirement1.getRequirement(), dtoDecisionQualityRequirementList.get(1).getRequirement());
    }
}