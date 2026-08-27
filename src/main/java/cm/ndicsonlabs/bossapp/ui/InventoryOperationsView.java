// src/main/java/com/institution/finance/ui/InventoryOperationsView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.InventoryItem;
import cm.ndicsonlabs.bossapp.domain.InventoryLocation;
import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.dto.InventoryBalanceLine;
import cm.ndicsonlabs.bossapp.repository.InventoryItemRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryLocationRepository;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
import cm.ndicsonlabs.bossapp.service.InventoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "inventory-operations", layout = MainLayout.class)
@PermitAll
public class InventoryOperationsView extends VerticalLayout {

    private final Grid<InventoryBalanceLine> grid = new Grid<>(InventoryBalanceLine.class);
    private final Span valuationSpan = new Span();

    public InventoryOperationsView(
            InventoryService inventoryService,
            InventoryLocationRepository locationRepository,
            InventoryItemRepository itemRepository,
            PatientAccountRepository patientAccountRepository
    ) {
        grid.setColumns(
                "itemCode",
                "itemName",
                "locationCode",
                "batchNumber",
                "expiryDate",
                "quantityOnHand",
                "averageCost",
                "stockValue"
        );

        refresh(inventoryService, null);

        Button receiveButton = new Button("Receive Stock", e -> {
            Dialog dialog = new Dialog();

            ComboBox<InventoryLocation> locationBox = locationBox(locationRepository);
            ComboBox<InventoryItem> itemBox = itemBox(itemRepository);

            TextField batchNumber = new TextField("Batch Number");
            DatePicker expiryDate = new DatePicker("Expiry Date");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            BigDecimalField unitCost = new BigDecimalField("Unit Cost");
            TextArea notes = new TextArea("Notes");

            Button save = new Button("Receive", event -> {
                try {
                    inventoryService.receiveStock(
                            locationBox.getValue().getId(),
                            itemBox.getValue().getId(),
                            batchNumber.getValue(),
                            expiryDate.getValue(),
                            quantity.getValue(),
                            unitCost.getValue(),
                            "MANUAL",
                            null,
                            notes.getValue()
                    );

                    refresh(inventoryService, null);
                    dialog.close();
                    Notification.show("Stock received.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    locationBox,
                    itemBox,
                    batchNumber,
                    expiryDate,
                    quantity,
                    unitCost,
                    notes
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button issueButton = new Button("Issue Stock", e -> {
            Dialog dialog = new Dialog();

            ComboBox<InventoryLocation> locationBox = locationBox(locationRepository);
            ComboBox<InventoryItem> itemBox = itemBox(itemRepository);

            TextField batchNumber = new TextField("Batch Number");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            TextArea notes = new TextArea("Notes");

            Button save = new Button("Issue", event -> {
                try {
                    inventoryService.issueStock(
                            locationBox.getValue().getId(),
                            itemBox.getValue().getId(),
                            batchNumber.getValue(),
                            quantity.getValue(),
                            "MANUAL",
                            null,
                            notes.getValue()
                    );

                    refresh(inventoryService, null);
                    dialog.close();
                    Notification.show("Stock issued.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(locationBox, itemBox, batchNumber, quantity, notes);
            dialog.add(form, save);
            dialog.open();
        });

        Button transferButton = new Button("Transfer Stock", e -> {
            Dialog dialog = new Dialog();

            ComboBox<InventoryLocation> fromBox = locationBox(locationRepository);
            ComboBox<InventoryLocation> toBox = locationBox(locationRepository);
            ComboBox<InventoryItem> itemBox = itemBox(itemRepository);

            TextField batchNumber = new TextField("Batch Number");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            TextArea notes = new TextArea("Notes");

            Button save = new Button("Transfer", event -> {
                try {
                    inventoryService.transferStock(
                            fromBox.getValue().getId(),
                            toBox.getValue().getId(),
                            itemBox.getValue().getId(),
                            batchNumber.getValue(),
                            quantity.getValue(),
                            notes.getValue()
                    );

                    refresh(inventoryService, null);
                    dialog.close();
                    Notification.show("Stock transferred.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(fromBox, toBox, itemBox, batchNumber, quantity, notes);
            dialog.add(form, save);
            dialog.open();
        });

        Button adjustButton = new Button("Adjust / Write Off", e -> {
            Dialog dialog = new Dialog();

            ComboBox<InventoryLocation> locationBox = locationBox(locationRepository);
            ComboBox<InventoryItem> itemBox = itemBox(itemRepository);

            TextField batchNumber = new TextField("Batch Number");
            BigDecimalField signedQuantity = new BigDecimalField("Signed Quantity");
            Checkbox writeOff = new Checkbox("Write Off");
            TextArea notes = new TextArea("Notes");

            Button save = new Button("Adjust", event -> {
                try {
                    inventoryService.adjustStock(
                            locationBox.getValue().getId(),
                            itemBox.getValue().getId(),
                            batchNumber.getValue(),
                            signedQuantity.getValue(),
                            writeOff.getValue(),
                            notes.getValue()
                    );

                    refresh(inventoryService, null);
                    dialog.close();
                    Notification.show("Stock adjusted.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(locationBox, itemBox, batchNumber, signedQuantity, writeOff, notes);
            dialog.add(form, save);
            dialog.open();
        });

        Button issueToPatientButton = new Button("Issue to Patient", e -> {
            Dialog dialog = new Dialog();

            ComboBox<InventoryLocation> locationBox = locationBox(locationRepository);
            ComboBox<InventoryItem> itemBox = itemBox(itemRepository);

            ComboBox<PatientAccount> patientBox = new ComboBox<>("Patient");
            patientBox.setItems(patientAccountRepository.findAll());
            patientBox.setItemLabelGenerator(p -> p.getPatientNumber() + " - " + p.getFullName());

            TextField batchNumber = new TextField("Batch Number");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            TextArea notes = new TextArea("Notes");

            Button save = new Button("Issue to Patient", event -> {
                try {
                    inventoryService.issueToPatient(
                            locationBox.getValue().getId(),
                            itemBox.getValue().getId(),
                            batchNumber.getValue(),
                            quantity.getValue(),
                            patientBox.getValue().getId(),
                            notes.getValue()
                    );

                    refresh(inventoryService, null);
                    dialog.close();
                    Notification.show("Stock issued to patient and patient charge created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(locationBox, itemBox, patientBox, batchNumber, quantity, notes);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e -> refresh(inventoryService, null));

        add(
                new H2("Inventory Operations"),
                valuationSpan,
                new HorizontalLayout(receiveButton, issueButton, transferButton, adjustButton, issueToPatientButton, refreshButton),
                grid
        );
    }

    private void refresh(InventoryService inventoryService, java.util.UUID locationId) {
        grid.setItems(inventoryService.balanceLines(locationId));
        valuationSpan.setText("Total stock value: " + inventoryService.valuation(locationId));
    }

    private ComboBox<InventoryLocation> locationBox(InventoryLocationRepository repository) {
        ComboBox<InventoryLocation> box = new ComboBox<>("Location");
        box.setItems(repository.findByOrderByCode());
        box.setItemLabelGenerator(InventoryLocation::getName);
        return box;
    }

    private ComboBox<InventoryItem> itemBox(InventoryItemRepository repository) {
        ComboBox<InventoryItem> box = new ComboBox<>("Item");
        box.setItems(repository.findByOrderByCode());
        box.setItemLabelGenerator(InventoryItem::toString);
        return box;
    }
}