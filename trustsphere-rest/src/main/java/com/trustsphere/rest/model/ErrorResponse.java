package com.trustsphere.rest.model;

public class ErrorResponse {
    private String code;
    private String message;
    private long timestamp;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}

