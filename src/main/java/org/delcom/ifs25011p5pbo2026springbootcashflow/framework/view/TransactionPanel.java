package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.BalanceSummary;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Panel transaksi: kartu ringkasan + toolbar + grid.
 * Dipakai FinanceView (bisa diubah) dan UserTransactionsView (admin, read-only).
 */
public class TransactionPanel extends VerticalLayout {
    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final int userId;
    private final boolean readOnly;

    private final Grid<Transaction> grid = new Grid<>();
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();

    private final Span totalValue = new Span("0");
    private final Span incomeValue = new Span();
    private final Span expenseValue = new Span();
    private final Span balanceValue = new Span();

    public TransactionPanel(FinanceUseCase useCase, FinancePresenter presenter, FileStorage storage,
                            int userId, boolean readOnly) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.storage = storage;
        this.userId = userId;
        this.readOnly = readOnly;
        setPadding(false);
        setSizeFull();

        add(buildSummary(), buildToolbar(), buildGrid());
        refresh();
    }

    // ---------- build ----------

    private HorizontalLayout buildSummary() {
        HorizontalLayout cards = new HorizontalLayout(
                card("Total Transaksi", totalValue, "card-total"),
                card("Pemasukan", incomeValue, "card-income"),
                card("Pengeluaran", expenseValue, "card-expense"),
                card("Saldo", balanceValue, "card-balance"));
        cards.setWidthFull();
        cards.getStyle().set("flex-wrap", "wrap");
        return cards;
    }

    private Div card(String title, Span value, String cssClass) {
        Span label = new Span(title);
        label.addClassName("summary-label");
        value.addClassName("summary-value");
        Div card = new Div(label, value);
        card.addClassNames("summary-card", cssClass);
        return card;
    }

    private HorizontalLayout buildToolbar() {
        search.setPlaceholder("Cari keterangan...");
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setValue(SortOption.NEWEST);
        sort.setLabel("Urutkan");
        sort.addValueChangeListener(e -> {
            grid.sort(List.of()); // urutan SortOption menggantikan sort header kolom
            refresh();
        });

        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        HorizontalLayout toolbar = new HorizontalLayout(search, sort, reload);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");

        if (!readOnly) {
            Button income = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                    e -> openForm(TransactionType.INCOME));
            income.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            Button expense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                    e -> openForm(TransactionType.EXPENSE));
            expense.addThemeVariants(ButtonVariant.LUMO_ERROR);
            toolbar.add(income, expense);
        }
        return toolbar;
    }

    private Grid<Transaction> buildGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true).setFlexGrow(2);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addComponentColumn(this::typeBadge).setHeader("Tipe")
                .setComparator(Comparator.comparing(Transaction::getType));
        grid.addComponentColumn(this::proofThumbnail).setHeader("Bukti");
        grid.addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        if (!readOnly) {
            grid.addComponentColumn(t -> {
                Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(t));
                delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                delete.setAriaLabel("Hapus transaksi " + t.getId());
                return delete;
            }).setHeader("Aksi");
        }
        GridStyles.apply(grid);
        return grid;
    }

    private Span typeBadge(Transaction t) {
        Span badge = new Span(t.getType().getLabel());
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(t.getType() == TransactionType.INCOME ? "success" : "error");
        return badge;
    }

    private Div proofThumbnail(Transaction t) {
        Div holder = new Div();
        if (t.getProofPath() == null) {
            holder.add(new Span("-"));
            return holder;
        }
        String src = "/" + t.getProofPath();
        Image thumb = new Image(src, "Bukti transaksi " + t.getId());
        thumb.setHeight("44px");
        thumb.getStyle().set("cursor", "pointer").set("border-radius", "6px").set("object-fit", "cover");
        thumb.addClickListener(e -> new ImagePreviewDialog("Bukti Transaksi #" + t.getId(), src).open());
        holder.add(thumb);
        return holder;
    }

    // ---------- actions ----------

    private void openForm(TransactionType type) {
        new TransactionFormDialog(type, userId, useCase, presenter, storage, this::refresh).open();
    }

    private void confirmDelete(Transaction tx) {
        AppDialog dialog = new AppDialog("Hapus Transaksi", "Konfirmasi");
        dialog.add(new Span("Hapus transaksi berikut? " + presenter.formatLine(tx)));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button confirm = new Button("Hapus");
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirm.addClickListener(e -> {
            try {
                Transaction deleted = useCase.delete(userId, tx.getId());
                storage.delete(deleted.getProofPath());
                Notification.show(presenter.deletedMessage(deleted), 3500, Notification.Position.BOTTOM_START);
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                Notification error = Notification.show(presenter.errorMessage(ex.getMessage()), 4500,
                        Notification.Position.MIDDLE);
                error.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    public void refresh() {
        grid.setItems(useCase.list(userId, search.getValue(), sort.getValue()));
        BalanceSummary summary = useCase.summarize(userId);
        totalValue.setText(String.valueOf(summary.count()));
        incomeValue.setText(presenter.formatMoney(summary.totalIncome()));
        expenseValue.setText(presenter.formatMoney(summary.totalExpense()));
        balanceValue.setText(presenter.formatMoney(summary.balance()));
    }
}
