package com.trustsphere.ejb.timer;

import com.trustsphere.ejb.service.AccountServiceBean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;

@Singleton
@Startup
public class InterestPostingTimerBean {

    @Inject
    private AccountServiceBean accountService;

    //@Transactional
    @TransactionAttribute(REQUIRES_NEW)
    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    public void postInterest() {
        accountService.applyDailyInterestToAllActiveAccounts();
    }
}
