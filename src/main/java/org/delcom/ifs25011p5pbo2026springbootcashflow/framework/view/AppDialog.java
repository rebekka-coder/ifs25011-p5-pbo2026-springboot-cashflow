package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Dialog dasar: header biru primer (#2563EB) + badge, footer abu-abu (#F3F4F6) dengan tombol di kanan.
 * Warna diatur di styles.css lewat class "cashflow-dialog".
 */
public class AppDialog extends Dialog {
    protected final ProgressBar progress = new ProgressBar();

    public AppDialog(String title, String badgeText) {
        addClassName("cashflow-dialog");
        setHeaderTitle(title);
        setCloseOnOutsideClick(false);
        setDraggable(true);

        Span badge = new Span(badgeText);
        badge.getElement().getThemeList().add("badge");
        getHeader().add(badge);

        progress.setIndeterminate(true);
        progress.setVisible(false);
    }

    /** Tombol aksi selalu di footer sebelah kanan. */
    protected void addAction(Button... buttons) {
        getFooter().add(buttons);
    }

    protected void busy(boolean busy) {
        progress.setVisible(busy);
    }
}
