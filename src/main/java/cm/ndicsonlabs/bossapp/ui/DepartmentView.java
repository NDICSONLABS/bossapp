package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Institution;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.InstitutionRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "departments", layout = MainLayout.class)
@PermitAll
public class DepartmentView extends VerticalLayout {

    private final Grid<Department> grid = new Grid<>(Department.class);

    private final TextField code = new TextField("Code");
    private final TextField name = new TextField("Name");
    private final TextField type = new TextField("Type");
    private final ComboBox<Institution> institutionBox = new ComboBox<>("Institution");

    private Department selected;

    public DepartmentView(DepartmentRepository repository, InstitutionRepository institutionRepository) {
        grid.setColumns("code", "name", "type");
        grid.setItems(repository.findAll());

        institutionBox.setItems(institutionRepository.findAll());
        institutionBox.setItemLabelGenerator(Institution::getName);

        Button addButton = new Button("Add Department", e -> openForm(null, repository));
        grid.addItemDoubleClickListener(event -> openForm(event.getItem(), repository));

        add(new H2("Departments"), new HorizontalLayout(addButton), grid);
    }

    private void openForm(Department department, DepartmentRepository repository) {
        Dialog dialog = new Dialog();

        selected = department != null ? department : new Department();

        if (department != null) {
            code.setValue(department.getCode());
            name.setValue(department.getName());
            type.setValue(department.getType());
            institutionBox.setValue(department.getInstitution());
        } else {
            code.clear();
            name.clear();
            type.clear();
            institutionBox.clear();
        }

        Button save = new Button("Save", event -> {
            if (institutionBox.getValue() == null) {
                Notification.show("Institution is required.");
                return;
            }

            selected.setCode(code.getValue());
            selected.setName(name.getValue());
            selected.setType(type.getValue());
            selected.setInstitution(institutionBox.getValue());
            selected.setActive(true);

            repository.save(selected);
            grid.setItems(repository.findAll());
            dialog.close();
        });

        FormLayout form = new FormLayout(code, name, type, institutionBox);
        dialog.add(form, save);
        dialog.open();
    }
}