# TrustSphere Banking System - Database Schema Documentation

## Overview

The TrustSphere Banking System uses MySQL 8.0+ as the primary database with Hibernate ORM for object-relational mapping. The schema is designed for high performance, security, and compliance with financial regulations.

**Database Engine:** MySQL 8.0+  
**Character Set:** utf8mb4  
**Collation:** utf8mb4_unicode_ci  
**Connection Pool:** HikariCP  
**ORM Framework:** Hibernate 6.x

## Database Configuration

### Connection Properties
```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/trustsphere?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=trustsphere
db.password=${DB_PASSWORD:secure-password}
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool (HikariCP)
db.pool.initialSize=10
db.pool.maxSize=50
db.pool.minIdle=5
db.pool.maxLifetime=1800000
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.leakDetectionThreshold=60000

# Hibernate Configuration
hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
hibernate.hbm2ddl.auto=validate
hibernate.show_sql=false
hibernate.format_sql=true
hibernate.use_sql_comments=true
hibernate.jdbc.batch_size=25
hibernate.order_inserts=true
hibernate.order_updates=true
hibernate.jdbc.batch_versioned_data=true
```

## Schema Tables

### 1. Users Table

**Table Name:** `users`  
**Description:** Stores user account information and authentication details.

```sql
CREATE TABLE users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    password_expires_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    INDEX idx_users_email (email),
    INDEX idx_users_status (status),
    INDEX idx_users_created_at (created_at),
    INDEX idx_users_last_login (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Columns:**
- `id`: Primary key (UUID)
- `email`: User's email address (unique)
- `password_hash`: Bcrypt hashed password
- `full_name`: User's full name
- `status`: Account status (ACTIVE, INACTIVE, SUSPENDED)
- `failed_login_attempts`: Count of failed login attempts
- `account_locked_until`: Timestamp when account lock expires
- `password_expires_at`: Password expiration timestamp
- `last_login_at`: Last successful login timestamp
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp
- `created_by`: User who created the record
- `updated_by`: User who last updated the record
- `version`: Optimistic locking version

### 2. Roles Table

**Table Name:** `roles`  
**Description:** Stores user roles and permissions.

```sql
CREATE TABLE roles (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. User Roles Table

**Table Name:** `user_roles`  
**Description:** Many-to-many relationship between users and roles.

```sql
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    
    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. Accounts Table

**Table Name:** `accounts`  
**Description:** Stores bank account information and balances.

```sql
CREATE TABLE accounts (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    account_type ENUM('SAVINGS', 'CHECKING', 'BUSINESS') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'FROZEN', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    interest_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    daily_transaction_limit DECIMAL(15,2) NOT NULL DEFAULT 10000.00,
    daily_transaction_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    overdraft_limit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_transaction_at TIMESTAMP NULL,
    last_interest_calculation TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_accounts_user_id (user_id),
    INDEX idx_accounts_account_number (account_number),
    INDEX idx_accounts_status (status),
    INDEX idx_accounts_type (account_type),
    INDEX idx_accounts_created_at (created_at),
    INDEX idx_accounts_last_transaction (last_transaction_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Columns:**
- `id`: Primary key (UUID)
- `account_number`: Unique account number
- `user_id`: Foreign key to users table
- `account_type`: Type of account (SAVINGS, CHECKING, BUSINESS)
- `balance`: Current account balance
- `status`: Account status (ACTIVE, FROZEN, CLOSED)
- `interest_rate`: Annual interest rate
- `daily_transaction_limit`: Daily transaction limit
- `daily_transaction_amount`: Current day's transaction amount
- `minimum_balance`: Minimum required balance
- `overdraft_limit`: Overdraft limit
- `last_transaction_at`: Last transaction timestamp
- `last_interest_calculation`: Last interest calculation timestamp
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp
- `created_by`: User who created the record
- `updated_by`: User who last updated the record
- `version`: Optimistic locking version

### 5. Transactions Table

**Table Name:** `transactions`  
**Description:** Stores all financial transactions.

```sql
CREATE TABLE transactions (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    source_account_id VARCHAR(36) NULL,
    target_account_id VARCHAR(36) NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('TRANSFER', 'DEPOSIT', 'WITHDRAWAL') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    description VARCHAR(500) NULL,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    fee_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate DECIMAL(10,6) NOT NULL DEFAULT 1.000000,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    session_id VARCHAR(100) NULL,
    failure_reason TEXT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    FOREIGN KEY (source_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    FOREIGN KEY (target_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    
    INDEX idx_transactions_source_account (source_account_id),
    INDEX idx_transactions_target_account (target_account_id),
    INDEX idx_transactions_type (type),
    INDEX idx_transactions_status (status),
    INDEX idx_transactions_timestamp (timestamp),
    INDEX idx_transactions_reference (reference_number),
    INDEX idx_transactions_amount (amount),
    INDEX idx_transactions_processed_at (processed_at),
    INDEX idx_transactions_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Columns:**
- `id`: Primary key (UUID)
- `source_account_id`: Source account (NULL for deposits)
- `target_account_id`: Target account (NULL for withdrawals)
- `amount`: Transaction amount
- `type`: Transaction type (TRANSFER, DEPOSIT, WITHDRAWAL)
- `status`: Transaction status (PENDING, COMPLETED, FAILED, CANCELLED)
- `description`: Transaction description
- `reference_number`: Unique reference number
- `fee_amount`: Transaction fee
- `currency`: Currency code
- `exchange_rate`: Exchange rate for foreign currency
- `ip_address`: Client IP address
- `user_agent`: Client user agent
- `session_id`: Session identifier
- `failure_reason`: Reason for failure if applicable
- `timestamp`: Transaction timestamp
- `processed_at`: Processing completion timestamp
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp
- `created_by`: User who created the record
- `updated_by`: User who last updated the record
- `version`: Optimistic locking version

### 6. Audit Logs Table

**Table Name:** `audit_logs`  
**Description:** Stores comprehensive audit trail for compliance and security.

```sql
CREATE TABLE audit_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    actor_user_id VARCHAR(36) NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(36) NULL,
    severity ENUM('INFO', 'WARNING', 'ERROR') NOT NULL DEFAULT 'INFO',
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    session_id VARCHAR(100) NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_audit_logs_actor_user (actor_user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_resource_type (resource_type),
    INDEX idx_audit_logs_resource_id (resource_id),
    INDEX idx_audit_logs_severity (severity),
    INDEX idx_audit_logs_timestamp (timestamp),
    INDEX idx_audit_logs_created_at (created_at),
    INDEX idx_audit_logs_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Columns:**
- `id`: Primary key (UUID)
- `actor_user_id`: User who performed the action
- `action`: Action performed
- `resource_type`: Type of resource affected
- `resource_id`: ID of resource affected
- `severity`: Log severity level (INFO, WARNING, ERROR)
- `details`: Detailed information about the action
- `ip_address`: Client IP address
- `user_agent`: Client user agent
- `session_id`: Session identifier
- `timestamp`: Action timestamp
- `created_at`: Record creation timestamp
- `created_by`: User who created the record
- `version`: Optimistic locking version

### 7. Notifications Table

**Table Name:** `notifications`  
**Description:** Stores user notifications and alerts.

```sql
CREATE TABLE notifications (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    type ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    version INT NOT NULL DEFAULT 1,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_read (read),
    INDEX idx_notifications_created_at (created_at),
    INDEX idx_notifications_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Columns:**
- `id`: Primary key (UUID)
- `user_id`: Recipient user ID
- `type`: Notification type (EMAIL, SMS, PUSH)
- `title`: Notification title
- `message`: Notification message
- `status`: Delivery status (PENDING, SENT, FAILED)
- `read`: Whether notification has been read
- `sent_at`: Timestamp when notification was sent
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp
- `created_by`: User who created the record
- `updated_by`: User who last updated the record
- `version`: Optimistic locking version

## Indexes and Performance

### Primary Indexes
- All tables have UUID primary keys for security and distribution
- Composite indexes on frequently queried columns
- Covering indexes for common query patterns

### Performance Optimizations
```sql
-- Daily transaction amount reset (runs daily at midnight)
CREATE EVENT reset_daily_transaction_amounts
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
    UPDATE accounts 
    SET daily_transaction_amount = 0.00 
    WHERE daily_transaction_amount > 0.00;

-- Interest calculation (runs monthly)
CREATE EVENT calculate_monthly_interest
ON SCHEDULE EVERY 1 MONTH
STARTS CURRENT_TIMESTAMP
DO
    UPDATE accounts 
    SET balance = balance + (balance * interest_rate / 12),
        last_interest_calculation = CURRENT_TIMESTAMP
    WHERE status = 'ACTIVE' 
    AND account_type = 'SAVINGS'
    AND interest_rate > 0.00;

-- Audit log cleanup (runs weekly, keeps 7 years)
CREATE EVENT cleanup_old_audit_logs
ON SCHEDULE EVERY 1 WEEK
STARTS CURRENT_TIMESTAMP
DO
    DELETE FROM audit_logs 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 YEAR);
```

## Constraints and Validation

### Foreign Key Constraints
- All foreign keys have proper CASCADE or SET NULL behavior
- Referential integrity is maintained at database level
- Indexes on foreign key columns for performance

### Check Constraints
```sql
-- Account balance cannot be negative (unless overdraft allowed)
ALTER TABLE accounts 
ADD CONSTRAINT chk_account_balance 
CHECK (balance >= -overdraft_limit);

-- Transaction amount must be positive
ALTER TABLE transactions 
ADD CONSTRAINT chk_transaction_amount 
CHECK (amount > 0.00);

-- Interest rate must be between 0 and 100%
ALTER TABLE accounts 
ADD CONSTRAINT chk_interest_rate 
CHECK (interest_rate >= 0.0000 AND interest_rate <= 1.0000);

-- Daily transaction limit must be positive
ALTER TABLE accounts 
ADD CONSTRAINT chk_daily_limit 
CHECK (daily_transaction_limit > 0.00);
```

### Unique Constraints
- Email addresses must be unique
- Account numbers must be unique
- Transaction reference numbers must be unique
- Role names must be unique

## Security Features

### Data Encryption
```sql
-- Encrypt sensitive columns (requires MySQL Enterprise)
ALTER TABLE users 
MODIFY COLUMN password_hash VARCHAR(255) 
ENCRYPTED WITH (ALGORITHM = 'AES_256');

ALTER TABLE audit_logs 
MODIFY COLUMN details TEXT 
ENCRYPTED WITH (ALGORITHM = 'AES_256');
```

### Row-Level Security
```sql
-- Create views for row-level security
CREATE VIEW user_accounts AS
SELECT * FROM accounts 
WHERE user_id = SESSION_USER_ID();

CREATE VIEW user_transactions AS
SELECT t.* FROM transactions t
JOIN accounts a ON t.source_account_id = a.id OR t.target_account_id = a.id
WHERE a.user_id = SESSION_USER_ID();
```

### Audit Triggers
```sql
-- Trigger to log account balance changes
DELIMITER //
CREATE TRIGGER tr_account_balance_audit
AFTER UPDATE ON accounts
FOR EACH ROW
BEGIN
    IF OLD.balance != NEW.balance THEN
        INSERT INTO audit_logs (
            actor_user_id, action, resource_type, resource_id,
            severity, details, timestamp
        ) VALUES (
            NEW.updated_by, 'BALANCE_CHANGED', 'ACCOUNT', NEW.id,
            'INFO', CONCAT('Balance changed from ', OLD.balance, ' to ', NEW.balance),
            CURRENT_TIMESTAMP
        );
    END IF;
END//
DELIMITER ;

-- Trigger to log transaction status changes
DELIMITER //
CREATE TRIGGER tr_transaction_status_audit
AFTER UPDATE ON transactions
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_logs (
            actor_user_id, action, resource_type, resource_id,
            severity, details, timestamp
        ) VALUES (
            NEW.updated_by, 'STATUS_CHANGED', 'TRANSACTION', NEW.id,
            'INFO', CONCAT('Status changed from ', OLD.status, ' to ', NEW.status),
            CURRENT_TIMESTAMP
        );
    END IF;
END//
DELIMITER ;
```

## Backup and Recovery

### Backup Strategy
```bash
#!/bin/bash
# Daily full backup script

BACKUP_DIR="/backup/trustsphere"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="trustsphere"

# Create backup directory
mkdir -p $BACKUP_DIR

# Full database backup
mysqldump \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    --add-drop-database \
    --create-options \
    --complete-insert \
    --extended-insert \
    --lock-tables=false \
    --set-charset \
    -u trustsphere \
    -p$DB_PASSWORD \
    $DB_NAME > $BACKUP_DIR/full_backup_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/full_backup_$DATE.sql

# Keep only last 30 days of backups
find $BACKUP_DIR -name "full_backup_*.sql.gz" -mtime +30 -delete

echo "Backup completed: full_backup_$DATE.sql.gz"
```

### Recovery Procedures
```sql
-- Restore from backup
mysql -u trustsphere -p trustsphere < full_backup_20250126_120000.sql

-- Point-in-time recovery (if using binary logs)
mysqlbinlog --start-datetime="2025-01-26 12:00:00" \
    --stop-datetime="2025-01-26 12:30:00" \
    mysql-bin.000001 | mysql -u trustsphere -p
```

## Monitoring and Maintenance

### Performance Monitoring Queries
```sql
-- Check slow queries
SELECT * FROM mysql.slow_log 
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY query_time DESC;

-- Check table sizes
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
    table_rows
FROM information_schema.tables 
WHERE table_schema = 'trustsphere'
ORDER BY (data_length + index_length) DESC;

-- Check index usage
SELECT 
    table_name,
    index_name,
    cardinality
FROM information_schema.statistics 
WHERE table_schema = 'trustsphere'
ORDER BY cardinality DESC;

-- Monitor connection pool
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';
```

### Maintenance Procedures
```sql
-- Analyze table statistics
ANALYZE TABLE users, accounts, transactions, audit_logs, notifications;

-- Optimize tables (run during maintenance window)
OPTIMIZE TABLE users, accounts, transactions, audit_logs, notifications;

-- Check and repair tables
CHECK TABLE users, accounts, transactions, audit_logs, notifications;
REPAIR TABLE users, accounts, transactions, audit_logs, notifications;
```

## Data Retention and Archiving

### Retention Policies
```sql
-- Archive old audit logs (older than 1 year)
CREATE TABLE audit_logs_archive LIKE audit_logs;

INSERT INTO audit_logs_archive 
SELECT * FROM audit_logs 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

DELETE FROM audit_logs 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Archive old transactions (older than 2 years)
CREATE TABLE transactions_archive LIKE transactions;

INSERT INTO transactions_archive 
SELECT * FROM transactions 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);

DELETE FROM transactions 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);
```

### Compliance Reporting
```sql
-- Generate compliance report
SELECT 
    'Total Users' as metric,
    COUNT(*) as value
FROM users
WHERE status = 'ACTIVE'

UNION ALL

SELECT 
    'Total Accounts' as metric,
    COUNT(*) as value
FROM accounts
WHERE status = 'ACTIVE'

UNION ALL

SELECT 
    'Total Transactions (Last 30 Days)' as metric,
    COUNT(*) as value
FROM transactions
WHERE created_at > DATE_SUB(NOW(), INTERVAL 30 DAY)

UNION ALL

SELECT 
    'Total Audit Logs (Last 30 Days)' as metric,
    COUNT(*) as value
FROM audit_logs
WHERE created_at > DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## Migration and Versioning

### Schema Versioning
```sql
-- Schema version table
CREATE TABLE schema_version (
    version VARCHAR(20) NOT NULL PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT NULL
);

-- Track schema changes
INSERT INTO schema_version (version, description) 
VALUES ('1.0.0', 'Initial schema creation');
```

### Migration Scripts
```bash
#!/bin/bash
# Database migration script

VERSION=$1
SCRIPT_DIR="migrations"

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    exit 1
fi

# Apply migration
mysql -u trustsphere -p$DB_PASSWORD trustsphere < $SCRIPT_DIR/migration_$VERSION.sql

# Update schema version
mysql -u trustsphere -p$DB_PASSWORD trustsphere -e "
INSERT INTO schema_version (version, description) 
VALUES ('$VERSION', 'Applied migration $VERSION');
"

echo "Migration $VERSION applied successfully"
```

## Troubleshooting

### Common Issues and Solutions

#### Connection Issues
```sql
-- Check connection limits
SHOW VARIABLES LIKE 'max_connections';
SHOW STATUS LIKE 'Threads_connected';

-- Kill long-running queries
SHOW PROCESSLIST;
KILL <process_id>;
```

#### Performance Issues
```sql
-- Check for table locks
SHOW STATUS LIKE 'Table_locks_waited';
SHOW STATUS LIKE 'Table_locks_immediate';

-- Check for slow queries
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';
```

#### Data Integrity Issues
```sql
-- Check for orphaned records
SELECT COUNT(*) FROM transactions t
LEFT JOIN accounts a ON t.source_account_id = a.id
WHERE t.source_account_id IS NOT NULL AND a.id IS NULL;

-- Check for data inconsistencies
SELECT 
    account_id,
    SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE -amount END) as calculated_balance,
    a.balance as actual_balance
FROM transactions t
JOIN accounts a ON t.source_account_id = a.id OR t.target_account_id = a.id
GROUP BY account_id
HAVING calculated_balance != actual_balance;
```

---

**For database support and questions:**
- **Email:** db-support@trustsphere.com
- **Documentation:** https://docs.trustsphere.com/database
- **Emergency:** +44 20 1234 5678 (24/7)