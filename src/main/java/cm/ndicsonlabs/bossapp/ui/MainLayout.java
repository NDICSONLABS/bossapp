// src/main/java/com/institution/finance/ui/MainLayout.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetCapitalizationView;
import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetDepreciationView;
import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetRegisterView;
import cm.ndicsonlabs.bossapp.ui.interdept.CostAllocationView;
import cm.ndicsonlabs.bossapp.ui.interdept.EliminationReportView;
import cm.ndicsonlabs.bossapp.ui.interdept.InternalInvoiceView;
import cm.ndicsonlabs.bossapp.ui.interdept.InternalServiceCatalogView;
import cm.ndicsonlabs.bossapp.ui.payroll.*;
import cm.ndicsonlabs.bossapp.ui.treasury.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import jakarta.annotation.security.PermitAll;


@PermitAll
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        Span title = new Span("Institution Finance");
        title.getStyle()
                .set("font-weight", "bold")
                .set("padding", "10px")
                .set("display", "block");

        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", DashboardView.class));
        nav.addItem(new SideNavItem("Departments", DepartmentView.class));

        SideNavItem assets = new SideNavItem("Fixed Assets");
        assets.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(assets);

        nav.addItem(new SideNavItem("Asset Register", AssetRegisterView.class));
        nav.addItem(new SideNavItem("Asset Capitalization", AssetCapitalizationView.class));
        nav.addItem(new SideNavItem("Depreciation & Disposal", AssetDepreciationView.class));

        SideNavItem education = new SideNavItem("Education");
        education.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(education);
        nav.addItem(new SideNavItem("Academic Calendar", AcademicCalendarView.class));
        nav.addItem(new SideNavItem("Fee Schedules", FeeScheduleView.class));
        nav.addItem(new SideNavItem("Student Charges", StudentChargeView.class));
        nav.addItem(new SideNavItem("Payment Plans", StudentPaymentPlanView.class));
        nav.addItem(new SideNavItem("Receipts", StudentReceiptView.class));
        nav.addItem(new SideNavItem("Cashier Sessions", CashierSessionView.class));

        SideNavItem health = new SideNavItem("Healthcare");
        health.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(health);
        nav.addItem(new SideNavItem("Patient Accounts", PatientAccountView.class));
        nav.addItem(new SideNavItem("Patient Encounters", PatientEncounterView.class));
        nav.addItem(new SideNavItem("Patient Charges", PatientChargeView.class));
        nav.addItem(new SideNavItem("Insurance Claims", InsuranceClaimView.class));

        SideNavItem procurement = new SideNavItem("Procurement");
        procurement.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(procurement);
        nav.addItem(new SideNavItem("Supplier Invoices", SupplierInvoiceView.class));

        SideNavItem procurementControl = new SideNavItem("Procurement and Supplier Control");
        procurementControl.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(procurementControl);

        nav.addItem(new SideNavItem("Purchase Requests", PurchaseRequestView.class));
        nav.addItem(new SideNavItem("Purchase Orders", PurchaseOrderView.class));
        nav.addItem(new SideNavItem("Procurement Invoices", ProcurementInvoiceView.class));
        nav.addItem(new SideNavItem("Procurement Control", ProcurementControlView.class));

        SideNavItem pharmacyCredit = new SideNavItem("Pharmacy and Supplier Credit");
        pharmacyCredit.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(pharmacyCredit);

        nav.addItem(new SideNavItem("Supplier Credit Control", SupplierCreditControlView.class));
        nav.addItem(new SideNavItem("Supplier Credit Ledger", SupplierCreditLedgerView.class));
        nav.addItem(new SideNavItem("Supplier Batches", SupplierBatchView.class));
        nav.addItem(new SideNavItem("Supplier Credit Alerts", SupplierCreditAlertView.class));
        nav.addItem(new SideNavItem("Pharmacy Daily Reconciliation", PharmacyDailyReconciliationView.class));

        SideNavItem inventory = new SideNavItem("Inventory and Stock");
        inventory.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(inventory);

        nav.addItem(new SideNavItem("Inventory Items", InventoryItemView.class));
        nav.addItem(new SideNavItem("Inventory Locations", InventoryLocationView.class));
        nav.addItem(new SideNavItem("Inventory Operations", InventoryOperationsView.class));
        nav.addItem(new SideNavItem("Inventory Reports", InventoryReportView.class));

        SideNavItem treasury = new SideNavItem("Treasury and Cash Management");
        treasury.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(treasury);

        nav.addItem(new SideNavItem("Treasury Dashboard", TreasuryDashboardView.class));
        nav.addItem(new SideNavItem("Treasury Accounts", TreasuryAccountView.class));
        nav.addItem(new SideNavItem("Cashbook", CashbookView.class));
        nav.addItem(new SideNavItem("Payment Posting", TreasuryPaymentPostingView.class));
        nav.addItem(new SideNavItem("Bank Statements", BankStatementView.class));
        nav.addItem(new SideNavItem("Bank Reconciliation", BankReconciliationView.class));

        SideNavItem finance = new SideNavItem("Finance");
        finance.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(finance);
        nav.addItem(new SideNavItem("Payments", PaymentView.class));

        SideNavItem accounting = new SideNavItem("Double-Entry Accounting");
        accounting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(accounting);
        nav.addItem(new SideNavItem("Chart of Accounts", ChartOfAccountsView.class));
        nav.addItem(new SideNavItem("Account Mappings", AccountMappingView.class));
        nav.addItem(new SideNavItem("Posting Workbench", PostingWorkbenchView.class));
        nav.addItem(new SideNavItem("Journal Entries", JournalEntryView.class));
        nav.addItem(new SideNavItem("Trial Balance", TrialBalanceView.class));
        nav.addItem(new SideNavItem("General Ledger", GeneralLedgerView.class));
        nav.addItem(new SideNavItem("GL Integration", GlIntegrationView.class));
        nav.addItem(new SideNavItem("GL Reconciliation", GlReconciliationView.class));
        nav.addItem(new SideNavItem("Financial Statements", FinancialStatementView.class));

        SideNavItem payroll = new SideNavItem("Payroll");
        payroll.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(payroll);

        nav.addItem(new SideNavItem("Employees", EmployeeView.class));
        nav.addItem(new SideNavItem("Payroll Components", PayrollComponentView.class));
        nav.addItem(new SideNavItem("Employee Salary Components", EmployeeSalaryComponentView.class));
        nav.addItem(new SideNavItem("Payroll Periods", PayrollPeriodView.class));
        nav.addItem(new SideNavItem("Payroll Runs", PayrollRunView.class));

        SideNavItem reporting = new SideNavItem("Reporting and Control");
        reporting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(reporting);
        nav.addItem(new SideNavItem("Reports", ReportControlView.class));
        nav.addItem(new SideNavItem("Audit Log", AuditLogView.class));
        nav.addItem(new SideNavItem("Formal Statements", "formal-statements"));

        SideNavItem budgeting = new SideNavItem("Budgeting and Grants");
        budgeting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(budgeting);

        nav.addItem(new SideNavItem("Budget Master Data", BudgetMasterDataView.class));
        nav.addItem(new SideNavItem("Budget Headers", BudgetHeaderView.class));
        nav.addItem(new SideNavItem("Budget Adjustments", BudgetAdjustmentView.class));
        nav.addItem(new SideNavItem("Budget Reports", BudgetReportView.class));

        SideNavItem central = new SideNavItem("Central Accounting");
        central.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(central);
        nav.addItem(new SideNavItem("Central Dashboard", CentralDashboardView.class));
        nav.addItem(new SideNavItem("Accounting Periods", AccountingPeriodView.class));
        nav.addItem(new SideNavItem("Department Submissions", DepartmentSubmissionView.class));
        nav.addItem(new SideNavItem("Period Close", PeriodCloseView.class));
        nav.addItem(new SideNavItem("Departmental Statements", DepartmentalStatementsView.class));

        SideNavItem internalBilling = new SideNavItem("Internal Billing and Allocation");
        internalBilling.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(internalBilling);

        nav.addItem(new SideNavItem("Internal Service Catalog", InternalServiceCatalogView.class));
        nav.addItem(new SideNavItem("Internal Invoices", InternalInvoiceView.class));
        nav.addItem(new SideNavItem("Cost Allocation", CostAllocationView.class));
        nav.addItem(new SideNavItem("Elimination Report", EliminationReportView.class));

        VerticalLayout drawer = new VerticalLayout(title, nav);
        drawer.setPadding(false);
        drawer.setSpacing(false);

        addToDrawer(drawer);
    }
}

