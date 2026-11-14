package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
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
        username.setWidthFull();
        username.setAutofocus(true);
        setInputId(username, "login-username"); // ID di <input>, bukan di <vaadin-text-field>

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        setInputId(password, "login-password"); // ID di <input type="password">

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit"); // tombol sudah benar: ID di <vaadin-button>
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

    /**
     * Memasang ID pada elemen <input> di dalam field Vaadin (bukan pada pembungkusnya),
     * lalu mengarahkan atribut "for" pada <label> ke ID tersebut.
     */
    private static void setInputId(Component field, String id) {
        field.addAttachListener(e -> field.getElement().executeJs(
                """
                let tries = 0;
                const apply = () => {
                    const input = this.inputElement || this.querySelector('input');
                    if (!input) return false;
                    input.id = $0;
                    const label = this.querySelector('label');
                    if (label) label.setAttribute('for', $0);
                    return true;
                };
                const tick = () => { if (!apply() && tries++ < 20) setTimeout(tick, 50); };
                tick();
                """, id));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }
}