package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.FinanceUseCase;

/** Form tambah pemasukan / pengeluaran + upload bukti opsional. */
public class TransactionFormDialog extends AppDialog {

    public TransactionFormDialog(TransactionType type, int userId, FinanceUseCase useCase,
                                 FinancePresenter presenter, FileStorage storage, Runnable onSaved) {
        super("Tambah " + type.getLabel(), type.getLabel());

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        description.setMaxLength(500);

        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        amount.setPlaceholder("contoh: 1000000");
        amount.setHelperText("Angka bulat, tanpa titik / koma");

        // Isi upload ditampung di array agar bisa dipakai lambda (nama + byte file).
        final String[] fileName = new String[1];
        final byte[][] fileData = new byte[1][];
        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            fileName[0] = metadata.fileName();
            fileData[0] = bytes;
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setUploadButton(new Button("Pilih bukti transaksi (opsional)"));
        upload.addFileRemovedListener(e -> {
            fileName[0] = null;
            fileData[0] = null;
        });

        VerticalLayout form = new VerticalLayout(description, amount, upload, progress);
        form.setPadding(false);
        form.setWidth("420px");
        form.setMaxWidth("85vw");
        add(form);

        Button cancel = new Button("Batal", e -> close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            busy(true);
            save.setEnabled(false);
            String proofPath = null;
            try {
                if (fileData[0] != null) {
                    proofPath = storage.store(fileName[0], fileData[0]);
                }
                Transaction tx = useCase.add(userId, type, description.getValue(), amount.getValue(), proofPath);
                Notification.show(presenter.addedMessage(tx), 3500, Notification.Position.BOTTOM_START);
                close();
                onSaved.run();
            } catch (DomainException ex) {
                storage.delete(proofPath); // jangan tinggalkan file yatim bila simpan gagal
                Notification error = Notification.show(presenter.errorMessage(ex.getMessage()), 4500,
                        Notification.Position.MIDDLE);
                error.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                busy(false);
                save.setEnabled(true);
            }
        });
        addAction(cancel, save);
    }
}
