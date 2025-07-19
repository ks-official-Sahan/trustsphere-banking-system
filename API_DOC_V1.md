# TrustSphere Banking System - API Documentation v1.0

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Authentication & Security](#authentication--security)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Business Rules](#business-rules)
8. [Database Schema](#database-schema)
9. [Deployment](#deployment)
10. [Frontend Integration Guide](#frontend-integration-guide)
11. [Reporting & Analytics](#reporting--analytics)
12. [Security Considerations](#security-considerations)
13. [Performance & Scalability](#performance--scalability)
14. [Monitoring & Logging](#monitoring--logging)

## Overview

TrustSphere Banking System is a comprehensive enterprise-grade banking application built with Jakarta EE 10, featuring a multi-tier architecture with RESTful APIs, EJB services, and JPA persistence. The system provides secure banking operations including user management, account management, transaction processing, audit logging, and notifications.

### Key Features
- **Multi-role User Management**: Admin, Teller, User, Auditor roles
- **Account Management**: Create, view, and manage bank accounts
- **Transaction Processing**: Secure fund transfers with atomic operations
- **Audit Logging**: Comprehensive audit trail for compliance
- **Real-time Notifications**: System, security, and financial notifications
- **JWT Authentication**: Secure token-based authentication
- **Rate Limiting**: Protection against abuse
- **CORS Support**: Cross-origin resource sharing enabled

### Technology Stack
- **Backend**: Jakarta EE 10, EJB 4.0, JPA 3.0, JAX-RS 3.0
- **Database**: MySQL 8.0 with Hibernate ORM
- **Authentication**: JWT (JSON Web Tokens)
- **Application Server**: GlassFish/Payara Server
- **Build Tool**: Maven 3.x
- **Java Version**: 17

## Architecture

### Module Structure
```
trustsphere-parent/
├── trustsphere-core/          # JPA entities, DTOs, enums
├── trustsphere-ejb/           # Business logic, services, DAOs
├── trustsphere-rest/          # REST API endpoints
└── trustsphere-ear/           # Enterprise Archive deployment
```

### Layered Architecture
1. **Presentation Layer**: REST resources with JWT authentication
2. **Business Layer**: EJB services with transaction management
3. **Data Access Layer**: JPA DAOs with named queries
4. **Persistence Layer**: MySQL database with Hibernate

### Security Architecture
- **Authentication**: JWT-based with Bearer token
- **Authorization**: Role-based access control (RBAC)
- **Audit Trail**: Comprehensive logging of all operations
- **Data Validation**: Bean Validation with custom constraints
- **CORS**: Cross-origin resource sharing configuration

## Authentication & Security

### JWT Configuration
```properties
jwt.secret.key=${JWT_SECRET_KEY:your-256-bit-secret-key-here-must-be-long-enough}
jwt.issuer=trustsphere
jwt.expiration.seconds=3600
jwt.signature.algorithm=HS256
```

### Authentication Flow
1. Client sends credentials to `/auth/login`
2. Server validates credentials and returns JWT token
3. Client includes JWT in Authorization header: `Bearer <token>`
4. Server validates JWT on each request

### Role-Based Access Control
- **ROLE_ADMIN**: Full system access, user management
- **ROLE_TELLER**: Account operations, transaction processing
- **ROLE_USER**: Personal account access, transfers
- **ROLE_AUDITOR**: Audit log access, compliance reporting

### Security Headers
- CORS enabled for cross-origin requests
- CSRF protection disabled (JWT-based auth)
- Rate limiting implemented
- Input validation and sanitization

## API Endpoints

### Base URL
```
https://your-domain.com/api
```

### Authentication Endpoints
```
POST /auth/login          # User login (returns JWT)
POST /auth/refresh        # Refresh JWT token
POST /auth/logout         # User logout
```

### User Management
```
POST   /users                    # Create user (Admin only)
GET    /users/{id}              # Get user by ID
GET    /users                   # List active users
PUT    /users/{id}/status       # Update user status
```

### Account Management
```
POST   /accounts                # Create account (Teller/Admin)
GET    /accounts/{id}           # Get account by ID
GET    /accounts/user/{userId}  # List user accounts
PUT    /accounts/{id}/status    # Update account status
```

### Transaction Management
```
POST   /transactions/transfer   # Process fund transfer
GET    /transactions/{id}       # Get transaction details
GET    /transactions/user/{userId} # List user transactions
```

### Audit & Notifications
```
GET    /audit/logs              # Get audit logs (Auditor/Admin)
GET    /audit/logs/severity/{level} # Get logs by severity
GET    /notifications/user/{userId} # Get user notifications
```

## Data Models

### User Entity
```java
{
  "id": "uuid-string",
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE|SUSPENDED",
  "roles": ["ROLE_USER"],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Account Entity
```java
{
  "id": "uuid-string",
  "accountNumber": "ACC123456789",
  "balance": "1000.00",
  "status": "ACTIVE|FROZEN|CLOSED",
  "userId": "user-uuid",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Transaction Entity
```java
{
  "id": "uuid-string",
  "sourceAccountId": "account-uuid",
  "targetAccountId": "account-uuid",
  "amount": "100.00",
  "type": "TRANSFER|DEPOSIT|WITHDRAWAL",
  "status": "PENDING|COMPLETED|FAILED|CANCELLED",
  "timestamp": "2024-01-01T00:00:00Z",
  "description": "Transfer description",
  "referenceNumber": "TXN123456789"
}
```

### Audit Log Entity
```java
{
  "id": "uuid-string",
  "actorUserId": "user-uuid",
  "action": "TRANSFER",
  "resourceType": "TRANSACTION",
  "resourceId": "resource-uuid",
  "severityLevel": "INFO|WARN|CRITICAL",
  "details": "Audit details",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Notification Entity
```java
{
  "id": "uuid-string",
  "userId": "user-uuid",
  "type": "SYSTEM|SECURITY|FINANCIAL",
  "message": "Notification message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Error Handling

### Standard Error Response
```json
{
  "code": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": 1704067200000
}
```

### Common Error Codes
- `INVALID_USER_DATA`: User validation failed
- `USER_EXISTS`: User already exists
- `USER_NOT_FOUND`: User not found
- `INVALID_ACCOUNT_DATA`: Account validation failed
- `ACCOUNT_NOT_FOUND`: Account not found
- `INSUFFICIENT_FUNDS`: Insufficient funds for transaction
- `UNAUTHORIZED_ACCESS`: Access denied
- `VALIDATION_FAILED`: Input validation failed
- `TRANSACTION_FAILED`: Transaction processing failed

### HTTP Status Codes
- `200 OK`: Successful operation
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `409 Conflict`: Business rule violation
- `500 Internal Server Error`: Server error

## Business Rules

### User Management
- Email addresses must be unique
- Passwords must be hashed using secure algorithm
- Users can have multiple roles
- User status can be ACTIVE or SUSPENDED

### Account Management
- Account numbers must be unique (10-20 characters)
- Balance cannot be negative
- Account status transitions: ACTIVE → FROZEN → CLOSED
- Daily interest rate: 0.05% (18% annually)

### Transaction Processing
- Minimum transfer amount: $0.01
- Maximum transfer amount: $1,000,000.00
- Source and target accounts cannot be the same
- Transactions are atomic (all-or-nothing)
- Insufficient funds prevent transaction completion

### Security Rules
- JWT tokens expire after 1 hour
- Rate limiting prevents abuse
- All operations are audited
- Data isolation: users can only access their own data

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    hierarchy_level INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### User Roles Table
```sql
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Accounts Table
```sql
CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    balance DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    source_account_id VARCHAR(36) NOT NULL,
    target_account_id VARCHAR(36),
    amount DECIMAL(19,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    reference_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    FOREIGN KEY (target_account_id) REFERENCES accounts(id)
);
```

### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    actor_user_id VARCHAR(36) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    severity_level VARCHAR(20) NOT NULL,
    details VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Notifications Table
```sql
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    message VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Deployment

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- GlassFish/Payara Server 6.0+

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=trustsphere
DB_USER=trustsphere_user
DB_PASSWORD=secure_password

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-here-must-be-long-enough

# Application Configuration
APP_CONTEXT_ROOT=/api
APP_PORT=8080
```

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Deploy to application server
mvn glassfish:deploy
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

## Frontend Integration Guide

### Authentication Flow
```javascript
// Login
const login = async (email, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  if (response.ok) {
    const { token } = await response.json();
    localStorage.setItem('jwt_token', token);
    return token;
  }
  throw new Error('Login failed');
};

// API calls with JWT
const apiCall = async (url, options = {}) => {
  const token = localStorage.getItem('jwt_token');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers
    }
  });
  
  if (response.status === 401) {
    // Token expired, redirect to login
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
  }
  
  return response;
};
```

### User Management
```javascript
// Create user
const createUser = async (userData) => {
  const response = await apiCall('/api/users', {
    method: 'POST',
    body: JSON.stringify(userData)
  });
  return response.json();
};

// Get user
const getUser = async (userId) => {
  const response = await apiCall(`/api/users/${userId}`);
  return response.json();
};

// List users
const listUsers = async () => {
  const response = await apiCall('/api/users');
  return response.json();
};
```

### Account Management
```javascript
// Create account
const createAccount = async (accountData) => {
  const response = await apiCall('/api/accounts', {
    method: 'POST',
    body: JSON.stringify(accountData)
  });
  return response.json();
};

// Get account
const getAccount = async (accountId) => {
  const response = await apiCall(`/api/accounts/${accountId}`);
  return response.json();
};

// List user accounts
const listUserAccounts = async (userId) => {
  const response = await apiCall(`/api/accounts/user/${userId}`);
  return response.json();
};
```

### Transaction Processing
```javascript
// Process transfer
const transfer = async (sourceId, targetId, amount, description) => {
  const formData = new FormData();
  formData.append('srcId', sourceId);
  formData.append('tgtId', targetId);
  formData.append('amount', amount);
  formData.append('description', description);
  
  const response = await apiCall('/api/transactions/transfer', {
    method: 'POST',
    body: formData
  });
  return response.json();
};
```

### Error Handling
```javascript
const handleApiError = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'API request failed');
  }
  return response;
};

// Usage
try {
  const result = await apiCall('/api/users');
  const data = await result.json();
  // Handle success
} catch (error) {
  console.error('API Error:', error.message);
  // Handle error (show notification, etc.)
}
```

## Reporting & Analytics

### Available Reports
1. **User Activity Report**: User login patterns, account access
2. **Transaction Report**: Transaction volumes, amounts, types
3. **Account Balance Report**: Account balances, interest earned
4. **Audit Trail Report**: System activity, security events
5. **Compliance Report**: Regulatory compliance data

### Data Export Endpoints
```
GET /reports/users/activity?startDate=&endDate=&format=csv
GET /reports/transactions/summary?startDate=&endDate=&format=json
GET /reports/accounts/balance?date=&format=xlsx
GET /reports/audit/logs?severity=&startDate=&endDate=&format=csv
```

### Analytics Queries
```sql
-- Daily transaction volume
SELECT DATE(timestamp) as date, COUNT(*) as count, SUM(amount) as total
FROM transactions 
WHERE status = 'COMPLETED'
GROUP BY DATE(timestamp)
ORDER BY date DESC;

-- User account distribution
SELECT u.status, COUNT(a.id) as account_count
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id
GROUP BY u.status;

-- Transaction type distribution
SELECT type, COUNT(*) as count, AVG(amount) as avg_amount
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY type;
```

## Security Considerations

### Data Protection
- All sensitive data encrypted at rest
- Passwords hashed using secure algorithms
- JWT tokens signed with strong keys
- Database connections use SSL/TLS

### Access Control
- Role-based access control (RBAC)
- Principle of least privilege
- Data isolation between users
- Audit logging of all operations

### Input Validation
- Bean Validation annotations on all entities
- Custom validation for business rules
- SQL injection prevention via JPA
- XSS protection through input sanitization

### Monitoring & Alerting
- Failed authentication attempts logged
- Suspicious transaction patterns detected
- Rate limiting violations monitored
- System health metrics tracked

## Performance & Scalability

### Database Optimization
- Indexed foreign keys and frequently queried columns
- Named queries for common operations
- Batch processing for bulk operations
- Connection pooling configured

### Caching Strategy
- Second-level cache enabled for entities
- Query cache for frequently accessed data
- JWT token caching for performance
- Static resource caching

### Scalability Features
- Stateless EJB services
- Horizontal scaling support
- Load balancing ready
- Microservices architecture compatible

### Performance Monitoring
- Response time tracking
- Database query performance
- Memory usage monitoring
- CPU utilization tracking

## Monitoring & Logging

### Log Levels
- **INFO**: Normal operations, successful transactions
- **WARN**: Business rule violations, retry attempts
- **ERROR**: System errors, failed operations
- **DEBUG**: Detailed debugging information

### Log Categories
- **Security**: Authentication, authorization, audit events
- **Business**: Transactions, account operations
- **System**: Application startup, configuration
- **Performance**: Response times, database queries

### Monitoring Endpoints
```
GET /health                    # Application health check
GET /metrics                   # System metrics
GET /metrics/jvm              # JVM metrics
GET /metrics/database         # Database metrics
```

### Alerting Rules
- High error rate (>5% of requests)
- Slow response times (>2 seconds)
- Database connection failures
- Authentication failures
- Unusual transaction patterns

---

## Conclusion

This API documentation provides comprehensive information for developing frontend applications, generating reports, and maintaining the TrustSphere Banking System. The system is designed with security, scalability, and maintainability in mind, following enterprise-grade best practices.

For additional support or questions, please refer to the project's README.md file or contact the development team.

**Version**: 1.0  
**Last Updated**: July 2025  
**Compatibility**: Jakarta EE 10, Java 17, MySQL 8.0+