// src/main/java/com/institution/finance/ui/ux/PageHeader.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class PageHeader extends Div {

    private final H2 title = new H2();
    private final Span description = new Span();
    private final HorizontalLayout toolbar = new HorizontalLayout();

    public PageHeader(String titleText) {
        this(titleText, null);
    }

    public PageHeader(String titleText, String descriptionText) {
        addClassName("page-header");

        title.setText(titleText);
        title.addClassName("page-title");

        description.setText(descriptionText != null ? descriptionText : "");
        description.addClassName("page-description");

        toolbar.addClassName("page-toolbar");
        toolbar.setSpacing(true);
        toolbar.setPadding(false);

        add(title, description, toolbar);
    }

    public void setTitle(String text) {
        title.setText(text);
    }

    public void setDescription(String text) {
        description.setText(text);
    }

    public void addToolbarComponent(Component component) {
        toolbar.add(component);
    }
}