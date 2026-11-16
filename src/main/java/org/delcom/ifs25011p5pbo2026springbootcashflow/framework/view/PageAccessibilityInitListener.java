package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * Perbaikan aksesibilitas pada HTML awal halaman Vaadin:
 * judul dokumen, bahasa, dan indikator koneksi (popover bawaan Vaadin yang berada di luar landmark).
 */
@Component
public class PageAccessibilityInitListener implements VaadinServiceInitListener {
    private static final String HIDE_CONNECTION_INDICATOR = """
            (function () {
                var hide = function () {
                    document.querySelectorAll('vaadin-connection-indicator')
                        .forEach(function (e) { e.setAttribute('aria-hidden', 'true'); });
                };
                hide();
                new MutationObserver(hide).observe(document.body, { childList: true });
            })();
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(this::modify);
    }

    private void modify(IndexHtmlResponse response) {
        Document document = response.getDocument();
        document.title("Cashflow");
        document.selectFirst("html").attr("lang", "id");
        document.body().appendElement("script").text(HIDE_CONNECTION_INDICATOR);
    }
}