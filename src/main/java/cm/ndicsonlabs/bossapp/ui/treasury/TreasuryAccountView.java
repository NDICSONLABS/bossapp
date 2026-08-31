// src/main/java/com/institution/finance/ui/TreasuryAccountView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.treasury.TreasuryService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "treasury-accounts", layout = MainLayout.class)
@PermitAll
public class TreasuryAccountView extends VerticalLayout {

    private final Grid<TreasuryAccount> grid = new Grid<>(TreasuryAccount.class);

    public TreasuryAccountView(
            TreasuryAccountRepository accountRepository,
            DepartmentRepository departmentRepository,
            TreasuryService treasuryService
    ) {
        grid.addColumn(account -> account.getDepartment() != null
                ? account.getDepartment().getName()
                : "").setHeader("Department");
        grid.setColumns(
                "code",
                "name",
                "accountType",
                "currency",
                "bankName",
                "accountNumber",
                "openingBalance",
                "currentBalance",
                "allowNegative",
                "active"
        );
        grid.setItems(accountRepository.findByActiveTrueOrderByCode());

        Button newButton = new Button("New Treasury Account", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<String> typeBox = new ComboBox<>("Account Type");
            typeBox.setItems("CASH", "BANK", "MOBILE_MONEY");
            typeBox.setValue("BANK");

            TextField currency = new TextField("Currency");

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);
            departmentBox.setClearButtonVisible(true);

            TextField bankName = new TextField("Bank Name");
            TextField accountNumber = new TextField("Account Number");
            TextField iban = new TextField("IBAN");
            TextField swift = new TextField("SWIFT");

            BigDecimalField openingBalance = new BigDecimalField("Opening Balance");
            Checkbox allowNegative = new Checkbox("Allow Negative Balance");

            Button save = new Button("Save", event -> {
                try {
                    treasuryService.createAccount(
                            code.getValue(),
                            name.getValue(),
                            typeBox.getValue(),
                            currency.getValue(),
                            departmentBox.getValue() != null ? departmentBox.getValue().getId() : null,
                            bankName.getValue(),
                            accountNumber.getValue(),
                            iban.getValue(),
                            swift.getValue(),
                            openingBalance.getValue(),
                            allowNegative.getValue()
                    );

                    grid.setItems(accountRepository.findByActiveTrueOrderByCode());
                    dialog.close();
                    Notification.show("Treasury account created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    code,
                    name,
                    typeBox,
                    currency,
                    departmentBox,
                    bankName,
                    accountNumber,
                    iban,
                    swift,
                    openingBalance,
                    allowNegative
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(accountRepository.findByActiveTrueOrderByCode())
        );

        add(
                new H2("Treasury Accounts"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}