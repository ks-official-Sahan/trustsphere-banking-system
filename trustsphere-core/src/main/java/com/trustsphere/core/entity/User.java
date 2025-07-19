package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.UserStatus;
import com.trustsphere.core.util.SecurityUtil;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users with roles and accounts.
 * Includes comprehensive validation and security features.
 */
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
        ),
        @NamedQuery(
                name = "User.findByRole",
                query = "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName"
        ),
        @NamedQuery(
                name = "User.findByStatusAndRole",
                query = "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.status = :status AND r.name = :roleName"
        )
})
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_created_at", columnList = "created_at")
})
public class User extends BaseAuditEntity {

    @NotNull
    @Email(message = "Email must be a valid email address")
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Full name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotNull
    @Size(min = 60, max = 255, message = "Hashed password must be between 60 and 255 characters")
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "password_expires_at")
    private Instant passwordExpiresAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role_id", columnList = "role_id")
            }
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();

    public User() {
    }

    public User(String email, String fullName, String hashedPassword, UserStatus status) {
        this.email = email;
        this.fullName = fullName;
        this.hashedPassword = hashedPassword;
        this.status = status;
    }

    /**
     * Creates a new user with hashed password.
     * 
     * @param email the user's email
     * @param fullName the user's full name
     * @param plainPassword the plain text password (will be hashed)
     * @param status the user status
     * @return a new User instance
     */
    public static User createUser(String email, String fullName, String plainPassword, UserStatus status) {
        if (!SecurityUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!SecurityUtil.isValidPassword(plainPassword)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
        
        String hashedPassword = SecurityUtil.hashPassword(plainPassword);
        User user = new User(email, fullName, hashedPassword, status);
        user.setPasswordChangedAt(Instant.now());
        user.setPasswordExpiresAt(Instant.now().plusSeconds(90 * 24 * 60 * 60)); // 90 days
        return user;
    }

    /**
     * Verifies a plain text password against the user's hashed password.
     * 
     * @param plainPassword the plain text password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword) {
        return SecurityUtil.verifyPassword(plainPassword, this.hashedPassword);
    }

    /**
     * Changes the user's password.
     * 
     * @param newPlainPassword the new plain text password
     */
    public void changePassword(String newPlainPassword) {
        if (!SecurityUtil.isValidPassword(newPlainPassword)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
        
        this.hashedPassword = SecurityUtil.hashPassword(newPlainPassword);
        this.passwordChangedAt = Instant.now();
        this.passwordExpiresAt = Instant.now().plusSeconds(90 * 24 * 60 * 60); // 90 days
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Records a successful login.
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Records a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            // Lock account for 30 minutes after 5 failed attempts
            this.accountLockedUntil = Instant.now().plusSeconds(30 * 60);
        }
    }

    /**
     * Checks if the account is currently locked.
     * 
     * @return true if account is locked, false otherwise
     */
    public boolean isAccountLocked() {
        return this.accountLockedUntil != null && Instant.now().isBefore(this.accountLockedUntil);
    }

    /**
     * Checks if the password has expired.
     * 
     * @return true if password has expired, false otherwise
     */
    public boolean isPasswordExpired() {
        return this.passwordExpiresAt != null && Instant.now().isAfter(this.passwordExpiresAt);
    }

    /**
     * Checks if the user has a specific role.
     * 
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Adds a role to the user.
     * 
     * @param role the role to add
     */
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    /**
     * Removes a role from the user.
     * 
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        if (role != null) {
            this.roles.remove(role);
        }
    }

    // Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
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

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(Instant accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Instant getPasswordExpiresAt() {
        return passwordExpiresAt;
    }

    public void setPasswordExpiresAt(Instant passwordExpiresAt) {
        this.passwordExpiresAt = passwordExpiresAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts != null ? accounts : new HashSet<>();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", email='" + SecurityUtil.maskSensitiveData(email, "email") + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status=" + status +
                ", lastLoginAt=" + lastLoginAt +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", accountLockedUntil=" + accountLockedUntil +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}