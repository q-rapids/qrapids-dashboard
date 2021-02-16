package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="route")
public class Route implements Serializable{

    // SerialVersion UID
    private static final long serialVersionUID = 11L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="required")
    private boolean required;

    @Column(name="path", unique = true)
    private String path;

    public Route() {
    }

    public Route(String name, String path) {
        this.name = name;
        this.path = path;
        this.required = false;
    }

    public Route(String name, String path, boolean required ) {
        this.name = name;
        this.path = path;
        this.required = required;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", required=" + required +
                ", path='" + path + '\'' +
                '}';
    }
}
