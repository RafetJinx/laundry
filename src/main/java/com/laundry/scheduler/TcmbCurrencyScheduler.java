package com.laundry.scheduler;

import com.laundry.service.TcmbCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A scheduler component that periodically triggers the update of
 * exchange rates from TCMB (Turkish Central Bank).
 */
@Component
public class TcmbCurrencyScheduler {

    @Autowired
    private TcmbCurrencyService tcmbCurrencyService;

    /**
     * Schedules a daily task to update currency exchange rates from TCMB.
     * <p>
     * This method runs every day at 07:00 server time (cron expression: "0 0 7 * * ?").
     * It invokes {@link TcmbCurrencyService#updateRatesFromTcmb()}, which retrieves
     * the latest rates from the TCMB XML feed.
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduleDailyRatesUpdate() {
        tcmbCurrencyService.updateRatesFromTcmb();
    }
}
