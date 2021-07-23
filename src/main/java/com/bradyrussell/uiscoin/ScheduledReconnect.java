package com.bradyrussell.uiscoin;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledReconnect {
    @Scheduled(fixedRate = 1000*60)
    public void retryPeersEveryMinute() {
        if(UISCoinContext.getNode() != null) {
            UISCoinContext.getNode().RetryPeers();
        }
    }
}
