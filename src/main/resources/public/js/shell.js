import { appState, pageDefinitions, configFields } from './state.js';
import { el, escapeHtml, notify } from './utils.js';

export class PageManager {
    constructor() {
        this.pageElements = [];
    }

    canAccessAccountPages() {
        return Boolean(appState.token) || Boolean(appState.userToken) || Boolean(appState.account);
    }

    canAccessSubscriptionPages() {
        return Boolean(appState.userToken) && Boolean(appState.account?.subscriber);
    }

    canAccessAuthorPages() {
        return Boolean(appState.token) || Boolean(appState.account?.administrator);
    }

    canAccessPage(page) {
        const definition = pageDefinitions.find((item) => item.key === page);
        if (!definition) return false;

        if (definition.requiresLogin) return this.canAccessAccountPages();
        if (definition.requiresSubscription) return this.canAccessSubscriptionPages();
        if (definition.requiresAuth) return this.canAccessAuthorPages();
        return true;
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
        const visiblePages = pageDefinitions.filter((page) => {
            if (page.key === 'user') return false;
            if (page.requiresLogin) return Boolean(appState.token) || Boolean(appState.account);
            if (page.requiresSubscription) return Boolean(appState.userToken) && Boolean(appState.account?.subscriber);
            if (page.requiresAuth) return Boolean(appState.token) || Boolean(appState.account?.administrator);
            return true;
        });
        const authActions = appState.account
            ? `
                <button type="button" class="nav-link icon-button ${appState.page === 'user' ? 'active' : ''}" data-nav-link="user" data-go-page="user" aria-label="User page" title="User page">
                    <i class="fas fa-user" aria-hidden="true"></i>
                    <span class="sr-only">User</span>
                </button>
            `
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
                <a href="https://niwer.dev/store" target="_blank" rel="noreferrer">
                    <i class="fas fa-store" aria-hidden="true"></i>
                </a>
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

        const title = this.active.kind === 'auth'
            ? 'Sign in'
            : this.active.kind === 'profile'
                ? 'Edit profile'
                : this.active.kind === 'config'
                    ? 'Edit config'
                    : this.active.kind === 'createLicense'
                        ? 'Create license'
                        : 'Create account';

        const body = this.active.kind === 'profile' ? this.renderProfileForm()
            : this.active.kind === 'config' ? this.renderConfigForm()
            : this.active.kind === 'createLicense' ? this.renderLicenseForm()
            : this.renderAuthOrAccountForm();

        this.root.innerHTML = `
            <div class="modal-backdrop" data-modal-close>
                <div class="modal panel" role="dialog" aria-modal="true" aria-label="${escapeHtml(title)}">
                    <div class="modal-header">
                        <div>
                            <h2>${escapeHtml(title)}</h2>
                        </div>
                        <button type="button" class="secondary modal-close" data-modal-close aria-label="Close dialog">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    ${body}
                </div>
            </div>
        `;

        this.bind();
    }

    renderLicenseForm() {
        return `
            <div class="modal-body">
                <form id="licenseForm" onsubmit="event.preventDefault()" class="modal-panel active stacked-form">
                    <label>
                        <span>Name</span>
                        <input type="text" name="name" placeholder="A name to identify this license key (e.g. 'John's license')" required>
                    </label>
                    <label>
                        <span>Duration in days</span>
                        <input type="number" name="duration_days" min="1" step="1" placeholder="30">
                    </label>
                    
                    <div class="button-row">
                        <button type="button" class="secondary" data-modal-close>Cancel</button>
                        <button type="submit" class="primary">Create license</button>
                    </div>
                </form>
            </div>
        `;
    }

