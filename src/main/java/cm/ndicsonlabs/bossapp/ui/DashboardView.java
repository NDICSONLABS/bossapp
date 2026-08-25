// src/main/java/com/institution/finance/ui/DashboardView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    public DashboardView(
            DepartmentRepository departmentRepository,
            StudentChargeRepository studentChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository
    ) {
        add(new H2("Dashboard"));
        add(new Span("Departments: " + departmentRepository.count()));
        add(new Span("Student charges: " + studentChargeRepository.count()));
        add(new Span("Supplier invoices: " + supplierInvoiceRepository.count()));
        add(new Span("Payments: " + paymentRepository.count()));
    }
}