package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quality_factor")
public class Factor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "weighted")
    private boolean weighted;

    @ManyToOne
    @JoinColumn(name="project_id", referencedColumnName = "id")
    private Project project;

    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="quality_factor_id")
    private List<QualityFactorMetrics> qualityFactorMetricsList = new ArrayList<>();

    public Factor() {
    }

    public Factor(String name, String description, List<QualityFactorMetrics> qualityMetrics, boolean weighted, Project project) {
        setName(name);
        setDescription(description);
        setQualityFactorMetricsList(qualityMetrics);
        setWeighted(weighted);
        setProject(project);
    }

    // Quality Factor without Quality Metrics
    public Factor(String name, String description, Project project) {
        setName(name);
        setDescription(description);
        setProject(project);
    }

    // Imported Quality Factor without Quality Metrics
    public Factor(String externalID,String name, String description, Project project) {
        setExternalId(externalID);
        setName(name);
        setDescription(description);
        setProject(project);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId () {
        if (this.externalId == null || this.externalId.isEmpty()) {
            this.externalId = name.replaceAll("\\s+","").toLowerCase();
        }
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (this.externalId == null || this.externalId.isEmpty()) {
            this.externalId = name.replaceAll("\\s+","").toLowerCase();
        }
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

    public List<String> getWeights() {
        List<String> metricsWeights = new ArrayList<>();
        for (int i = 0; i < this.qualityFactorMetricsList.size(); i ++) {
            metricsWeights.add(String.valueOf(this.qualityFactorMetricsList.get(i).getMetric().getId()));
            metricsWeights.add(String.valueOf(this.qualityFactorMetricsList.get(i).getWeight()));
        }
        // list with metric external id and its corresponding weight
        return metricsWeights;
    }

    public List<String> getWeightsWithExternalId() {
        List<String> metricsWeights = new ArrayList<>();
        for (int i = 0; i < this.qualityFactorMetricsList.size(); i ++) {
            metricsWeights.add(String.valueOf(this.qualityFactorMetricsList.get(i).getMetric().getExternalId()));
            metricsWeights.add(String.valueOf(this.qualityFactorMetricsList.get(i).getWeight()));
        }
        // list with metric external id and its corresponding weight
        return metricsWeights;
    }

    public List<String> getMetricsIds() {
        List<String> metrics_ids = new ArrayList<>();
        for (int i = 0; i < this.qualityFactorMetricsList.size(); i ++) {
            metrics_ids.add(String.valueOf(this.qualityFactorMetricsList.get(i).getMetric().getId()));
        }
        return metrics_ids;
    }

    public List<String> getMetrics() {
        List<String> metrics_ids = new ArrayList<>();
        for (int i = 0; i < this.qualityFactorMetricsList.size(); i ++) {
            metrics_ids.add(String.valueOf(this.qualityFactorMetricsList.get(i).getMetric().getExternalId()));
        }
        return metrics_ids;
    }

    public List<QualityFactorMetrics> getQualityFactorMetricsList() {
        return qualityFactorMetricsList;
    }

    public void setQualityFactorMetricsList(List<QualityFactorMetrics> qfMetricsList) {
        this.qualityFactorMetricsList = qfMetricsList;
    }
}
