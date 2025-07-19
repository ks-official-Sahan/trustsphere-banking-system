# TrustSphere Banking System - API Documentation

## Overview

The TrustSphere Banking System provides a comprehensive REST API for banking operations with enterprise-grade security, audit capabilities, and compliance features.

**Base URL:** `https://api.trustsphere.com/v1`  
**Version:** 1.0.0  
**Contact:** support@trustsphere.com

## Authentication

The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Getting a JWT Token

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure-password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-here",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

## Rate Limiting

- **Per Minute:** 100 requests
- **Per Hour:** 1,000 requests
- **Headers:** `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After`

## Error Handling

All errors follow a consistent format:

```json
{
  "error": "error_code",
  "message": "Human-readable error message",
  "timestamp": "2025-01-26T10:30:00Z",
  "path": "/api/v1/transactions/transfer"
}
```

### Common HTTP Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request (validation error)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (business rule violation)
- `422` - Unprocessable Entity
- `429` - Too Many Requests (rate limit exceeded)
- `500` - Internal Server Error

## Endpoints

### Health Checks

#### GET /health
Get comprehensive system health information.

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-01-26T10:30:00Z",
  "uptime": 86400000,
  "version": "1.0.0",
  "system": {
    "memory": {
      "heapUsed": 1073741824,
      "heapMax": 2147483648,
      "nonHeapUsed": 268435456,
      "nonHeapMax": 536870912
    },
    "threads": {
      "active": 25,
      "peak": 30,
      "daemon": 20
    },
    "runtime": {
      "availableProcessors": 8,
      "freeMemory": 1073741824,
      "totalMemory": 2147483648,
      "maxMemory": 4294967296
    }
  },
  "database": {
    "status": "UP",
    "message": "Database connection is healthy",
    "timestamp": "2025-01-26T10:30:00Z"
  }
}
```

#### GET /health/liveness
Simple liveness check for load balancers.

#### GET /health/readiness
Readiness check including database connectivity.

### Authentication

#### POST /auth/login
Authenticate user and get JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "secure-password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-here",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "user": {
    "id": "user-123",
    "email": "user@example.com",
    "fullName": "John Doe",
    "roles": ["ROLE_USER"]
  }
}
```

