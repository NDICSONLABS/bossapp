// src/main/java/com/institution/finance/ui/FeeScheduleView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AcademicTerm;
import cm.ndicsonlabs.bossapp.domain.AcademicYear;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.FeeSchedule;
import cm.ndicsonlabs.bossapp.repository.AcademicTermRepository;
import cm.ndicsonlabs.bossapp.repository.AcademicYearRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.FeeScheduleRepository;
import cm.ndicsonlabs.bossapp.service.EducationFinanceService;
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

@Route(value = "fee-schedules", layout = MainLayout.class)
@PermitAll
public class FeeScheduleView extends VerticalLayout {

    private final Grid<FeeSchedule> grid = new Grid<>(FeeSchedule.class);

    public FeeScheduleView(
            FeeScheduleRepository feeScheduleRepository,
            DepartmentRepository departmentRepository,
            AcademicYearRepository academicYearRepository,
            AcademicTermRepository academicTermRepository,
            EducationFinanceService educationFinanceService
    ) {
        grid.addColumn(fs -> fs.getDepartment().getName()).setHeader("Department");
        grid.addColumn(fs -> fs.getAcademicYear().getName()).setHeader("Year");
        grid.addColumn(fs -> fs.getTerm() != null ? fs.getTerm().getName() : "").setHeader("Term");
        grid.setColumns(
                "programOrClass",
                "studentCategory",
                "feeType",
                "amount",
                "dueDate",
                "installmentNumber",
                "mandatory",
                "active"
        );
        grid.setItems(feeScheduleRepository.findAll());

        Button newButton = new Button("New Fee Schedule", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            ComboBox<AcademicYear> yearBox = new ComboBox<>("Academic Year");
            yearBox.setItems(academicYearRepository.findAll());
            yearBox.setItemLabelGenerator(AcademicYear::getName);

            ComboBox<AcademicTerm> termBox = new ComboBox<>("Term");
            termBox.setItems(academicTermRepository.findAll());
            termBox.setItemLabelGenerator(AcademicTerm::getName);
            termBox.setClearButtonVisible(true);

            TextField programOrClass = new TextField("Program/Class");
            TextField studentCategory = new TextField("Student Category");
            TextField feeType = new TextField("Fee Type");
            BigDecimalField amount = new BigDecimalField("Amount");
            DatePicker dueDate = new DatePicker("Due Date");

            Button save = new Button("Save", event -> {
                if (departmentBox.getValue() == null || yearBox.getValue() == null ||
                        feeType.isEmpty() || amount.getValue() == null || amount.getValue().signum() <= 0) {
                    Notification.show("Department, academic year, fee type, and positive amount are required.");
                    return;
                }

                FeeSchedule schedule = new FeeSchedule();
                schedule.setDepartment(departmentBox.getValue());
                schedule.setAcademicYear(yearBox.getValue());
                schedule.setTerm(termBox.getValue());
                schedule.setProgramOrClass(programOrClass.getValue());
                schedule.setStudentCategory(studentCategory.getValue());
                schedule.setFeeType(feeType.getValue());
                schedule.setAmount(amount.getValue());
                schedule.setDueDate(dueDate.getValue());
                schedule.setInstallmentNumber(1);
                schedule.setMandatory(true);
                schedule.setActive(true);

                feeScheduleRepository.save(schedule);
                grid.setItems(feeScheduleRepository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    departmentBox,
                    yearBox,
                    termBox,
                    programOrClass,
                    studentCategory,
                    feeType,
                    amount,
                    dueDate
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button generateButton = new Button("Generate Student Charges", e -> {
            FeeSchedule selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a fee schedule first.");
                return;
            }

            try {
                int created = educationFinanceService.generateChargesForFeeSchedule(selected.getId());
                Notification.show("Generated " + created + " student charge(s).");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        add(
                new H2("Fee Schedules"),
                new HorizontalLayout(newButton, generateButton),
                grid
        );
    }
}