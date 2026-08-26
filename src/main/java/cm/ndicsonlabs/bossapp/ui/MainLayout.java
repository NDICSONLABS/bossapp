// src/main/java/com/institution/finance/ui/MainLayout.java
package cm.ndicsonlabs.bossapp.ui;

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

        SideNavItem finance = new SideNavItem("Finance");
        finance.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(finance);
        nav.addItem(new SideNavItem("Payments", PaymentView.class));

        SideNavItem reporting = new SideNavItem("Reporting and Control");
        reporting.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(reporting);
        nav.addItem(new SideNavItem("Reports", ReportControlView.class));
        nav.addItem(new SideNavItem("Audit Log", AuditLogView.class));

        SideNavItem central = new SideNavItem("Central Accounting");
        central.getStyle().set("font-weight", "bold").set("padding", "10px").set("display", "block");
        nav.addItem(central);
        nav.addItem(new SideNavItem("Central Dashboard", CentralDashboardView.class));
        nav.addItem(new SideNavItem("Accounting Periods", AccountingPeriodView.class));
        nav.addItem(new SideNavItem("Department Submissions", DepartmentSubmissionView.class));

        VerticalLayout drawer = new VerticalLayout(title, nav);
        drawer.setPadding(false);
        drawer.setSpacing(false);

        addToDrawer(drawer);
    }
}

