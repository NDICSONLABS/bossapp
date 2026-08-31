// src/main/java/com/institution/finance/ui/TreasuryDashboardView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.dto.CashPositionLine;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.treasury.TreasuryService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "treasury-dashboard", layout = MainLayout.class)
@PermitAll
public class TreasuryDashboardView extends VerticalLayout {

    private final Grid<TreasuryAccount> accountGrid = new Grid<>(TreasuryAccount.class);
    private final Grid<CashPositionLine> positionGrid = new Grid<>(CashPositionLine.class);

    public TreasuryDashboardView(
            TreasuryAccountRepository accountRepository,
            TreasuryService treasuryService
    ) {
        accountGrid.addColumn(account -> account.getDepartment() != null
                ? account.getDepartment().getName()
                : "").setHeader("Department");
        accountGrid.setColumns(
                "code",
                "name",
                "accountType",
                "currency",
                "openingBalance",
                "currentBalance",
                "active"
        );

        positionGrid.setColumns(
                "departmentName",
                "accountCount",
                "totalBalance"
        );

        Button refreshButton = new Button("Refresh", e -> refresh(accountRepository, treasuryService));

        refresh(accountRepository, treasuryService);

        add(
                new H2("Treasury Dashboard"),
                new HorizontalLayout(refreshButton),
                new H2("Treasury Accounts"),
                accountGrid,
                new H2("Cash Position by Department"),
                positionGrid
        );
    }

    private void refresh(TreasuryAccountRepository accountRepository, TreasuryService treasuryService) {
        accountGrid.setItems(accountRepository.findByActiveTrueOrderByCode());
        positionGrid.setItems(treasuryService.cashPositionByDepartment());
    }
}