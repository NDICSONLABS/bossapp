// src/main/java/com/institution/finance/ui/StudentPaymentPlanView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.PaymentPlanInstallment;
import cm.ndicsonlabs.bossapp.domain.Student;
import cm.ndicsonlabs.bossapp.domain.StudentPaymentPlan;
import cm.ndicsonlabs.bossapp.repository.PaymentPlanInstallmentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentPaymentPlanRepository;
import cm.ndicsonlabs.bossapp.repository.StudentRepository;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "student-payment-plans", layout = MainLayout.class)
@PermitAll
public class StudentPaymentPlanView extends VerticalLayout {

    private final Grid<StudentPaymentPlan> planGrid = new Grid<>(StudentPaymentPlan.class);
    private final Grid<PaymentPlanInstallment> installmentGrid = new Grid<>(PaymentPlanInstallment.class);

    public StudentPaymentPlanView(
            StudentPaymentPlanRepository planRepository,
            PaymentPlanInstallmentRepository installmentRepository,
            StudentRepository studentRepository,
            EducationFinanceService educationFinanceService
    ) {
        planGrid.addColumn(plan -> plan.getStudent().getStudentNumber()).setHeader("Student");
        planGrid.setColumns(
                "totalDebt",
                "downPayment",
                "installmentAmount",
                "frequency",
                "firstDueDate",
                "numberOfInstallments",
                "approvalStatus",
                "status"
        );
        planGrid.setItems(planRepository.findAll());

        installmentGrid.setColumns(
                "installmentNumber",
                "dueDate",
                "amount",
                "paidAmount",
                "status"
        );

        planGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                installmentGrid.setItems();
            } else {
                installmentGrid.setItems(
                        installmentRepository.findByPaymentPlanIdOrderByInstallmentNumberAsc(event.getValue().getId())
                );
            }
        });

        Button newPlanButton = new Button("New Payment Plan", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Student> studentBox = new ComboBox<>("Student");
            studentBox.setItems(studentRepository.findAll());
            studentBox.setItemLabelGenerator(student -> student.getStudentNumber() + " - " + student.getFullName());

            BigDecimalField totalDebt = new BigDecimalField("Total Debt");
            BigDecimalField downPayment = new BigDecimalField("Down Payment");
            BigDecimalField installmentAmount = new BigDecimalField("Installment Amount");

            ComboBox<String> frequency = new ComboBox<>("Frequency");
            frequency.setItems("MONTHLY", "WEEKLY", "QUARTERLY");
            frequency.setValue("MONTHLY");

            DatePicker firstDueDate = new DatePicker("First Due Date");
            IntegerField numberOfInstallments = new IntegerField("Number of Installments");
            TextField responsibleOfficer = new TextField("Responsible Officer");

            Button save = new Button("Save", event -> {
                try {
                    if (studentBox.getValue() == null) {
                        Notification.show("Select a student.");
                        return;
                    }

                    educationFinanceService.createPaymentPlan(
                            studentBox.getValue().getId(),
                            totalDebt.getValue(),
                            downPayment.getValue(),
                            installmentAmount.getValue(),
                            frequency.getValue(),
                            firstDueDate.getValue() != null ? firstDueDate.getValue() : LocalDate.now(),
                            numberOfInstallments.getValue(),
                            responsibleOfficer.getValue(),
                            null
                    );

                    planGrid.setItems(planRepository.findAll());
                    dialog.close();
                    Notification.show("Payment plan created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    studentBox,
                    totalDebt,
                    downPayment,
                    installmentAmount,
                    frequency,
                    firstDueDate,
                    numberOfInstallments,
                    responsibleOfficer
            );

            dialog.add(form, save);
            dialog.open();
        });

        add(
                new H2("Student Payment Plans"),
                new HorizontalLayout(newPlanButton),
                planGrid,
                new H2("Installments"),
                installmentGrid
        );
    }
}