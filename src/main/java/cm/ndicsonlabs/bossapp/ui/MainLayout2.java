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
public class MainLayout2 extends AppLayout implements RouterLayout {

    public MainLayout2() {
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

        nav.addItem(new SideNavItem("Dashboard", "dashboard"));

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