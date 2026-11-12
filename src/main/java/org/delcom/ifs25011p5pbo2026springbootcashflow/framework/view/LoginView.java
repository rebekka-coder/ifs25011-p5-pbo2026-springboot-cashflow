package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.Key;
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
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public LoginView(AuthUseCase auth, IUserRepository users) {
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField username = new TextField("Username");
        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);

        PasswordField password = new PasswordField("Password");
        password.setId("login-password");
        password.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(e -> {
            progress.setVisible(true);
            loginButton.setEnabled(false);
            try {
                User user = auth.login(username.getValue(), password.getValue());
                AuthSession.login(user);
                UI.getCurrent().getPage().setLocation("/");
            } catch (ValidationException ex) {
                Notification n = Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                loginButton.setEnabled(true);
            }
        });

        Div card = new Div(new H2("Masuk ke Cashflow"), username, password, progress, loginButton,
                new RouterLink("Belum punya akun? Daftar", RegisterView.class));
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
