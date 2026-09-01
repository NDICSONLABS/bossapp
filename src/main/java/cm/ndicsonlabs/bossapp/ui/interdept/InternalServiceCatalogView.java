// src/main/java/com/institution/finance/ui/InternalServiceCatalogView.java
package cm.ndicsonlabs.bossapp.ui.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.InternalServiceCatalog;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalServiceCatalogRepository;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "internal-service-catalog", layout = MainLayout.class)
@PermitAll
public class InternalServiceCatalogView extends VerticalLayout {

    private final Grid<InternalServiceCatalog> grid = new Grid<>(InternalServiceCatalog.class);

    public InternalServiceCatalogView(InternalServiceCatalogRepository repository) {
        grid.setColumns("code", "name", "description", "defaultPrice", "active");
        grid.setItems(repository.findByActiveTrueOrderByCode());

        Button newButton = new Button("New Internal Service", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");
            TextArea description = new TextArea("Description");
            BigDecimalField defaultPrice = new BigDecimalField("Default Price");

            Button save = new Button("Save", event -> {
                InternalServiceCatalog service = new InternalServiceCatalog();
                service.setCode(code.getValue());
                service.setName(name.getValue());
                service.setDescription(description.getValue());
                service.setDefaultPrice(defaultPrice.getValue());
                service.setActive(true);

                repository.save(service);
                grid.setItems(repository.findByActiveTrueOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, description, defaultPrice);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(repository.findByActiveTrueOrderByCode())
        );

        add(
                new H2("Internal Service Catalog"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}