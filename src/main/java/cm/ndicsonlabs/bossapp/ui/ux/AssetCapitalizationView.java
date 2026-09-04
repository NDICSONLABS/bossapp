// src/main/java/com/institution/finance/ui/AssetCapitalizationView.java
package cm.ndicsonlabs.bossapp.ui.ux;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetCategory;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.repository.fixedasset.AssetCategoryRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.service.fixedasset.AssetManagementService;
import cm.ndicsonlabs.bossapp.ui.MainLayout2;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "asset-capitalizations", layout = MainLayout2.class)
@PermitAll
public class AssetCapitalizationView extends VerticalLayout {

    public AssetCapitalizationView(
            AssetManagementService assetService,
            AssetCategoryRepository categoryRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository
    ) {
        PageHeader header = new PageHeader(
                "Asset Capitalization",
                "Capitalize assets using a guided wizard."
        );

        Button openWizard = new Button("Capitalize Asset");
        openWizard.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        openWizard.addClickListener(e -> {
            WizardModel model = new WizardModel();

            List<WizardStep> steps = new ArrayList<>();
            steps.add(new IdentificationStep(model, categoryRepository, departmentRepository, employeeRepository));
            steps.add(new FinancialStep(model));
            steps.add(new ReviewStep(model));

            WizardDialog wizard = new WizardDialog("Capitalize Fixed Asset", steps);

            wizard.setOnFinish(() -> {
                try {
                    assetService.capitalizeAsset(
                            model.category.getId(),
                            model.department.getId(),
                            model.custodian != null ? model.custodian.getId() : null,
                            model.description,
                            model.serialNumber,
                            model.physicalLocation,
                            model.acquisitionDate,
                            model.cost
                    );

                    Notification.show("Asset capitalized successfully.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            wizard.open();
        });

        header.addToolbarComponent(openWizard);

        add(header);
    }

    private static class WizardModel {
        AssetCategory category;
        Department department;
        Employee custodian;
        String description;
        String serialNumber;
        String physicalLocation;
        LocalDate acquisitionDate;
        BigDecimal cost;
    }

    private static class IdentificationStep implements WizardStep {

        private final WizardModel model;
        private final VerticalLayout content = new VerticalLayout();

        private final ComboBox<AssetCategory> categoryBox = new ComboBox<>("Asset Category");
        private final ComboBox<Department> departmentBox = new ComboBox<>("Department");
        private final ComboBox<Employee> custodianBox = new ComboBox<>("Custodian");
        private final TextField description = new TextField("Description");
        private final TextField serialNumber = new TextField("Serial Number");
        private final TextField physicalLocation = new TextField("Physical Location");
        private final DatePicker acquisitionDate = new DatePicker("Acquisition Date");

        private IdentificationStep(
                WizardModel model,
                AssetCategoryRepository categoryRepository,
                DepartmentRepository departmentRepository,
                EmployeeRepository employeeRepository
        ) {
            this.model = model;

            categoryBox.setItems(categoryRepository.findByActiveTrueOrderByCode());
            categoryBox.setItemLabelGenerator(AssetCategory::toString);

            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            custodianBox.setItems(employeeRepository.findByActiveTrueOrderByEmployeeNumber());
            custodianBox.setItemLabelGenerator(Employee::toString);
            custodianBox.setClearButtonVisible(true);

            acquisitionDate.setValue(LocalDate.now());

            FormLayout form = new FormLayout(
                    categoryBox,
                    departmentBox,
                    custodianBox,
                    description,
                    serialNumber,
                    physicalLocation,
                    acquisitionDate
            );

            content.add(form);
        }

        @Override
        public String getTitle() {
            return "Identification";
        }

        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public boolean isValid() {
            if (categoryBox.getValue() == null) {
                return false;
            }

            if (departmentBox.getValue() == null) {
                return false;
            }

            if (description.getValue() == null || description.getValue().isBlank()) {
                return false;
            }

            if (acquisitionDate.getValue() == null) {
                return false;
            }

            model.category = categoryBox.getValue();
            model.department = departmentBox.getValue();
            model.custodian = custodianBox.getValue();
            model.description = description.getValue();
            model.serialNumber = serialNumber.getValue();
            model.physicalLocation = physicalLocation.getValue();
            model.acquisitionDate = acquisitionDate.getValue();

            return true;
        }
    }

    private static class FinancialStep implements WizardStep {

        private final WizardModel model;
        private final VerticalLayout content = new VerticalLayout();
        private final BigDecimalField cost = new BigDecimalField("Acquisition Cost");

        private FinancialStep(WizardModel model) {
            this.model = model;

            FormLayout form = new FormLayout(cost);
            content.add(form);
        }

        @Override
        public String getTitle() {
            return "Financial Details";
        }

        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public boolean isValid() {
            if (cost.getValue() == null || cost.getValue().signum() <= 0) {
                return false;
            }

            model.cost = cost.getValue();

            return true;
        }
    }

    private static class ReviewStep implements WizardStep {

        private final WizardModel model;
        private final VerticalLayout content = new VerticalLayout();
        private final Span summary = new Span();

        private ReviewStep(WizardModel model) {
            this.model = model;
            content.add(summary);
        }

        @Override
        public String getTitle() {
            return "Review";
        }

        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public void onEnter() {
            summary.setText(
                    "Category: " + safe(model.category) +
                    ", Department: " + safe(model.department) +
                    ", Cost: " + safe(model.cost) +
                    ", Date: " + safe(model.acquisitionDate)
            );
        }

        @Override
        public boolean isValid() {
            return true;
        }

        private String safe(Object value) {
            return value != null ? value.toString() : "";
        }
    }
}