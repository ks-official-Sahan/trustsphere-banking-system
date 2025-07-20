package com.trustsphere.ejb.remote;

import com.trustsphere.core.dto.LoginRequestDTO;
import com.trustsphere.core.dto.LoginResponseDTO;
import com.trustsphere.core.dto.RefreshTokenRequestDTO;
import com.trustsphere.ejb.exception.AuthenticationException;
import jakarta.ejb.Remote;

@Remote
public interface AuthServiceRemote {

    /**
     * Authenticate user with email and password
     * @param loginRequest the login credentials
     * @return LoginResponseDTO containing JWT tokens and user info
     * @throws AuthenticationException if authentication fails
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest) throws AuthenticationException;

    /**
     * Refresh JWT tokens using refresh token
     * @param refreshRequest the refresh token request
     * @return LoginResponseDTO containing new JWT tokens
     * @throws AuthenticationException if refresh token is invalid
     */
    LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) throws AuthenticationException;

    /**
     * Logout user by invalidating tokens
     * @param refreshToken the refresh token to invalidate
     */
    void logout(String refreshToken);

    /**
     * Validate if a user exists and is active
     * @param email the user email
     * @return true if user exists and is active
     */
    boolean isUserActiveByEmail(String email);
}