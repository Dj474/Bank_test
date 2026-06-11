package com.example.bankcards.repository.user;

import com.example.bankcards.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
