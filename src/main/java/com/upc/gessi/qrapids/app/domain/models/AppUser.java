package com.upc.gessi.qrapids.app.domain.models;

import com.upc.gessi.qrapids.app.config.libs.RouteFilter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AppUser ")
public class AppUser implements Serializable{

    // SerialVersion UID
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name="username", unique = true)
	private String username;

	@Column(name="email", unique = true)
	private String email;

	@Column(name="admin")
	private boolean admin;

	@Column(name="password")
	private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="usergroup_id")
    private UserGroup userGroup;

    @Column(name="appuser_question")
    private Question appuser_question;

    @Column(name="question")
    private String question;

	public AppUser() { }

    public AppUser(String username, String email, boolean admin, String password, UserGroup userGroup, Question appuser_question, String question) {
        this.username = username;
        this.email = email;
        this.admin = admin;
        this.password = password;
        this.userGroup = userGroup;
        this.appuser_question = appuser_question;
        this.question = question;
    }

    public AppUser(String username, String email, boolean admin, String password) {
        this.username = username;
        this.email = email;
        this.admin = admin;
        this.password = password;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public void setAppuser_question(Question appuser_question) {
        this.appuser_question = appuser_question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean getAdmin() {
        return admin;
    }

    public String getPassword() {

	    return password;

    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    @OneToOne
    public Question getAppuser_question() {
        return appuser_question;
    }

    public String getQuestion() {
        return question;
    }

    public boolean hasRoute ( String route ) {

        if ( this.admin )
            return true;

	    else if (this.userGroup != null) {

            // Validation library
            RouteFilter filter = new RouteFilter();

            List<Route> routes = new ArrayList<Route>( this.userGroup.getRoutes() );

            return filter.userURLAttemp( route, routes );

        }

	    return false;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                ", password='" + password + '\'' +
                ", userGroup=" + userGroup +
                ", appuser_question=" + appuser_question +
                ", question='" + question + '\'' +
                '}';
    }
}
