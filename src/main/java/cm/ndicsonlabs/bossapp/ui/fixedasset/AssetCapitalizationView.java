// src/main/java/com/institution/finance/ui/AssetCapitalizationView.java
package cm.ndicsonlabs.bossapp.ui.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetCategory;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.repository.fixedasset.AssetCategoryRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.service.fixedasset.AssetManagementService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "asset-capitalization", layout = MainLayout.class)
@PermitAll
public class AssetCapitalizationView extends VerticalLayout {

    public AssetCapitalizationView(
            AssetManagementService assetService,
            AssetCategoryRepository categoryRepo,
            DepartmentRepository deptRepo,
            EmployeeRepository empRepo
    ) {
        Button capitalizeButton = new Button("Capitalize New Asset", e -> {
            Dialog dialog = new Dialog();

            ComboBox<AssetCategory> categoryBox = new ComboBox<>("Category");
            categoryBox.setItems(categoryRepo.findByActiveTrueOrderByCode());
            categoryBox.setItemLabelGenerator(AssetCategory::toString);

            ComboBox<Department> deptBox = new ComboBox<>("Assigned Department / Facility");
            deptBox.setItems(deptRepo.findAll());
            deptBox.setItemLabelGenerator(Department::getName);

            ComboBox<Employee> empBox = new ComboBox<>("Custodian");
            empBox.setItems(empRepo.findByActiveTrueOrderByEmployeeNumber());
            empBox.setItemLabelGenerator(Employee::toString);
            empBox.setClearButtonVisible(true);

            TextField desc = new TextField("Description");
            TextField serial = new TextField("Serial Number");
            TextField location = new TextField("Physical Location (e.g., Room 101)");
            DatePicker acqDate = new DatePicker("Acquisition Date");
            acqDate.setValue(LocalDate.now());
            BigDecimalField cost = new BigDecimalField("Acquisition Cost");

            Button save = new Button("Capitalize & Post to GL", ev -> {
                try {
                    assetService.capitalizeAsset(
                            categoryBox.getValue().getId(), deptBox.getValue().getId(),
                            empBox.getValue() != null ? empBox.getValue().getId() : null,
                            desc.getValue(), serial.getValue(), location.getValue(),
                            acqDate.getValue(), cost.getValue()
                    );
                    dialog.close();
                    Notification.show("Asset capitalized and posted to GL.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(categoryBox, deptBox, empBox, desc, serial, location, acqDate, cost);
            dialog.add(form, save);
            dialog.open();
        });

        add(new H2("Asset Acquisition & Capitalization"), new HorizontalLayout(capitalizeButton));
    }
}