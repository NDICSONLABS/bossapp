// src/main/java/com/institution/finance/service/ReconciliationJobService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.ReconciliationJobRun;
import cm.ndicsonlabs.bossapp.repository.ReconciliationJobRunRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ReconciliationJobService {

    private final ReconciliationJobRunRepository jobRunRepository;
    private final OperationalReversalService operationalReversalService;

    @Value("${app.auto-reversal.enabled:false}")
    private boolean autoReversalEnabled;

    public ReconciliationJobService(
            ReconciliationJobRunRepository jobRunRepository,
            OperationalReversalService operationalReversalService
    ) {
        this.jobRunRepository = jobRunRepository;
        this.operationalReversalService = operationalReversalService;
    }

    public void runDaily() {
        ReconciliationJobRun run = new ReconciliationJobRun();
        run.setJobCode("DAILY_RECONCILIATION");
        run.setStatus("RUNNING");
        run.setStartedAt(Instant.now());

        jobRunRepository.save(run);

        try {
            int reversed = operationalReversalService.detectAndReverseDuplicatePostings(autoReversalEnabled);

            run.setStatus("SUCCESS");
            run.setMessage("Automatic reversals executed: " + reversed);
        } catch (Exception ex) {
            run.setStatus("ERROR");
            run.setMessage(ex.getMessage());
        } finally {
            run.setCompletedAt(Instant.now());
            jobRunRepository.save(run);
        }
    }
}