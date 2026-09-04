// src/main/java/com/institution/finance/ui/ux/SaveBar.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class SaveBar extends HorizontalLayout {

    private final Button saveButton = new Button("Save");
    private final Button discardButton = new Button("Discard");
    private final Span status = new Span("No changes");

    private boolean dirty = false;

    private Runnable onSave;
    private Runnable onDiscard;

    public SaveBar() {
        addClassName("save-bar");
        setSpacing(true);
        setPadding(false);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);

        discardButton.setEnabled(false);

        status.addClassName("save-status");

        saveButton.addClickListener(e -> {
            if (onSave != null) {
                onSave.run();
                markClean();
            }
        });

        discardButton.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Discard changes?");
            dialog.setText("Unsaved changes will be lost.");
            dialog.setCancelable(true);
            dialog.setConfirmButton(new Button("Discard"));
            dialog.addConfirmListener(event -> {
                if (onDiscard != null) {
                    onDiscard.run();
                }
                markClean();
            });
            dialog.open();
        });

        add(status, discardButton, saveButton);
    }

    public void track(HasValue<?, ?>... fields) {
        for (HasValue<?, ?> field : fields) {
            field.addValueChangeListener(event -> markDirty());
        }
    }

    public void markDirty() {
        dirty = true;
        saveButton.setEnabled(true);
        discardButton.setEnabled(true);
        status.setText("Unsaved changes");
        status.addClassName("unsaved");
    }

    public void markClean() {
        dirty = false;
        saveButton.setEnabled(false);
        discardButton.setEnabled(false);
        status.setText("Saved");
        status.removeClassName("unsaved");
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setOnDiscard(Runnable onDiscard) {
        this.onDiscard = onDiscard;
    }
}