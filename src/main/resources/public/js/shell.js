import { appState, pageDefinitions } from './state.js';
import { el, escapeHtml, notify } from './utils.js';

export class PageManager {
    constructor() {
        this.pageElements = [];
    }

    canAccessPage(page) {
        const definition = pageDefinitions.find((item) => item.key === page);
        if (!definition) return false;

        return !definition.requiresAuth || Boolean(appState.token);
    }

    bind() {
        this.pageElements = Array.from(document.querySelectorAll('[data-page]'));

        window.addEventListener('hashchange', () => {
            const nextPage = window.location.hash.replace('#', '') || 'overview';
            this.go(nextPage, false);
        });
    }

    go(page, pushHash = true) {
        const target = this.canAccessPage(page) ? page : 'overview';
        appState.page = target;

        if (pushHash && window.location.hash.replace('#', '') !== target) {
            window.location.hash = target;
        }

        this.pageElements.forEach((element) => {
            element.classList.toggle('page-active', element.dataset.page === target);
        });

        document.querySelectorAll('[data-nav-link]').forEach((link) => {
            link.classList.toggle('active', link.dataset.navLink === target);
        });

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

export class NavigationBar {
    render() {
        const visiblePages = pageDefinitions.filter((page) => !page.requiresAuth || Boolean(appState.token));
        const authActions = appState.account
            ? '<button type="button" class="secondary" data-action="logout">Logout</button>'
            : `
                <button type="button" class="secondary" data-open-modal="auth">Sign in</button>
                <button type="button" class="secondary" data-open-modal="createAccount">Create account</button>
            `;
        const pageButtons = visiblePages.map((page) => `
            <button type="button" class="nav-link ${page.key === appState.page ? 'active' : ''}" data-nav-link="${page.key}" data-go-page="${page.key}">${escapeHtml(page.label)}</button>
        `).join('');

        return `
            <header class="app-header panel">
                <div class="brand-block">
                    <img class="brand-mark" src="./assets/photon_logo.png" alt="Photon logo">
                </div>
                <div class="header-actions header-pages">
                    ${pageButtons}
                </div>
                <div class="header-actions header-auth">
                    ${authActions}
                </div>
            </header>
        `;
    }

    refresh() {
        const mount = el('appChrome');
        mount.innerHTML = this.render();
    }
}

export class FooterBar {
    render() {
        const accountLabel = appState.account ? `Signed in as ${appState.account.username || appState.account.email}` : 'Public access';
        return `
            <footer class="app-footer panel">
                <p>Photon web panel</p>
            </footer>
        `;
    }

    refresh() {
        const mount = el('appFooter');
        mount.innerHTML = this.render();
    }
}

export class ModalManager {
    constructor(app) {
        this.app = app;
        this.root = el('modalRoot');
        this.active = null;
    }

    open(kind, payload = {}) {
        this.active = { kind, payload };
        this.render();
    }

    close() {
        this.active = null;
        this.render();
    }

    render() {
        if (!this.active) {
            this.root.innerHTML = '';
            return;
        }

        const title = this.active.kind === 'auth' ? 'Sign in' : 'Create account';
        const body = this.renderAuthOrAccountForm();

        this.root.innerHTML = `
            <div class="modal-backdrop" data-modal-close>
                <div class="modal panel" role="dialog" aria-modal="true" aria-label="${escapeHtml(title)}">
                    <div class="modal-header">
                        <div>
                            <p class="eyebrow">${escapeHtml('Authentication')}</p>
                            <h2>${escapeHtml(title)}</h2>
                        </div>
                        <button type="button" class="secondary modal-close" data-modal-close>Close</button>
                    </div>
                    ${body}
                </div>
            </div>
        `;

        this.bind();
    }

    renderAuthOrAccountForm() {
        const initialTab = this.active.kind === 'createAccount' ? 'create' : 'signIn';
        return `
            <div class="modal-tabs" data-auth-tabs>
                <button type="button" class="tab-button ${initialTab === 'signIn' ? 'active' : ''}" data-auth-tab="signIn">Sign in</button>
                <button type="button" class="tab-button ${initialTab === 'create' ? 'active' : ''}" data-auth-tab="create">Create account</button>
            </div>
            <div class="modal-body">
                <form id="signInForm" class="modal-panel ${initialTab === 'signIn' ? 'active' : ''}" data-auth-panel="signIn">
                    <label>
                        <span>Email</span>
                        <input type="email" name="email" autocomplete="email" placeholder="author@project.com" required>
                    </label>
                    <label>
                        <span>Password</span>
                        <input type="password" name="password" autocomplete="current-password" placeholder="Your password" required>
                    </label>
                    <button type="submit" class="primary">Sign in</button>
                </form>

                <form id="createAccountForm" class="modal-panel ${initialTab === 'create' ? 'active' : ''}" data-auth-panel="create">
                    <label>
                        <span>Username</span>
                        <input type="text" name="username" autocomplete="username" placeholder="Your username" required>
                    </label>
                    <label>
                        <span>Email</span>
                        <input type="email" name="email" autocomplete="email" placeholder="author@project.com" required>
                    </label>
                    <label>
                        <span>Password</span>
                        <input type="password" name="password" autocomplete="new-password" placeholder="Create a password" required>
                    </label>
                    <button type="submit" class="primary">Create account</button>
                </form>
            </div>
        `;
    }


    bind() {
        this.root.querySelectorAll('[data-modal-close]').forEach((button) => {
            button.addEventListener('click', (event) => {
                if (button.classList.contains('modal-backdrop') && event.target !== button) {
                    return;
                }

                if (button.dataset.modalClose !== undefined || event.target === button) {
                    this.close();
                }
            });
        });

        this.root.querySelectorAll('[data-auth-tab]').forEach((button) => {
            button.addEventListener('click', () => {
                const nextTab = button.dataset.authTab;
                this.root.querySelectorAll('[data-auth-panel]').forEach((panel) => panel.classList.toggle('active', panel.dataset.authPanel === nextTab));
            });
        });

        const signInForm = this.root.querySelector('#signInForm');
        if (signInForm) {
            signInForm.addEventListener('submit', (event) => {
                this.app.handleLogin(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }

        const createAccountForm = this.root.querySelector('#createAccountForm');
        if (createAccountForm) {
            createAccountForm.addEventListener('submit', (event) => {
                this.app.handleCreateAccount(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }
        
        this.root.querySelector('.modal-backdrop')?.addEventListener('click', (event) => {
            if (event.target === event.currentTarget) this.close();
        });
    }
}

export class HeaderBar {
    constructor(app) {
        this.app = app;
    }

    refresh() {
        this.app.navigation.refresh();
        this.app.footer.refresh();
    }
}