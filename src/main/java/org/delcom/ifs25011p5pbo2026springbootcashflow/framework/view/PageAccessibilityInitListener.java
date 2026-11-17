package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Menyuntikkan elemen aksesibilitas langsung ke HTML awal halaman Vaadin (sebelum JavaScript jalan):
 * judul dokumen, bahasa, landmark main, h1, dan teks "memuat". Penting untuk audit otomatis
 * yang memeriksa halaman sebelum Vaadin selesai menggambar.
 */
@Component
public class PageAccessibilityInitListener implements VaadinServiceInitListener {
    private static final String SR_ONLY =
            "position:absolute;width:1px;height:1px;margin:-1px;padding:0;overflow:hidden;"
                    + "clip:rect(0,0,0,0);white-space:nowrap;border:0";

    private static final String CSS = """
            .boot-loading{margin:0;padding:24px;font-family:Roboto,"Segoe UI",Arial,sans-serif;color:#4b5563}
            main:has(flow-container-root) > .boot-loading{display:none}
            """;

    /** Indikator koneksi bawaan Vaadin dipindah ke dalam main dan disembunyikan dari pembaca layar. */
    private static final String FIX_CONNECTION_INDICATOR = """
            (function () {
                var fix = function () {
                    var main = document.querySelector('main');
                    document.querySelectorAll('vaadin-connection-indicator').forEach(function (e) {
                        e.setAttribute('aria-hidden', 'true');
                        if (main && e.parentElement !== main) { main.appendChild(e); }
                    });
                };
                fix();
                new MutationObserver(fix).observe(document.body, { childList: true });
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
        document.head().appendElement("style").text(CSS);

        Element outlet = document.getElementById("outlet");
        if (outlet != null) {
            outlet.wrap("<main style=\"display:block;height:100%\"></main>");
            Element main = outlet.parent();
            main.prependElement("p").addClass("boot-loading").text("Memuat Cashflow...");
            main.prependElement("h1").attr("style", SR_ONLY).text("Cashflow");
        }
        document.body().appendElement("script").text(FIX_CONNECTION_INDICATOR);
    }
}