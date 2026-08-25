// src/main/java/com/institution/finance/ui/PaymentView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "payments", layout = MainLayout.class)
@PermitAll
public class PaymentView extends VerticalLayout {

    public PaymentView(PaymentRepository paymentRepository) {
        Grid<Payment> grid = new Grid<>(Payment.class);
        grid.setColumns(
                "paymentNumber",
                "paymentDate",
                "direction",
                "amount",
                "unallocatedAmount",
                "payerOrPayee",
                "status"
        );
        grid.setItems(paymentRepository.findAll());

        add(new H2("Payments"), grid);
    }
}