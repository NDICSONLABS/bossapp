// src/main/java/com/institution/finance/ui/StudentChargeView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Student;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;

@Route(value = "student-charges", layout = MainLayout.class)
@PermitAll
public class StudentChargeView extends VerticalLayout {

    private final Grid<StudentCharge> grid = new Grid<>(StudentCharge.class);

    public StudentChargeView(
            StudentChargeRepository chargeRepository,
            StudentRepository studentRepository,
            EducationFinanceService educationFinanceService
    ) {
        grid.addColumn(charge -> charge.getStudent().getStudentNumber()).setHeader("Student");
        grid.setColumns(
                "serviceCategory",
                "chargeDate",
                "dueDate",
                "originalAmount",
                "scholarshipAmount",
                "discountAmount",
                "waiverAmount",
                "netAmount",
                "paidAmount",
                "status"
        );
        grid.setItems(chargeRepository.findAll());

        Button newChargeButton = new Button("New Charge", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Student> studentBox = new ComboBox<>("Student");
            studentBox.setItems(studentRepository.findAll());
            studentBox.setItemLabelGenerator(student -> student.getStudentNumber() + " - " + student.getFullName());

            TextField serviceCategory = new TextField("Fee Type");
            DatePicker dueDate = new DatePicker("Due Date");
            BigDecimalField amount = new BigDecimalField("Amount");

            Button save = new Button("Save", event -> {
                if (studentBox.getValue() == null || amount.getValue() == null || amount.getValue().signum() <= 0) {
                    Notification.show("Student and positive amount are required.");
                    return;
                }

                StudentCharge charge = new StudentCharge();
                charge.setStudent(studentBox.getValue());
                charge.setDepartment(studentBox.getValue().getDepartment());
                charge.setServiceCategory(serviceCategory.getValue());
                charge.setChargeDate(LocalDate.now());
                charge.setDueDate(dueDate.getValue() != null ? dueDate.getValue() : LocalDate.now().plusDays(30));
                charge.setAmount(amount.getValue());
                charge.setOriginalAmount(amount.getValue());
                charge.setNetAmount(amount.getValue());
                charge.setPaidAmount(BigDecimal.ZERO);
                charge.setStatus("POSTED");
                charge.setAccountingStatus("NOT_SUBMITTED");

                chargeRepository.save(charge);
                grid.setItems(chargeRepository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(studentBox, serviceCategory, dueDate, amount);
            dialog.add(form, save);
            dialog.open();
        });

        Button paymentButton = new Button("Record Payment + Receipt", e -> {
            StudentCharge selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a student charge first.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField payer = new TextField("Payer");
            ComboBox<String> method = new ComboBox<>("Payment Method");
            method.setItems("CASH", "BANK_TRANSFER", "MOBILE_MONEY", "CARD", "CHECK", "OTHER");
            method.setValue("CASH");

            Button save = new Button("Save", event -> {
                try {
                    educationFinanceService.recordStudentPaymentWithReceipt(
                            selected.getId(),
                            amount.getValue(),
                            payer.getValue(),
                            method.getValue(),
                            LocalDate.now()
                    );

                    grid.setItems(chargeRepository.findAll());
                    dialog.close();
                    Notification.show("Payment recorded and receipt generated.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(amount, payer, method);
            dialog.add(form, save);
            dialog.open();
        });

        Button adjustmentButton = new Button("Apply Scholarship/Discount/Waiver", e -> {
            StudentCharge selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a student charge first.");
                return;
            }

            Dialog dialog = new Dialog();

            ComboBox<String> type = new ComboBox<>("Adjustment Type");
            type.setItems("SCHOLARSHIP", "DISCOUNT", "WAIVER");
            type.setValue("SCHOLARSHIP");

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField reason = new TextField("Reason");

            Button save = new Button("Apply", event -> {
                try {
                    educationFinanceService.applyAdjustment(
                            selected.getId(),
                            type.getValue(),
                            amount.getValue(),
                            reason.getValue()
                    );

                    grid.setItems(chargeRepository.findAll());
                    dialog.close();
                    Notification.show("Adjustment applied.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(type, amount, reason);
            dialog.add(form, save);
            dialog.open();
        });

        add(
                new H2("Student Charges"),
                new HorizontalLayout(newChargeButton, paymentButton, adjustmentButton),
                grid
        );
    }
}

//// src/main/java/com/institution/finance/ui/StudentChargeView.java
//package cm.ndicsonlabs.bossapp.ui;
//
//import cm.ndicsonlabs.bossapp.domain.Student;
//import cm.ndicsonlabs.bossapp.domain.StudentCharge;
//import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
//import cm.ndicsonlabs.bossapp.repository.StudentRepository;
//import cm.ndicsonlabs.bossapp.service.FinanceService;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.datepicker.DatePicker;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.H2;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.BigDecimalField;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.PermitAll;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//@Route(value = "student-charges", layout = MainLayout.class)
//@PermitAll
//public class StudentChargeView extends VerticalLayout {
//
//    private final Grid<StudentCharge> grid = new Grid<>(StudentCharge.class);
//
//    public StudentChargeView(
//            StudentChargeRepository chargeRepository,
//            StudentRepository studentRepository,
//            FinanceService financeService
//    ) {
//        grid.setColumns("chargeDate", "dueDate", "amount", "paidAmount", "status");
//        grid.setItems(chargeRepository.findAll());
//
//        Button newChargeButton = new Button("New Charge", e -> openNewChargeDialog(chargeRepository, studentRepository));
//        Button payButton = new Button("Record Payment", e -> openPaymentDialog(chargeRepository, financeService));
//
//        add(
//                new H2("Student Charges"),
//                new HorizontalLayout(newChargeButton, payButton),
//                grid
//        );
//    }
//
//    private void openNewChargeDialog(
//            StudentChargeRepository chargeRepository,
//            StudentRepository studentRepository
//    ) {
//        Dialog dialog = new Dialog();
//
//        ComboBox<Student> studentBox = new ComboBox<>("Student");
//        studentBox.setItems(studentRepository.findAll());
//        studentBox.setItemLabelGenerator(student -> student.getStudentNumber() + " - " + student.getFullName());
//
//        DatePicker chargeDate = new DatePicker("Charge Date");
//        DatePicker dueDate = new DatePicker("Due Date");
//        BigDecimalField amount = new BigDecimalField("Amount");
//
//        Button save = new Button("Save", event -> {
//            Student student = studentBox.getValue();
//
//            if (student == null || amount.getValue() == null || amount.getValue().signum() <= 0) {
//                Notification.show("Student and positive amount are required.");
//                return;
//            }
//
//            StudentCharge charge = new StudentCharge();
//            charge.setStudent(student);
//            charge.setDepartment(student.getDepartment());
//            charge.setChargeDate(chargeDate.getValue() != null ? chargeDate.getValue() : LocalDate.now());
//            charge.setDueDate(dueDate.getValue() != null ? dueDate.getValue() : charge.getChargeDate());
//            charge.setAmount(amount.getValue());
//            charge.setPaidAmount(BigDecimal.ZERO);
//            charge.setStatus("POSTED");
//
//            chargeRepository.save(charge);
//            grid.setItems(chargeRepository.findAll());
//            dialog.close();
//        });
//
//        FormLayout form = new FormLayout(studentBox, chargeDate, dueDate, amount);
//        dialog.add(form, save);
//        dialog.open();
//    }
//
//    private void openPaymentDialog(
//            StudentChargeRepository chargeRepository,
//            FinanceService financeService
//    ) {
//        StudentCharge selected = grid.asSingleSelect().getValue();
//
//        if (selected == null) {
//            Notification.show("Select a student charge first.");
//            return;
//        }
//
//        Dialog dialog = new Dialog();
//
//        BigDecimalField amount = new BigDecimalField("Amount");
//        TextField payer = new TextField("Payer");
//
//        Button save = new Button("Save", event -> {
//            try {
//                financeService.recordStudentPayment(selected, amount.getValue(), payer.getValue(), LocalDate.now());
//                grid.setItems(chargeRepository.findAll());
//                dialog.close();
//                Notification.show("Payment recorded.");
//            } catch (Exception ex) {
//                Notification.show(ex.getMessage());
//            }
//        });
//
//        FormLayout form = new FormLayout(amount, payer);
//        dialog.add(form, save);
//        dialog.open();
//    }
//}