package sait.khanh.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sait.khanh.api.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    // retreive or find the user by email (unique email)
    Optional<User> findByEmail(String email);
}
