package com.evmaster.repository;

import com.evmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNic(String nic);

    List<User> findByUserType(User.UserType userType);

    long countByUserType(User.UserType userType);
}
