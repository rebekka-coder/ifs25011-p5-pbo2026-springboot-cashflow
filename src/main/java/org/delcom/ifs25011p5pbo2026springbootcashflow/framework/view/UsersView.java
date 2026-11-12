package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.UserUseCase;

import java.util.Map;

/** (ADMIN) daftar semua pengguna. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public UsersView(IUserRepository users, UserUseCase userUseCase) {
        this.users = users;
        PageCard card = new PageCard("Daftar Pengguna");

        Grid<User> grid = new Grid<>();
        grid.addColumn(User::getId).setHeader("ID").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true);
        grid.addComponentColumn(u -> {
            Button open = new Button("Lihat Transaksi", e -> UI.getCurrent().navigate("user-transactions",
                    QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            open.addThemeVariants(ButtonVariant.LUMO_SMALL);
            return open;
        }).setHeader("Aksi");
        GridStyles.apply(grid);
        grid.setItems(userUseCase.list());

        card.add(grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        AuthSession.currentUser(users).ifPresent(user -> {
            if (!user.isAdmin()) {
                event.forwardTo(FinanceView.class);
            }
        });
    }
}
