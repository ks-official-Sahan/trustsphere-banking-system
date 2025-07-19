package com.trustsphere.ejb.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import com.trustsphere.core.entity.Account;
import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.ejb.api.AccountServiceRemote;
import com.trustsphere.ejb.dao.AccountDAO;
import com.trustsphere.core.dto.AccountDTO;
import com.trustsphere.ejb.exception.AccountNotFoundException;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateless
@RolesAllowed({"ROLE_ADMIN", "ROLE_TELLER", "ROLE_USER"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AccountServiceBean implements AccountServiceRemote {

    @EJB
    private AccountDAO accountDAO;

    @Override
    public AccountDTO createAccount(AccountDTO dto) {
        Account account = mapToEntity(dto);
        Account created = accountDAO.create(account);
        return mapToDTO(created);
    }

    @Override
    public AccountDTO getAccountById(String id) throws AccountNotFoundException {
        Account account = accountDAO.findById(id);
        if (account == null) {
            throw new AccountNotFoundException(id);
        }
        return mapToDTO(account);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AccountDTO> listActiveByUser(String userId) {
        return accountDAO.findActiveByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String id, AccountStatus status) throws AccountNotFoundException {
        Account account = accountDAO.findById(id);
        if (account == null) {
            throw new AccountNotFoundException(id);
        }
        account.setStatus(status);
        accountDAO.update(account);
    }

    private Account mapToEntity(AccountDTO dto) {
        Account account = new Account();
        account.setAccountNumber(dto.getAccountNumber());
        account.setBalance(dto.getBalance());
        account.setStatus(dto.getStatus());
        return account;
    }

    private AccountDTO mapToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus());
        dto.setUserId(account.getUser().getId());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void applyDailyInterestToAllActiveAccounts() {
        List<Account> accounts = accountDAO.findActiveAccounts();

        //interest rate: 0.05% daily (annual 18%)
        BigDecimal rate = new BigDecimal("0.0005");

        int count = 0;
        for (Account acc : accounts) {
            BigDecimal interest = acc.getBalance().multiply(rate)
                    .setScale(2, RoundingMode.HALF_UP);
            acc.setBalance(acc.getBalance().add(interest));

            accountDAO.update(acc);
            if (++count % 25 == 0) {
                accountDAO.flushBatch();
            }
        }
        accountDAO.flushBatch();
    }

//    public void applyDailyInterestToAllActiveAccounts() {
//        List<Account> accounts = accountDAO.findActiveAccounts();
//
//        //interest rate: 0.05% daily (annual 18%)
//        BigDecimal interestRate = new BigDecimal("0.0005");
//
//        for (Account acc : accounts) {
//            BigDecimal interest = acc.getBalance().multiply(interestRate);
//            acc.setBalance(acc.getBalance().add(interest));
//
//            accountDAO.update(acc);
//        }
//    }

}