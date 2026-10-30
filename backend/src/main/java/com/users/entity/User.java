package com.users.entity;

import com.users.enums.ProfileType;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "profile_type")
    private ProfileType profile;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(name = "credit_balance", nullable = false)
    private java.math.BigDecimal creditBalance = java.math.BigDecimal.ZERO;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public java.math.BigDecimal getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(java.math.BigDecimal balance) {
        this.creditBalance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ProfileType getProfile() {
        return profile;
    }

    public void setProfile(ProfileType profile) {
        this.profile = profile;
    }
}
