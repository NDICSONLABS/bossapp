// src/main/java/com/institution/finance/ui/JournalEntryView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.service.AccountingPostingService;
import com.vaadin.flow.component.button.Button;
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

@Route(value = "journal-entries", layout = MainLayout.class)
@PermitAll
public class JournalEntryView extends VerticalLayout {

    private final Grid<AccountingEntry> entryGrid = new Grid<>(AccountingEntry.class);
    private final Grid<AccountingEntryLine> lineGrid = new Grid<>(AccountingEntryLine.class);

    public JournalEntryView(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            AccountingPostingService postingService
    ) {
        entryGrid.setColumns(
                "entryNumber",
                "entryDate",
                "sourceType",
                "status",
                "postedBy",
                "postedAt",
                "description"
        );
        entryGrid.setItems(entryRepository.findByOrderByCreatedAtDesc());

        lineGrid.addColumn(line -> line.getAccountCode().getCode()).setHeader("Account");
        lineGrid.setColumns("debit", "credit", "description");

        entryGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
            } else {
                lineGrid.setItems(lineRepository.findByEntryId(event.getValue().getId()));
            }
        });

        Button reverseButton = new Button("Reverse Selected Entry", e -> {
            AccountingEntry selected = entryGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an accounting entry first.");
                return;
            }

            Dialog dialog = new Dialog();
            TextArea reason = new TextArea("Reversal Reason");
            reason.setWidthFull();

            Button confirm = new Button("Reverse", event -> {
                try {
                    postingService.reverseEntry(selected.getId(), reason.getValue());
                    entryGrid.setItems(entryRepository.findByOrderByCreatedAtDesc());
                    lineGrid.setItems(lineRepository.findByEntryId(selected.getId()));
                    dialog.close();
                    Notification.show("Entry reversed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(reason);
            dialog.add(form, confirm);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                entryGrid.setItems(entryRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Journal Entries"),
                new HorizontalLayout(reverseButton, refreshButton),
                entryGrid,
                new H2("Entry Lines"),
                lineGrid
        );
    }
}