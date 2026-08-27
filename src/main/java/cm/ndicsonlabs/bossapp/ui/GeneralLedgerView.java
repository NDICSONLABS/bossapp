// src/main/java/com/institution/finance/ui/GeneralLedgerView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.dto.LedgerLine;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import cm.ndicsonlabs.bossapp.service.AccountingReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "general-ledger", layout = MainLayout.class)
@PermitAll
public class GeneralLedgerView extends VerticalLayout {

    private final Grid<LedgerLine> grid = new Grid<>(LedgerLine.class);

    public GeneralLedgerView(
            AccountCodeRepository accountCodeRepository,
            AccountingReportService reportService
    ) {
        ComboBox<AccountCode> accountBox = new ComboBox<>("Account");
        accountBox.setItems(accountCodeRepository.findByOrderByCode());
        accountBox.setItemLabelGenerator(account -> account.getCode() + " - " + account.getName());

        DatePicker from = new DatePicker("From");
        DatePicker to = new DatePicker("To");

        from.setValue(LocalDate.now().withDayOfYear(1));
        to.setValue(LocalDate.now());

        Button refreshButton = new Button("Refresh", e -> {
            if (accountBox.getValue() == null) {
                Notification.show("Select an account.");
                return;
            }

            grid.setItems(reportService.generalLedger(
                    accountBox.getValue().getId(),
                    from.getValue(),
                    to.getValue()
            ));
        });

        grid.setColumns(
                "entryNumber",
                "entryDate",
                "description",
                "debit",
                "credit"
        );

        add(
                new H2("General Ledger"),
                new HorizontalLayout(accountBox, from, to, refreshButton),
                grid
        );
    }
}