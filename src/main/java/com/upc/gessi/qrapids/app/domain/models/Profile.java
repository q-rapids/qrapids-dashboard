package com.upc.gessi.qrapids.app.domain.models;

import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private QualityLevel qualityLevel = QualityLevel.ALL;

    public enum QualityLevel {
        ALL, METRICS_FACTORS, METRICS
    }

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Radar'")
    private DetailedViews dsiView = DetailedViews.Radar;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Radar'")
    private DetailedViews dqfView = DetailedViews.Radar;

    public enum DetailedViews {
        Radar, Stacked, Polar
    }

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Gauge'")
    private MetricsView mView = MetricsView.Gauge;

    public enum MetricsView {
        Gauge, Slider
    }

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Graph'")
    private QualityModelView qmView = QualityModelView.Graph;

    public enum QualityModelView {
        Graph, Sunburst
    }

    @OneToMany (cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name="profile_id")
    private List<ProfileProjects> profileProjectsList;

    @OneToMany (cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name="profile_id")
    private List<ProfileProjectStrategicIndicators> profileProjectStrategicIndicatorsList;

    public Profile() {}

    public Profile(String name, String description, QualityLevel qualityLevel) {
        this.name = name;
        this.description = description;
        this.qualityLevel = qualityLevel;
    }

    public Profile(String name, String description, QualityLevel qualityLevel, DetailedViews dsiview, DetailedViews dqfview, MetricsView mview, QualityModelView qmview) {
        this.name = name;
        this.description = description;
        this.qualityLevel = qualityLevel;
        this.dsiView = dsiview;
        this.dqfView = dqfview;
        this.mView = mview;
        this.qmView = qmview;
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

    public QualityLevel getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(QualityLevel qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public DetailedViews getDsiView() {
        return dsiView;
    }

    public void setDsiView(DetailedViews dsiView) {
        this.dsiView = dsiView;
    }

    public DetailedViews getDqfView() {
        return dqfView;
    }

    public void setDqfView(DetailedViews dqfView) {
        this.dqfView = dqfView;
    }

    public MetricsView getMetView() {
        return mView;
    }

    public void setMetView(MetricsView mView) {
        this.mView = mView;
    }

    public QualityModelView getQmView() {
        return qmView;
    }

    public void setQmView(QualityModelView qmView) {
        this.qmView = qmView;
    }

    public List<Project> getProjects() {
        List<Project> result = new ArrayList<>();
        for (ProfileProjects pp : this.profileProjectsList) {
            result.add(pp.project);
        }
        return result;
    }

    public List<ProfileProjects> getProfileProjectsList() {
        return profileProjectsList;
    }

    public void setProfileProjectsList(List<ProfileProjects> profileProjectsList) {
        this.profileProjectsList = profileProjectsList;
    }

    public List<ProfileProjectStrategicIndicators> getProfileProjectStrategicIndicatorsList() {
        return profileProjectStrategicIndicatorsList;
    }

    public void setProfileProjectStrategicIndicatorsList(List<ProfileProjectStrategicIndicators> profileProjectStrategicIndicatorsList) {
        this.profileProjectStrategicIndicatorsList = profileProjectStrategicIndicatorsList;
    }

    public boolean getAllSIByProject (Project prj){
        boolean allSI = true;
        for (ProfileProjects pp : this.profileProjectsList) {
            if (pp.getProject().getId() == prj.getId()){
                allSI =  pp.isAllSI();
            }
        }
        return allSI;
    }
}
