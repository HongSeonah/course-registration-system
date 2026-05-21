package com.example.courseregistration.domain.user.repository;

import com.example.courseregistration.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
