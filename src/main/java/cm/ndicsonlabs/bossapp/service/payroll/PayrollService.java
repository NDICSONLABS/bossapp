// src/main/java/com/institution/finance/service/PayrollService.java
package cm.ndicsonlabs.bossapp.service.payroll;

import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.domain.payroll.EmployeePayrollRun;
import cm.ndicsonlabs.bossapp.domain.payroll.EmployeeSalaryComponent;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollComponent;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollPeriod;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollRunLine;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeePayrollRunRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeSalaryComponentRepository;
import cm.ndicsonlabs.bossapp.repository.FundRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollComponentRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollRunLineRepository;
import cm.ndicsonlabs.bossapp.service.AuditService;
import cm.ndicsonlabs.bossapp.service.BudgetControlService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import cm.ndicsonlabs.bossapp.service.treasury.TreasuryService;
import cm.ndicsonlabs.bossapp.ui.payroll.PayrollAccountingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryComponentRepository salaryComponentRepository;
    private final PayrollComponentRepository payrollComponentRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmployeePayrollRunRepository payrollRunRepository;
    private final PayrollRunLineRepository payrollRunLineRepository;
    private final FundRepository fundRepository;
    private final GrantAwardRepository grantAwardRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final PayrollAccountingService payrollAccountingService;
    private final BudgetControlService budgetControlService;
    private final TreasuryService treasuryService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public PayrollService(
            EmployeeRepository employeeRepository,
            EmployeeSalaryComponentRepository salaryComponentRepository,
            PayrollComponentRepository payrollComponentRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            EmployeePayrollRunRepository payrollRunRepository,
            PayrollRunLineRepository payrollRunLineRepository,
            FundRepository fundRepository,
            GrantAwardRepository grantAwardRepository,
            BudgetLineRepository budgetLineRepository,
            PayrollAccountingService payrollAccountingService,
            BudgetControlService budgetControlService,
            TreasuryService treasuryService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.employeeRepository = employeeRepository;
        this.salaryComponentRepository = salaryComponentRepository;
        this.payrollComponentRepository = payrollComponentRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollRunLineRepository = payrollRunLineRepository;
        this.fundRepository = fundRepository;
        this.grantAwardRepository = grantAwardRepository;
        this.budgetLineRepository = budgetLineRepository;
        this.payrollAccountingService = payrollAccountingService;
        this.budgetControlService = budgetControlService;
        this.treasuryService = treasuryService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public PayrollPeriod createPeriod(
            Integer fiscalYear,
            Integer periodNumber,
            LocalDate startDate,
            LocalDate endDate,
            UUID fundId,
            UUID grantAwardId,
            UUID budgetLineId
    ) {
        requirePayrollPrivilege();

        Fund fund = fundId != null
                ? fundRepository.findById(fundId).orElseThrow(() -> new IllegalArgumentException("Fund not found"))
                : null;

        GrantAward grant = grantAwardId != null
                ? grantAwardRepository.findById(grantAwardId).orElseThrow(() -> new IllegalArgumentException("Grant not found"))
                : null;

        BudgetLine budgetLine = budgetLineId != null
                ? budgetLineRepository.findById(budgetLineId).orElseThrow(() -> new IllegalArgumentException("Budget line not found"))
                : null;

        PayrollPeriod period = new PayrollPeriod();
        period.setFiscalYear(fiscalYear);
        period.setPeriodNumber(periodNumber);
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setFund(fund);
        period.setGrantAward(grant);
        period.setBudgetLine(budgetLine);
        period.setStatus("DRAFT");
        period.setPreparedBy(currentUserService.username());

        payrollPeriodRepository.save(period);

        auditService.log(
                "PAYROLL_PERIOD",
                period.getId(),
                "CREATE_PAYROLL_PERIOD",
                null,
                period.toString(),
                "Payroll period created"
        );

        return period;
    }

    @Transactional
    public PayrollPeriod calculatePayroll(UUID periodId) {
        requirePayrollPrivilege();

        PayrollPeriod period = getPeriod(periodId);

        if (!"DRAFT".equals(period.getStatus()) && !"CALCULATED".equals(period.getStatus())) {
            throw new IllegalStateException("Only draft or calculated payroll periods can be recalculated.");
        }

        List<EmployeePayrollRun> existingRuns = payrollRunRepository.findByPayrollPeriodId(periodId);

        for (EmployeePayrollRun run : existingRuns) {
            payrollRunLineRepository.deleteByEmployeePayrollRunId(run.getId());
        }

        payrollRunRepository.deleteAll(existingRuns);

        List<Employee> employees = employeeRepository.findByActiveTrueOrderByEmployeeNumber();
        List<PayrollComponent> components = payrollComponentRepository.findByActiveTrueOrderByCode();

        PayrollComponent basicComponent = payrollComponentRepository.findByCode("BASIC")
                .orElseThrow(() -> new IllegalStateException("BASIC payroll component not found"));

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        for (Employee employee : employees) {
            List<EmployeeSalaryComponent> employeeComponents = salaryComponentRepository
                    .findByEmployeeIdAndActiveTrue(employee.getId());

            Map<UUID, EmployeeSalaryComponent> employeeComponentMap = new HashMap<>();

            for (EmployeeSalaryComponent employeeComponent : employeeComponents) {
                employeeComponentMap.put(employeeComponent.getPayrollComponent().getId(), employeeComponent);
            }

            BigDecimal basicAmount = calculateComponentAmount(
                    basicComponent,
                    employeeComponentMap.get(basicComponent.getId()),
                    BigDecimal.ZERO
            );

            List<PayrollRunLine> lines = new ArrayList<>();

            BigDecimal gross = BigDecimal.ZERO;
            BigDecimal deductions = BigDecimal.ZERO;

            for (PayrollComponent component : components) {
                EmployeeSalaryComponent employeeComponent = employeeComponentMap.get(component.getId());

                BigDecimal amount = calculateComponentAmount(component, employeeComponent, basicAmount);

                if (amount.signum() == 0) {
                    continue;
                }

                if ("EARNING".equals(component.getComponentType())) {
                    gross = gross.add(amount);
                } else if ("DEDUCTION".equals(component.getComponentType())) {
                    deductions = deductions.add(amount);
                }

                PayrollRunLine line = new PayrollRunLine();
                line.setPayrollComponent(component);
                line.setLineType(component.getComponentType());
                line.setAmount(amount);

                lines.add(line);
            }

            if (gross.signum() == 0 && deductions.signum() == 0) {
                continue;
            }

            BigDecimal net = gross.subtract(deductions);

            if (net.signum() < 0) {
                throw new IllegalStateException(
                        "Payroll calculation produced a negative net amount for employee " + employee.getEmployeeNumber()
                );
            }

            EmployeePayrollRun run = new EmployeePayrollRun();
            run.setPayrollPeriod(period);
            run.setEmployee(employee);
            run.setDepartment(employee.getDepartment());
            run.setFund(period.getFund());
            run.setGrantAward(period.getGrantAward());
            run.setBudgetLine(period.getBudgetLine());
            run.setGrossAmount(gross);
            run.setTotalDeductions(deductions);
            run.setNetAmount(net);
            run.setStatus("CALCULATED");

            payrollRunRepository.save(run);

            for (PayrollRunLine line : lines) {
                line.setEmployeePayrollRun(run);
            }

            payrollRunLineRepository.saveAll(lines);

            totalGross = totalGross.add(gross);
            totalDeductions = totalDeductions.add(deductions);
        }

        period.setTotalGross(totalGross);
        period.setTotalDeductions(totalDeductions);
        period.setTotalNet(totalGross.subtract(totalDeductions));
        period.setStatus("CALCULATED");

        payrollPeriodRepository.save(period);

        auditService.log(
                "PAYROLL_PERIOD",
                period.getId(),
                "CALCULATE_PAYROLL",
                null,
                period.getTotalNet().toPlainString(),
                "Payroll calculated"
        );

        return period;
    }

    @Transactional
    public PayrollPeriod approvePayroll(UUID periodId) {
        requirePayrollPrivilege();

        PayrollPeriod period = getPeriod(periodId);

        if (!"CALCULATED".equals(period.getStatus())) {
            throw new IllegalStateException("Only calculated payroll periods can be approved.");
        }

        if (period.getTotalGross().signum() == 0) {
            throw new IllegalStateException("Cannot approve payroll with zero gross amount.");
        }

        payrollAccountingService.postPayrollAccrual(period);

        if (period.getBudgetLine() != null) {
            budgetControlService.registerDirectExpense(
                    period.getBudgetLine().getId(),
                    period.getTotalGross(),
                    "PAYROLL_PERIOD",
                    period.getId()
            );
        }

        period.setStatus("APPROVED");
        period.setApprovedBy(currentUserService.username());
        period.setApprovedAt(Instant.now());

        payrollPeriodRepository.save(period);

        auditService.log(
                "PAYROLL_PERIOD",
                period.getId(),
                "APPROVE_PAYROLL",
                null,
                period.getTotalNet().toPlainString(),
                "Payroll approved"
        );

        return period;
    }

    @Transactional
    public PayrollPeriod payPayroll(UUID periodId, UUID treasuryAccountId) {
        requirePayrollPrivilege();

        PayrollPeriod period = getPeriod(periodId);

        if (!"APPROVED".equals(period.getStatus())) {
            throw new IllegalStateException("Only approved payroll periods can be paid.");
        }

        BigDecimal netAmount = period.getTotalNet();

        if (netAmount.signum() <= 0) {
            throw new IllegalStateException("Payroll net amount must be greater than zero.");
        }

        TreasuryTransaction transaction = treasuryService.postManualTransaction(
                treasuryAccountId,
                "OUT",
                netAmount,
                LocalDate.now(),
                "PAYROLL-" + period.getFiscalYear() + "-P" + period.getPeriodNumber(),
                "Payroll payment for " + period
        );

        payrollAccountingService.postPayrollPayment(period, netAmount);

        period.setStatus("PAID");
        period.setPaymentReference(transaction.getTransactionNumber());
        period.setPaidAt(Instant.now());

        payrollPeriodRepository.save(period);

        auditService.log(
                "PAYROLL_PERIOD",
                period.getId(),
                "PAY_PAYROLL",
                null,
                transaction.getTransactionNumber(),
                "Payroll paid"
        );

        return period;
    }

    private BigDecimal calculateComponentAmount(
            PayrollComponent component,
            EmployeeSalaryComponent employeeComponent,
            BigDecimal basicAmount
    ) {
        if ("FIXED".equals(component.getCalculationType())) {
            if (employeeComponent != null && employeeComponent.getAmount() != null) {
                return employeeComponent.getAmount();
            }

            return component.getDefaultAmount() != null ? component.getDefaultAmount() : BigDecimal.ZERO;
        }

        if ("PERCENTAGE_OF_BASIC".equals(component.getCalculationType())) {
            BigDecimal percentage = null;

            if (employeeComponent != null && employeeComponent.getPercentage() != null) {
                percentage = employeeComponent.getPercentage();
            } else if (component.getDefaultPercent() != null) {
                percentage = component.getDefaultPercent();
            }

            if (percentage == null || basicAmount == null || basicAmount.signum() == 0) {
                return BigDecimal.ZERO;
            }

            return basicAmount
                    .multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }

        throw new IllegalStateException("Unsupported payroll calculation type: " + component.getCalculationType());
    }

    private PayrollPeriod getPeriod(UUID periodId) {
        return payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll period not found"));
    }

    private void requirePayrollPrivilege() {
        if (!currentUserService.hasPrivilege("PAYROLL_MANAGE")) {
            throw new AccessDeniedException("Current user does not have payroll management privilege.");
        }
    }
}