package com.example.bankcards.repository.user;

import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    default User byId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

}