    renderAuthOrAccountForm() {
        const initialTab = this.active.kind === 'createAccount' ? 'create' : 'signIn';
        const purchaseToken = this.active.payload?.purchaseToken || appState.purchaseToken || '';
        return `
            <div class="modal-tabs" data-auth-tabs>
                <button type="button" class="tab-button ${initialTab === 'signIn' ? 'active' : ''}" data-auth-tab="signIn">Sign in</button>
                <button type="button" class="tab-button ${initialTab === 'create' ? 'active' : ''}" data-auth-tab="create">Create account</button>
            </div>
            <div class="modal-body">
                <form id="signInForm" onsubmit="event.preventDefault()" class="modal-panel ${initialTab === 'signIn' ? 'active' : ''}" data-auth-panel="signIn">
                    <label>
                        <span>Email</span>
                        <input type="email" name="email" autocomplete="email" placeholder="joe@gmail.com" required>
                    </label>
                    <label class="password-field">
                        <span>Password</span>
                        <div class="password-field-row">
                            <input type="password" name="password" autocomplete="current-password" placeholder="Your password" required>
                            <button type="button" class="secondary password-toggle" data-password-toggle data-target="password" aria-label="Show password" aria-pressed="false">
                                <i class="password-toggle-icon password-toggle-icon-show fas fa-eye" aria-hidden="true"></i>
                                <i class="password-toggle-icon password-toggle-icon-hide fas fa-eye-slash" aria-hidden="true"></i>
                            </button>
                        </div>
                    </label>
                    <button type="submit" class="primary">Sign in</button>
                </form>

                <form id="createAccountForm" onsubmit="event.preventDefault()" class="modal-panel ${initialTab === 'create' ? 'active' : ''}" data-auth-panel="create">
                    ${purchaseToken ? '<input type="hidden" name="token" value="' + escapeHtml(purchaseToken) + '">' : ''}
                    ${purchaseToken ? '<p class="hint">Purchase token detected. Your subscription will be linked automatically after account creation.</p>' : ''}
                    <label>
                        <span>Username</span>
                        <input type="text" name="username" autocomplete="username" placeholder="JoeDalton_" required>
                    </label>
                    <label>
                        <span>Email</span>
                        <input type="email" name="email" autocomplete="email" placeholder="joe@gmail.com" required>
                    </label>
                    <label class="password-field">
                        <span>Password</span>
                        <div class="password-field-row">
                            <input type="password" name="password" autocomplete="new-password" placeholder="$aZf*&my1HJd3" required>
                            <button type="button" class="secondary password-toggle" data-password-toggle data-target="password" aria-label="Show password" aria-pressed="false">
                                <i class="password-toggle-icon password-toggle-icon-show fas fa-eye" aria-hidden="true"></i>
                                <i class="password-toggle-icon password-toggle-icon-hide fas fa-eye-slash" aria-hidden="true"></i>
                            </button>
                        </div>
                    </label>
                    <p id="createAccountPasswordMeter" class="hint form-feedback password-strength" aria-live="polite">Password strength: empty</p>
                    <label class="password-field">
                        <span>Confirm password</span>
                        <div class="password-field-row">
                            <input type="password" name="confirmPassword" autocomplete="new-password" placeholder="Repeat your password" required>
                            <button type="button" class="secondary password-toggle" data-password-toggle data-target="confirmPassword" aria-label="Show password" aria-pressed="false">
                                <i class="password-toggle-icon password-toggle-icon-show fas fa-eye" aria-hidden="true"></i>
                                <i class="password-toggle-icon password-toggle-icon-hide fas fa-eye-slash" aria-hidden="true"></i>
                            </button>
                        </div>
                    </label>
                    <p id="createAccountFeedback" class="hint form-feedback" aria-live="polite"></p>
                    <button type="submit" class="primary">Create account</button>
                </form>
            </div>
        `;
    }

