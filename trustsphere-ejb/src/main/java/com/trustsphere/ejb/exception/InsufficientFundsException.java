package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

import java.math.BigDecimal;

@ApplicationException(rollback = true)
public class InsufficientFundsException extends BusinessException {

    private BigDecimal availableBalance;
    private BigDecimal requestedAmount;

    public InsufficientFundsException(String acctId) {
        super("Insufficient funds: " + acctId);
    }

    public InsufficientFundsException(String acctId, BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds on acc %s. Available: %s, Requested: %s", acctId,
                availableBalance, requestedAmount), "INSUFFICIENT_FUNDS", true);
    }

    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds. Available: %s, Requested: %s",
                availableBalance, requestedAmount), "INSUFFICIENT_FUNDS", true);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
