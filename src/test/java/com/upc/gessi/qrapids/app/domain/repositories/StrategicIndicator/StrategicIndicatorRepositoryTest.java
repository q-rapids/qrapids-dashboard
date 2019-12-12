package com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator;

import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorQualityFactors;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StrategicIndicatorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StrategicIndicatorRepository strategicIndicatorRepository;

    @Test
    public void findByName() {
        // Given
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        entityManager.persist(project);

        String strategicIndicator1Name = "Product Quality";
        String strategicIndicator1Description = "Quality of the product built";
        Strategic_Indicator strategicIndicator1 = new Strategic_Indicator(strategicIndicator1Name, strategicIndicator1Description, null, project);
        List<StrategicIndicatorQualityFactors> qualityFactors1 = new ArrayList<>();
        StrategicIndicatorQualityFactors factor1 = new StrategicIndicatorQualityFactors("codequality", -1, strategicIndicator1);
        qualityFactors1.add(factor1);
        StrategicIndicatorQualityFactors factor2 = new StrategicIndicatorQualityFactors( "softwarestability", 1, strategicIndicator1);
        qualityFactors1.add(factor2);
        StrategicIndicatorQualityFactors factor3 = new StrategicIndicatorQualityFactors( "testingstatus", -1, strategicIndicator1);
        qualityFactors1.add(factor3);
        strategicIndicator1.setQuality_factors(qualityFactors1);
        strategicIndicator1.setWeighted(false);
        entityManager.persist(strategicIndicator1);


        String strategicIndicator2Name = "Blocking";
        String strategicIndicator2Description = "Blocking elements";
        Strategic_Indicator strategicIndicator2 = new Strategic_Indicator(strategicIndicator2Name, strategicIndicator2Description, null, project);
        List<StrategicIndicatorQualityFactors> qualityFactors2 = new ArrayList<>();
        StrategicIndicatorQualityFactors factor4 = new StrategicIndicatorQualityFactors("blockingcode", -1, strategicIndicator2);
        qualityFactors2.add(factor4);
        StrategicIndicatorQualityFactors factor5 = new StrategicIndicatorQualityFactors( "testingstatus", 1, strategicIndicator2);
        qualityFactors2.add(factor5);
        StrategicIndicatorQualityFactors factor6 = new StrategicIndicatorQualityFactors( "qualityissuespecification", -1, strategicIndicator2);
        qualityFactors2.add(factor6);
        strategicIndicator2.setQuality_factors(qualityFactors2);
        strategicIndicator2.setWeighted(false);
        entityManager.persistAndFlush(strategicIndicator2);

        // When
        Strategic_Indicator strategicIndicatorFound = strategicIndicatorRepository.findByName(strategicIndicator1Name);

        // Then
        assertEquals(strategicIndicator1, strategicIndicatorFound);
    }

    @Test
    public void findByProject_Id() {
        // Given
        String project1ExternalId = "test1";
        String project1Name = "Test";
        String project1Description = "Test project";
        Project project1 = new Project(project1ExternalId, project1Name, project1Description, null, true);
        entityManager.persist(project1);

        String project2ExternalId = "test";
        String project2Name = "Test";
        String project2Description = "Test project";
        Project project2 = new Project(project2ExternalId, project2Name, project2Description, null, true);
        entityManager.persist(project2);

        String strategicIndicator1Name = "Product Quality";
        String strategicIndicator1Description = "Quality of the product built";
        Strategic_Indicator strategicIndicator1 = new Strategic_Indicator(strategicIndicator1Name, strategicIndicator1Description, null, project1);
        List<StrategicIndicatorQualityFactors> qualityFactors1 = new ArrayList<>();
        StrategicIndicatorQualityFactors factor1 = new StrategicIndicatorQualityFactors("codequality", -1, strategicIndicator1);
        qualityFactors1.add(factor1);
        StrategicIndicatorQualityFactors factor2 = new StrategicIndicatorQualityFactors( "softwarestability", 1, strategicIndicator1);
        qualityFactors1.add(factor2);
        StrategicIndicatorQualityFactors factor3 = new StrategicIndicatorQualityFactors( "testingstatus", -1, strategicIndicator1);
        qualityFactors1.add(factor3);
        strategicIndicator1.setQuality_factors(qualityFactors1);
        strategicIndicator1.setWeighted(false);
        entityManager.persist(strategicIndicator1);

        String strategicIndicator2Name = "Blocking";
        String strategicIndicator2Description = "Blocking elements";
        Strategic_Indicator strategicIndicator2 = new Strategic_Indicator(strategicIndicator2Name, strategicIndicator2Description, null, project2);
        List<StrategicIndicatorQualityFactors> qualityFactors2 = new ArrayList<>();
        StrategicIndicatorQualityFactors factor4 = new StrategicIndicatorQualityFactors("blockingcode", -1, strategicIndicator2);
        qualityFactors2.add(factor4);
        StrategicIndicatorQualityFactors factor5 = new StrategicIndicatorQualityFactors( "testingstatus", 1, strategicIndicator2);
        qualityFactors2.add(factor5);
        StrategicIndicatorQualityFactors factor6 = new StrategicIndicatorQualityFactors( "qualityissuespecification", -1, strategicIndicator2);
        qualityFactors2.add(factor6);
        strategicIndicator2.setQuality_factors(qualityFactors2);
        strategicIndicator2.setWeighted(false);
        entityManager.persistAndFlush(strategicIndicator2);

        // When
        List<Strategic_Indicator> strategicIndicatorListFound = strategicIndicatorRepository.findByProject_Id(project2.getId());

        // Then
        int expectedNumberStrategicIndicatorsFound = 1;
        assertEquals(expectedNumberStrategicIndicatorsFound, strategicIndicatorListFound.size());
        assertEquals(strategicIndicator2, strategicIndicatorListFound.get(0));
    }

    @Test
    public void existsByExternalIdAndProject_Id() {
        // Given
        String projectExternalId = "test";
        String projectName = "Test";
        String projectDescription = "Test project";
        Project project = new Project(projectExternalId, projectName, projectDescription, null, true);
        entityManager.persist(project);

        String strategicIndicator1Name = "Product Quality";
        String strategicIndicator1Description = "Quality of the product built";
        Strategic_Indicator strategicIndicator1 = new Strategic_Indicator(strategicIndicator1Name, strategicIndicator1Description, null, project);
        List<StrategicIndicatorQualityFactors> qualityFactors1 = new ArrayList<>();
        StrategicIndicatorQualityFactors factor1 = new StrategicIndicatorQualityFactors("codequality", -1, strategicIndicator1);
        qualityFactors1.add(factor1);
        StrategicIndicatorQualityFactors factor2 = new StrategicIndicatorQualityFactors( "softwarestability", 1, strategicIndicator1);
        qualityFactors1.add(factor2);
        StrategicIndicatorQualityFactors factor3 = new StrategicIndicatorQualityFactors( "testingstatus", -1, strategicIndicator1);
        qualityFactors1.add(factor3);
        strategicIndicator1.setQuality_factors(qualityFactors1);
        strategicIndicator1.setWeighted(false);
        entityManager.persistAndFlush(strategicIndicator1);

        // When
        boolean exists = strategicIndicatorRepository.existsByExternalIdAndProject_Id(strategicIndicator1.getExternalId(), project.getId());

        // Then
        assertTrue(exists);
    }
}