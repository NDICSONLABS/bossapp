// src/main/java/com/institution/finance/ui/PostingWorkbenchView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.service.AccountingPostingService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.CollaborativeJSpreadsheetkit;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetColumn;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetOptions;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetWorksheet;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.UUID;

@Route(value = "posting-workbench", layout = MainLayout.class)
@PermitAll
public class PostingWorkbenchView extends VerticalLayout {

    public PostingWorkbenchView(AccountingPostingService postingService, CurrentUserService currentUserService) {
        ComboBox<String> sourceType = new ComboBox<>("Source Type");
        sourceType.setItems(
                "STUDENT_CHARGE",
                "PATIENT_CHARGE",
                "SUPPLIER_INVOICE",
                "PAYMENT"
        );
        sourceType.setValue("STUDENT_CHARGE");

        TextField transactionId = new TextField("Transaction ID (UUID)");
        transactionId.setWidthFull();

        Button postButton = new Button("Post to Accounting", e -> {
            try {
                UUID id = UUID.fromString(transactionId.getValue());

                switch (sourceType.getValue()) {
                    case "STUDENT_CHARGE" -> postingService.postStudentCharge(id);
                    case "PATIENT_CHARGE" -> postingService.postPatientCharge(id);
                    case "SUPPLIER_INVOICE" -> postingService.postSupplierInvoice(id);
                    case "PAYMENT" -> postingService.postPayment(id);
                    default -> throw new IllegalArgumentException("Unsupported source type");
                }

                Notification.show("Transaction posted to accounting.");
            } catch (IllegalArgumentException ex) {
                Notification.show("Enter a valid UUID transaction ID.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        add(
                new H2("Posting Workbench"),
                new HorizontalLayout(sourceType, transactionId, postButton)
        );

        // Inside PostingWorkbenchView.java

        UserInfo localUser = new UserInfo(currentUserService.username(), currentUserService.username());
        String journalEntryId = transactionId.getValue();
        String topicId = "journal-entry-" + journalEntryId; // Unique per draft journal entry

        CollaborativeJSpreadsheetkit collabSheet = new CollaborativeJSpreadsheetkit(topicId, localUser);
        collabSheet.setHeight("500px");

        // Configure columns for Debits and Credits
        JSpreadsheetWorksheet ws = new JSpreadsheetWorksheet();
        List<Object> accountSources = List.of(
                "STUDENT_CHARGE",
                "PATIENT_CHARGE",
                "SUPPLIER_INVOICE",
                "PAYMENT"
        );
        ws.setColumns(List.of(
                JSpreadsheetColumn.dropdown("Account", 200, accountSources),
                JSpreadsheetColumn.text("Description", 250),
                JSpreadsheetColumn.numeric("Debit", 120),
                JSpreadsheetColumn.numeric("Credit", 120)
        ));

        collabSheet.setOptions(JSpreadsheetOptions.withSingleWorksheet(ws));

        // The CollaborativeJSpreadsheetkit automatically syncs cell changes
// across all connected users in real-time via Vaadin Collaboration Engine.
        add(collabSheet);
    }
}