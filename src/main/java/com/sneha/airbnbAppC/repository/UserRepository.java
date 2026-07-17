package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}