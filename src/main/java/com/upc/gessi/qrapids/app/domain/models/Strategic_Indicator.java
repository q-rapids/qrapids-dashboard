package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
    This class is not following the class naming rules, it should be named as StrategicIndicator, without '_', but
    there is an unsolved issue with the hibernate and we need to add the '_' in order to have de name
    'strategic_indicator_quality_factors name for the table in the database, the table storing the quality_factors list
 */

//TODO: refactor to have the name of the table 'strategic_indicator_quality_factor' independent to the this class name

@Entity
@Table(name="strategic_indicator",
        uniqueConstraints = @UniqueConstraint(columnNames = {"external_id", "projectId"}))
public class Strategic_Indicator implements Serializable {

    // SerialVersion UID
    private static final long serialVersionUID = 14L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="external_id")
    private String externalId;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name = "network")
    private byte[] network;

    // we need to keep the name of this list as quality_factors, it is the name of the table in the database
    // ToDo: This should be changed, the name of the local variables should be no directly connected to table names in database
    @ElementCollection
    private List<String> quality_factors = new ArrayList<String>();

    @ManyToOne
    @JoinColumn(name="projectId", referencedColumnName = "id")
    private Project project;


    public Strategic_Indicator() {
    }

    public Strategic_Indicator(String name, String description, byte[] network, List<String> qualityFactors, Project project) {
        setName(name);
        setDescription(description);
        setNetwork(network);
        setQuality_factors(qualityFactors);
        setProject(project);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExternalID (String externalId)  {
        this.externalId = externalId;
    }

    public String getExternalId () {
        if (this.externalId == null || this.externalId.isEmpty()) {
            this.externalId = name.replaceAll("\\s+","").toLowerCase();
        }
        return this.externalId;
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

    public byte[] getNetwork() {
        return network;
    }

    public void setNetwork(byte[] network) {
        this.network = network;
    }

    public List<String> getQuality_factors() {
        return quality_factors;
    }

    public void setQuality_factors(List<String> quality_factors) {
        this.quality_factors = quality_factors;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
