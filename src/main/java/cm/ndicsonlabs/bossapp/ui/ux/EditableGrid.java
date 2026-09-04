// src/main/java/com/institution/finance/ui/ux/EditableGrid.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditableGrid<T> extends VerticalLayout {

    public interface RowFactory<T> {
        T create();

        T copy(T source);
    }

    private final Grid<T> grid;
    private final Binder<T> binder;
    private final List<T> items;
    private final List<T> originalItems;
    private final ListDataProvider<T> dataProvider;
    private final RowFactory<T> rowFactory;
    private final Consumer<List<T>> saveHandler;

    private final Button addButton = new Button("Add Row");
    private final Button duplicateButton = new Button("Duplicate");
    private final Button deleteButton = new Button("Delete");
    private final Button saveButton = new Button("Save");
    private final Button discardButton = new Button("Discard");
    private final Span saveStatus = new Span("Saved");

    private boolean dirty = false;

    public EditableGrid(
            Class<T> beanType,
            List<T> initialItems,
            RowFactory<T> rowFactory,
            Consumer<List<T>> saveHandler
    ) {
        this.items = new ArrayList<>(initialItems);
        this.originalItems = new ArrayList<>(initialItems);
        this.rowFactory = rowFactory;
        this.saveHandler = saveHandler;
        this.grid = new Grid<>(beanType, false);
        this.binder = new Binder<>(beanType);
        this.dataProvider = new ListDataProvider<>(items);

        grid.setDataProvider(dataProvider);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addClassName("grid-toolbar");
        toolbar.setSpacing(true);
        toolbar.setPadding(false);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveStatus.addClassName("save-status");

        addButton.addClickListener(e -> addRow());
        duplicateButton.addClickListener(e -> duplicateRow());
        deleteButton.addClickListener(e -> deleteRow());
        saveButton.addClickListener(e -> save());
        discardButton.addClickListener(e -> discard());

        toolbar.add(addButton, duplicateButton, deleteButton, discardButton, saveButton, saveStatus);

        add(toolbar, grid);
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public Binder<T> getBinder() {
        return binder;
    }

    public List<T> getItems() {
        return items;
    }

    public void refresh() {
        dataProvider.refreshAll();
    }

    private void addRow() {
        T item = rowFactory.create();
        items.add(item);
        dataProvider.refreshAll();
        markDirty();

        grid.asSingleSelect().setValue(item);
        grid.getEditor().editItem(item);
    }

    private void duplicateRow() {
        T selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            Notification.show("Select a row to duplicate.");
            return;
        }

        T copy = rowFactory.copy(selected);
        items.add(copy);
        dataProvider.refreshAll();
        markDirty();

        grid.asSingleSelect().setValue(copy);
        grid.getEditor().editItem(copy);
    }

    private void deleteRow() {
        T selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            Notification.show("Select a row to delete.");
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete row?");
        dialog.setText("The selected row will be removed.");
        dialog.setCancelable(true);
        dialog.setConfirmButton(new Button("Delete"));
        dialog.addConfirmListener(event -> {
            items.remove(selected);
            dataProvider.refreshAll();
            markDirty();
        });
        dialog.open();
    }

    private void save() {
        if (!binder.isValid()) {
            Notification.show("Please correct validation errors before saving.");
            return;
        }

        if (saveHandler != null) {
            saveHandler.accept(new ArrayList<>(items));
        }

        originalItems.clear();
        originalItems.addAll(new ArrayList<>(items));

        markClean();
        Notification.show("Changes saved.");
    }

    private void discard() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Discard changes?");
        dialog.setText("All unsaved grid changes will be lost.");
        dialog.setCancelable(true);
        dialog.setConfirmButton(new Button("Discard"));
        dialog.addConfirmListener(event -> {
            items.clear();
            items.addAll(originalItems);
            dataProvider.refreshAll();
            markClean();
        });
        dialog.open();
    }

    private void markDirty() {
        dirty = true;
        saveStatus.setText("Unsaved changes");
        saveStatus.addClassName("unsaved");
    }

    private void markClean() {
        dirty = false;
        saveStatus.setText("Saved");
        saveStatus.removeClassName("unsaved");
    }

    public boolean isDirty() {
        return dirty;
    }
}