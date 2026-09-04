//// src/main/java/com/institution/finance/ui/MainLayout.java
//package cm.ndicsonlabs.bossapp.ui;
//
//import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetCapitalizationView;
//import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetDepreciationView;
//import cm.ndicsonlabs.bossapp.ui.fixedasset.AssetRegisterView;
//import cm.ndicsonlabs.bossapp.ui.interdept.CostAllocationView;
//import cm.ndicsonlabs.bossapp.ui.interdept.EliminationReportView;
//import cm.ndicsonlabs.bossapp.ui.interdept.InternalInvoiceView;
//import cm.ndicsonlabs.bossapp.ui.interdept.InternalServiceCatalogView;
//import cm.ndicsonlabs.bossapp.ui.payroll.*;
//import cm.ndicsonlabs.bossapp.ui.treasury.*;
//import com.vaadin.flow.component.applayout.AppLayout;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.sidenav.SideNav;
//import com.vaadin.flow.component.sidenav.SideNavItem;
//import com.vaadin.flow.router.RouterLayout;
//import jakarta.annotation.security.PermitAll;
//
//
//@PermitAll
//public class MainLayout extends AppLayout implements RouterLayout {
//
//    public MainLayout() {
//        Span title = new Span("Institution Finance");
//        title.getStyle()
//                .set("font-weight", "bold")
//                .set("padding", "10px")
//                .set("display", "block");
//
//        SideNav nav = new SideNav();
//
//        nav.addItem(new SideNavItem("Dashboard", DashboardView.class));
//        nav.addItem(new SideNavItem("Departments", DepartmentView.class));
//
//        SideNavItem assets = new SideNavItem("Fixed Assets");
//        assets.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(assets);
//
//        nav.addItem(new SideNavItem("Asset Register", AssetRegisterView.class));
//        nav.addItem(new SideNavItem("Asset Capitalization", AssetCapitalizationView.class));
//        nav.addItem(new SideNavItem("Depreciation & Disposal", AssetDepreciationView.class));
//
//        SideNavItem education = new SideNavItem("Education");
//        education.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(education);
//        nav.addItem(new SideNavItem("Academic Calendar", AcademicCalendarView.class));
//        nav.addItem(new SideNavItem("Fee Schedules", FeeScheduleView.class));
//        nav.addItem(new SideNavItem("Student Charges", StudentChargeView.class));
//        nav.addItem(new SideNavItem("Payment Plans", StudentPaymentPlanView.class));
//        nav.addItem(new SideNavItem("Receipts", StudentReceiptView.class));
//        nav.addItem(new SideNavItem("Cashier Sessions", CashierSessionView.class));
//
//        SideNavItem health = new SideNavItem("Healthcare");
//        health.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(health);
//        nav.addItem(new SideNavItem("Patient Accounts", PatientAccountView.class));
//        nav.addItem(new SideNavItem("Patient Encounters", PatientEncounterView.class));
//        nav.addItem(new SideNavItem("Patient Charges", PatientChargeView.class));
//        nav.addItem(new SideNavItem("Insurance Claims", InsuranceClaimView.class));
//
//        SideNavItem procurement = new SideNavItem("Procurement");
//        procurement.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(procurement);
//        nav.addItem(new SideNavItem("Supplier Invoices", SupplierInvoiceView.class));
//
//        SideNavItem procurementControl = new SideNavItem("Procurement and Supplier Control");
//        procurementControl.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(procurementControl);
//
//        nav.addItem(new SideNavItem("Purchase Requests", PurchaseRequestView.class));
//        nav.addItem(new SideNavItem("Purchase Orders", PurchaseOrderView.class));
//        nav.addItem(new SideNavItem("Procurement Invoices", ProcurementInvoiceView.class));
//        nav.addItem(new SideNavItem("Procurement Control", ProcurementControlView.class));
//
//        SideNavItem pharmacyCredit = new SideNavItem("Pharmacy and Supplier Credit");
//        pharmacyCredit.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(pharmacyCredit);
//
//        nav.addItem(new SideNavItem("Supplier Credit Control", SupplierCreditControlView.class));
//        nav.addItem(new SideNavItem("Supplier Credit Ledger", SupplierCreditLedgerView.class));
//        nav.addItem(new SideNavItem("Supplier Batches", SupplierBatchView.class));
//        nav.addItem(new SideNavItem("Supplier Credit Alerts", SupplierCreditAlertView.class));
//        nav.addItem(new SideNavItem("Pharmacy Daily Reconciliation", PharmacyDailyReconciliationView.class));
//
//        SideNavItem inventory = new SideNavItem("Inventory and Stock");
//        inventory.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(inventory);
//
//        nav.addItem(new SideNavItem("Inventory Items", InventoryItemView.class));
//        nav.addItem(new SideNavItem("Inventory Locations", InventoryLocationView.class));
//        nav.addItem(new SideNavItem("Inventory Operations", InventoryOperationsView.class));
//        nav.addItem(new SideNavItem("Inventory Reports", InventoryReportView.class));
//
//        SideNavItem treasury = new SideNavItem("Treasury and Cash Management");
//        treasury.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(treasury);
//
//        nav.addItem(new SideNavItem("Treasury Dashboard", TreasuryDashboardView.class));
//        nav.addItem(new SideNavItem("Treasury Accounts", TreasuryAccountView.class));
//        nav.addItem(new SideNavItem("Cashbook", CashbookView.class));
//        nav.addItem(new SideNavItem("Payment Posting", TreasuryPaymentPostingView.class));
//        nav.addItem(new SideNavItem("Bank Statements", BankStatementView.class));
//        nav.addItem(new SideNavItem("Bank Reconciliation", BankReconciliationView.class));
//
//        SideNavItem finance = new SideNavItem("Finance");
//        finance.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(finance);
//        nav.addItem(new SideNavItem("Payments", PaymentView.class));
//
//        SideNavItem accounting = new SideNavItem("Double-Entry Accounting");
//        accounting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(accounting);
//        nav.addItem(new SideNavItem("Chart of Accounts", ChartOfAccountsView.class));
//        nav.addItem(new SideNavItem("Account Mappings", AccountMappingView.class));
//        nav.addItem(new SideNavItem("Posting Workbench", PostingWorkbenchView.class));
//        nav.addItem(new SideNavItem("Journal Entries", JournalEntryView.class));
//        nav.addItem(new SideNavItem("Trial Balance", TrialBalanceView.class));
//        nav.addItem(new SideNavItem("General Ledger", GeneralLedgerView.class));
//        nav.addItem(new SideNavItem("GL Integration", GlIntegrationView.class));
//        nav.addItem(new SideNavItem("GL Reconciliation", GlReconciliationView.class));
//        nav.addItem(new SideNavItem("Financial Statements", FinancialStatementView.class));
//
//        SideNavItem payroll = new SideNavItem("Payroll");
//        payroll.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(payroll);
//
//        nav.addItem(new SideNavItem("Employees", EmployeeView.class));
//        nav.addItem(new SideNavItem("Payroll Components", PayrollComponentView.class));
//        nav.addItem(new SideNavItem("Employee Salary Components", EmployeeSalaryComponentView.class));
//        nav.addItem(new SideNavItem("Payroll Periods", PayrollPeriodView.class));
//        nav.addItem(new SideNavItem("Payroll Runs", PayrollRunView.class));
//
//        SideNavItem reporting = new SideNavItem("Reporting and Control");
//        reporting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(reporting);
//        nav.addItem(new SideNavItem("Reports", ReportControlView.class));
//        nav.addItem(new SideNavItem("Audit Log", AuditLogView.class));
//        nav.addItem(new SideNavItem("Formal Statements", "formal-statements"));
//
//        SideNavItem budgeting = new SideNavItem("Budgeting and Grants");
//        budgeting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(budgeting);
//
//        nav.addItem(new SideNavItem("Budget Master Data", BudgetMasterDataView.class));
//        nav.addItem(new SideNavItem("Budget Headers", BudgetHeaderView.class));
//        nav.addItem(new SideNavItem("Budget Adjustments", BudgetAdjustmentView.class));
//        nav.addItem(new SideNavItem("Budget Reports", BudgetReportView.class));
//
//        SideNavItem central = new SideNavItem("Central Accounting");
//        central.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(central);
//        nav.addItem(new SideNavItem("Central Dashboard", CentralDashboardView.class));
//        nav.addItem(new SideNavItem("Accounting Periods", AccountingPeriodView.class));
//        nav.addItem(new SideNavItem("Department Submissions", DepartmentSubmissionView.class));
//        nav.addItem(new SideNavItem("Period Close", PeriodCloseView.class));
//        nav.addItem(new SideNavItem("Departmental Statements", DepartmentalStatementsView.class));
//
//        SideNavItem internalBilling = new SideNavItem("Internal Billing and Allocation");
//        internalBilling.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
//        nav.addItem(internalBilling);
//
//        nav.addItem(new SideNavItem("Internal Service Catalog", InternalServiceCatalogView.class));
//        nav.addItem(new SideNavItem("Internal Invoices", InternalInvoiceView.class));
//        nav.addItem(new SideNavItem("Cost Allocation", CostAllocationView.class));
//        nav.addItem(new SideNavItem("Elimination Report", EliminationReportView.class));
//
//        VerticalLayout drawer = new VerticalLayout(title, nav);
//        drawer.setPadding(false);
//        drawer.setSpacing(false);
//
//        addToDrawer(drawer);
//    }
//}
//
// src/main/java/com/institution/finance/ui/MainLayout.java
package cm.ndicsonlabs.bossapp.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);

        Button drawerToggle = new Button("Menu");
        drawerToggle.addClickListener(e -> setDrawerOpened(!isDrawerOpened()));

        Span appTitle = new Span("Institution Finance");
        appTitle.addClassName("app-title");

        addToNavbar(drawerToggle, appTitle);

        addToDrawer(createNavigation());
    }

    private VerticalLayout createNavigation() {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("collapsible", true);

        nav.addItem(new SideNavItem("Dashboard", ""));

        SideNavItem receivables = new SideNavItem("Receivables");
        receivables.addItem(new SideNavItem("Student Charges", "student-charges"));
        receivables.addItem(new SideNavItem("Payment Plans", "student-payment-plans"));
        receivables.addItem(new SideNavItem("Receipts", "student-receipts"));
        receivables.addItem(new SideNavItem("Patient Accounts", "patient-accounts"));
        receivables.addItem(new SideNavItem("Patient Encounters", "patient-encounters"));
        receivables.addItem(new SideNavItem("Patient Charges", "patient-charges"));
        receivables.addItem(new SideNavItem("Insurance Claims", "insurance-claims"));
        nav.addItem(receivables);

        SideNavItem payables = new SideNavItem("Payables and Suppliers");
        payables.addItem(new SideNavItem("Purchase Requests", "purchase-requests"));
        payables.addItem(new SideNavItem("Purchase Orders", "purchase-orders"));
        payables.addItem(new SideNavItem("Procurement Invoices", "procurement-invoices"));
        payables.addItem(new SideNavItem("Procurement Control", "procurement-control"));
        payables.addItem(new SideNavItem("Supplier Credit Control", "supplier-credit-control"));
        payables.addItem(new SideNavItem("Supplier Credit Ledger", "supplier-credit-ledger"));
        payables.addItem(new SideNavItem("Supplier Batches", "supplier-batches"));
        payables.addItem(new SideNavItem("Supplier Alerts", "supplier-credit-alerts"));
        nav.addItem(payables);

        SideNavItem cash = new SideNavItem("Cash and Treasury");
        cash.addItem(new SideNavItem("Treasury Dashboard", "treasury-dashboard"));
        cash.addItem(new SideNavItem("Treasury Accounts", "treasury-accounts"));
        cash.addItem(new SideNavItem("Cashbook", "cashbook"));
        cash.addItem(new SideNavItem("Payment Posting", "treasury-payment-posting"));
        cash.addItem(new SideNavItem("Bank Statements", "bank-statements"));
        cash.addItem(new SideNavItem("Bank Reconciliation", "bank-reconciliation"));
        cash.addItem(new SideNavItem("Cashier Sessions", "cashier-sessions"));
        cash.addItem(new SideNavItem("Pharmacy Reconciliation", "pharmacy-daily-reconciliation"));
        nav.addItem(cash);

        SideNavItem budget = new SideNavItem("Budget and Grants");
        budget.addItem(new SideNavItem("Budget Master Data", "budget-master-data"));
        budget.addItem(new SideNavItem("Budget Headers", "budget-headers"));
        budget.addItem(new SideNavItem("Budget Adjustments", "budget-adjustments"));
        budget.addItem(new SideNavItem("Budget Reports", "budget-reports"));
        nav.addItem(budget);

        SideNavItem payroll = new SideNavItem("Payroll");
        payroll.addItem(new SideNavItem("Employees", "employees"));
        payroll.addItem(new SideNavItem("Payroll Components", "payroll-components"));
        payroll.addItem(new SideNavItem("Salary Components", "employee-salary-components"));
        payroll.addItem(new SideNavItem("Payroll Periods", "payroll-periods"));
        payroll.addItem(new SideNavItem("Payroll Runs", "payroll-runs"));
        nav.addItem(payroll);

        SideNavItem assets = new SideNavItem("Fixed Assets");
        assets.addItem(new SideNavItem("Asset Register", "asset-register"));
        assets.addItem(new SideNavItem("Asset Capitalization", "asset-capitalization"));
        assets.addItem(new SideNavItem("Depreciation and Disposal", "asset-depreciation"));
        nav.addItem(assets);

        SideNavItem inventory = new SideNavItem("Inventory");
        inventory.addItem(new SideNavItem("Items", "inventory-items"));
        inventory.addItem(new SideNavItem("Locations", "inventory-locations"));
        inventory.addItem(new SideNavItem("Operations", "inventory-operations"));
        inventory.addItem(new SideNavItem("Reports", "inventory-reports"));
        nav.addItem(inventory);

        SideNavItem internal = new SideNavItem("Internal Billing");
        internal.addItem(new SideNavItem("Service Catalog", "internal-service-catalog"));
        internal.addItem(new SideNavItem("Internal Invoices", "internal-invoices"));
        internal.addItem(new SideNavItem("Cost Allocation", "cost-allocation"));
        internal.addItem(new SideNavItem("Elimination Report", "elimination-report"));
        nav.addItem(internal);

        SideNavItem accounting = new SideNavItem("Accounting");
        accounting.addItem(new SideNavItem("Chart of Accounts", "chart-of-accounts"));
        accounting.addItem(new SideNavItem("Account Mappings", "account-mappings"));
        accounting.addItem(new SideNavItem("Posting Workbench", "posting-workbench"));
        accounting.addItem(new SideNavItem("Journal Entries", "journal-entries"));
        accounting.addItem(new SideNavItem("Trial Balance", "trial-balance"));
        accounting.addItem(new SideNavItem("General Ledger", "general-ledger"));
        accounting.addItem(new SideNavItem("GL Integration", "gl-integration"));
        accounting.addItem(new SideNavItem("GL Reconciliation", "gl-reconciliation"));
        accounting.addItem(new SideNavItem("Financial Statements", "financial-statements"));
        accounting.addItem(new SideNavItem("Departmental Statements", "departmental-statements"));
        accounting.addItem(new SideNavItem("Period Close", "period-close"));
        nav.addItem(accounting);

        SideNavItem central = new SideNavItem("Central Control");
        central.addItem(new SideNavItem("Central Dashboard", "central-accounting"));
        central.addItem(new SideNavItem("Accounting Periods", "accounting-periods"));
        central.addItem(new SideNavItem("Department Submissions", "department-submissions"));
        central.addItem(new SideNavItem("Reports", "reports-control"));
        central.addItem(new SideNavItem("Audit Log", "audit-log"));
        nav.addItem(central);

        SideNavItem educationAdmin = new SideNavItem("Education Setup");
        educationAdmin.addItem(new SideNavItem("Academic Calendar", "academic-calendar"));
        educationAdmin.addItem(new SideNavItem("Fee Schedules", "fee-schedules"));
        nav.addItem(educationAdmin);

        SideNavItem administration = new SideNavItem("Administration");
        administration.addItem(new SideNavItem("Departments", "departments"));
        nav.addItem(administration);

        VerticalLayout drawer = new VerticalLayout(nav);
        drawer.setPadding(false);
        drawer.setSpacing(false);

        return drawer;
    }
}