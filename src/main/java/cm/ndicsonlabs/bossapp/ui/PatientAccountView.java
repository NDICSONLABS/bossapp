package cm.ndicsonlabs.bossapp.ui;


import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.InsuranceProvider;
import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.InsuranceProviderRepository;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "patient-accounts", layout = MainLayout.class)
@PermitAll
public class PatientAccountView extends VerticalLayout {

    private final Grid<PatientAccount> grid = new Grid<>(PatientAccount.class);

    public PatientAccountView(
            PatientAccountRepository repository,
            DepartmentRepository departmentRepository,
            InsuranceProviderRepository insuranceProviderRepository
    ) {
        grid.setColumns("patientNumber", "fullName", "active");
        grid.setItems(repository.findAll());

        Button addButton = new Button("New Patient Account", e -> {
            Dialog dialog = new Dialog();

            TextField patientNumber = new TextField("Patient Number");
            TextField fullName = new TextField("Full Name");

            ComboBox<Department> departmentBox = new ComboBox<>("Facility");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            ComboBox<InsuranceProvider> insurerBox = new ComboBox<>("Insurance Provider");
            insurerBox.setItems(insuranceProviderRepository.findAll());
            insurerBox.setItemLabelGenerator(InsuranceProvider::getName);

            Button save = new Button("Save", event -> {
                if (patientNumber.isEmpty() || fullName.isEmpty() || departmentBox.getValue() == null) {
                    Notification.show("Patient number, full name, and facility are required.");
                    return;
                }

                PatientAccount account = new PatientAccount();
                account.setPatientNumber(patientNumber.getValue());
                account.setFullName(fullName.getValue());
                account.setDepartment(departmentBox.getValue());
                account.setInsuranceProvider(insurerBox.getValue());
                account.setActive(true);

                repository.save(account);
                grid.setItems(repository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(patientNumber, fullName, departmentBox, insurerBox);
            dialog.add(form, save);
            dialog.open();
        });

        add(new H2("Patient Accounts"), addButton, grid);
    }
}