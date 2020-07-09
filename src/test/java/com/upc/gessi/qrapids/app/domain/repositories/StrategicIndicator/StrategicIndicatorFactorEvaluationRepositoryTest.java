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

@RunWith(SpringRunner.class)
@DataJpaTest
public class StrategicIndicatorFactorEvaluationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StrategicIndicatorQualityFactorsRepository strategicIndicatorQualityFactorsRepository;

    @Test
    public void findByStrategic_indicatorId() {
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
        StrategicIndicatorQualityFactors factor2 = new StrategicIndicatorQualityFactors( "softwarestability", -1, strategicIndicator1);
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
        StrategicIndicatorQualityFactors factor5 = new StrategicIndicatorQualityFactors( "testingstatus", -1, strategicIndicator2);
        qualityFactors2.add(factor5);
        StrategicIndicatorQualityFactors factor6 = new StrategicIndicatorQualityFactors( "qualityissuespecification", -1, strategicIndicator2);
        qualityFactors2.add(factor6);
        strategicIndicator2.setQuality_factors(qualityFactors2);
        strategicIndicator2.setWeighted(false);
        entityManager.persistAndFlush(strategicIndicator2);

        // When
        List<StrategicIndicatorQualityFactors> strategicIndicatorQualityFactorsList = strategicIndicatorQualityFactorsRepository.findByStrategic_indicator(strategicIndicator1);
        List<String> strategicIndicatorQualityFactorsListFound = convertToString(strategicIndicatorQualityFactorsList);

        // Then
        int expectedNumberStrategicIndicatorsQualityFactorsFound = 3;
        assertEquals(expectedNumberStrategicIndicatorsQualityFactorsFound, strategicIndicatorQualityFactorsList.size());
        assertEquals(strategicIndicator1.getWeights(), strategicIndicatorQualityFactorsListFound);
    }

    private List<String> convertToString (List<StrategicIndicatorQualityFactors> strategicIndicatorQualityFactorsList) {
        List <String> siqfList = new ArrayList<>();
        for (StrategicIndicatorQualityFactors s : strategicIndicatorQualityFactorsList) {
            siqfList.add(s.getQuality_factor());
            siqfList.add(String.valueOf(s.getWeight()));
        }
        return siqfList;
    }

}
