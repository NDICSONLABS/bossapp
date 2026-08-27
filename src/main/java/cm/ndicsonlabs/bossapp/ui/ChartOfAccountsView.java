// src/main/java/com/institution/finance/ui/ChartOfAccountsView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "chart-of-accounts", layout = MainLayout.class)
@PermitAll
public class ChartOfAccountsView extends VerticalLayout {

    private final Grid<AccountCode> grid = new Grid<>(AccountCode.class);

    public ChartOfAccountsView(AccountCodeRepository repository) {
        grid.setColumns("code", "name", "accountType", "normalBalance", "active");
        grid.setItems(repository.findByOrderByCode());

        Button addButton = new Button("New Account", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<String> accountType = new ComboBox<>("Account Type");
            accountType.setItems("ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE");
            accountType.setValue("ASSET");

            ComboBox<String> normalBalance = new ComboBox<>("Normal Balance");
            normalBalance.setItems("DEBIT", "CREDIT");
            normalBalance.setValue("DEBIT");

            Button save = new Button("Save", event -> {
                if (code.isEmpty() || name.isEmpty()) {
                    Notification.show("Code and name are required.");
                    return;
                }

                AccountCode account = new AccountCode();
                account.setCode(code.getValue());
                account.setName(name.getValue());
                account.setAccountType(accountType.getValue());
                account.setNormalBalance(normalBalance.getValue());
                account.setActive(true);

                repository.save(account);
                grid.setItems(repository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, accountType, normalBalance);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(repository.findByOrderByCode())
        );

        add(
                new H2("Chart of Accounts"),
                new HorizontalLayout(addButton, refreshButton),
                grid
        );
    }
}