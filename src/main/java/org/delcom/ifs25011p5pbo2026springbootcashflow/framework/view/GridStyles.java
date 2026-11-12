package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di semua halaman. */
public final class GridStyles {
    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("440px");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
    }
}