#### POST /auth/refresh
Refresh JWT token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "refresh-token-here"
}
```

#### POST /auth/logout
Logout user and invalidate tokens.

### Users

#### GET /users
Get list of users (Admin only).

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `status` (optional): Filter by status (ACTIVE, INACTIVE, SUSPENDED)
- `role` (optional): Filter by role

**Response:**
```json
{
  "content": [
    {
      "id": "user-123",
      "email": "user@example.com",
      "fullName": "John Doe",
      "status": "ACTIVE",
      "roles": ["ROLE_USER"],
      "createdAt": "2025-01-26T10:30:00Z",
      "lastLoginAt": "2025-01-26T09:15:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

#### GET /users/{id}
Get user by ID.

**Response:**
```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-01-26T10:30:00Z",
  "lastLoginAt": "2025-01-26T09:15:00Z",
  "failedLoginAttempts": 0,
  "accountLockedUntil": null,
  "passwordExpiresAt": "2025-04-26T10:30:00Z"
}
```

#### POST /users
Create new user (Admin only).

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "fullName": "Jane Smith",
  "password": "SecurePassword123!",
  "roles": ["ROLE_USER"]
}
```

#### PUT /users/{id}
Update user (Admin or self).

**Request Body:**
```json
{
  "fullName": "Jane Smith Updated",
  "status": "ACTIVE"
}
```

#### DELETE /users/{id}
Delete user (Admin only).

### Accounts

#### GET /accounts
Get list of accounts.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `status` (optional): Filter by status (ACTIVE, FROZEN, CLOSED)
- `type` (optional): Filter by account type (SAVINGS, CHECKING, BUSINESS)
- `userId` (optional): Filter by user ID

**Response:**
```json
{
  "content": [
    {
      "id": "account-123",
      "accountNumber": "ACC123456789012",
      "balance": "10000.00",
      "status": "ACTIVE",
      "accountType": "SAVINGS",
      "interestRate": "0.025",
      "userId": "user-123",
      "createdAt": "2025-01-26T10:30:00Z",
      "lastTransactionAt": "2025-01-26T09:15:00Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

#### GET /accounts/{id}
Get account by ID.

**Response:**
```json
{
  "id": "account-123",
  "accountNumber": "ACC123456789012",
  "balance": "10000.00",
  "status": "ACTIVE",
  "accountType": "SAVINGS",
  "interestRate": "0.025",
  "dailyTransactionLimit": "10000.00",
  "dailyTransactionAmount": "500.00",
  "minimumBalance": "100.00",
  "overdraftLimit": "0.00",
  "userId": "user-123",
  "createdAt": "2025-01-26T10:30:00Z",
  "lastTransactionAt": "2025-01-26T09:15:00Z",
  "lastInterestCalculation": "2025-01-26T02:00:00Z"
}
```

#### POST /accounts
Create new account.

**Request Body:**
```json
{
  "userId": "user-123",
  "accountType": "SAVINGS",
  "initialBalance": "1000.00",
  "interestRate": "0.025",
  "dailyTransactionLimit": "10000.00"
}
```

#### PUT /accounts/{id}
Update account (limited fields).

**Request Body:**
```json
{
  "dailyTransactionLimit": "15000.00",
  "interestRate": "0.030"
}
```

### Transactions

#### GET /transactions
Get list of transactions.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `status` (optional): Filter by status (PENDING, COMPLETED, FAILED, CANCELLED)
- `type` (optional): Filter by type (TRANSFER, DEPOSIT, WITHDRAWAL)
- `accountId` (optional): Filter by account ID
- `userId` (optional): Filter by user ID
- `startDate` (optional): Filter from date (ISO 8601)
- `endDate` (optional): Filter to date (ISO 8601)
- `minAmount` (optional): Minimum amount
- `maxAmount` (optional): Maximum amount

**Response:**
```json
{
  "content": [
    {
      "id": "transaction-123",
      "sourceAccountId": "account-123",
      "targetAccountId": "account-456",
      "amount": "500.00",
      "type": "TRANSFER",
      "status": "COMPLETED",
      "description": "Transfer to savings account",
      "referenceNumber": "TXN123456789012",
      "timestamp": "2025-01-26T10:30:00Z",
      "processedAt": "2025-01-26T10:30:05Z",
      "feeAmount": "0.00",
      "currency": "USD"
    }
  ],
  "totalElements": 200,
  "totalPages": 10,
  "size": 20,
  "number": 0
}
```

#### GET /transactions/{id}
Get transaction by ID.

**Response:**
```json
{
  "id": "transaction-123",
  "sourceAccountId": "account-123",
  "targetAccountId": "account-456",
  "amount": "500.00",
  "type": "TRANSFER",
  "status": "COMPLETED",
  "description": "Transfer to savings account",
  "referenceNumber": "TXN123456789012",
  "timestamp": "2025-01-26T10:30:00Z",
  "processedAt": "2025-01-26T10:30:05Z",
  "feeAmount": "0.00",
  "currency": "USD",
  "exchangeRate": "1.000000",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "sessionId": "session-123"
}
```

#### POST /transactions/transfer
Create a transfer transaction.

**Request Body (Form Data):**
```
srcId=account-123&tgtId=account-456&amount=500.00&description=Transfer to savings
```

**Response:**
```json
{
  "id": "transaction-123",
  "sourceAccountId": "account-123",
  "targetAccountId": "account-456",
  "amount": "500.00",
  "type": "TRANSFER",
  "status": "COMPLETED",
  "timestamp": "2025-01-26T10:30:00Z",
  "referenceNumber": "TXN123456789012"
}
```

#### POST /transactions/deposit
Create a deposit transaction.

**Request Body (Form Data):**
```
accountId=account-123&amount=1000.00&description=Cash deposit
```

#### POST /transactions/withdrawal
Create a withdrawal transaction.

**Request Body (Form Data):**
```
accountId=account-123&amount=200.00&description=ATM withdrawal
```

### Audit Logs

#### GET /audit-logs
Get audit logs (Admin only).

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `action` (optional): Filter by action
- `resourceType` (optional): Filter by resource type
- `userId` (optional): Filter by user ID
- `startDate` (optional): Filter from date (ISO 8601)
- `endDate` (optional): Filter to date (ISO 8601)
- `severity` (optional): Filter by severity (INFO, WARNING, ERROR)

**Response:**
```json
{
  "content": [
    {
      "id": "audit-123",
      "actorUserId": "user-123",
      "action": "TRANSFER_SUCCESS",
      "resourceType": "TRANSACTION",
      "resourceId": "transaction-123",
      "severity": "INFO",
      "details": "Transfer: 500.00 from ACC123456789012 to ACC456789012345",
      "timestamp": "2025-01-26T10:30:00Z",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0..."
    }
  ],
  "totalElements": 1000,
  "totalPages": 50,
  "size": 20,
  "number": 0
}
```

### Notifications

#### GET /notifications
Get user notifications.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `type` (optional): Filter by type (EMAIL, SMS, PUSH)
- `status` (optional): Filter by status (PENDING, SENT, FAILED)
- `read` (optional): Filter by read status (true/false)

**Response:**
```json
{
  "content": [
    {
      "id": "notification-123",
      "userId": "user-123",
      "type": "EMAIL",
      "title": "Transaction Completed",
      "message": "Your transfer of $500.00 has been completed successfully.",
      "status": "SENT",
      "read": false,
      "createdAt": "2025-01-26T10:30:00Z",
      "sentAt": "2025-01-26T10:30:05Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

#### PUT /notifications/{id}/read
Mark notification as read.

#### DELETE /notifications/{id}
Delete notification.

## Data Models

### User
```json
{
  "id": "string",
  "email": "string",
  "fullName": "string",
  "status": "ACTIVE|INACTIVE|SUSPENDED",
  "roles": ["string"],
  "createdAt": "datetime",
  "lastLoginAt": "datetime",
  "failedLoginAttempts": "number",
  "accountLockedUntil": "datetime|null",
  "passwordExpiresAt": "datetime"
}
```

### Account
```json
{
  "id": "string",
  "accountNumber": "string",
  "balance": "decimal",
  "status": "ACTIVE|FROZEN|CLOSED",
  "accountType": "SAVINGS|CHECKING|BUSINESS",
  "interestRate": "decimal",
  "dailyTransactionLimit": "decimal",
  "dailyTransactionAmount": "decimal",
  "minimumBalance": "decimal",
  "overdraftLimit": "decimal",
  "userId": "string",
  "createdAt": "datetime",
  "lastTransactionAt": "datetime",
  "lastInterestCalculation": "datetime"
}
```

### Transaction
```json
{
  "id": "string",
  "sourceAccountId": "string",
  "targetAccountId": "string|null",
  "amount": "decimal",
  "type": "TRANSFER|DEPOSIT|WITHDRAWAL",
  "status": "PENDING|COMPLETED|FAILED|CANCELLED",
  "description": "string",
  "referenceNumber": "string",
  "timestamp": "datetime",
  "processedAt": "datetime|null",
  "feeAmount": "decimal",
  "currency": "string",
  "exchangeRate": "decimal",
  "ipAddress": "string",
  "userAgent": "string",
  "sessionId": "string"
}
```

### AuditLog
```json
{
  "id": "string",
  "actorUserId": "string",
  "action": "string",
  "resourceType": "string",
  "resourceId": "string",
  "severity": "INFO|WARNING|ERROR",
  "details": "string",
  "timestamp": "datetime",
  "ipAddress": "string",
  "userAgent": "string"
}
```

### Notification
```json
{
  "id": "string",
  "userId": "string",
  "type": "EMAIL|SMS|PUSH",
  "title": "string",
  "message": "string",
  "status": "PENDING|SENT|FAILED",
  "read": "boolean",
  "createdAt": "datetime",
  "sentAt": "datetime|null"
}
```

## Security

### CSRF Protection
For state-changing operations (POST, PUT, DELETE), include a CSRF token:

```
X-CSRF-Token: <csrf-token>
```

### Input Validation
All inputs are validated and sanitized to prevent injection attacks.

### Rate Limiting
Requests are rate-limited per IP address to prevent abuse.

### Audit Logging
All operations are logged for compliance and security monitoring.

## Compliance

### GDPR Compliance
- Data retention policies
- Right to be forgotten
- Data portability
- Consent management

### Financial Compliance
- Transaction monitoring
- Suspicious activity reporting
- Audit trail maintenance
- Regulatory reporting

## SDKs and Libraries

### JavaScript/TypeScript
```bash
npm install @trustsphere/banking-api
```

```javascript
import { TrustSphereAPI } from '@trustsphere/banking-api';

const api = new TrustSphereAPI({
  baseURL: 'https://api.trustsphere.com/v1',
  token: 'your-jwt-token'
});

// Get accounts
const accounts = await api.accounts.list();

// Create transfer
const transfer = await api.transactions.transfer({
  srcId: 'account-123',
  tgtId: 'account-456',
  amount: 500.00,
  description: 'Transfer to savings'
});
```

### Python
```bash
pip install trustsphere-banking-api
```

```python
from trustsphere_banking_api import TrustSphereAPI

api = TrustSphereAPI(
    base_url='https://api.trustsphere.com/v1',
    token='your-jwt-token'
)

# Get accounts
accounts = api.accounts.list()

# Create transfer
transfer = api.transactions.transfer(
    src_id='account-123',
    tgt_id='account-456',
    amount=500.00,
    description='Transfer to savings'
)
```

## Support

For API support and questions:
- **Email:** api-support@trustsphere.com
- **Documentation:** https://docs.trustsphere.com/api
- **Status Page:** https://status.trustsphere.com

## Changelog

### v1.0.0 (2025-01-26)
- Initial API release
- User management
- Account management
- Transaction processing
- Audit logging
- Notification system
- Health checks
- Security features (JWT, CSRF, Rate limiting)