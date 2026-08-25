package cm.ndicsonlabs.bossapp.ui;


import cm.ndicsonlabs.bossapp.domain.InsuranceClaim;
import cm.ndicsonlabs.bossapp.domain.InsuranceProvider;
import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.repository.InsuranceClaimRepository;
import cm.ndicsonlabs.bossapp.repository.InsuranceProviderRepository;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
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

@Route(value = "insurance-claims", layout = MainLayout.class)
@PermitAll
public class InsuranceClaimView extends VerticalLayout {

    private final Grid<InsuranceClaim> grid = new Grid<>(InsuranceClaim.class);

    public InsuranceClaimView(
            InsuranceClaimRepository repository,
            PatientAccountRepository patientAccountRepository,
            InsuranceProviderRepository insuranceProviderRepository,
            PatientBillingService patientBillingService
    ) {
        grid.addColumn(claim -> claim.getPatientAccount().getPatientNumber()).setHeader("Patient");
        grid.setColumns("claimNumber", "claimDate", "amount", "approvedAmount", "paidAmount", "status");
        grid.setItems(repository.findAll());

        Button newClaimButton = new Button("New Insurance Claim", e -> {
            Dialog dialog = new Dialog();

            ComboBox<PatientAccount> patientBox = new ComboBox<>("Patient");
            patientBox.setItems(patientAccountRepository.findAll());
            patientBox.setItemLabelGenerator(p -> p.getPatientNumber() + " - " + p.getFullName());

            ComboBox<InsuranceProvider> providerBox = new ComboBox<>("Insurance Provider");
            providerBox.setItems(insuranceProviderRepository.findAll());
            providerBox.setItemLabelGenerator(InsuranceProvider::getName);

            TextField claimNumber = new TextField("Claim Number");
            DatePicker claimDate = new DatePicker("Claim Date");
            BigDecimalField amount = new BigDecimalField("Amount");

            Button save = new Button("Save", event -> {
                if (patientBox.getValue() == null || providerBox.getValue() == null ||
                        claimNumber.isEmpty() || amount.getValue() == null || amount.getValue().signum() <= 0) {
                    Notification.show("Patient, insurance provider, claim number, and positive amount are required.");
                    return;
                }

                InsuranceClaim claim = new InsuranceClaim();
                claim.setClaimNumber(claimNumber.getValue());
                claim.setPatientAccount(patientBox.getValue());
                claim.setInsuranceProvider(providerBox.getValue());
                claim.setClaimDate(claimDate.getValue() != null ? claimDate.getValue() : LocalDate.now());
                claim.setAmount(amount.getValue());
                claim.setStatus("SUBMITTED");

                repository.save(claim);
                grid.setItems(repository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(patientBox, providerBox, claimNumber, claimDate, amount);
            dialog.add(form, save);
            dialog.open();
        });

        Button paymentButton = new Button("Record Insurance Payment", e -> {
            InsuranceClaim selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an insurance claim first.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField payer = new TextField("Payer");

            Button save = new Button("Save", event -> {
                try {
                    patientBillingService.recordInsuranceClaimPayment(selected, amount.getValue(), payer.getValue(), LocalDate.now());
                    grid.setItems(repository.findAll());
                    dialog.close();
                    Notification.show("Insurance payment recorded.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(amount, payer);
            dialog.add(form, save);
            dialog.open();
        });

        add(
                new H2("Insurance Claims"),
                new HorizontalLayout(newClaimButton, paymentButton),
                grid
        );
    }
}