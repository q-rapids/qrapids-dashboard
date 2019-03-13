package com.upc.gessi.qrapids.app.domain.models;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "product")
public class Product {	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "logo")
    private byte[] logo;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "product_project",
                joinColumns = { @JoinColumn(name = "product_id") },
                inverseJoinColumns = { @JoinColumn(name = "project_id") })
    private List<Project> projects;
    
    public Product(){}
    
    public Product(String name, String description, byte[] logo, List<Project> projects) {
    	this.name = name;
    	this.description = description;
    	this.logo = logo;
    	this.projects = projects;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }
    
    public List<Project> getProjects() {
        return projects;
    }

    public void setLogo(List<Project> projects) {
        this.projects = projects;
    }
    
}

