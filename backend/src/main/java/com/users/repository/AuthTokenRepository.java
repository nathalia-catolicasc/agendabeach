package com.users.repository;

import com.users.entity.AuthToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    @Modifying
    @Query("delete from AuthToken t where t.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
