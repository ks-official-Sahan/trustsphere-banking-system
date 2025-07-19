package com.trustsphere.ejb.remote;

import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.core.dto.AccountDTO;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface AccountServiceRemote {

    AccountDTO createAccount(AccountDTO dto);

    AccountDTO getAccountById(String id);

    List<AccountDTO> listActiveByUser(String userId);

    void updateStatus(String id, AccountStatus status);

    //void updateStatus(String id, AccountStatus status, String reason, User currentUser);

    void applyDailyInterestToAllActiveAccounts();

}