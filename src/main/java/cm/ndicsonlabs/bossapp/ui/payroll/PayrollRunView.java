// src/main/java/com/institution/finance/ui/PayrollRunView.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.EmployeePayrollRun;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollPeriod;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollRunLine;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeePayrollRunRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollRunLineRepository;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "payroll-runs", layout = MainLayout.class)
@PermitAll
public class PayrollRunView extends VerticalLayout {

    private final Grid<EmployeePayrollRun> runGrid = new Grid<>(EmployeePayrollRun.class);
    private final Grid<PayrollRunLine> lineGrid = new Grid<>(PayrollRunLine.class);

    public PayrollRunView(
            PayrollPeriodRepository periodRepository,
            EmployeePayrollRunRepository runRepository,
            PayrollRunLineRepository lineRepository
    ) {
        ComboBox<PayrollPeriod> periodBox = new ComboBox<>("Payroll Period");
        periodBox.setItems(periodRepository.findByOrderByCreatedAtDesc());
        periodBox.setItemLabelGenerator(PayrollPeriod::toString);

        runGrid.addColumn(run -> run.getEmployee().getEmployeeNumber()).setHeader("Employee");
        runGrid.addColumn(run -> run.getEmployee().getFullName()).setHeader("Name");
        runGrid.setColumns(
                "grossAmount",
                "totalDeductions",
                "netAmount",
                "status"
        );

        lineGrid.addColumn(line -> line.getPayrollComponent().getName()).setHeader("Component");
        lineGrid.setColumns("lineType", "amount");

        periodBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                runGrid.setItems();
                lineGrid.setItems();
            } else {
                runGrid.setItems(runRepository.findByPayrollPeriodId(event.getValue().getId()));
                lineGrid.setItems();
            }
        });

        runGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
            } else {
                lineGrid.setItems(lineRepository.findByEmployeePayrollRunIdOrderByCreatedAtAsc(event.getValue().getId()));
            }
        });

        add(
                new H2("Payroll Runs"),
                periodBox,
                runGrid,
                new H2("Payroll Run Lines"),
                lineGrid
        );
    }
}