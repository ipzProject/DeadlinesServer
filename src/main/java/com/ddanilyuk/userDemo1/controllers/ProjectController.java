package com.ddanilyuk.userDemo1.controllers;

import com.ddanilyuk.userDemo1.extensions.UserExtension;
import com.ddanilyuk.userDemo1.model.*;
import com.ddanilyuk.userDemo1.repositories.DeadlineRepository;
import com.ddanilyuk.userDemo1.repositories.ProjectRepository;
import com.ddanilyuk.userDemo1.repositories.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DeadlineRepository deadlineRepository;


    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository, DeadlineRepository deadlineRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.deadlineRepository = deadlineRepository;
    }


    @GetMapping("{uuid}/allProjects")
    @JsonView(Views.projectView.class)
    public List<Project> allProjects(@PathVariable String uuid) {
        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Project> projects = user.getProjectsCreated();
            projects.addAll(user.getProjectsAppended());
            return projects;
        } else {
            throw new UserExtension("User is not found");
        }
    }


    @PostMapping("{uuid}/createProject")
    @JsonView(Views.projectView.class)
    public Project createProject(@PathVariable String uuid, @Valid @RequestBody ComplaintProject complaintProject) {

        Project project = complaintProject.project;
        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));

        if (!userOptional.isPresent()) {
            throw new UserExtension("User is not found");
        } else if (project.getProjectDescription() == null) {
            throw new UserExtension("Invalid projectDescription");
        } else if (project.getProjectName() == null || project.getProjectName().equals("")) {
            throw new UserExtension("Invalid projectName");
        } else {
            if (project.getProjectCreationTime() == 0) {
                Date dateNow = new Date();
                project.setProjectCreationTime(dateNow.getTime());
            }
            project.setProjectOwner(userOptional.get());

            projectRepository.save(project);

            List<String> usersToAdd = complaintProject.usersToAdd;
            for (String userToAdd : usersToAdd) {
                addUserToProject(uuid, userToAdd, String.valueOf(project.getProjectId()));
            }

            return project;

        }
    }


    @PostMapping("{uuidOwner}/{projectID}/addUserToProject/{uuidUserToAdd}")
    @JsonView({Views.projectView.class})
    public Project addUserToProject(@PathVariable String uuidOwner, @PathVariable String uuidUserToAdd, @PathVariable String projectID) {

        Optional<User> userToAddOptional = userRepository.findUserByUuid(UUID.fromString(uuidUserToAdd));
        Optional<User> userOwnerOptional = userRepository.findUserByUuid(UUID.fromString(uuidOwner));
        Optional<Project> projectOptional = projectRepository.findByProjectId(Integer.parseInt(projectID));

        if (!userToAddOptional.isPresent()) {
            throw new UserExtension("User to add not found");
        } else if (!userOwnerOptional.isPresent()) {
            throw new UserExtension("User owner not found");
        } else if (!projectOptional.isPresent()) {
            throw new UserExtension("Project not found");
        } else {
            User userToAdd = userToAddOptional.get();
            User userOwner = userOwnerOptional.get();
            Project project = projectOptional.get();

            if (project.getProjectOwner().getUuid().equals(userOwner.getUuid())) {

                if (project.getProjectUsers().contains(userToAdd)) {
                    throw new UserExtension("User is already in this project");
                } else {
//                    project.getProjectUsersUuid().add(userToAdd.getUuid());
                    project.getProjectUsers().add(userToAdd);
//                    project.getProjectInvitedUsers().add(userToAdd);
                }

//                Set<Project> projects = userToAdd.getProjectsAppended();
//                projects.add(project);
//                userToAdd.setProjectsAppended(projects);

//                List<Project> projectsInvitations = userToAdd.getProjectsInvitations();
//                projectsInvitations.add(project);
//                userToAdd.setProjectsInvitations(projectsInvitations);

                userRepository.save(userToAdd);

                return projectRepository.save(project);
//                return project;
            } else {
                throw new UserExtension("Invalid project owner");
            }
        }
    }


    @PostMapping("{uuid}/{projectID}/addDeadline")
    @JsonView({Views.projectView.class})
    public Project addDeadlineToProject(@PathVariable String uuid, @PathVariable String projectID, @RequestBody ComplaintDeadline complaintDeadline) {

        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));
        Optional<Project> projectToAddOptional = projectRepository.findByProjectId(Integer.parseInt(projectID));

        Deadline deadline = complaintDeadline.deadline;

        if (!userOptional.isPresent()) {
            throw new UserExtension("User not found");
        } else if (!projectToAddOptional.isPresent()) {
            throw new UserExtension("Project not found");
        } else if (deadline.getDeadlineName() == null || deadline.getDeadlineName().equals("")) {
            throw new UserExtension("Invalid deadlineName");
        } else if (deadline.getDeadlineDescription() == null) {
            throw new UserExtension("Invalid deadlineDescription");
        } else {

            User user = userOptional.get();
            Project projectToAdd = projectToAddOptional.get();

            if (projectToAdd.getProjectOwner().getUuid().equals(user.getUuid())) {

                if (deadline.getDeadlineCreatedTime() == 0) {
                    Date dateNow = new Date();
                    deadline.setDeadlineCreatedTime(dateNow.getTime());
                }

                deadline.setProject(projectToAdd);
                deadline.setDeadlineProjectId(projectToAdd.getProjectId());
                projectToAdd.getDeadlines().add(deadline);


                deadlineRepository.save(deadline);

                projectRepository.save(projectToAdd);

                List<String> usersUUIDToAdd = complaintDeadline.usersToAdd;

                for (String userToAdd : usersUUIDToAdd) {
//                    Optional<User> userToAddOptional = userRepository.findUserByUuid(UUID.fromString(userToAdd));
//                    if (!userToAddOptional.isPresent()) {
//                        throw new UserExtension("User to add not found");
//                    }
                    addExecutorToDeadline(uuid, projectID, String.valueOf(deadline.getDeadlineId()), userToAdd);
                }


                return projectToAdd;
            } else {
                throw new UserExtension("Invalid project owner");
            }
        }

    }


    @PostMapping("{uuidOwner}/{projectID}/{deadlineId}/addExecutor/{uuidUserToAdd}")
    @JsonView({Views.projectView.class})
    public Project addExecutorToDeadline(@PathVariable String uuidOwner,
                                         @PathVariable String projectID,
                                         @PathVariable String deadlineId,
                                         @PathVariable String uuidUserToAdd) {

        Optional<User> userToAddOptional = userRepository.findUserByUuid(UUID.fromString(uuidUserToAdd));
        Optional<User> userOwnerOptional = userRepository.findUserByUuid(UUID.fromString(uuidOwner));
        Optional<Project> projectOptional = projectRepository.findByProjectId(Integer.parseInt(projectID));

        if (!userToAddOptional.isPresent()) {
            throw new UserExtension("User to add not found");
        } else if (!userOwnerOptional.isPresent()) {
            throw new UserExtension("User owner not found");
        } else if (!projectOptional.isPresent()) {
            throw new UserExtension("Project not found");
        } else {
            User userToAdd = userToAddOptional.get();
            User userOwner = userOwnerOptional.get();
            Project project = projectOptional.get();

            if (project.getProjectOwner().getUuid().equals(userOwner.getUuid())) {

                if (project.getProjectUsers().contains(userToAdd)) {
                    List<Deadline> projectDeadlines = project.getDeadlines();
                    for (Deadline deadline : projectDeadlines) {
                        if (deadline.getDeadlineId() == Integer.parseInt(deadlineId)) {
                            deadline.getDeadlineExecutorsUuid().add(userToAdd.getUuid());

                            return projectRepository.save(project);
                        }
                    }
                    throw new UserExtension("Deadline not found");
                } else {
                    throw new UserExtension("User to add is not in this project");
                }
            } else {
                throw new UserExtension("Invalid project owner");
            }
        }


    }


    @GetMapping("deadlineDetail/{id}")
    @JsonView({Views.deadlinesDetailView.class})
    public Deadline findDeadline(@PathVariable String id) {
        Optional<Deadline> deadlineOptional = deadlineRepository.findById(Integer.parseInt(id));

        if (deadlineOptional.isPresent()) {

            return deadlineOptional.get();
        } else {
            throw new UserExtension("Deadline not found");
        }
    }


    @GetMapping("projectDetail/{id}")
    @JsonView({Views.projectView.class})
    public Project findProject(@PathVariable String id) {
        Optional<Project> projectOptional = projectRepository.findById(Integer.parseInt(id));

        if (projectOptional.isPresent()) {

            return projectOptional.get();
        } else {
            throw new UserExtension("Project not found");
        }
    }


    @DeleteMapping("projectDelete/{id}")
    public String deleteProject(@PathVariable String id) {
        Optional<Project> projectOptional = projectRepository.findById(Integer.parseInt(id));

        if (projectOptional.isPresent()) {
//            projectRepository.delete(projectOptional.get());
            projectRepository.deleteById(projectOptional.get().getProjectId());

            return "deleted";
        } else {
            throw new UserExtension("Project not found");
        }
    }


    @DeleteMapping("userDelete/{id}")
    public String deleteUser(@PathVariable String id) {
        Optional<User> userOptional = userRepository.findById(Integer.parseInt(id));

        if (userOptional.isPresent()) {
//            projectRepository.delete(projectOptional.get());
            userRepository.deleteById(userOptional.get().getUserId());

            return "deleted";
        } else {
            throw new UserExtension("Project not found");
        }
    }

    @DeleteMapping("deadlineDelete/{id}")
    public String deadlineDelete(@PathVariable String id) {
        Optional<Deadline> deadlineOptional = deadlineRepository.findById(Integer.parseInt(id));

        if (deadlineOptional.isPresent()) {
//            projectRepository.delete(projectOptional.get());
            deadlineRepository.deleteById(deadlineOptional.get().getDeadlineId());
            return "deleted";
        } else {
            throw new UserExtension("Deadline not found");
        }
    }




}
