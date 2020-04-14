package com.ddanilyuk.userDemo1.model;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "usr")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(Views.defaultView.class)
    private int userId;

    @JsonView(Views.defaultView.class)
    private String userFirstName;

    @JsonView(Views.defaultView.class)
    private String userSecondName;

    @JsonView(Views.defaultView.class)
    private String username;

    @Column
    @JsonView(Views.defaultView.class)
    private long userCreationTime;

    private String password;


    @Column(name = "uuid", updatable = false, nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @JsonView(Views.defaultView.class)
    private UUID uuid;


    @OneToMany(mappedBy = "projectOwner", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH, orphanRemoval = true)
    @JsonView(Views.usersView.class)
    private List<Project> projectsCreated = new ArrayList<>();


    @Column
    @JsonView(Views.usersView.class)
//    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "projectUsers", cascade = {CascadeType.MERGE})
    @ManyToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "projects_appended",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Project> projectsAppended = new ArrayList<>();

//    @Column
//    @JsonView(Views.usersView.class)
//    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
//    @JoinTable(
//            name = "projects_invitations",
//            joinColumns = @JoinColumn(name = "project_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id"))
//    private List<Project> projectsInvitations = new ArrayList<>();





    public User() {
    }

    public User(String userFirstName, String userSecondName) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
    }

    public User(String userFirstName, String userSecondName, String username, String password) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
        this.username = username;
        this.password = password;
        this.uuid = UUID.randomUUID();
    }

    public User(String userFirstName, String userSecondName, String username, String password, List<Project> projects) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
        this.username = username;
        this.password = password;
        this.projectsCreated = projects;
    }

    public User(String userFirstName, String userSecondName, List<Project> projects) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
        this.projectsCreated = projects;
    }

    public long getUserCreationTime() {
        return userCreationTime;
    }

    public void setUserCreationTime(long userCreationTime) {
        this.userCreationTime = userCreationTime;
    }

//    public List<Project> getProjectsInvitations() {
//        return projectsInvitations;
//    }
//
//    public void setProjectsInvitations(List<Project> projectsInvitations) {
//        this.projectsInvitations = projectsInvitations;
//    }

    public List<Project> getProjectsCreated() {
        return projectsCreated;
    }

    public void setProjectsCreated(List<Project> projectsCreated) {
        this.projectsCreated = projectsCreated;
    }

    public List<Project> getProjectsAppended() {
        return projectsAppended;
    }

    public void setProjectsAppended(List<Project> projectsAppended) {
        this.projectsAppended = projectsAppended;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserSecondName() {
        return userSecondName;
    }

    public void setUserSecondName(String userSecondName) {
        this.userSecondName = userSecondName;
    }


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userFirstName='" + userFirstName + '\'' +
                ", userSecondName='" + userSecondName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", uuid=" + uuid +
                ", projects=" + projectsCreated +
                '}';
    }
}
