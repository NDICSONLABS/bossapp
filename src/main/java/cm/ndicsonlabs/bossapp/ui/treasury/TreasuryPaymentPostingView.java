// src/main/java/com/institution/finance/ui/TreasuryPaymentPostingView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.treasury.TreasuryService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "treasury-payment-posting", layout = MainLayout.class)
@PermitAll
public class TreasuryPaymentPostingView extends VerticalLayout {

    private final Grid<Payment> paymentGrid = new Grid<>(Payment.class);

    public TreasuryPaymentPostingView(
            PaymentRepository paymentRepository,
            TreasuryAccountRepository accountRepository,
            TreasuryService treasuryService
    ) {
        paymentGrid.setColumns(
                "paymentNumber",
                "paymentDate",
                "direction",
                "amount",
                "payerOrPayee",
                "status"
        );
        paymentGrid.setItems(paymentRepository.findAll());

        ComboBox<TreasuryAccount> accountBox = new ComboBox<>("Treasury Account");
        accountBox.setItems(accountRepository.findByActiveTrueOrderByCode());
        accountBox.setItemLabelGenerator(TreasuryAccount::toString);

        Button postButton = new Button("Post Selected Payment to Treasury", e -> {
            Payment selectedPayment = paymentGrid.asSingleSelect().getValue();

            if (selectedPayment == null) {
                Notification.show("Select a payment.");
                return;
            }

            if (accountBox.getValue() == null) {
                Notification.show("Select a treasury account.");
                return;
            }

            try {
                treasuryService.postPaymentToAccount(selectedPayment.getId(), accountBox.getValue().getId());
                Notification.show("Payment posted to treasury account.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                paymentGrid.setItems(paymentRepository.findAll())
        );

        add(
                new H2("Treasury Payment Posting"),
                new HorizontalLayout(accountBox, postButton, refreshButton),
                paymentGrid
        );
    }
}