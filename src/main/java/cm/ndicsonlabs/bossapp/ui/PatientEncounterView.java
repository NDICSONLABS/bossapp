package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.domain.PatientEncounter;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
import cm.ndicsonlabs.bossapp.repository.PatientEncounterRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "patient-encounters", layout = MainLayout.class)
@PermitAll
public class PatientEncounterView extends VerticalLayout {

    private final Grid<PatientEncounter> grid = new Grid<>(PatientEncounter.class);

    public PatientEncounterView(
            PatientEncounterRepository repository,
            PatientAccountRepository patientAccountRepository
    ) {
        grid.addColumn(encounter -> encounter.getPatientAccount().getPatientNumber()).setHeader("Patient");
        grid.setColumns("encounterType", "encounterDate", "status");
        grid.setItems(repository.findAll());

        Button addButton = new Button("New Encounter", e -> {
            Dialog dialog = new Dialog();

            ComboBox<PatientAccount> patientBox = new ComboBox<>("Patient");
            patientBox.setItems(patientAccountRepository.findAll());
            patientBox.setItemLabelGenerator(p -> p.getPatientNumber() + " - " + p.getFullName());

            TextField encounterType = new TextField("Encounter Type");
            DatePicker encounterDate = new DatePicker("Encounter Date");

            Button save = new Button("Save", event -> {
                if (patientBox.getValue() == null || encounterType.isEmpty()) {
                    Notification.show("Patient and encounter type are required.");
                    return;
                }

                PatientEncounter encounter = new PatientEncounter();
                encounter.setPatientAccount(patientBox.getValue());
                encounter.setDepartment(patientBox.getValue().getDepartment());
                encounter.setEncounterType(encounterType.getValue());
                encounter.setEncounterDate(encounterDate.getValue() != null ? encounterDate.getValue() : LocalDate.now());
                encounter.setStatus("OPEN");

                repository.save(encounter);
                grid.setItems(repository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(patientBox, encounterType, encounterDate);
            dialog.add(form, save);
            dialog.open();
        });

        add(new H2("Patient Encounters"), addButton, grid);
    }
}