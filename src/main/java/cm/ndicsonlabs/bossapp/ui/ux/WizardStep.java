// src/main/java/com/institution/finance/ui/ux/WizardStep.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.Component;

public interface WizardStep {

    String getTitle();

    Component getContent();

    boolean isValid();

    default void onEnter() {
    }

    default void onLeave() {
    }
}