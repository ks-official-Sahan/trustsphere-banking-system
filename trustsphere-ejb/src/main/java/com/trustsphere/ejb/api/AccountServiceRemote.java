package com.trustsphere.ejb.api;

import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.ejb.dto.AccountDTO;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface AccountServiceRemote {

    AccountDTO createAccount(AccountDTO dto);

    AccountDTO getAccountById(String id);

    List<AccountDTO> listActiveByUser(String userId);

    void updateStatus(String id, AccountStatus status);
}