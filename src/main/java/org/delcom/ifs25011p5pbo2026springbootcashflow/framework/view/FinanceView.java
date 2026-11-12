package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.FinanceUseCase;

/** Dashboard transaksi milik akun yang sedang login (USER & ADMIN). */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Catatan Keuangan | Cashflow")
public class FinanceView extends VerticalLayout {

    public FinanceView(IUserRepository users, FinanceUseCase useCase, FinancePresenter presenter,
                       FileStorage storage) {
        setSizeFull();
        User user = AuthSession.currentUser(users).orElse(null);
        if (user == null) {
            return; // MainLayout akan mengalihkan ke /login
        }
        PageCard card = new PageCard("Catatan Keuangan");
        card.add(new TransactionPanel(useCase, presenter, storage, user.getId(), false));
        add(card);
    }
}
