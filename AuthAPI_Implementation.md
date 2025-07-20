# AuthAPI Implementation with JAX-RS

This document describes the complete AuthAPI implementation using JAX-RS for the TrustSphere Banking System.

## Overview

The AuthAPI provides secure authentication endpoints for user login, token refresh, and logout operations. It follows a JWT-based authentication pattern with role-based access control.

## Architecture

### Layered Structure
```
trustsphere-rest/         # JAX-RS REST Layer
├── AuthResource          # REST endpoints
└── AuthenticationExceptionMapper # Exception handling

trustsphere-ejb/          # Business Logic Layer  
├── AuthServiceBean       # Authentication business logic
├── JWTService           # JWT token generation/validation
└── AuthServiceRemote    # EJB remote interface

trustsphere-core/         # Data Transfer Layer
├── LoginRequestDTO      # Login request payload
├── LoginResponseDTO     # Login response with tokens
├── RefreshTokenRequestDTO # Token refresh payload
└── PasswordHasher       # BCrypt password utilities
```

## API Endpoints

### Base URL: `/api/auth`

#### 1. User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "user-uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "status": "ACTIVE",
    "roleNames": ["ROLE_USER"]
  },
  "issuedAt": "2024-01-01T12:00:00Z"
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `401 Unauthorized`: User account inactive
- `400 Bad Request`: Validation errors

#### 2. Token Refresh
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):** Same as login response with new tokens

**Error Responses:**
- `401 Unauthorized`: Invalid refresh token
- `401 Unauthorized`: User no longer active

#### 3. User Logout
```http
POST /api/auth/logout
Authorization: Bearer <access-token>
```

**Response (200 OK):**
```json
{
  "message": "Logout successful"
}
```

#### 4. Health Check
```http
GET /api/auth/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "AuthAPI"
}
```

#### 5. User Validation (Internal)
```http
GET /api/auth/validate/{email}
```

**Response (200 OK):**
```json
{
  "email": "user@example.com",
  "active": true
}
```

## Security Features

### JWT Token Structure
- **Access Token**: Short-lived (1 hour), contains user info and roles
- **Refresh Token**: Long-lived (7 days), used to obtain new access tokens
- **Algorithm**: HMAC SHA-256 (HS256)
- **Issuer**: "trustsphere"

### Password Security
- **Hashing**: BCrypt with cost factor 12
- **Library**: jBCrypt (org.mindrot.jbcrypt)
- **Validation**: Minimum 8 characters, complexity requirements

### Rate Limiting
- Integrated with existing RateLimitFilter
- Special handling for auth endpoints
- Configurable limits per endpoint

## Implementation Details

### 1. DTOs (Data Transfer Objects)

#### LoginRequestDTO
```java
@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;

@NotBlank(message = "Password is required")
@Size(min = 8, max = 255)
private String password;
```

#### LoginResponseDTO
```java
private String accessToken;
private String refreshToken;
private String tokenType = "Bearer";
private Long expiresIn;
private UserDTO user;
private Instant issuedAt;
```

### 2. EJB Services

#### AuthServiceBean
- **@Stateless**: Stateless session bean
- **@TransactionAttribute**: Transactional operations
- **Dependencies**: UserDAO, JWTService
- **Methods**: login(), refreshToken(), logout(), isUserActiveByEmail()

#### JWTService
- **@Stateless**: Utility service for JWT operations
- **Methods**: generateAccessToken(), generateRefreshToken(), validateToken()
- **Configuration**: Hardcoded for demo (should be externalized)

### 3. JAX-RS Resource

#### AuthResource
- **@Path("/auth")**: Base path for authentication endpoints
- **@Produces/Consumes**: JSON media type
- **Error Handling**: Comprehensive exception handling
- **Security Headers**: Cache control, authentication headers

### 4. Exception Handling

#### AuthenticationException
- Extends BusinessException
- Contains error codes and messages
- Mapped to 401 Unauthorized responses

#### AuthenticationExceptionMapper
- **@Provider**: JAX-RS exception mapper
- Maps AuthenticationException to HTTP responses
- Includes WWW-Authenticate header

## Configuration

### Dependencies Added

#### trustsphere-core/pom.xml
```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

#### trustsphere-rest/pom.xml
Already includes JWT dependencies:
- io.jsonwebtoken:jjwt-api
- io.jsonwebtoken:jjwt-impl
- io.jsonwebtoken:jjwt-jackson

### Application Configuration
Updated `ApplicationConfig.java` to include:
- AuthResource
- AuthenticationExceptionMapper

## Security Considerations

### Production Recommendations

1. **JWT Secret Management**
   - Use environment variables or secure configuration
   - Rotate secrets regularly
   - Use RSA keys for better security

2. **Token Blacklisting**
   - Implement token revocation list
   - Store revoked tokens in Redis/database
   - Check blacklist in authentication filter

3. **Rate Limiting**
   - Implement stricter limits on auth endpoints
   - Use sliding window rate limiting
   - Monitor for brute force attacks

4. **Audit Logging**
   - Log all authentication attempts
   - Include IP addresses and user agents
   - Monitor for suspicious patterns

5. **Password Policies**
   - Enforce complex password requirements
   - Implement password history
   - Require regular password changes

## Testing

### Manual Testing with cURL

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

#### Token Refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

#### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access-token>"
```

## Integration with Existing System

### JWT Authentication Filter
The existing `JWTAuthenticationFilter` already expects these endpoints:
- Excludes `/auth/login` and `/auth/refresh` from authentication
- Validates JWT tokens from Authorization header
- Extracts user info and roles for authorization

### Rate Limiting
The existing `RateLimitFilter` includes special handling for auth endpoints with appropriate rate limits.

### CORS Support
The existing `CorsFilter` includes Authorization header in allowed headers for cross-origin requests.

## Error Codes

| Code | Description |
|------|-------------|
| INVALID_CREDENTIALS | Invalid email or password |
| USER_INACTIVE | User account is not active |
| USER_NOT_FOUND | User account not found |
| INVALID_REFRESH_TOKEN | Refresh token is invalid or expired |
| LOGIN_FAILED | System error during login |
| REFRESH_FAILED | System error during token refresh |
| AUTHENTICATION_ERROR | Generic authentication error |

## Monitoring and Metrics

### Recommended Metrics
- Login success/failure rates
- Token refresh frequency
- Authentication endpoint response times
- Failed authentication attempts by IP
- User session duration

### Log Levels
- **INFO**: Successful operations
- **WARN**: Authentication failures, invalid tokens
- **ERROR**: System errors, unexpected exceptions

## Future Enhancements

1. **Multi-Factor Authentication (MFA)**
2. **OAuth 2.0 / OpenID Connect support**
3. **Social login integration**
4. **Password reset functionality**
5. **Account lockout mechanisms**
6. **Session management**
7. **Device tracking**
8. **Geolocation-based security**

## Conclusion

The AuthAPI provides a robust, secure authentication system using industry-standard JWT tokens and BCrypt password hashing. It integrates seamlessly with the existing TrustSphere Banking System architecture and provides a foundation for future security enhancements.