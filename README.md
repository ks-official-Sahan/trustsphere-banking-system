# TrustSphere Banking System

A comprehensive, enterprise-grade banking system built with Java EE / Jakarta EE 10, featuring secure transaction processing, audit logging, and compliance monitoring.

## 🏗️ Project Overview

TrustSphere Banking System is a modern, scalable banking platform designed for financial institutions requiring robust security, comprehensive audit trails, and regulatory compliance. The system provides a complete solution for user management, account operations, transaction processing, and financial reporting.

## ✨ Key Features

### 🔐 Security & Authentication
- **JWT-based Authentication** with role-based access control
- **Bcrypt Password Hashing** for secure credential storage
- **Rate Limiting** to prevent API abuse
- **CSRF Protection** for state-changing operations
- **Input Sanitization** to prevent injection attacks
- **Account Lockout** after failed login attempts
- **Password Expiration** policies

### 💰 Banking Operations
- **Multi-Account Support** (Savings, Checking, Business)
- **Real-time Transaction Processing** with validation
- **Interest Calculation** for savings accounts
- **Daily Transaction Limits** with automatic reset
- **Overdraft Protection** with configurable limits
- **Transaction Fees** and currency support

### 📊 Audit & Compliance
- **Comprehensive Audit Logging** of all system activities
- **GDPR Compliance** features
- **Financial Regulatory Compliance** (FCA, SOX, PCI DSS)
- **Data Retention Policies** (7-year retention)
- **Suspicious Activity Monitoring**
- **Compliance Reporting** capabilities

### 🔔 Notifications & Alerts
- **Multi-channel Notifications** (Email, SMS, Push)
- **Real-time Transaction Alerts**
- **Security Notifications** for suspicious activities
- **System Maintenance Notifications**

### 📈 Monitoring & Health
- **Health Check Endpoints** for load balancers
- **Performance Monitoring** with metrics
- **Database Connection Pooling** (HikariCP)
- **Caching Layer** (Ehcache)
- **Comprehensive Logging** (SLF4J)

## 🏛️ Architecture

### Technology Stack
- **Backend:** Java EE / Jakarta EE 10
- **ORM:** Hibernate 6.x with JPA
- **Database:** MySQL 8.0+
- **Application Server:** GlassFish 7.0+
- **Build Tool:** Maven 3.8+
- **Security:** JWT, bcrypt, Bean Validation
- **Messaging:** JMS for asynchronous processing
- **Caching:** Ehcache for performance optimization

### Project Structure
```
TrustSphere Banking System/
├── pom.xml                          # Parent POM with dependency management
├── trustsphere-core/                # Core entities, DTOs, and utilities
├── trustsphere-ejb/                 # Business logic and services
├── trustsphere-rest/                # REST API endpoints
├── trustsphere-ear/                 # Enterprise Archive for deployment
├── API_DOCUMENTATION.md             # Comprehensive API documentation
├── DATABASE_SCHEMA.md               # Database schema documentation
└── README.md                        # This file
```

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- GlassFish 7.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/trustsphere/banking-system.git
   cd banking-system
   ```

2. **Configure database**
   ```sql
   CREATE DATABASE trustsphere;
   CREATE USER 'trustsphere'@'localhost' IDENTIFIED BY 'secure-password';
   GRANT ALL PRIVILEGES ON trustsphere.* TO 'trustsphere'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure environment variables**
   ```bash
   export JWT_SECRET_KEY="your-256-bit-secret-key-here"
   export DB_PASSWORD="secure-password"
   export DB_URL="jdbc:mysql://localhost:3306/trustsphere"
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Deploy to GlassFish**
   ```bash
   # Copy the EAR file to GlassFish autodeploy directory
   cp trustsphere-ear/target/trustsphere-ear.ear $GLASSFISH_HOME/domains/domain1/autodeploy/
   ```

### Configuration

The system uses a comprehensive configuration approach:

- **Parent POM:** Centralized dependency management and build configuration
- **Application Properties:** Environment-specific settings
- **Persistence XML:** Database and Hibernate configuration
- **Environment Variables:** Sensitive configuration (passwords, keys)

## 📚 API Documentation

The system provides a comprehensive REST API with the following endpoints:

### Authentication
- `POST /auth/login` - User authentication
- `POST /auth/refresh` - Token refresh
- `POST /auth/logout` - User logout

### Users
- `GET /users` - List users (Admin)
- `GET /users/{id}` - Get user details
- `POST /users` - Create user (Admin)
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user (Admin)

### Accounts
- `GET /accounts` - List accounts
- `GET /accounts/{id}` - Get account details
- `POST /accounts` - Create account
- `PUT /accounts/{id}` - Update account

### Transactions
- `GET /transactions` - List transactions
- `GET /transactions/{id}` - Get transaction details
- `POST /transactions/transfer` - Create transfer
- `POST /transactions/deposit` - Create deposit
- `POST /transactions/withdrawal` - Create withdrawal

### Audit & Notifications
- `GET /audit-logs` - Get audit logs (Admin)
- `GET /notifications` - Get user notifications
- `PUT /notifications/{id}/read` - Mark notification as read

### Health Checks
- `GET /health` - Comprehensive health check
- `GET /health/liveness` - Simple liveness check
- `GET /health/readiness` - Readiness check

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

## 🗄️ Database Schema

The system uses a well-designed relational database with the following key tables:

- **users** - User accounts and authentication
- **roles** - System roles and permissions
- **user_roles** - User-role relationships
- **accounts** - Bank accounts and balances
- **transactions** - Financial transactions
- **audit_logs** - Comprehensive audit trail
- **notifications** - User notifications

For detailed database documentation, see [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md).

## 🔧 Development

### Code Quality
The project follows strict code quality standards:

- **Static Analysis:** SpotBugs for bug detection
- **Code Style:** Checkstyle for consistent formatting
- **Testing:** Comprehensive unit and integration tests
- **Documentation:** Javadoc for all public APIs

### Security Best Practices
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CSRF protection
- Rate limiting
- Secure password handling
- Audit logging

### Performance Optimization
- Connection pooling (HikariCP)
- Query optimization
- Caching (Ehcache)
- Batch processing
- Index optimization

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Security Tests
```bash
mvn spotbugs:check
mvn checkstyle:check
```

## 📊 Monitoring

### Health Checks
The system provides comprehensive health monitoring:

```bash
# Basic health check
curl http://localhost:8080/trustsphere-rest/api/health

