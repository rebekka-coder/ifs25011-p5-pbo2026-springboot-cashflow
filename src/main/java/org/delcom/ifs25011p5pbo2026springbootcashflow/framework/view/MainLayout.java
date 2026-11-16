package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth.AuthSession;

import java.util.Optional;

/** Layout utama: header (banner), sidebar, area isi (main), menu avatar, dan guard login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final Main main = new Main();
    private Integer builtForUserId;

    public MainLayout(IUserRepository users) {
        this.users = users;
        setPrimarySection(Section.DRAWER);
        main.getStyle().set("display", "block").set("height", "100%");
        setContent(main); // isi halaman berada di dalam landmark <main>
    }

    /** Isi halaman dimasukkan ke <main>, bukan langsung ke AppLayout. */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        main.getElement().removeAllChildren();
        if (content != null) {
            main.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(users);
        if (user.isEmpty()) {
            // /login adalah halaman HTML biasa (LoginPageFilter), bukan rute Vaadin: muat ulang penuh
            event.getUI().getPage().setLocation("/login");
            return;
        }
        int currentId = user.get().getId();
        if (builtForUserId == null) {
            build(user.get());
            builtForUserId = currentId;
        } else if (builtForUserId.intValue() != currentId) {
            event.getUI().getPage().reload();
        }
    }

    private void build(User user) {
        Span title = new Span("Cashflow"); // bukan heading: <h1> milik judul halaman
        title.addClassName("app-title");

        Span name = new Span(user.getFullName());
        Span role = new Span(user.isAdmin() ? "ADMIN" : "USER");
        role.getElement().getThemeList().add("badge");
        if (user.isAdmin()) {
            role.getElement().getThemeList().add("primary");
        }

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage("/" + user.getPhotoPath());
        }
        avatar.getStyle().set("cursor", "pointer");
        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        menu.addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().getPage().setLocation("/login");
        });

        HorizontalLayout right = new HorizontalLayout(name, role, avatar);
        right.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        right.getStyle().set("margin-left", "auto").set("padding-right", "var(--lumo-space-m)");

        HorizontalLayout navbar = new HorizontalLayout(new DrawerToggle(), title, right);
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Header banner = new Header(navbar); // landmark banner untuk seluruh isi navbar
        banner.getStyle().set("width", "100%");
        addToNavbar(banner);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Catatan Keuangan", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi Pengguna", UserTransactionsView.class,
                    VaadinIcon.LIST_UL.create()));
        }
        addToDrawer(nav);
    }
}