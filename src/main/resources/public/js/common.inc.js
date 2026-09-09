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