# Detailed health information
curl http://localhost:8080/trustsphere-rest/api/health -H "Authorization: Bearer <token>"
```

### Metrics
- Database connection pool status
- Transaction processing metrics
- Error rates and response times
- System resource utilization

### Logging
- Application logs with structured logging
- Audit logs for compliance
- Security event logging
- Performance monitoring logs

## 🔒 Security Features

### Authentication & Authorization
- JWT-based stateless authentication
- Role-based access control (RBAC)
- Session management
- Password policies and expiration

### Data Protection
- Encryption at rest for sensitive data
- TLS 1.3 for data in transit
- Data masking for sensitive fields
- Secure key management

### Compliance
- GDPR compliance features
- Data retention policies
- Right to be forgotten
- Audit trail maintenance

## 🚀 Deployment

### Production Deployment

1. **Database Setup**
   ```sql
   CREATE DATABASE trustsphere_prod;
   CREATE USER 'trustsphere_prod'@'%' IDENTIFIED BY 'very-secure-password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON trustsphere_prod.* TO 'trustsphere_prod'@'%';
   ```

2. **Application Server Configuration**
   ```bash
   # Configure GlassFish domain
   asadmin create-domain --user admin trustsphere_prod
   asadmin start-domain trustsphere_prod
   
   # Configure JVM options
   asadmin set server.jvm-options="-Xms2g -Xmx4g -XX:+UseG1GC"
   ```

3. **Load Balancer Configuration (Nginx)**
   ```nginx
   upstream trustsphere_backend {
       server app1.trustsphere.com:8080;
       server app2.trustsphere.com:8080;
       server app3.trustsphere.com:8080;
   }
   
   server {
       listen 443 ssl http2;
       server_name api.trustsphere.com;
       
       location / {
           proxy_pass http://trustsphere_backend;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

### Environment Variables
```bash
# Required environment variables
JWT_SECRET_KEY=your-256-bit-secret-key
DB_PASSWORD=secure-database-password
DB_URL=jdbc:mysql://localhost:3306/trustsphere

# Optional environment variables
LOG_LEVEL=INFO
CACHE_TTL=3600
RATE_LIMIT_PER_MINUTE=100
```

## 📈 Performance

### Optimization Features
- **Connection Pooling:** HikariCP for efficient database connections
- **Caching:** Ehcache for frequently accessed data
- **Batch Processing:** Optimized for bulk operations
- **Query Optimization:** Proper indexing and query tuning
- **Asynchronous Processing:** JMS for non-blocking operations

### Scalability
- **Horizontal Scaling:** Stateless design supports multiple instances
- **Database Sharding:** Prepared for future scaling
- **Load Balancing:** Ready for load balancer integration
- **Caching Strategy:** Multi-level caching approach

## 🔄 Maintenance

### Backup Strategy
- Daily full database backups
- Point-in-time recovery capability
- Automated backup rotation
- Off-site backup storage

### Monitoring
- Real-time system monitoring
- Automated alerting
- Performance metrics collection
- Error tracking and reporting

### Updates
- Zero-downtime deployment capability
- Database migration scripts
- Backward compatibility maintenance
- Automated testing for updates

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Standards
- Follow Java coding conventions
- Add Javadoc for public methods
- Write unit tests for new features
- Update documentation as needed

## 📞 Support

### Documentation
- [API Documentation](API_DOCUMENTATION.md)
- [Database Schema](DATABASE_SCHEMA.md)
- [Deployment Guide](DEPLOYMENT.md)

### Contact
- **Email:** support@trustsphere.com
- **Documentation:** https://docs.trustsphere.com
- **Status Page:** https://status.trustsphere.com

### Emergency Support
- **24/7 Hotline:** +44 20 1234 5678
- **Emergency Email:** emergency@trustsphere.com

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 🙏 Acknowledgments

- Java EE / Jakarta EE community
- Hibernate ORM team
- MySQL development team
- Open source contributors

---

**TrustSphere Banking System** - Building the future of secure banking technology.

*Last updated: January 26, 2025*