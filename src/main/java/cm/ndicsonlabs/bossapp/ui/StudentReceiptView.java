// src/main/java/com/institution/finance/ui/StudentReceiptView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.StudentReceipt;
import cm.ndicsonlabs.bossapp.repository.StudentReceiptRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "student-receipts", layout = MainLayout.class)
@PermitAll
public class StudentReceiptView extends VerticalLayout {

    public StudentReceiptView(StudentReceiptRepository repository) {
        Grid<StudentReceipt> grid = new Grid<>(StudentReceipt.class);

        grid.addColumn(receipt -> receipt.getStudent().getStudentNumber()).setHeader("Student");
        grid.addColumn(receipt -> receipt.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "receiptNumber",
                "receivedDate",
                "amount",
                "paymentMethod",
                "payer",
                "cashier",
                "status"
        );

        grid.setItems(repository.findAll());

        add(new H2("Student Receipts"), grid);
    }
}