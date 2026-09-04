// src/main/java/com/institution/finance/ui/ux/Breadcrumbs.java
package cm.ndicsonlabs.bossapp.ui.ux;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class Breadcrumbs extends HorizontalLayout {

    public Breadcrumbs(String... nameAndHrefPairs) {
        addClassName("breadcrumbs");
        setSpacing(true);
        setPadding(false);

        for (int i = 0; i < nameAndHrefPairs.length; i += 2) {
            String name = nameAndHrefPairs[i];
            String href = nameAndHrefPairs.length > i + 1 ? nameAndHrefPairs[i + 1] : null;

            if (href != null && !href.isBlank()) {
                add(new Anchor(href, name));
            } else {
                add(new Span(name));
            }

            if (i + 2 < nameAndHrefPairs.length) {
                Span separator = new Span("/");
                separator.addClassName("breadcrumb-separator");
                add(separator);
            }
        }
    }
}