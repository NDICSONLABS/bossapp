package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierBatch;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierBatchRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Route(value = "supplier-batches", layout = MainLayout.class)
@PermitAll
public class SupplierBatchView extends VerticalLayout {

    private final Grid<SupplierBatch> grid = new Grid<>(SupplierBatch.class);

    public SupplierBatchView(
            SupplierBatchRepository batchRepository,
            SupplierRepository supplierRepository,
            DepartmentRepository departmentRepository
    ) {
        grid.addColumn(batch -> batch.getSupplier().getName()).setHeader("Supplier");
        grid.addColumn(batch -> batch.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "batchNumber",
                "expiryDate",
                "quantity",
                "unitCost",
                "amount",
                "status"
        );
        grid.setItems(batchRepository.findByOrderByCreatedAtDesc());

        Button newBatchButton = new Button("New Batch", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Supplier> supplierBox = new ComboBox<>("Supplier");
            supplierBox.setItems(supplierRepository.findAll());
            supplierBox.setItemLabelGenerator(Supplier::getName);

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            TextField batchNumber = new TextField("Batch Number");
            DatePicker expiryDate = new DatePicker("Expiry Date");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            BigDecimalField unitCost = new BigDecimalField("Unit Cost");

            Button save = new Button("Save", event -> {
                if (supplierBox.getValue() == null || departmentBox.getValue() == null || batchNumber.isEmpty()) {
                    Notification.show("Supplier, department, and batch number are required.");
                    return;
                }

                SupplierBatch batch = new SupplierBatch();
                batch.setSupplier(supplierBox.getValue());
                batch.setDepartment(departmentBox.getValue());
                batch.setBatchNumber(batchNumber.getValue());
                batch.setExpiryDate(expiryDate.getValue());
                batch.setQuantity(quantity.getValue());
                batch.setUnitCost(unitCost.getValue());

                BigDecimal amount = batch.getQuantity() != null && batch.getUnitCost() != null
                        ? batch.getQuantity().multiply(batch.getUnitCost()).setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                batch.setAmount(amount);
                batch.setStatus("ACTIVE");

                batchRepository.save(batch);
                grid.setItems(batchRepository.findByOrderByCreatedAtDesc());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    supplierBox,
                    departmentBox,
                    batchNumber,
                    expiryDate,
                    quantity,
                    unitCost
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(batchRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Supplier Batches"),
                new HorizontalLayout(newBatchButton, refreshButton),
                grid
        );
    }
}