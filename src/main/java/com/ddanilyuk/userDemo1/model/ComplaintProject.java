package com.ddanilyuk.userDemo1.model;

import java.util.List;

public class ComplaintProject {
    public Project project;
    public List<String> usersToAdd;

    public ComplaintProject(Project project, List<String> usersToAdd) {
        this.project = project;
        this.usersToAdd = usersToAdd;
    }
}
