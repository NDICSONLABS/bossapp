// src/main/java/com/institution/finance/ui/ux/Toolbar.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class Toolbar extends HorizontalLayout {

    public Toolbar(Component... components) {
        addClassName("toolbar");
        setSpacing(true);
        setPadding(false);
        add(components);
    }
}