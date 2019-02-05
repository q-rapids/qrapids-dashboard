package com.upc.gessi.qrapids.app.domain.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="usergroup ")
public class UserGroup implements Serializable {

    // SerialVersion UID
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="default_group")
    private boolean default_group;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "userGroup_route",
                joinColumns = { @JoinColumn(name = "usergroup_id") },
                inverseJoinColumns = { @JoinColumn(name = "route_id") })
    private Set<Route> routes = new HashSet<Route>(0);

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "userGroup")
    private List<AppUser> appUsers = new ArrayList<>();

    public UserGroup() {
    }

    public UserGroup(String name, Set<Route> routes) {
        this.name = name;
        this.routes = routes;
    }

    public UserGroup(String name, Set<Route> routes, List<AppUser> appUsers) {
        this.name = name;
        this.routes = routes;
        this.appUsers = appUsers;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefault_group(boolean group_default) {
        this.default_group = group_default;
    }

    public void setRoutes(Set<Route> routes) {
        this.routes = routes;
    }

    public void setAppUsers(List<AppUser> appUsers) {
        this.appUsers = appUsers;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean getDefault_group() {
        return default_group;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public List<AppUser> getAppUsers() {
        return appUsers;
    }
}
