package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman. */
public class PageCard extends VerticalLayout {
    public PageCard(String title) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        add(new H2(title));
    }
}
