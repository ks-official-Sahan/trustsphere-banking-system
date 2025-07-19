# TrustSphere Banking System - API Documentation

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Error Handling](#error-handling)
5. [API Endpoints](#api-endpoints)
6. [Data Models](#data-models)
7. [Security](#security)
8. [Deployment](#deployment)
9. [Frontend Integration](#frontend-integration)

## Overview

TrustSphere Banking System is a comprehensive banking platform built with Jakarta EE 10, providing secure and scalable banking operations. The system supports user management, account operations, transaction processing, and comprehensive audit logging.

### Key Features

- **User Management**: Secure user registration and authentication
- **Account Management**: Multi-account support with balance tracking
- **Transaction Processing**: Secure fund transfers with validation
- **Audit Logging**: Comprehensive activity tracking
- **Role-Based Access Control**: Granular permissions
- **Real-time Notifications**: Email and SMS notifications
- **Health Monitoring**: System health and performance monitoring

### Technology Stack

- **Backend**: Jakarta EE 10, EJB 4.0, JPA 3.2
- **Database**: MySQL 8.0 with HikariCP connection pooling
- **Security**: JWT authentication, BCrypt password hashing
- **Caching**: Hibernate second-level cache with JCache
- **Monitoring**: Health checks, metrics, and audit logging

## Authentication

The API uses JWT (JSON Web Token) for authentication. All protected endpoints require a valid JWT token in the Authorization header.

### Authentication Flow

1. User provides credentials
2. System validates credentials and returns JWT token
3. Client includes JWT token in subsequent requests
4. Server validates JWT token for each request

### JWT Token Format

```
Authorization: Bearer <jwt_token>
```

### JWT Claims

- `sub`: User ID
- `iss`: Issuer (trustsphere)
- `exp`: Expiration time
- `roles`: User roles (array)

## Base URL

```
Production: https://api.trustsphere.com/api
Development: http://localhost:8080/api
```

## Error Handling

The API returns consistent error responses with the following structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2024-01-01T12:00:00Z",
  "path": "/api/accounts/123"
}
```

### Common HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict
- `422 Unprocessable Entity`: Validation error
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

## API Endpoints

### User Management

#### POST /users

Create a new user account.

**Request Body:**

```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "password": "SecurePassword123!",
  "status": "ACTIVE"
}
```

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z",
  "roleNames": ["ROLE_USER"]
}
```

#### GET /users/{id}

Get user by ID.

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z",
  "roleNames": ["ROLE_USER"]
}
```

#### PUT /users/{id}

Update user information.

#### DELETE /users/{id}

Deactivate user account.

### Account Management

#### POST /accounts

Create a new bank account.

**Request Body:**

```json
{
  "accountNumber": "1234567890",
  "balance": "1000.00",
  "status": "ACTIVE",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "1234567890",
  "balance": "1000.00",
  "status": "ACTIVE",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

#### GET /accounts/{id}

Get account by ID.

#### GET /accounts/user/{userId}

Get all active accounts for a user.

**Response:**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "accountNumber": "1234567890",
    "balance": "1000.00",
    "status": "ACTIVE",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "createdAt": "2024-01-01T12:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z"
  }
]
```

#### PUT /accounts/{id}/status

Update account status.

**Query Parameters:**

- `status`: ACTIVE, FROZEN, CLOSED
- `reason`: Reason for status change

### Transaction Management

#### POST /transactions/transfer

Transfer funds between accounts.

**Request Body:**

```json
{
  "sourceAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "targetAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": "100.00",
  "description": "Payment for services"
}
```

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "sourceAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "targetAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": "100.00",
  "type": "TRANSFER",
  "status": "COMPLETED",
  "timestamp": "2024-01-01T12:00:00Z",
  "referenceNumber": "TXN123456789"
}
```

#### GET /transactions/account/{accountId}

Get transaction history for an account.

**Query Parameters:**

- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `fromDate`: Start date (ISO format)
- `toDate`: End date (ISO format)

**Response:**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440003",
      "sourceAccountId": "550e8400-e29b-41d4-a716-446655440001",
      "targetAccountId": "550e8400-e29b-41d4-a716-446655440002",
      "amount": "100.00",
      "type": "TRANSFER",
      "status": "COMPLETED",
      "timestamp": "2024-01-01T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

#### GET /transactions/{id}

Get transaction by ID.

### Audit Logs

#### GET /audit-logs

Get audit logs with filtering.

**Query Parameters:**

- `userId`: Filter by user ID
- `action`: Filter by action type
- `resourceType`: Filter by resource type
- `fromDate`: Start date (ISO format)
- `toDate`: End date (ISO format)
- `severity`: Filter by severity level
- `page`: Page number
- `size`: Page size

**Response:**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440004",
      "actorUserId": "550e8400-e29b-41d4-a716-446655440000",
      "action": "TRANSFER",
      "resourceType": "TRANSACTION",
      "resourceId": "550e8400-e29b-41d4-a716-446655440003",
      "severityLevel": "INFO",
      "details": "Transfer: 100.00 from account 1234567890 to account 0987654321",
      "timestamp": "2024-01-01T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### Notifications

#### GET /notifications/user/{userId}

Get notifications for a user.

**Query Parameters:**

- `type`: Filter by notification type
- `read`: Filter by read status
- `page`: Page number
- `size`: Page size

**Response:**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440005",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "type": "TRANSACTION_COMPLETED",
    "title": "Transaction Completed",
    "message": "Your transfer of $100.00 has been completed successfully.",
    "read": false,
    "createdAt": "2024-01-01T12:00:00Z"
  }
]
```

#### PUT /notifications/{id}/read

Mark notification as read.

## Data Models

### User

```json
{
  "id": "string (UUID)",
  "email": "string (email format)",
  "fullName": "string (2-100 characters)",
  "status": "ACTIVE | SUSPENDED",
  "createdAt": "datetime (ISO 8601)",
  "updatedAt": "datetime (ISO 8601)",
  "roleNames": ["string array"]
}
```

### Account

```json
{
  "id": "string (UUID)",
  "accountNumber": "string (10-20 digits)",
  "balance": "decimal (precision: 19, scale: 2)",
  "status": "ACTIVE | FROZEN | CLOSED",
  "userId": "string (UUID)",
  "createdAt": "datetime (ISO 8601)",
  "updatedAt": "datetime (ISO 8601)"
}
```

### Transaction

```json
{
  "id": "string (UUID)",
  "sourceAccountId": "string (UUID)",
  "targetAccountId": "string (UUID, optional)",
  "amount": "decimal (precision: 19, scale: 2, min: 0.01)",
  "type": "DEPOSIT | WITHDRAWAL | TRANSFER",
  "status": "PENDING | COMPLETED | FAILED | CANCELLED",
  "timestamp": "datetime (ISO 8601)",
  "description": "string (optional)",
  "referenceNumber": "string (optional)"
}
```

### AuditLog

```json
{
  "id": "string (UUID)",
  "actorUserId": "string (UUID)",
  "action": "string",
  "resourceType": "string",
  "resourceId": "string (UUID)",
  "severityLevel": "INFO | WARNING | ERROR | CRITICAL",
  "details": "string",
  "timestamp": "datetime (ISO 8601)"
}
```

### Notification

```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "type": "TRANSACTION_COMPLETED | ACCOUNT_UPDATE | SECURITY_ALERT | SYSTEM_MAINTENANCE",
  "title": "string",
  "message": "string",
  "read": "boolean",
  "createdAt": "datetime (ISO 8601)"
}
```

## Security

### Authentication

- JWT-based authentication
- Token expiration: 1 hour (configurable)
- Secure token storage and transmission

### Authorization

- Role-based access control (RBAC)
- Granular permissions per endpoint
- User data isolation

### Data Protection

- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CSRF protection (configurable)

### Password Security

- BCrypt password hashing (cost: 12)
- Minimum password requirements
- Account lockout after failed attempts

## Deployment

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- GlassFish 7.0 or higher
- Maven 3.8 or higher

### Environment Variables

```bash
# Database Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/trustsphere
DATABASE_USERNAME=trustsphere_user
DATABASE_PASSWORD=secure_password

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-here

