// src/main/java/com/institution/finance/ui/InventoryLocationView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.InventoryLocation;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryLocationRepository;
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

@Route(value = "inventory-locations", layout = MainLayout.class)
@PermitAll
public class InventoryLocationView extends VerticalLayout {

    private final Grid<InventoryLocation> grid = new Grid<>(InventoryLocation.class);

    public InventoryLocationView(
            InventoryLocationRepository locationRepository,
            DepartmentRepository departmentRepository
    ) {
        grid.addColumn(location -> location.getDepartment().getName()).setHeader("Department");
        grid.setColumns("code", "name", "locationType", "active");
        grid.setItems(locationRepository.findByOrderByCode());

        Button newButton = new Button("New Location", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            ComboBox<String> typeBox = new ComboBox<>("Location Type");
            typeBox.setItems("PHARMACY", "STORE", "WARD", "SCHOOL_STORE", "CENTRAL_STORE");
            typeBox.setValue("STORE");

            Button save = new Button("Save", event -> {
                if (code.isEmpty() || name.isEmpty() || departmentBox.getValue() == null) {
                    Notification.show("Code, name, and department are required.");
                    return;
                }

                InventoryLocation location = new InventoryLocation();
                location.setCode(code.getValue());
                location.setName(name.getValue());
                location.setDepartment(departmentBox.getValue());
                location.setLocationType(typeBox.getValue());
                location.setActive(true);

                locationRepository.save(location);
                grid.setItems(locationRepository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, departmentBox, typeBox);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(locationRepository.findByOrderByCode())
        );

        add(
                new H2("Inventory Locations"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}