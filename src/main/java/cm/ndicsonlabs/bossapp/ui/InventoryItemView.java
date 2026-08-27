// src/main/java/com/institution/finance/ui/InventoryItemView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.InventoryItem;
import cm.ndicsonlabs.bossapp.domain.ItemCategory;
import cm.ndicsonlabs.bossapp.domain.UnitOfMeasure;
import cm.ndicsonlabs.bossapp.repository.UnitOfMeasureRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryItemRepository;
import cm.ndicsonlabs.bossapp.repository.ItemCategoryRepository;
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

@Route(value = "inventory-items", layout = MainLayout.class)
@PermitAll
public class InventoryItemView extends VerticalLayout {

    private final Grid<InventoryItem> grid = new Grid<>(InventoryItem.class);

    public InventoryItemView(
            InventoryItemRepository itemRepository,
            ItemCategoryRepository categoryRepository,
            UnitOfMeasureRepository unitRepository
    ) {
        grid.addColumn(item -> item.getCategory().getName()).setHeader("Category");
        grid.addColumn(item -> item.getUnitOfMeasure().getName()).setHeader("Unit");
        grid.setColumns(
                "code",
                "name",
                "batchControlled",
                "expiryControlled",
                "standardCost",
                "salePrice",
                "active"
        );
        grid.setItems(itemRepository.findByOrderByCode());

        Button newButton = new Button("New Item", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<ItemCategory> categoryBox = new ComboBox<>("Category");
            categoryBox.setItems(categoryRepository.findAll());
            categoryBox.setItemLabelGenerator(ItemCategory::getName);

            ComboBox<UnitOfMeasure> unitBox = new ComboBox<>("Unit");
            unitBox.setItems(unitRepository.findAll());
            unitBox.setItemLabelGenerator(UnitOfMeasure::getName);

            Checkbox batchControlled = new Checkbox("Batch Controlled");
            Checkbox expiryControlled = new Checkbox("Expiry Controlled");

            BigDecimalField standardCost = new BigDecimalField("Standard Cost");
            BigDecimalField salePrice = new BigDecimalField("Sale Price");

            Button save = new Button("Save", event -> {
                if (code.isEmpty() || name.isEmpty() || categoryBox.getValue() == null || unitBox.getValue() == null) {
                    Notification.show("Code, name, category, and unit are required.");
                    return;
                }

                InventoryItem item = new InventoryItem();
                item.setCode(code.getValue());
                item.setName(name.getValue());
                item.setCategory(categoryBox.getValue());
                item.setUnitOfMeasure(unitBox.getValue());
                item.setBatchControlled(batchControlled.getValue());
                item.setExpiryControlled(expiryControlled.getValue());
                item.setStandardCost(standardCost.getValue());
                item.setSalePrice(salePrice.getValue());
                item.setActive(true);

                itemRepository.save(item);
                grid.setItems(itemRepository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    code,
                    name,
                    categoryBox,
                    unitBox,
                    batchControlled,
                    expiryControlled,
                    standardCost,
                    salePrice
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(itemRepository.findByOrderByCode())
        );

        add(
                new H2("Inventory Items"),
                new HorizontalLayout(newButton, refreshButton),
                grid
        );
    }
}