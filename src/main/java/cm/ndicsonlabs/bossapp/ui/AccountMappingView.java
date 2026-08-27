// src/main/java/com/institution/finance/ui/AccountMappingView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "account-mappings", layout = MainLayout.class)
@PermitAll
public class AccountMappingView extends VerticalLayout {

    private final Grid<AccountMapping> grid = new Grid<>(AccountMapping.class);

    public AccountMappingView(
            AccountMappingRepository mappingRepository,
            AccountCodeRepository accountCodeRepository
    ) {
        grid.addColumn(mapping -> mapping.getAccountCode().getCode()).setHeader("Account Code");
        grid.addColumn(mapping -> mapping.getAccountCode().getName()).setHeader("Account Name");
        grid.setColumns("mappingType", "active");
        grid.setItems(mappingRepository.findAllByOrderByMappingType());

        ComboBox<AccountCode> accountBox = new ComboBox<>("Account");
        accountBox.setItems(accountCodeRepository.findByOrderByCode());
        accountBox.setItemLabelGenerator(account -> account.getCode() + " - " + account.getName());

        Button saveButton = new Button("Assign Account to Selected Mapping", e -> {
            AccountMapping selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an account mapping first.");
                return;
            }

            if (accountBox.getValue() == null) {
                Notification.show("Select an account.");
                return;
            }

            selected.setAccountCode(accountBox.getValue());
            selected.setActive(true);

            mappingRepository.save(selected);
            grid.setItems(mappingRepository.findAllByOrderByMappingType());
            Notification.show("Account mapping updated.");
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(mappingRepository.findAllByOrderByMappingType())
        );

        add(
                new H2("Account Mappings"),
                new HorizontalLayout(accountBox, saveButton, refreshButton),
                grid
        );
    }
}