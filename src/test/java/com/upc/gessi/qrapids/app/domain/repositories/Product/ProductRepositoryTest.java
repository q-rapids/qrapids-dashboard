package com.upc.gessi.qrapids.app.domain.repositories.Product;

import com.upc.gessi.qrapids.app.domain.models.Product;
import com.upc.gessi.qrapids.app.domain.models.Project;
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
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void findByName() {
        // Given
        Project project1 = new Project("project1", "Project 1", "test project", null, true);
        entityManager.persist(project1);
        Project project2 = new Project("project2", "Project 2", "test project", null, true);
        entityManager.persist(project2);
        List<Project> projectList1 = new ArrayList<>();
        projectList1.add(project1);
        projectList1.add(project2);
        String product1Name = "Product 1";
        String product1Description = "test product";
        Product product1 = new Product(product1Name, product1Description, null, projectList1);
        entityManager.persist(product1);

        Project project3 = new Project("project3", "Project 3", "test project", null, true);
        entityManager.persist(project3);
        Project project4 = new Project("project4", "Project 4", "test project", null, true);
        entityManager.persist(project4);
        List<Project> projectList2 = new ArrayList<>();
        projectList2.add(project3);
        projectList2.add(project4);
        String product2Name = "Product 2";
        String product2Description = "test product";
        Product product2 = new Product(product2Name, product2Description, null, projectList1);
        entityManager.persistAndFlush(product2);

        // When
        Product productFound = productRepository.findByName(product1Name);

        // Then
        assertEquals(product1, productFound);
    }
}