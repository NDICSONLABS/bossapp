// src/main/java/com/institution/finance/ui/EmployeeView.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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

@Route(value = "employees", layout = MainLayout.class)
@PermitAll
public class EmployeeView extends VerticalLayout {

    private final Grid<Employee> grid = new Grid<>(Employee.class);

    public EmployeeView(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository
    ) {
        grid.addColumn(employee -> employee.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "employeeNumber",
                "fullName",
                "jobTitle",
                "hireDate",
                "terminationDate",
                "active"
        );
        grid.setItems(employeeRepository.findByActiveTrueOrderByEmployeeNumber());

        Button newButton = new Button("New Employee", e -> {
            Dialog dialog = new Dialog();

            TextField employeeNumber = new TextField("Employee Number");
            TextField fullName = new TextField("Full Name");
            TextField jobTitle = new TextField("Job Title");
            TextField taxId = new TextField("Tax ID");
            TextField bankName = new TextField("Bank Name");
            TextField bankAccount = new TextField("Bank Account");

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            DatePicker hireDate = new DatePicker("Hire Date");
            Checkbox active = new Checkbox("Active");
            active.setValue(true);

            Button save = new Button("Save", event -> {
                if (employeeNumber.isEmpty() || fullName.isEmpty() || departmentBox.getValue() == null) {
                    Notification.show("Employee number, full name, and department are required.");
                    return;
                }

                Employee employee = new Employee();
                employee.setEmployeeNumber(employeeNumber.getValue());
                employee.setFullName(fullName.getValue());
                employee.setDepartment(departmentBox.getValue());
                employee.setJobTitle(jobTitle.getValue());
                employee.setTaxId(taxId.getValue());
                employee.setBankName(bankName.getValue());
                employee.setBankAccountNumber(bankAccount.getValue());
                employee.setHireDate(hireDate.getValue());
                employee.setActive(active.getValue());

                employeeRepository.save(employee);
                grid.setItems(employeeRepository.findByActiveTrueOrderByEmployeeNumber());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    employeeNumber,
                    fullName,
                    departmentBox,
                    jobTitle,
                    taxId,
                    bankName,
                    bankAccount,
                    hireDate,
                    active
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(employeeRepository.findByActiveTrueOrderByEmployeeNumber())
        );

        add(
                new H2("Employees"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}