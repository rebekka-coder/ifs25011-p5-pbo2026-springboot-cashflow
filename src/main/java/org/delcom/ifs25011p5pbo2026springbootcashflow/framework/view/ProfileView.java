package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.UserUseCase;

/** Ubah nama, password, dan foto profil. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    public ProfileView(IUserRepository users, UserUseCase userUseCase, FileStorage storage) {
        User user = AuthSession.currentUser(users).orElse(null);
        if (user == null) {
            return;
        }
        PageCard card = new PageCard("Profil Saya");
        card.setMaxWidth("520px");

        Avatar avatar = new Avatar(user.getFullName());
        avatar.setHeight("96px");
        avatar.setWidth("96px");
        if (user.getPhotoPath() != null) {
            avatar.setImage("/" + user.getPhotoPath());
        }

        TextField username = new TextField("Username");
        username.setValue(user.getUsername());
        username.setReadOnly(true);
        username.setWidthFull();

        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.getFullName());
        fullName.setWidthFull();

        PasswordField newPassword = new PasswordField("Password baru (kosongkan jika tidak diubah)");
        newPassword.setWidthFull();

        final String[] photoName = new String[1];
        final byte[][] photoData = new byte[1][];
        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            photoName[0] = metadata.fileName();
            photoData[0] = bytes;
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setUploadButton(new Button("Pilih foto profil"));
        upload.addFileRemovedListener(e -> {
            photoName[0] = null;
            photoData[0] = null;
        });

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button save = new Button("Simpan Perubahan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            String newPhoto = null;
            try {
                userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
                if (photoData[0] != null) {
                    newPhoto = storage.store(photoName[0], photoData[0]);
                    String oldPhoto = user.getPhotoPath();
                    userUseCase.updatePhoto(user.getId(), newPhoto);
                    storage.delete(oldPhoto);
                }
                Notification.show("Profil berhasil diperbarui", 3000, Notification.Position.BOTTOM_START);
                UI.getCurrent().getPage().reload(); // sidebar + avatar ikut berubah
            } catch (DomainException ex) {
                if (newPhoto != null) {
                    storage.delete(newPhoto);
                }
                Notification n = Notification.show(ex.getMessage(), 4500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });

        card.add(avatar, username, fullName, newPassword, upload, progress, save);
        add(card);
    }
}
