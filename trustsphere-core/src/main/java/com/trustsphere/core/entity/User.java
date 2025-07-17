package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.UserStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@NamedQueries({
        @NamedQuery(
                name = "User.findByEmail",
                query = "SELECT u FROM User u WHERE u.email = :email"
        ),
        @NamedQuery(
                name = "User.findActiveByEmail",
                query = "SELECT u FROM User u WHERE u.email = :email AND u.status = com.trustsphere.core.enums.UserStatus.ACTIVE"
        ),
        @NamedQuery(
                name = "User.findAll",
                query = "SELECT u FROM User u ORDER BY u.createdAt DESC"
        ),
        @NamedQuery(
                name = "User.countByStatus",
                query = "SELECT COUNT(u) FROM User u WHERE u.status = :status"
        ),
        @NamedQuery(
                name = "User.findActiveUsers",
                query = "SELECT u FROM User u WHERE u.status = com.trustsphere.core.enums.UserStatus.ACTIVE"
        )
})
@Entity
@Table(name = "users")
public class User extends BaseAuditEntity {

    @NotNull
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotNull
    @Size(min = 8, max = 255)
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Account> accounts;

    public User() {
    }

    public User(String email, String fullName, String hashedPassword, UserStatus status) {
        this.email = email;
        this.fullName = fullName;
        this.hashedPassword = hashedPassword;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}