package com.ddanilyuk.userDemo1.controllers;

import com.ddanilyuk.userDemo1.extensions.SuccessException;
import com.ddanilyuk.userDemo1.extensions.ServiceException;
import com.ddanilyuk.userDemo1.model.Project;
import com.ddanilyuk.userDemo1.model.User;
import com.ddanilyuk.userDemo1.model.Views;
import com.ddanilyuk.userDemo1.repositories.ProjectRepository;
import com.ddanilyuk.userDemo1.repositories.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserController(UserRepository userRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }


    // Test request (Delete with release)
    @GetMapping("/all")
    @JsonView(Views.usersView.class)
    public List<User> index() {
        return userRepository.findAll();
    }


    @PostMapping("/registration")
    @JsonView(Views.defaultView.class)
    public User registration(@RequestBody Map<String, String> body) {
        String userFirstName = body.get("userFirstName");
        String userSecondName = body.get("userSecondName");
        String username = body.get("username");
        String password = body.get("password");

        if (userFirstName == null || userFirstName.equals("")) {
            throw new ServiceException("Invalid userFirstName");
        } else if (userSecondName == null || userSecondName.equals("")) {
            throw new ServiceException("Invalid userSecondName");
        } else if (username == null || username.equals("")) {
            throw new ServiceException("Invalid username");
        } else if (password == null || password.equals("")) {
            throw new ServiceException("Invalid password");
        } else {
            Optional<User> isExistUser = userRepository.findByUsername(username);

            if (!isExistUser.isPresent()) {
                User user = new User(userFirstName, userSecondName, username, password);
                user.setPassword(passwordEncoder.encode(user.getPassword()));

                Date dateNow = new Date();
                user.setUserCreationTime(dateNow.getTime());

                return userRepository.save(user);
            } else {
                throw new ServiceException("User is already exist");
            }
        }
    }


    @PostMapping("/login")
    @JsonView(Views.defaultView.class)
    public User loginUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.equals("")) {
            throw new ServiceException("Invalid username");
        } else if (password == null || password.equals("")) {
            throw new ServiceException("Invalid password");
        } else {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                    return user;
                } else {
                    throw new ServiceException("Password is wrong");
                }
            } else {
                throw new ServiceException("User not found");
            }
        }
    }


    @GetMapping("{uuid}/details")
    @JsonView(Views.usersView.class)
    public User details(@PathVariable String uuid) {
        Optional<User> optionalUser = userRepository.findUserByUuid(UUID.fromString(uuid));
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new ServiceException("User not found");
        }
    }


    @GetMapping("findByUsername/{username}")
    @JsonView(Views.defaultView.class)
    public List<User> findByUsername(@PathVariable String username) {
        Optional<List<User>> optionalUsers = userRepository.findAllByUsername(username);
        if (optionalUsers.isPresent()) {
            return optionalUsers.get();
        } else {
            throw new ServiceException("Users not found");
        }
    }


    @GetMapping("{uuid}/getInvitations")
    @JsonView(Views.projectView.class)
    public List<Project> getInvitations(@PathVariable String uuid) {
        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getProjectsInvited();
        } else {
            throw new ServiceException("User not found");
        }
    }


    @PostMapping("{uuid}/acceptInvite/{projectID}")
    @JsonView(Views.projectView.class)
    public Project acceptInvite(@PathVariable String uuid, @PathVariable String projectID) {

        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));
        Optional<Project> projectOptional = projectRepository.findByProjectId(Integer.parseInt(projectID));

        if (!userOptional.isPresent()) {
            throw new ServiceException("User not found");
        } else if (!projectOptional.isPresent()) {
            throw new ServiceException("Project not found");
        } else {
            User user = userOptional.get();
            Project project = projectOptional.get();
            List<Project> projects =  user.getProjectsInvited();
            if (projects.contains(project)) {
                project.getProjectUsers().add(user);
                project.getProjectUsersInvited().remove(user);
                return projectRepository.save(project);
            } else {
                throw new ServiceException("You are not invited to this project");
            }
        }
    }


    @Modifying
    @PostMapping("{uuid}/editUser")
    @JsonView(Views.usersView.class)
    public User editUser(@PathVariable String uuid, @RequestBody User userEdited) {

        Optional<User> userToEditOptional = userRepository.findUserByUuid(UUID.fromString(uuid));

        if (!userToEditOptional.isPresent()) {
            throw new ServiceException("User not found");
        } else {
            User user = userToEditOptional.get();

            if (userEdited.getUsername() != null && !userEdited.getUsername().equals("")) {
                user.setUsername(userEdited.getUsername());
            }
            if (userEdited.getUserFirstName() != null && !userEdited.getUserFirstName().equals("")) {
                user.setUserFirstName(userEdited.getUserFirstName());
            }
            if (userEdited.getUserSecondName() != null && !userEdited.getUserSecondName().equals("")) {
                user.setUserSecondName(userEdited.getUserSecondName());
            }
            if (userEdited.getPassword() != null && !userEdited.getPassword().equals("")) {
                user.setPassword(passwordEncoder.encode(userEdited.getPassword()));
            }

            return userRepository.save(user);
        }
    }

//    @Modifying
//    @PostMapping("{uuid}/editUser")
//    @JsonView(Views.usersView.class)
//    public User editUser(@PathVariable String uuid, @RequestBody Map<String, String> body) {
//        UUID userToEditUUID = UUID.fromString(uuid);
//        String userFirstName = body.get("userFirstName");
//        String userSecondName = body.get("userSecondName");
//        String username = body.get("username");
//        String password = body.get("password");
//
//        Optional<User> userToEditOptional = userRepository.findUserByUuid(userToEditUUID);
//
//        if (!userToEditOptional.isPresent()) {
//            throw new ServiceException("User not found");
//        } else {
//            User user = userToEditOptional.get();
//            user.setUserFirstName(userFirstName);
//            user.setUserSecondName(userSecondName);
//            user.setUsername(username);
//            user.setPassword(passwordEncoder.encode(password));
//            return userRepository.save(user);
//        }
//    }


    @DeleteMapping("{uuid}/deleteUser")
    public String deleteUser(@PathVariable String uuid)  {
        Optional<User> userOptional = userRepository.findUserByUuid(UUID.fromString(uuid));

        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            throw new SuccessException("Deleted");
        } else {
            throw new ServiceException("User not found");
        }
    }

}