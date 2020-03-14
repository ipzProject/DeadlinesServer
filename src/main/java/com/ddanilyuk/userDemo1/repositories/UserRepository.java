package com.ddanilyuk.userDemo1.repositories;

import com.ddanilyuk.userDemo1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(int id);

    Optional<User> findByUsername(String username);

//    User findByUuid(UUID uuid);

    Optional<User> findUserByUuid(UUID uuid);


    @Query("select u from User u where username like %?1%")
    Optional<List<User>> findAllByUsername(String username);

}