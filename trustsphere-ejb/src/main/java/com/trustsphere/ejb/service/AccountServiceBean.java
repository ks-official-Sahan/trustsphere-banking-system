package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.Account;
import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.ejb.api.AccountServiceRemote;
import com.trustsphere.ejb.dao.AccountDAO;
import com.trustsphere.ejb.dto.AccountDTO;
import com.trustsphere.ejb.exception.AccountNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
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
    public AccountDTO getAccountById(String id) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            throw new AccountNotFoundException(id);
        }
        return mapToDTO(account);
    }

    @Override
    public List<AccountDTO> listActiveByUser(String userId) {
        return accountDAO.findActiveByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String id, AccountStatus status) {
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
        //dto.setCreatedAt(account.getCreatedAt());
        //dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}