    renderProfileForm() {
        const account = this.active.payload?.account ?? appState.account ?? {};
        return `
            <form id="profileForm" onsubmit="event.preventDefault()" class="modal-panel active stacked-form">
                <label>
                    <span>Username</span>
                    <input type="text" name="username" autocomplete="username" value="${escapeHtml(account.username ?? '')}" required>
                </label>
                <label>
                    <span>Email</span>
                    <input type="email" name="email" autocomplete="email" value="${escapeHtml(account.email ?? '')}" required>
                </label>
                <hr class="form-divider">
                <label>
                    <span>New password</span>
                    <div class="password-field-row">
                        <input type="password" name="newPassword" autocomplete="new-password" placeholder="Leave empty to keep your current password">
                        <button type="button" class="secondary password-toggle" data-password-toggle data-target="newPassword" aria-label="Show password" aria-pressed="false">
                            <svg class="password-toggle-icon password-toggle-icon-show" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Zm10 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" fill="currentColor"></path>
                            </svg>
                            <svg class="password-toggle-icon password-toggle-icon-hide" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M3.5 4.5 19.5 20.5l1.4-1.4-2.7-2.7C21 15 22 12 22 12s-3.5-7-10-7c-1.5 0-2.9.3-4.1.8L4.9 3.1 3.5 4.5ZM9 10l1.6 1.6A2 2 0 0 1 12 10a2 2 0 0 1 2 2c0 .4-.1.7-.3 1l1.6 1.6A4 4 0 0 0 9 10Zm3 8c6.5 0 10-7 10-7s-1-2-3.2-4.1l-2.1 2.1c.8.9 1.3 1.8 1.6 2.4-1.1 2-3.6 5-6.3 5-.7 0-1.4-.1-2-.3l-1.8 1.8c1.2.4 2.5.6 3.8.6Zm-8.2-2.9 2.1-2.1C4.8 12.1 4 12 4 12s3.5-7 10-7c.9 0 1.8.1 2.6.3l1.8-1.8C16.9 3.6 14.6 3 12 3 5.5 3 2 12 2 12s1 2 3.2 4.1Z" fill="currentColor"></path>
                            </svg>
                        </button>
                    </div>
                </label>
                <p id="profilePasswordMeter" class="hint form-feedback password-strength" aria-live="polite">Password strength: empty</p>
                <label>
                    <span>Confirm new password</span>
                    <div class="password-field-row">
                        <input type="password" name="confirmPassword" autocomplete="new-password" placeholder="Repeat the new password">
                        <button type="button" class="secondary password-toggle" data-password-toggle data-target="confirmPassword" aria-label="Show password" aria-pressed="false">
                            <svg class="password-toggle-icon password-toggle-icon-show" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Zm10 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" fill="currentColor"></path>
                            </svg>
                            <svg class="password-toggle-icon password-toggle-icon-hide" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M3.5 4.5 19.5 20.5l1.4-1.4-2.7-2.7C21 15 22 12 22 12s-3.5-7-10-7c-1.5 0-2.9.3-4.1.8L4.9 3.1 3.5 4.5ZM9 10l1.6 1.6A2 2 0 0 1 12 10a2 2 0 0 1 2 2c0 .4-.1.7-.3 1l1.6 1.6A4 4 0 0 0 9 10Zm3 8c6.5 0 10-7 10-7s-1-2-3.2-4.1l-2.1 2.1c.8.9 1.3 1.8 1.6 2.4-1.1 2-3.6 5-6.3 5-.7 0-1.4-.1-2-.3l-1.8 1.8c1.2.4 2.5.6 3.8.6Zm-8.2-2.9 2.1-2.1C4.8 12.1 4 12 4 12s3.5-7 10-7c.9 0 1.8.1 2.6.3l1.8-1.8C16.9 3.6 14.6 3 12 3 5.5 3 2 12 2 12s1 2 3.2 4.1Z" fill="currentColor"></path>
                            </svg>
                        </button>
                    </div>
                </label>
                <p id="profilePasswordFeedback" class="hint form-feedback" aria-live="polite"></p>
                <hr class="form-divider">
                <label>
                    <span>Current password</span>
                    <div class="password-field-row">
                        <input type="password" name="currentPassword" autocomplete="current-password" placeholder="Confirm your identity" required>
                        <button type="button" class="secondary password-toggle" data-password-toggle data-target="currentPassword" aria-label="Show password" aria-pressed="false">
                            <svg class="password-toggle-icon password-toggle-icon-show" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Zm10 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" fill="currentColor"></path>
                            </svg>
                            <svg class="password-toggle-icon password-toggle-icon-hide" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M3.5 4.5 19.5 20.5l1.4-1.4-2.7-2.7C21 15 22 12 22 12s-3.5-7-10-7c-1.5 0-2.9.3-4.1.8L4.9 3.1 3.5 4.5ZM9 10l1.6 1.6A2 2 0 0 1 12 10a2 2 0 0 1 2 2c0 .4-.1.7-.3 1l1.6 1.6A4 4 0 0 0 9 10Zm3 8c6.5 0 10-7 10-7s-1-2-3.2-4.1l-2.1 2.1c.8.9 1.3 1.8 1.6 2.4-1.1 2-3.6 5-6.3 5-.7 0-1.4-.1-2-.3l-1.8 1.8c1.2.4 2.5.6 3.8.6Zm-8.2-2.9 2.1-2.1C4.8 12.1 4 12 4 12s3.5-7 10-7c.9 0 1.8.1 2.6.3l1.8-1.8C16.9 3.6 14.6 3 12 3 5.5 3 2 12 2 12s1 2 3.2 4.1Z" fill="currentColor"></path>
                            </svg>
                        </button>
                    </div>
                </label>
                <p id="profileUpdateFeedback" class="hint form-feedback" aria-live="polite"></p>
                <div class="button-row">
                    <button type="button" class="secondary" data-modal-close>Cancel</button>
                    <button type="submit" class="primary">Save changes</button>
                </div>
            </form>
        `;
    }

