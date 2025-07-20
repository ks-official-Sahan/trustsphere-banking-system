package com.trustsphere.rest.resource;

import com.trustsphere.core.dto.UserDTO;
import com.trustsphere.core.entity.User;
import com.trustsphere.core.util.PasswordHasher;
import com.trustsphere.ejb.dao.UserDAO;
import com.trustsphere.rest.model.AuthRequest;
import com.trustsphere.rest.model.AuthResponse;
import com.trustsphere.rest.model.ErrorResponse;
import com.trustsphere.rest.security.JWTConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

    @EJB
    private UserDAO userDAO;

    @Inject
    private JWTConfiguration jwtConfig;

    @POST
    @Path("/login")
    public Response login(@Valid AuthRequest request) {
        try {
            if (request.getEmail() == null || request.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_REQUEST", "Email and password are required"))
                        .build();
            }
            User user = userDAO.findByEmail(request.getEmail());
            if (user == null || user.getStatus() == null || !user.getStatus().name().equals("ACTIVE")) {
                LOGGER.warn("Login failed: user not found or inactive [{}]", request.getEmail());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"))
                        .build();
            }
            if (!PasswordHasher.verifyPassword(request.getPassword(), user.getHashedPassword())) {
                LOGGER.warn("Login failed: invalid password [{}]", request.getEmail());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"))
                        .build();
            }
            // Build JWT
            String issuer = jwtConfig.getIssuer();
            long expiration = jwtConfig.getTokenExpirationSeconds();
            Instant now = Instant.now();
            Key signingKey = jwtConfig.getSecretKey() != null && !jwtConfig.getSecretKey().isEmpty()
                    ? Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes())
                    : jwtConfig.getPublicKey();
            Set<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
            String token = Jwts.builder()
                    .setSubject(user.getEmail())
                    .setIssuer(issuer)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(expiration)))
                    .claim("roles", roles)
                    .signWith(signingKey, jwtConfig.isRSASignature() ? SignatureAlgorithm.RS256 : SignatureAlgorithm.HS256)
                    .compact();
            // Build response
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setFullName(user.getFullName());
            userDTO.setStatus(user.getStatus());
            userDTO.setRoleNames(roles);
            AuthResponse authResponse = new AuthResponse(token, userDTO);
            return Response.ok(authResponse).build();
        } catch (Exception e) {
            LOGGER.error("Login error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("LOGIN_FAILED", "Internal server error"))
                    .build();
        }
    }
}