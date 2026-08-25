// src/main/java/com/institution/finance/ui/PatientChargeView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import cm.ndicsonlabs.bossapp.domain.PatientEncounter;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PatientEncounterRepository;
import cm.ndicsonlabs.bossapp.service.PatientBillingService;
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
import java.time.LocalDate;

@Route(value = "patient-charges", layout = MainLayout.class)
@PermitAll
public class PatientChargeView extends VerticalLayout {

    private final Grid<PatientCharge> grid = new Grid<>(PatientCharge.class);

    public PatientChargeView(
            PatientChargeRepository repository,
            PatientAccountRepository patientAccountRepository,
            PatientBillingService patientBillingService
    ) {
        grid.addColumn(charge -> charge.getPatientAccount().getPatientNumber()).setHeader("Patient");
        grid.setColumns("serviceCategory", "chargeDate", "dueDate", "amount", "paidAmount", "status");
        grid.setItems(repository.findAll());

        Button newChargeButton = new Button("New Patient Charge", e -> {
            Dialog dialog = new Dialog();

            ComboBox<PatientAccount> patientBox = new ComboBox<>("Patient");
            patientBox.setItems(patientAccountRepository.findAll());
            patientBox.setItemLabelGenerator(p -> p.getPatientNumber() + " - " + p.getFullName());

            TextField serviceCategory = new TextField("Service Category");
            DatePicker dueDate = new DatePicker("Due Date");
            BigDecimalField amount = new BigDecimalField("Amount");

            Button save = new Button("Save", event -> {
                if (patientBox.getValue() == null || amount.getValue() == null || amount.getValue().signum() <= 0) {
                    Notification.show("Patient and positive amount are required.");
                    return;
                }

                PatientCharge charge = new PatientCharge();
                charge.setPatientAccount(patientBox.getValue());
                charge.setDepartment(patientBox.getValue().getDepartment());
                charge.setServiceCategory(serviceCategory.getValue());
                charge.setChargeDate(LocalDate.now());
                charge.setDueDate(dueDate.getValue() != null ? dueDate.getValue() : LocalDate.now().plusDays(15));
                charge.setAmount(amount.getValue());
                charge.setPaidAmount(BigDecimal.ZERO);
                charge.setStatus("POSTED");

                repository.save(charge);
                grid.setItems(repository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(patientBox, serviceCategory, dueDate, amount);
            dialog.add(form, save);
            dialog.open();
        });

        Button paymentButton = new Button("Record Patient Payment", e -> {
            PatientCharge selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a patient charge first.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField payer = new TextField("Payer");

            Button save = new Button("Save", event -> {
                try {
                    patientBillingService.recordPatientPayment(selected, amount.getValue(), payer.getValue(), LocalDate.now());
                    grid.setItems(repository.findAll());
                    dialog.close();
                    Notification.show("Patient payment recorded.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(amount, payer);
            dialog.add(form, save);
            dialog.open();
        });

        add(
                new H2("Patient Charges"),
                new HorizontalLayout(newChargeButton, paymentButton),
                grid
        );
    }
}
