// src/main/java/com/institution/finance/ui/AccountingPeriodView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.service.CentralAccountingService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "accounting-periods", layout = MainLayout.class)
@PermitAll
public class AccountingPeriodView extends VerticalLayout {

    private final Grid<AccountingPeriod> grid = new Grid<>(AccountingPeriod.class);

    public AccountingPeriodView(
            AccountingPeriodRepository repository,
            CentralAccountingService centralAccountingService,
            CurrentUserService currentUserService
    ) {
        grid.setColumns(
                "fiscalYear",
                "periodNumber",
                "startDate",
                "endDate",
                "status",
                "openedBy",
                "closedBy",
                "lockedDate",
                "reopenedBy",
                "reopenReason"
        );

        grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc());

        boolean canOpen = currentUserService.hasPrivilege("ACCOUNTING_PERIOD_OPEN");
        boolean canLock = currentUserService.hasPrivilege("ACCOUNTING_PERIOD_LOCK");

        Button openButton = new Button("Open Period", event -> {
            AccountingPeriod selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an accounting period first.");
                return;
            }

            Dialog dialog = new Dialog();
            TextArea reason = new TextArea("Reopen Reason");
            reason.setWidthFull();

            Button confirm = new Button("Open Period", e -> {
                try {
                    centralAccountingService.openPeriod(selected.getId(), reason.getValue());
                    grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc());
                    dialog.close();
                    Notification.show("Period opened.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(reason);
            dialog.add(form, confirm);
            dialog.open();
        });

        Button lockButton = new Button("Lock Period", event -> {
            AccountingPeriod selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an accounting period first.");
                return;
            }

            ConfirmDialog dialog = new ConfirmDialog(
                    "Lock Period",
                    "Lock " + selected + "? Locked periods prevent new department submissions.",
                    "OK",
                    confirm -> {
                        try {
                            centralAccountingService.lockPeriod(selected.getId());
                            grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc());
                            Notification.show("Period locked.");
                        } catch (Exception ex) {
                            Notification.show(ex.getMessage());
                        }
                    }
            );

            dialog.setCancelable(true);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", event ->
                grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc())
        );

        openButton.setEnabled(canOpen);
        lockButton.setEnabled(canLock);

        add(
                new H2("Accounting Periods"),
                new HorizontalLayout(openButton, lockButton, refreshButton),
                grid
        );
    }
}
//// src/main/java/com/institution/finance/ui/AccountingPeriodView.java
//package cm.ndicsonlabs.bossapp.ui;
//
//import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
//import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
//import cm.ndicsonlabs.bossapp.service.CentralAccountingService;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.H2;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.PermitAll;
//
//@Route(value = "accounting-periods", layout = MainLayout.class)
//@PermitAll
//public class AccountingPeriodView extends VerticalLayout {
//
//    private final Grid<AccountingPeriod> grid = new Grid<>(AccountingPeriod.class);
//
//    public AccountingPeriodView(
//            AccountingPeriodRepository repository,
//            CentralAccountingService centralAccountingService
//    ) {
//        grid.setColumns(
//                "fiscalYear",
//                "periodNumber",
//                "startDate",
//                "endDate",
//                "status",
//                "openedBy",
//                "closedBy",
//                "lockedDate"
//        );
//
//        grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc());
//
//        Button lockButton = new Button("Lock Period", event -> {
//            AccountingPeriod selected = grid.asSingleSelect().getValue();
//
//            if (selected == null) {
//                Notification.show("Select an accounting period first.");
//                return;
//            }
//
//            ConfirmDialog dialog = new ConfirmDialog(
//                    "Lock Period",
//                    "Lock " + selected + "? Locked periods prevent new department submissions.",
//                    "YES or NO",
//                    confirm -> {
//                        try {
//                            centralAccountingService.lockPeriod(selected.getId());
//                            grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc());
//                            Notification.show("Period locked.");
//                        } catch (Exception ex) {
//                            Notification.show(ex.getMessage());
//                        }
//                    }
//            );
//
//            dialog.setCancelable(true);
//            dialog.open();
//        });
//
//        Button refreshButton = new Button("Refresh", event ->
//                grid.setItems(repository.findByOrderByFiscalYearDescPeriodNumberAsc())
//        );
//
//        add(
//                new H2("Accounting Periods"),
//                new HorizontalLayout(lockButton, refreshButton),
//                grid
//        );
//    }
//}