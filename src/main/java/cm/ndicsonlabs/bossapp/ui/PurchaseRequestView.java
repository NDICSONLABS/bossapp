// src/main/java/com/institution/finance/ui/PurchaseRequestView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PurchaseRequest;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseRequestRepository;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "purchase-requests", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestView extends VerticalLayout {

    private final Grid<PurchaseRequest> grid = new Grid<>(PurchaseRequest.class);

    public PurchaseRequestView(
            PurchaseRequestRepository repository,
            DepartmentRepository departmentRepository
    ) {
        grid.addColumn(request -> request.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "requestedBy",
                "requestDate",
                "neededBy",
                "estimatedAmount",
                "status",
                "description"
        );
        grid.setItems(repository.findByOrderByCreatedAtDesc());

        Button newButton = new Button("New Purchase Request", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            TextField requestedBy = new TextField("Requested By");
            DatePicker neededBy = new DatePicker("Needed By");
            BigDecimalField estimatedAmount = new BigDecimalField("Estimated Amount");
            TextArea description = new TextArea("Description");

            Button save = new Button("Save", event -> {
                if (departmentBox.getValue() == null) {
                    Notification.show("Department is required.");
                    return;
                }

                PurchaseRequest request = new PurchaseRequest();
                request.setDepartment(departmentBox.getValue());
                request.setRequestedBy(requestedBy.getValue());
                request.setRequestDate(LocalDate.now());
                request.setNeededBy(neededBy.getValue());
                request.setEstimatedAmount(estimatedAmount.getValue());
                request.setDescription(description.getValue());
                request.setStatus("SUBMITTED");

                repository.save(request);
                grid.setItems(repository.findByOrderByCreatedAtDesc());
                dialog.close();
            });

            FormLayout form = new FormLayout(departmentBox, requestedBy, neededBy, estimatedAmount, description);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(repository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Purchase Requests"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}