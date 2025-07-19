package com.trustsphere.ejb.timer;

import com.trustsphere.ejb.remote.AccountServiceRemote;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import jakarta.transaction.Transactional;

import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;

@Singleton
@Startup
public class InterestPostingTimerBean {

    @EJB
    private AccountServiceRemote accountService;

    //@Transactional
    @TransactionAttribute(REQUIRES_NEW)
    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    public void postInterest() {
        accountService.applyDailyInterestToAllActiveAccounts();
    }
}
