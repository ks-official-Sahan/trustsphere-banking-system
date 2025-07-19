package com.trustsphere.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class TransferRequest {
    @NotNull
    @Pattern(regexp="^[A-Z0-9]{10,20}$")
    public String srcId;

    @NotNull @Pattern(regexp="^[A-Z0-9]{10,20}$")
    public String tgtId;

    @NotNull @DecimalMin("0.01")
    public BigDecimal amount;

    public String description;
}