    renderConfigForm() {
        const config = this.active.payload?.config ?? appState.config ?? {};
        const fields = (configFields || Object.keys(config).map(k => ({ key: k, label: k, type: 'text' }))).map(({ key, label, type }) => `
            <label>
                <span>${escapeHtml(label)}</span>
                <input type="${type}" data-config-key="${key}" value="${escapeHtml(config[key] ?? '')}">
            </label>
        `).join('');

        return `
            <form id="configModalForm" onsubmit="event.preventDefault()" class="modal-panel active stacked-form">
                <div>
                    <p class="eyebrow">Network config</p>
                    <h3>Edit runtime settings</h3>
                </div>
                ${fields}
                <div class="button-row">
                    <button type="button" class="secondary" data-modal-close>Cancel</button>
                    <button type="submit" class="primary">Save changes</button>
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
                this.root.querySelectorAll('[data-auth-panel]').forEach((panel) => panel.classList.toggle('active', panel.dataset.authPanel === nextTab));
            });
        });

        const signInForm = this.root.querySelector('#signInForm');
        if (signInForm) {
            signInForm.addEventListener('submit', (event) => {
                this.app.handleLogin(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }

        this.root.querySelectorAll('[data-password-toggle]').forEach((button) => {
            button.addEventListener('click', () => {
                const targetName = button.dataset.target;
                const form = button.closest('form');
                const input = form?.querySelector(`[name="${targetName}"]`);
                if (!(input instanceof HTMLInputElement)) return;

                const isVisible = input.type === 'text';
                input.type = isVisible ? 'password' : 'text';
                button.dataset.visible = isVisible ? 'false' : 'true';
                button.setAttribute('aria-pressed', String(!isVisible));
                button.setAttribute('aria-label', isVisible ? 'Show password' : 'Hide password');
            });
        });

        const profileForm = this.root.querySelector('#profileForm');
        if (profileForm) {
            const updateProfilePasswordMeter = () => {
                const password = profileForm.querySelector('[name="newPassword"]')?.value || '';
                const meter = this.root.querySelector('#profilePasswordMeter');

                if (!meter) return;

                const strength = password.length === 0 ? 0 : password.length < 6 ? 1 : password.length < 10 ? 2 : password.length < 14 ? 3 : password.length < 18 ? 4 : 5;
                const strengthLabel = ['empty', 'weak', 'fair', 'good', 'strong', 'excellent'][strength];
                meter.dataset.strength = String(strength);
                meter.textContent = `Password strength: ${strengthLabel}`;
            };

            const updateProfileFeedback = () => {
                const password = profileForm.querySelector('[name="newPassword"]')?.value || '';
                const confirmPassword = profileForm.querySelector('[name="confirmPassword"]')?.value || '';
                const feedback = this.root.querySelector('#profilePasswordFeedback');

                if (!feedback) return;

                const hasMismatch = password.length > 0 && confirmPassword.length > 0 && password !== confirmPassword;
                feedback.textContent = hasMismatch ? 'Passwords do not match.' : '';
                feedback.dataset.state = hasMismatch ? 'error' : 'idle';
            };

            profileForm.querySelectorAll('[name="newPassword"], [name="confirmPassword"]').forEach((input) => {
                input.addEventListener('input', () => {
                    updateProfilePasswordMeter();
                    updateProfileFeedback();
                });
            });

            updateProfilePasswordMeter();
            updateProfileFeedback();

            profileForm.addEventListener('submit', (event) => {
                this.app.handlePasswordChange(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }

        const createAccountForm = this.root.querySelector('#createAccountForm');
        if (createAccountForm) {
            const updatePasswordMeter = () => {
                const password = createAccountForm.querySelector('[name="password"]')?.value || '';
                const meter = this.root.querySelector('#createAccountPasswordMeter');

                if (!meter) return;

                const strength = password.length === 0 ? 0 : password.length < 6 ? 1 : password.length < 10 ? 2 : password.length < 14 ? 3 : password.length < 18 ? 4 : 5;
                const strengthLabel = ['empty', 'weak', 'fair', 'good', 'strong', 'excellent'][strength];
                meter.dataset.strength = String(strength);
                meter.textContent = `Password strength: ${strengthLabel}`;
            };

            const updateFeedback = () => {
                const password = createAccountForm.querySelector('[name="password"]')?.value || '';
                const confirmPassword = createAccountForm.querySelector('[name="confirmPassword"]')?.value || '';
                const feedback = this.root.querySelector('#createAccountFeedback');

                if (!feedback) return;

                const hasMismatch = password.length > 0 && confirmPassword.length > 0 && password !== confirmPassword;
                feedback.textContent = hasMismatch ? 'Passwords do not match.' : '';
                feedback.dataset.state = hasMismatch ? 'error' : 'idle';
            };

            createAccountForm.querySelectorAll('[name="password"], [name="confirmPassword"]').forEach((input) => {
                input.addEventListener('input', () => {
                    updatePasswordMeter();
                    updateFeedback();
                });
            });

            updatePasswordMeter();
            updateFeedback();

            createAccountForm.addEventListener('submit', (event) => {
                this.app.handleCreateAccount(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }

        const licenseForm = this.root.querySelector('#licenseForm');
        if (licenseForm) {
            licenseForm.addEventListener('submit', (event) => {
                this.app.handleCreateLicense(event).then(() => this.close()).catch((error) => notify(error.message, 'error'));
            });
        }

        const configModalForm = this.root.querySelector('#configModalForm');
        if (configModalForm) {
            configModalForm.addEventListener('submit', (event) => {
                event.preventDefault();
                this.app.saveConfig(configModalForm).then(() => this.close()).catch((error) => notify(error.message, 'error'));
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