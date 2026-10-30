package com.users.repository;

import com.users.entity.AuthToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {}
