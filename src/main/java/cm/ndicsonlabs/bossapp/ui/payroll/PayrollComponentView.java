// src/main/java/com/institution/finance/ui/PayrollComponentView.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.PayrollComponent;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollComponentRepository;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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

@Route(value = "payroll-components", layout = MainLayout.class)
@PermitAll
public class PayrollComponentView extends VerticalLayout {

    private final Grid<PayrollComponent> grid = new Grid<>(PayrollComponent.class);

    public PayrollComponentView(PayrollComponentRepository repository) {
        grid.setColumns(
                "code",
                "name",
                "componentType",
                "calculationType",
                "defaultAmount",
                "defaultPercent",
                "taxable",
                "statutory",
                "active"
        );
        grid.setItems(repository.findByActiveTrueOrderByCode());

        Button newButton = new Button("New Payroll Component", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<String> componentType = new ComboBox<>("Component Type");
            componentType.setItems("EARNING", "DEDUCTION");
            componentType.setValue("EARNING");

            ComboBox<String> calculationType = new ComboBox<>("Calculation Type");
            calculationType.setItems("FIXED", "PERCENTAGE_OF_BASIC");
            calculationType.setValue("FIXED");

            BigDecimalField defaultAmount = new BigDecimalField("Default Amount");
            BigDecimalField defaultPercent = new BigDecimalField("Default Percent");

            Checkbox taxable = new Checkbox("Taxable");
            Checkbox statutory = new Checkbox("Statutory");
            Checkbox active = new Checkbox("Active");
            active.setValue(true);

            Button save = new Button("Save", event -> {
                if (code.isEmpty() || name.isEmpty()) {
                    Notification.show("Code and name are required.");
                    return;
                }

                PayrollComponent component = new PayrollComponent();
                component.setCode(code.getValue());
                component.setName(name.getValue());
                component.setComponentType(componentType.getValue());
                component.setCalculationType(calculationType.getValue());
                component.setDefaultAmount(defaultAmount.getValue());
                component.setDefaultPercent(defaultPercent.getValue());
                component.setTaxable(taxable.getValue());
                component.setStatutory(statutory.getValue());
                component.setActive(active.getValue());

                repository.save(component);
                grid.setItems(repository.findByActiveTrueOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    code,
                    name,
                    componentType,
                    calculationType,
                    defaultAmount,
                    defaultPercent,
                    taxable,
                    statutory,
                    active
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(repository.findByActiveTrueOrderByCode())
        );

        add(
                new H2("Payroll Components"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}