import { appState, pageDefinitions } from './state.js';
import { el, escapeHtml, notify } from './utils.js';

export class PageManager {
    constructor() {
        this.pageElements = [];
    }

    bind() {
        this.pageElements = Array.from(document.querySelectorAll('[data-page]'));

        window.addEventListener('hashchange', () => {
            const nextPage = window.location.hash.replace('#', '') || 'overview';
            this.go(nextPage, false);
        });
    }

    go(page, pushHash = true) {
        const target = pageDefinitions.some((definition) => definition.key === page) ? page : 'overview';
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
        return `
            <header class="app-header panel">
                <div class="brand-block">
                    <div class="brand-mark">P</div>
                    <div>
                        <p class="eyebrow">Photon control room</p>
                        <strong>Network operations</strong>
                    </div>
                </div>
                <div class="header-actions">
                    <span class="status-pill neutral" id="authState">Logged out</span>
                    <button type="button" class="secondary" data-open-modal="auth">Sign in</button>
                    <button type="button" class="secondary" data-open-modal="createAccount">Create account</button>
                    <button type="button" class="secondary" data-action="logout">Logout</button>
                </div>
            </header>

            <nav class="app-nav panel">
                ${pageDefinitions.map((page) => `
                    <button type="button" class="nav-link ${page.key === appState.page ? 'active' : ''}" data-nav-link="${page.key}" data-go-page="${page.key}">${escapeHtml(page.label)}</button>
                `).join('')}
                <div class="nav-spacer"></div>
                <span class="nav-hint">Use the pages for status, news, config, tables, and operations.</span>
            </nav>
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
                <div>
                    <strong>Photon web panel</strong>
                    <p>${escapeHtml(accountLabel)}</p>
                </div>
                <div class="footer-links">
                    <span>${escapeHtml(new Date().getFullYear())}</span>
                    <span>Public status and project-author tools</span>
                </div>
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

        const title = this.active.kind === 'news'
            ? (this.active.payload.id ? 'Edit news' : 'Add news')
            : this.active.kind === 'auth'
                ? 'Sign in'
                : 'Create account';

        const body = this.active.kind === 'news'
            ? this.renderNewsForm()
            : this.renderAuthOrAccountForm();

        this.root.innerHTML = `
            <div class="modal-backdrop" data-modal-close>
                <div class="modal panel" role="dialog" aria-modal="true" aria-label="${escapeHtml(title)}">
                    <div class="modal-header">
                        <div>
                            <p class="eyebrow">${escapeHtml(this.active.kind === 'news' ? 'News management' : 'Authentication')}</p>
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

    renderNewsForm() {
        const news = this.active.payload;
        return `
            <form id="newsModalForm" class="stacked-form modal-form">
                <input type="hidden" name="id" value="${escapeHtml(news.id || '')}">
                <label>
                    <span>Title</span>
                    <input type="text" name="title" maxlength="255" value="${escapeHtml(news.title || '')}" placeholder="Season launch" required>
                </label>
                <label>
                    <span>English content</span>
                    <textarea name="contentEn" rows="4" placeholder="English announcement text" required>${escapeHtml(news.contentEn || '')}</textarea>
                </label>
                <label>
                    <span>French content</span>
                    <textarea name="contentFr" rows="4" placeholder="Texte d'annonce en français" required>${escapeHtml(news.contentFr || '')}</textarea>
                </label>
                <label>
                    <span>Image URL</span>
                    <input type="url" name="imageUrl" value="${escapeHtml(news.imageUrl || '')}" placeholder="https://...">
                </label>
                <div class="button-row">
                    <button type="submit" class="primary">${news.id ? 'Update news' : 'Create news'}</button>
                    <button type="button" class="secondary" data-modal-close>Cancel</button>
                </div>
            </form>
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
                this.root.querySelectorAll('[data-auth-tab]').forEach((tabButton) => tabButton.classList.toggle('active', tabButton.dataset.authTab === nextTab));
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

        const newsForm = this.root.querySelector('#newsModalForm');
        if (newsForm) {
            newsForm.addEventListener('submit', (event) => {
                this.app.handleNewsModalSubmit(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
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