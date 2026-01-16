package com.evmaster.repository;

import com.evmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for User authentication and basic CRUD operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNic(String nic);

    List<User> findByUserType(User.UserType userType);

    /** CRUCIAL FIX: Define custom count methods for the Admin Dashboard stats. */
    long countByUserType(User.UserType userType);
}