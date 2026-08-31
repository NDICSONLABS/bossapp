// src/main/java/com/institution/finance/service/BudgetReportService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.dto.BudgetLineRow;
import cm.ndicsonlabs.bossapp.dto.FundUtilizationLine;
import cm.ndicsonlabs.bossapp.dto.GrantUtilizationLine;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BudgetReportService {

    private final BudgetLineRepository budgetLineRepository;
    private final GrantAwardRepository grantAwardRepository;
    private final BudgetControlService budgetControlService;

    public BudgetReportService(
            BudgetLineRepository budgetLineRepository,
            GrantAwardRepository grantAwardRepository,
            BudgetControlService budgetControlService
    ) {
        this.budgetLineRepository = budgetLineRepository;
        this.grantAwardRepository = grantAwardRepository;
        this.budgetControlService = budgetControlService;
    }

    @Transactional(readOnly = true)
    public List<BudgetLineRow> budgetLines() {
        List<BudgetLineRow> rows = new ArrayList<>();

        for (BudgetLine line : budgetLineRepository.findAll()) {
            rows.add(new BudgetLineRow(
                    line.getBudgetHeader().getFund().getCode(),
                    line.getBudgetHeader().getGrantAward() != null
                            ? line.getBudgetHeader().getGrantAward().getCode()
                            : "",
                    line.getBudgetHeader().getDepartment().getName(),
                    line.getExpenseCategory(),
                    nullSafe(line.getOriginalAmount()),
                    nullSafe(line.getAdjustedAmount()),
                    nullSafe(line.getReservedAmount()),
                    nullSafe(line.getSpentAmount()),
                    budgetControlService.availableAmount(line),
                    line.getBudgetHeader().getStatus()
            ));
        }

        return rows.stream()
                .sorted(Comparator.comparing(BudgetLineRow::getFundCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FundUtilizationLine> fundUtilization() {
        Map<UUID, FundTotals> totals = new LinkedHashMap<>();

        for (BudgetLine line : budgetLineRepository.findAll()) {
            UUID fundId = line.getBudgetHeader().getFund().getId();

            FundTotals total = totals.computeIfAbsent(
                    fundId,
                    key -> new FundTotals(
                            line.getBudgetHeader().getFund().getCode(),
                            line.getBudgetHeader().getFund().getName()
                    )
            );

            total.budget = total.budget.add(nullSafe(line.getOriginalAmount()).add(nullSafe(line.getAdjustedAmount())));
            total.spent = total.spent.add(nullSafe(line.getSpentAmount()));
        }

        return totals.values()
                .stream()
                .map(total -> new FundUtilizationLine(
                        total.fundCode,
                        total.fundName,
                        total.budget,
                        total.spent,
                        total.budget.subtract(total.spent)
                ))
                .sorted(Comparator.comparing(FundUtilizationLine::getFundCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GrantUtilizationLine> grantUtilization() {
        List<GrantAward> grants = grantAwardRepository.findAll();

        Map<UUID, GrantTotals> totals = new LinkedHashMap<>();

        for (GrantAward grant : grants) {
            totals.put(
                    grant.getId(),
                    new GrantTotals(
                            grant.getCode(),
                            grant.getName(),
                            grant.getDonor().getName(),
                            nullSafe(grant.getTotalAmount())
                    )
            );
        }

        for (BudgetLine line : budgetLineRepository.findAll()) {
            if (line.getBudgetHeader().getGrantAward() == null) {
                continue;
            }

            UUID grantId = line.getBudgetHeader().getGrantAward().getId();

            GrantTotals total = totals.get(grantId);

            if (total != null) {
                total.allocated = total.allocated.add(
                        nullSafe(line.getOriginalAmount()).add(nullSafe(line.getAdjustedAmount()))
                );

                total.spent = total.spent.add(nullSafe(line.getSpentAmount()));
            }
        }

        return totals.values()
                .stream()
                .map(total -> new GrantUtilizationLine(
                        total.grantCode,
                        total.grantName,
                        total.donorName,
                        total.awardAmount,
                        total.allocated,
                        total.spent,
                        total.allocated.subtract(total.spent)
                ))
                .sorted(Comparator.comparing(GrantUtilizationLine::getGrantCode))
                .toList();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static class FundTotals {
        private final String fundCode;
        private final String fundName;
        private BigDecimal budget = BigDecimal.ZERO;
        private BigDecimal spent = BigDecimal.ZERO;

        private FundTotals(String fundCode, String fundName) {
            this.fundCode = fundCode;
            this.fundName = fundName;
        }
    }

    private static class GrantTotals {
        private final String grantCode;
        private final String grantName;
        private final String donorName;
        private final BigDecimal awardAmount;
        private BigDecimal allocated = BigDecimal.ZERO;
        private BigDecimal spent = BigDecimal.ZERO;

        private GrantTotals(String grantCode, String grantName, String donorName, BigDecimal awardAmount) {
            this.grantCode = grantCode;
            this.grantName = grantName;
            this.donorName = donorName;
            this.awardAmount = awardAmount;
        }
    }
}