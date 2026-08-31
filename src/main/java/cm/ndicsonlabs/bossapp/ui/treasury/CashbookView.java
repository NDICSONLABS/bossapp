// src/main/java/com/institution/finance/ui/CashbookView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.treasury.TreasuryService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "cashbook", layout = MainLayout.class)
@PermitAll
public class CashbookView extends VerticalLayout {

    private final Grid<TreasuryTransaction> grid = new Grid<>(TreasuryTransaction.class);

    public CashbookView(
            TreasuryAccountRepository accountRepository,
            TreasuryService treasuryService
    ) {
        ComboBox<TreasuryAccount> accountBox = new ComboBox<>("Treasury Account");
        accountBox.setItems(accountRepository.findByActiveTrueOrderByCode());
        accountBox.setItemLabelGenerator(TreasuryAccount::toString);

        DatePicker from = new DatePicker("From");
        DatePicker to = new DatePicker("To");

        grid.setColumns(
                "transactionNumber",
                "transactionDate",
                "direction",
                "amount",
                "reference",
                "status",
                "description"
        );

        Button refreshButton = new Button("Refresh", e -> {
            if (accountBox.getValue() == null) {
                Notification.show("Select a treasury account.");
                return;
            }

            grid.setItems(treasuryService.cashbook(
                    accountBox.getValue().getId(),
                    from.getValue(),
                    to.getValue()
            ));
        });

        Button receiptButton = new Button("Record Receipt", e ->
                openManualDialog(accountBox, "IN", treasuryService, from, to, grid)
        );

        Button paymentButton = new Button("Record Payment", e ->
                openManualDialog(accountBox, "OUT", treasuryService, from, to, grid)
        );

        add(
                new H2("Cashbook"),
                new HorizontalLayout(accountBox, from, to, refreshButton, receiptButton, paymentButton),
                grid
        );
    }

    private void openManualDialog(
            ComboBox<TreasuryAccount> accountBox,
            String direction,
            TreasuryService treasuryService,
            DatePicker from,
            DatePicker to,
            Grid<TreasuryTransaction> grid
    ) {
        if (accountBox.getValue() == null) {
            Notification.show("Select a treasury account.");
            return;
        }

        Dialog dialog = new Dialog();

        BigDecimalField amount = new BigDecimalField("Amount");
        DatePicker date = new DatePicker("Date");
        date.setValue(LocalDate.now());

        TextField reference = new TextField("Reference");
        TextArea description = new TextArea("Description");

        Button save = new Button("Save", event -> {
            try {
                treasuryService.postManualTransaction(
                        accountBox.getValue().getId(),
                        direction,
                        amount.getValue(),
                        date.getValue(),
                        reference.getValue(),
                        description.getValue()
                );

                grid.setItems(treasuryService.cashbook(
                        accountBox.getValue().getId(),
                        from.getValue(),
                        to.getValue()
                ));

                dialog.close();
                Notification.show("Treasury transaction recorded.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        FormLayout form = new FormLayout(amount, date, reference, description);
        dialog.add(form, save);
        dialog.open();
    }
}