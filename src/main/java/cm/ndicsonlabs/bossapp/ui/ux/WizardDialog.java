// src/main/java/com/institution/finance/ui/ux/WizardDialog.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public class WizardDialog extends Dialog {

    private final List<WizardStep> steps;
    private final VerticalLayout content = new VerticalLayout();
    private final HorizontalLayout stepBar = new HorizontalLayout();

    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    private final Button finishButton = new Button("Finish");
    private final Button cancelButton = new Button("Cancel");

    private int currentIndex = 0;
    private boolean dirty = false;
    private Runnable onFinish;

    public WizardDialog(String title, List<WizardStep> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();

        setHeaderTitle(title);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setWidth("900px");

        content.setPadding(false);
        content.setSpacing(true);

        stepBar.addClassName("wizard-step-bar");
        stepBar.setSpacing(true);

        content.add(stepBar);

        add(content);

        backButton.addClickListener(e -> goBack());

        nextButton.addClickListener(e -> goNext());

        finishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        finishButton.addClickListener(e -> finish());

        cancelButton.addClickListener(e -> cancel());

        getFooter().add(cancelButton, backButton, nextButton, finishButton);

        render();
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    public void markDirty() {
        dirty = true;
    }

    private void render() {
        if (steps.isEmpty()) {
            content.add(new Span("No wizard steps configured."));
            return;
        }

        renderStepBar();

        content.removeAll();
        content.add(stepBar);

        WizardStep currentStep = steps.get(currentIndex);
        currentStep.onEnter();
        content.add(currentStep.getContent());

        backButton.setEnabled(currentIndex > 0);
        nextButton.setVisible(currentIndex < steps.size() - 1);
        finishButton.setVisible(currentIndex == steps.size() - 1);
    }

    private void renderStepBar() {
        stepBar.removeAll();

        for (int i = 0; i < steps.size(); i++) {
            Span step = new Span((i + 1) + ". " + steps.get(i).getTitle());
            step.addClassName("wizard-step");

            if (i == currentIndex) {
                step.addClassName("current");
            }

            stepBar.add(step);
        }
    }

    private void goBack() {
        if (currentIndex == 0) {
            return;
        }

        WizardStep current = steps.get(currentIndex);
        current.onLeave();

        currentIndex--;

        render();
    }

    private void goNext() {
        WizardStep current = steps.get(currentIndex);

        if (!current.isValid()) {
            Notification.show("Please correct the highlighted issues before continuing.");
            return;
        }

        current.onLeave();

        currentIndex++;

        render();
    }

    private void finish() {
        for (int i = 0; i < steps.size(); i++) {
            if (!steps.get(i).isValid()) {
                currentIndex = i;
                render();
                Notification.show("Please complete step: " + steps.get(i).getTitle());
                return;
            }
        }

        if (onFinish != null) {
            onFinish.run();
        }

        dirty = false;
        close();
    }

    private void cancel() {
        if (!dirty) {
            close();
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Cancel wizard?");
        dialog.setText("Unsaved information entered in this wizard will be lost.");
        dialog.setCancelable(true);
        dialog.setConfirmButton(new Button("Discard and Cancel"));
        dialog.addConfirmListener(event -> close());
        dialog.open();
    }
}