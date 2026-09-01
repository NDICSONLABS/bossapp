// src/main/java/com/institution/finance/service/EliminationReportService.java
package cm.ndicsonlabs.bossapp.service.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRun;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRunRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalInvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EliminationReportService {

    private final InternalInvoiceRepository invoiceRepository;
    private final CostAllocationRunRepository runRepository;

    public EliminationReportService(
            InternalInvoiceRepository invoiceRepository,
            CostAllocationRunRepository runRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.runRepository = runRepository;
    }

    public BigDecimal internalRevenue(LocalDate from, LocalDate to) {
        return invoiceRepository.findByTransactionDateBetweenAndStatusIn(
                        from,
                        to,
                        List.of("POSTED", "SETTLED")
                )
                .stream()
                .map(InternalInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal openInternalReceivables() {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc("POSTED")
                .stream()
                .map(InternalInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal allocations(LocalDate from, LocalDate to) {
        return runRepository.findAll()
                .stream()
                .filter(run -> isWithinPeriod(run, from, to))
                .map(CostAllocationRun::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<InternalInvoice> openInvoices() {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc("POSTED");
    }

    private boolean isWithinPeriod(CostAllocationRun run, LocalDate from, LocalDate to) {
        LocalDate runDate = LocalDate.of(run.getPeriodYear(), run.getPeriodMonth(), 1)
                .plusMonths(1)
                .minusDays(1);

        return !runDate.isBefore(from) && !runDate.isAfter(to);
    }
}