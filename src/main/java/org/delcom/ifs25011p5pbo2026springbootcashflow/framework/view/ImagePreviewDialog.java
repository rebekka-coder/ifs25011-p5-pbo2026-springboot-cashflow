package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/**
 * Modal preview gambar. Klik gambar untuk toggle zoom 1.0x <-> 1.8x;
 * tombol Perkecil / Perbesar / Tutup di kanan footer.
 */
public class ImagePreviewDialog extends AppDialog {
    private static final double MIN = 1.0;
    private static final double MAX = 3.0;
    private static final double STEP = 0.4;
    private static final double TOGGLE = 1.8;

    private final Image image;
    private double zoom = MIN;

    public ImagePreviewDialog(String title, String src) {
        super(title, "Pratinjau");

        image = new Image(src, title);
        image.getStyle().set("max-width", "none").set("cursor", "zoom-in").set("display", "block");
        image.addClickListener(e -> setZoom(zoom > MIN ? MIN : TOGGLE));

        Div viewport = new Div(image);
        viewport.getStyle().set("width", "min(640px, 80vw)").set("max-height", "60vh").set("overflow", "auto");
        add(viewport);

        Button zoomOut = new Button("Perkecil", e -> setZoom(Math.max(MIN, zoom - STEP)));
        Button zoomIn = new Button("Perbesar", e -> setZoom(Math.min(MAX, zoom + STEP)));
        Button close = new Button("Tutup", e -> close());
        addAction(zoomOut, zoomIn, close);
        setZoom(MIN);
    }

    private void setZoom(double value) {
        zoom = value;
        image.getStyle().set("width", Math.round(zoom * 100) + "%");
        image.getStyle().set("cursor", zoom > MIN ? "zoom-out" : "zoom-in");
    }
}
