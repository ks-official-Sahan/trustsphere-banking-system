package com.trustsphere.ejb.timer;

import com.trustsphere.ejb.service.AccountServiceBean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Singleton
@Startup
public class InterestPostingTimerBean {

    @Inject
    private AccountServiceBean accountService;

    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    @Transactional
    public void postInterest() {
        accountService.applyDailyInterestToAllActiveAccounts();
    }
}
