package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quality_factor")
public class QualityFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "externalId", unique = true)
    private String externalId;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "weighted")
    private boolean weighted;

    @ManyToOne
    @JoinColumn(name="projectId", referencedColumnName = "id")
    private Project project;

    @OneToMany (cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name="quality_factor_id")
    private List<QualityFactorMetrics> metrics = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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

    public boolean isWeighted() {
        return weighted;
    }

    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<QualityFactorMetrics> getMetricsWeights() {
        return metrics;
    }

    public void setMetricsWeights(List<QualityFactorMetrics> metrics) {
        this.metrics = metrics;
    }
}
