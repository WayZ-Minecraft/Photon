class IncNav extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <nav>
                <div class="nav-brand">
                    <img src="assets/photon_logo.png" alt="Photon" style="height: 36px; width: auto; object-fit: contain;">
                </div>
                <div class="nav-links" id="navMenu">
                    <a class="nav-link active" data-target="overview">Overview</a>
                    <a class="nav-link" data-target="downloads">Downloads</a>
                    <a class="nav-link sub-required hidden" data-target="licenses">Licenses</a>
                    <a class="nav-link admin-required hidden" data-target="admin">Admin</a>
                </div>
                <div class="nav-actions">
                    <button class="icon-btn" id="themeToggle" title="Toggle Theme"><i class="fa-solid fa-moon"></i></button>
                    <button class="btn primary guest-only" onclick="UI.openModal('authModal')">Sign In</button>
                    <button class="icon-btn auth-required hidden" id="navProfileBtn" onclick="UI.navigate('user')" title="Profile"><i class="fa-solid fa-user"></i></button>
                    <button class="icon-btn auth-required hidden" onclick="App.logout()" title="Logout"><i class="fa-solid fa-sign-out-alt"></i></button>
                    <button class="icon-btn mobile-menu-btn" onclick="UI.toggleMobileMenu()" title="Menu"><i class="fa-solid fa-bars"></i></button>
                </div>
            </nav>
        `;
    }
}
customElements.define('inc-nav', IncNav);

class IncFooter extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <div class="footer-links">
                <a href="#" id="footerStoreLink" target="_blank" title="Store"><i class="fa-solid fa-store fa-lg"></i></a>
                <a href="#" id="footerTosLink" target="_blank" title="Terms of Service"><i class="fa-solid fa-scale-balanced fa-lg"></i></a>
                <a href="#" id="footerTosaleLink" target="_blank" title="Terms of Sale"><i class="fa-solid fa-scale-unbalanced fa-lg"></i></a>
                <a href="#" id="footerPrivacyLink" target="_blank" title="Privacy Policy"><i class="fa-solid fa-shield-halved fa-lg"></i></a>
            </div>
        `;
    }
}
customElements.define('inc-footer', IncFooter); 