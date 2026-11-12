package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.UserUseCase;

import java.util.List;

/** (ADMIN) melihat transaksi pengguna lain, read-only. Bisa dibuka dengan ?userId=. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final List<User> allUsers;
    private final Select<User> picker = new Select<>();
    private final VerticalLayout content = new VerticalLayout();

    public UserTransactionsView(IUserRepository users, UserUseCase userUseCase, FinanceUseCase financeUseCase,
                                FinancePresenter presenter, FileStorage storage) {
        this.users = users;
        this.allUsers = userUseCase.list();

        PageCard card = new PageCard("Transaksi Pengguna (Read-only)");
        picker.setLabel("Pilih pengguna");
        picker.setWidth("360px");
        picker.setItems(allUsers);
        picker.setItemLabelGenerator(u -> u.getFullName() + " (" + u.getUsername() + ")");
        picker.addValueChangeListener(e -> {
            content.removeAll();
            if (e.getValue() != null) {
                content.add(new TransactionPanel(financeUseCase, presenter, storage, e.getValue().getId(), true));
            }
        });
        content.setPadding(false);
        card.add(picker, content);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(users).orElse(null);
        if (current == null) {
            return;
        }
        if (!current.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        event.getLocation().getQueryParameters().getSingleParameter("userId").ifPresent(raw -> {
            try {
                int id = Integer.parseInt(raw);
                allUsers.stream().filter(u -> u.getId() == id).findFirst().ifPresent(picker::setValue);
            } catch (NumberFormatException ignored) {
                // userId tidak valid: biarkan pilihan kosong
            }
        });
    }
}
