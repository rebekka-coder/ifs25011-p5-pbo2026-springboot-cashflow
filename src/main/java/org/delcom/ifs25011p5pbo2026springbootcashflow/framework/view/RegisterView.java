package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public RegisterView(AuthUseCase auth, IUserRepository users) {
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField fullName = new TextField("Nama lengkap");
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField confirm = new PasswordField("Ulangi password");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button register = new Button("Daftar");
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        register.setWidthFull();
        register.addClickListener(e -> {
            progress.setVisible(true);
            register.setEnabled(false);
            try {
                if (!password.getValue().equals(confirm.getValue())) {
                    throw new ValidationException("Konfirmasi password tidak sama");
                }
                auth.register(username.getValue(), password.getValue(), fullName.getValue());
                Notification.show("Registrasi berhasil, silakan masuk", 3500, Notification.Position.BOTTOM_START);
                UI.getCurrent().navigate(LoginView.class);
            } catch (ValidationException ex) {
                Notification n = Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                register.setEnabled(true);
            }
        });

        Div card = new Div(new H2("Daftar Akun Cashflow"), fullName, username, password, confirm, progress,
                register, new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }
}
