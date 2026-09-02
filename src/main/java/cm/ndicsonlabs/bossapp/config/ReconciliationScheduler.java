// src/main/java/com/institution/finance/service/ReconciliationScheduler.java
package cm.ndicsonlabs.bossapp.config;

import cm.ndicsonlabs.bossapp.service.ReconciliationJobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "app.reconciliation.enabled", havingValue = "true")
public class ReconciliationScheduler {

    private final ReconciliationJobService reconciliationJobService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ReconciliationScheduler(ReconciliationJobService reconciliationJobService) {
        this.reconciliationJobService = reconciliationJobService;
    }

    @Scheduled(cron = "${app.reconciliation.cron}")
    public void runScheduledReconciliation() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            reconciliationJobService.runDaily();
        } finally {
            running.set(false);
        }
    }
}