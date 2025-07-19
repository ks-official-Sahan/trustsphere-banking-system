package com.trustsphere.core.config;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * Custom metadata builder contributor for Hibernate configuration.
 * Provides additional SQL functions and metadata configuration.
 */
public class CustomMetadataBuilderContributor implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        // Register custom SQL functions
        metadataBuilder.applySqlFunction("uuid", new StandardSQLFunction("UUID"));
        metadataBuilder.applySqlFunction("now", new StandardSQLFunction("NOW"));
        metadataBuilder.applySqlFunction("date_format", new StandardSQLFunction("DATE_FORMAT"));
        metadataBuilder.applySqlFunction("concat", new StandardSQLFunction("CONCAT"));
        metadataBuilder.applySqlFunction("length", new StandardSQLFunction("LENGTH"));
        metadataBuilder.applySqlFunction("upper", new StandardSQLFunction("UPPER"));
        metadataBuilder.applySqlFunction("lower", new StandardSQLFunction("LOWER"));
        metadataBuilder.applySqlFunction("trim", new StandardSQLFunction("TRIM"));
        metadataBuilder.applySqlFunction("substring", new StandardSQLFunction("SUBSTRING"));
        metadataBuilder.applySqlFunction("replace", new StandardSQLFunction("REPLACE"));
        
        // Register aggregate functions
        metadataBuilder.applySqlFunction("count", new StandardSQLFunction("COUNT", StandardBasicTypes.LONG));
        metadataBuilder.applySqlFunction("sum", new StandardSQLFunction("SUM"));
        metadataBuilder.applySqlFunction("avg", new StandardSQLFunction("AVG"));
        metadataBuilder.applySqlFunction("min", new StandardSQLFunction("MIN"));
        metadataBuilder.applySqlFunction("max", new StandardSQLFunction("MAX"));
        
        // Register date/time functions
        metadataBuilder.applySqlFunction("year", new StandardSQLFunction("YEAR", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("month", new StandardSQLFunction("MONTH", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("day", new StandardSQLFunction("DAY", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("hour", new StandardSQLFunction("HOUR", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("minute", new StandardSQLFunction("MINUTE", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("second", new StandardSQLFunction("SECOND", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("datediff", new StandardSQLFunction("DATEDIFF", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("date_add", new StandardSQLFunction("DATE_ADD"));
        metadataBuilder.applySqlFunction("date_sub", new StandardSQLFunction("DATE_SUB"));
        
        // Register mathematical functions
        metadataBuilder.applySqlFunction("abs", new StandardSQLFunction("ABS"));
        metadataBuilder.applySqlFunction("round", new StandardSQLFunction("ROUND"));
        metadataBuilder.applySqlFunction("ceil", new StandardSQLFunction("CEIL"));
        metadataBuilder.applySqlFunction("floor", new StandardSQLFunction("FLOOR"));
        metadataBuilder.applySqlFunction("mod", new StandardSQLFunction("MOD"));
        metadataBuilder.applySqlFunction("power", new StandardSQLFunction("POWER"));
        metadataBuilder.applySqlFunction("sqrt", new StandardSQLFunction("SQRT"));
        
        // Register string functions
        metadataBuilder.applySqlFunction("left", new StandardSQLFunction("LEFT"));
        metadataBuilder.applySqlFunction("right", new StandardSQLFunction("RIGHT"));
        metadataBuilder.applySqlFunction("mid", new StandardSQLFunction("MID"));
        metadataBuilder.applySqlFunction("locate", new StandardSQLFunction("LOCATE", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("instr", new StandardSQLFunction("INSTR", StandardBasicTypes.INTEGER));
        metadataBuilder.applySqlFunction("reverse", new StandardSQLFunction("REVERSE"));
        metadataBuilder.applySqlFunction("repeat", new StandardSQLFunction("REPEAT"));
        metadataBuilder.applySqlFunction("space", new StandardSQLFunction("SPACE"));
        
        // Register conditional functions
        metadataBuilder.applySqlFunction("if", new StandardSQLFunction("IF"));
        metadataBuilder.applySqlFunction("ifnull", new StandardSQLFunction("IFNULL"));
        metadataBuilder.applySqlFunction("nullif", new StandardSQLFunction("NULLIF"));
        metadataBuilder.applySqlFunction("case", new StandardSQLFunction("CASE"));
        
        // Register type conversion functions
        metadataBuilder.applySqlFunction("cast", new StandardSQLFunction("CAST"));
        metadataBuilder.applySqlFunction("convert", new StandardSQLFunction("CONVERT"));
        
        // Register JSON functions (MySQL 5.7+)
        metadataBuilder.applySqlFunction("json_extract", new StandardSQLFunction("JSON_EXTRACT"));
        metadataBuilder.applySqlFunction("json_set", new StandardSQLFunction("JSON_SET"));
        metadataBuilder.applySqlFunction("json_remove", new StandardSQLFunction("JSON_REMOVE"));
        metadataBuilder.applySqlFunction("json_contains", new StandardSQLFunction("JSON_CONTAINS"));
        metadataBuilder.applySqlFunction("json_length", new StandardSQLFunction("JSON_LENGTH"));
        metadataBuilder.applySqlFunction("json_keys", new StandardSQLFunction("JSON_KEYS"));
        metadataBuilder.applySqlFunction("json_array", new StandardSQLFunction("JSON_ARRAY"));
        metadataBuilder.applySqlFunction("json_object", new StandardSQLFunction("JSON_OBJECT"));
        
        // Register encryption functions
        metadataBuilder.applySqlFunction("aes_encrypt", new StandardSQLFunction("AES_ENCRYPT"));
        metadataBuilder.applySqlFunction("aes_decrypt", new StandardSQLFunction("AES_DECRYPT"));
        metadataBuilder.applySqlFunction("md5", new StandardSQLFunction("MD5"));
        metadataBuilder.applySqlFunction("sha1", new StandardSQLFunction("SHA1"));
        metadataBuilder.applySqlFunction("sha2", new StandardSQLFunction("SHA2"));
        
        // Register random functions
        metadataBuilder.applySqlFunction("rand", new StandardSQLFunction("RAND"));
        metadataBuilder.applySqlFunction("uuid_short", new StandardSQLFunction("UUID_SHORT"));
        
        // Register system functions
        metadataBuilder.applySqlFunction("user", new StandardSQLFunction("USER"));
        metadataBuilder.applySqlFunction("database", new StandardSQLFunction("DATABASE"));
        metadataBuilder.applySqlFunction("version", new StandardSQLFunction("VERSION"));
        metadataBuilder.applySqlFunction("connection_id", new StandardSQLFunction("CONNECTION_ID"));
        
        // Register information schema functions
        metadataBuilder.applySqlFunction("last_insert_id", new StandardSQLFunction("LAST_INSERT_ID"));
        metadataBuilder.applySqlFunction("row_count", new StandardSQLFunction("ROW_COUNT"));
        metadataBuilder.applySqlFunction("found_rows", new StandardSQLFunction("FOUND_ROWS"));
        
        // Configure additional metadata settings
        // Note: Some methods may not be available in all Hibernate versions
        try {
            metadataBuilder.enableExplicitDiscriminatorsForJoinedSubclassSupport(true);
            metadataBuilder.enableImplicitDiscriminatorsForJoinedSubclassSupport(true);
        } catch (Exception e) {
            // Ignore if methods are not available
        }
    }
}