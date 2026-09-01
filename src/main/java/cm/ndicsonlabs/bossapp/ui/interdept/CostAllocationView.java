// src/main/java/com/institution/finance/ui/CostAllocationView.java
package cm.ndicsonlabs.bossapp.ui.interdept;



import cm.ndicsonlabs.bossapp.domain.Department;


import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRule;
import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRuleTarget;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRuleRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRuleTargetRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRunRepository;
import cm.ndicsonlabs.bossapp.service.interdept.CostAllocationService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "cost-allocation", layout = MainLayout.class)
@PermitAll
public class CostAllocationView extends VerticalLayout {

    private final Grid<CostAllocationRule> ruleGrid = new Grid<>(CostAllocationRule.class);
    private final Grid<CostAllocationRuleTarget> targetGrid = new Grid<>(CostAllocationRuleTarget.class);

    public CostAllocationView(
            CostAllocationRuleRepository ruleRepository,
            CostAllocationRuleTargetRepository targetRepository,
            CostAllocationRunRepository runRepository,
            DepartmentRepository departmentRepository,
            CostAllocationService allocationService
    ) {
        ruleGrid.addColumn(rule -> rule.getSourceDepartment().getName()).setHeader("Source Department");
        ruleGrid.setColumns("name", "description", "active");
        ruleGrid.setItems(ruleRepository.findByActiveTrueOrderByName());

        targetGrid.addColumn(target -> target.getReceiverDepartment().getName()).setHeader("Receiver Department");
        targetGrid.setColumns("percentage");

        ruleGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                targetGrid.setItems();
            } else {
                targetGrid.setItems(targetRepository.findByRuleId(event.getValue().getId()));
            }
        });

        Button newRuleButton = new Button("New Allocation Rule", e -> {
            Dialog dialog = new Dialog();

            TextField name = new TextField("Rule Name");
            TextArea description = new TextArea("Description");

            ComboBox<Department> sourceBox = new ComboBox<>("Source Department");
            sourceBox.setItems(departmentRepository.findAll());
            sourceBox.setItemLabelGenerator(Department::getName);

            Button save = new Button("Save", event -> {
                try {
                    allocationService.createRule(
                            sourceBox.getValue().getId(),
                            name.getValue(),
                            description.getValue()
                    );

                    ruleGrid.setItems(ruleRepository.findByActiveTrueOrderByName());
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(name, sourceBox, description);
            dialog.add(form, save);
            dialog.open();
        });

        Button addTargetButton = new Button("Add Target Department", e -> {
            CostAllocationRule selectedRule = ruleGrid.asSingleSelect().getValue();

            if (selectedRule == null) {
                Notification.show("Select an allocation rule.");
                return;
            }

            Dialog dialog = new Dialog();

            ComboBox<Department> receiverBox = new ComboBox<>("Receiver Department");
            receiverBox.setItems(departmentRepository.findAll());
            receiverBox.setItemLabelGenerator(Department::getName);

            BigDecimalField percentage = new BigDecimalField("Percentage");

            Button save = new Button("Save", event -> {
                try {
                    allocationService.addTarget(
                            selectedRule.getId(),
                            receiverBox.getValue().getId(),
                            percentage.getValue()
                    );

                    targetGrid.setItems(targetRepository.findByRuleId(selectedRule.getId()));
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(receiverBox, percentage);
            dialog.add(form, save);
            dialog.open();
        });

        Button runButton = new Button("Run Allocation", e -> {
            CostAllocationRule selectedRule = ruleGrid.asSingleSelect().getValue();

            if (selectedRule == null) {
                Notification.show("Select an allocation rule.");
                return;
            }

            Dialog dialog = new Dialog();

            IntegerField year = new IntegerField("Year");
            year.setValue(LocalDate.now().getYear());

            IntegerField month = new IntegerField("Month");
            month.setValue(LocalDate.now().getMonthValue());

            BigDecimalField totalAmount = new BigDecimalField("Total Allocation Amount");

            Button save = new Button("Run", event -> {
                try {
                    allocationService.runAllocation(
                            selectedRule.getId(),
                            year.getValue(),
                            month.getValue(),
                            totalAmount.getValue()
                    );

                    dialog.close();
                    Notification.show("Cost allocation executed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(year, month, totalAmount);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e -> {
            ruleGrid.setItems(ruleRepository.findByActiveTrueOrderByName());

            CostAllocationRule selectedRule = ruleGrid.asSingleSelect().getValue();

            if (selectedRule != null) {
                targetGrid.setItems(targetRepository.findByRuleId(selectedRule.getId()));
            }
        });

        add(
                new H2("Cost Allocation"),
                new HorizontalLayout(newRuleButton, addTargetButton, runButton, refreshButton),
                ruleGrid,
                new H2("Allocation Targets"),
                targetGrid
        );
    }
}