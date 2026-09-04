// src/main/java/com/institution/finance/ui/ux/SectionCard.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;

public class SectionCard extends Div {

    public SectionCard(String title, Component... content) {
        addClassName("section-card");

        H3 header = new H3(title);
        header.addClassName("section-title");

        Div body = new Div();
        body.addClassName("section-body");
        body.add(content);

        add(header, body);
    }
}