# Application Configuration
APP_ENVIRONMENT=production

# SMTP Configuration (optional)
SMTP_HOST=smtp.gmail.com
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

### Build and Deploy

```bash
# Build the project
mvn clean package

# Deploy to GlassFish
asadmin deploy trustsphere-ear/target/trustsphere-ear.ear
```

### Database Setup

```sql
-- Create database
CREATE DATABASE trustsphere CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'trustsphere_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON trustsphere.* TO 'trustsphere_user'@'localhost';
FLUSH PRIVILEGES;
```

## Frontend Integration

### Authentication Flow

1. **Login**: POST to `/auth/login` with credentials
2. **Store Token**: Save JWT token securely (localStorage/sessionStorage)
3. **Include Token**: Add `Authorization: Bearer <token>` header to requests
4. **Refresh Token**: Use refresh endpoint when token expires
5. **Logout**: Clear stored token

### Example Frontend Code (JavaScript)

#### Login

```javascript
async function login(email, password) {
  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem("jwt_token", data.token);
    return data;
  } else {
    throw new Error("Login failed");
  }
}
```

#### Authenticated Request

```javascript
async function getAccounts() {
  const token = localStorage.getItem("jwt_token");
  const response = await fetch("/api/accounts/user/current", {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  if (response.ok) {
    return await response.json();
  } else {
    throw new Error("Failed to fetch accounts");
  }
}
```

#### Transfer Funds

```javascript
async function transferFunds(
  sourceAccountId,
  targetAccountId,
  amount,
  description
) {
  const token = localStorage.getItem("jwt_token");
  const response = await fetch("/api/transactions/transfer", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      sourceAccountId,
      targetAccountId,
      amount,
      description,
    }),
  });

  if (response.ok) {
    return await response.json();
  } else {
    const error = await response.json();
    throw new Error(error.message);
  }
}
```

### Error Handling

```javascript
function handleApiError(error) {
  if (error.status === 401) {
    // Token expired or invalid
    localStorage.removeItem("jwt_token");
    window.location.href = "/login";
  } else if (error.status === 429) {
    // Rate limit exceeded
    alert("Too many requests. Please try again later.");
  } else {
    // Other errors
    console.error("API Error:", error);
    alert("An error occurred. Please try again.");
  }
}
```

### Real-time Updates

For real-time updates, consider implementing WebSocket connections or Server-Sent Events (SSE) for:

- Transaction notifications
- Account balance updates
- System maintenance alerts

### Security Best Practices

1. **HTTPS Only**: Always use HTTPS in production
2. **Token Storage**: Store tokens securely (httpOnly cookies preferred)
3. **Input Validation**: Validate all user inputs
4. **Error Handling**: Don't expose sensitive information in errors
5. **Rate Limiting**: Implement client-side rate limiting
6. **Logout**: Clear all stored data on logout

## Support and Documentation

For additional support and documentation:

- **API Documentation**: Available at `/api/swagger-ui`
- **OpenAPI Spec**: Available at `/api/openapi.json`
- **Health Checks**: Monitor system health at `/api/health`
- **Logs**: Check application logs for debugging

---

**Version**: 1.0.0  
**Last Updated**: July 2025  
**Contact**: support@trustsphere.com
