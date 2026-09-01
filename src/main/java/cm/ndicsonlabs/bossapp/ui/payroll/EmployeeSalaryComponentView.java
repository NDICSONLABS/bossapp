// src/main/java/com/institution/finance/ui/EmployeeSalaryComponentView.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.domain.payroll.EmployeeSalaryComponent;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollComponent;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeSalaryComponentRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollComponentRepository;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "employee-salary-components", layout = MainLayout.class)
@PermitAll
public class EmployeeSalaryComponentView extends VerticalLayout {

    private final Grid<EmployeeSalaryComponent> grid = new Grid<>(EmployeeSalaryComponent.class);

    public EmployeeSalaryComponentView(
            EmployeeSalaryComponentRepository repository,
            EmployeeRepository employeeRepository,
            PayrollComponentRepository componentRepository
    ) {
        grid.addColumn(item -> item.getEmployee().getEmployeeNumber()).setHeader("Employee");
        grid.addColumn(item -> item.getPayrollComponent().getName()).setHeader("Component");
        grid.setColumns(
                "amount",
                "percentage",
                "effectiveDate",
                "endDate",
                "active"
        );
        grid.setItems(repository.findAll());

        Button newButton = new Button("Assign Salary Component", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Employee> employeeBox = new ComboBox<>("Employee");
            employeeBox.setItems(employeeRepository.findByActiveTrueOrderByEmployeeNumber());
            employeeBox.setItemLabelGenerator(Employee::toString);

            ComboBox<PayrollComponent> componentBox = new ComboBox<>("Payroll Component");
            componentBox.setItems(componentRepository.findByActiveTrueOrderByCode());
            componentBox.setItemLabelGenerator(PayrollComponent::toString);

            BigDecimalField amount = new BigDecimalField("Amount");
            BigDecimalField percentage = new BigDecimalField("Percentage");

            Button save = new Button("Save", event -> {
                if (employeeBox.getValue() == null || componentBox.getValue() == null) {
                    Notification.show("Employee and payroll component are required.");
                    return;
                }

                EmployeeSalaryComponent item = new EmployeeSalaryComponent();
                item.setEmployee(employeeBox.getValue());
                item.setPayrollComponent(componentBox.getValue());
                item.setAmount(amount.getValue());
                item.setPercentage(percentage.getValue());
                item.setActive(true);

                repository.save(item);
                grid.setItems(repository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(employeeBox, componentBox, amount, percentage);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(repository.findAll())
        );

        add(
                new H2("Employee Salary Components"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}