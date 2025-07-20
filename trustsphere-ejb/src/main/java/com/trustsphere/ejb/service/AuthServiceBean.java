package com.trustsphere.ejb.service;

import com.trustsphere.core.dto.LoginRequestDTO;
import com.trustsphere.core.dto.LoginResponseDTO;
import com.trustsphere.core.dto.RefreshTokenRequestDTO;
import com.trustsphere.core.dto.UserDTO;
import com.trustsphere.core.entity.Role;
import com.trustsphere.core.entity.User;
import com.trustsphere.core.enums.UserStatus;
import com.trustsphere.core.util.PasswordHasher;
import com.trustsphere.ejb.dao.UserDAO;
import com.trustsphere.ejb.exception.AuthenticationException;
import com.trustsphere.ejb.remote.AuthServiceRemote;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AuthServiceBean implements AuthServiceRemote {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceBean.class);

    @EJB
    private UserDAO userDAO;

    @EJB
    private JWTService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) throws AuthenticationException {
        LOGGER.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            // Find user by email
            User user = userDAO.findByEmail(loginRequest.getEmail());
            if (user == null) {
                LOGGER.warn("Login failed - user not found: {}", loginRequest.getEmail());
                throw new AuthenticationException("INVALID_CREDENTIALS", "Invalid email or password");
            }

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                LOGGER.warn("Login failed - user not active: {} (status: {})", 
                           loginRequest.getEmail(), user.getStatus());
                throw new AuthenticationException("USER_INACTIVE", "User account is not active");
            }

            // Verify password
            if (!PasswordHasher.verifyPassword(loginRequest.getPassword(), user.getHashedPassword())) {
                LOGGER.warn("Login failed - invalid password for user: {}", loginRequest.getEmail());
                throw new AuthenticationException("INVALID_CREDENTIALS", "Invalid email or password");
            }

            // Extract roles
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

            // Create user DTO (without sensitive information)
            UserDTO userDTO = mapToUserDTO(user);

            LOGGER.info("Login successful for user: {}", loginRequest.getEmail());
            return new LoginResponseDTO(
                    accessToken,
                    refreshToken,
                    jwtService.getAccessTokenExpirationSeconds(),
                    userDTO
            );

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during login for user: {}", loginRequest.getEmail(), e);
            throw new AuthenticationException("LOGIN_FAILED", "Authentication failed due to system error");
        }
    }

    @Override
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) throws AuthenticationException {
        LOGGER.info("Token refresh attempt");

        try {
            String refreshToken = refreshRequest.getRefreshToken();

            // Validate refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                LOGGER.warn("Invalid refresh token - not a refresh token type");
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Invalid refresh token");
            }

            // Extract user information from refresh token
            String email = jwtService.getEmailFromToken(refreshToken);
            String userId = jwtService.getUserIdFromToken(refreshToken);

            // Verify user still exists and is active
            User user = userDAO.findById(userId);
            if (user == null) {
                LOGGER.warn("Refresh token failed - user not found: {}", userId);
                throw new AuthenticationException("USER_NOT_FOUND", "User not found");
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                LOGGER.warn("Refresh token failed - user not active: {} (status: {})", 
                           email, user.getStatus());
                throw new AuthenticationException("USER_INACTIVE", "User account is not active");
            }

            // Extract roles
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
            String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

            // Create user DTO
            UserDTO userDTO = mapToUserDTO(user);

            LOGGER.info("Token refresh successful for user: {}", email);
            return new LoginResponseDTO(
                    newAccessToken,
                    newRefreshToken,
                    jwtService.getAccessTokenExpirationSeconds(),
                    userDTO
            );

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during token refresh", e);
            throw new AuthenticationException("REFRESH_FAILED", "Token refresh failed due to system error");
        }
    }

    @Override
    public void logout(String refreshToken) {
        LOGGER.info("Logout request received");
        
        try {
            if (refreshToken != null) {
                String email = jwtService.getEmailFromToken(refreshToken);
                LOGGER.info("User logged out: {}", email);
            }
            
            // In a production system, you would typically:
            // 1. Add the token to a blacklist/revocation list
            // 2. Store revoked tokens in a cache or database
            // 3. The authentication filter would check this blacklist
            
            // For now, we just log the logout event
            LOGGER.info("Logout completed successfully");
            
        } catch (Exception e) {
            LOGGER.warn("Error processing logout: {}", e.getMessage());
            // Don't throw exception for logout - it should always succeed
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean isUserActiveByEmail(String email) {
        try {
            User user = userDAO.findByEmail(email);
            return user != null && user.getStatus() == UserStatus.ACTIVE;
        } catch (Exception e) {
            LOGGER.error("Error checking user status for email: {}", email, e);
            return false;
        }
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);
        
        return dto;
    }
}