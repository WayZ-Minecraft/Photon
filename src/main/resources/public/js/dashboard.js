import { api } from './api.js';
import { appState, configFields } from './state.js';
import { el, escapeHtml, formatDate, formatValue, notify } from './utils.js';
import { FooterBar, HeaderBar, ModalManager, NavigationBar, PageManager } from './shell.js';

class DashboardApp {
    constructor() {
        this.navigation = new NavigationBar(this);
        this.footer = new FooterBar(this);
        this.modalManager = new ModalManager(this);
        this.pageManager = new PageManager();
        this.header = new HeaderBar(this);
        this.tableColumns = [];
    }

    mount() {
        const template = el('appTemplate');
        el('appShell').appendChild(template.content.cloneNode(true));
        this.pageManager.bind();
        this.header.refresh();
        this.footer.refresh();
        this.wireChromeEvents();
        this.renderAll();
        this.pageManager.go(appState.page, false);

        const urlParams = new URLSearchParams(window.location.search);
        const shouldOpenAuth = (!appState.account) && (urlParams.get('auth') === '1' || window.location.hash === '#auth');
        if (shouldOpenAuth) {
            this.modalManager.open('auth');
            urlParams.delete('auth');
            const nextQuery = urlParams.toString();
            const nextHash = window.location.hash === '#auth' ? '' : window.location.hash;
            const nextUrl = `${window.location.pathname}${nextQuery ? `?${nextQuery}` : ''}${nextHash}`;
            window.history.replaceState({}, document.title, nextUrl);
        }

        if (appState.purchaseToken && !appState.account) {
            this.modalManager.open('createAccount', { purchaseToken: appState.purchaseToken });
        }
    }

    wireChromeEvents() {
        if (document.body.dataset.photonChromeBound === 'true') return;
        document.body.dataset.photonChromeBound = 'true';

        document.addEventListener('click', (event) => {
            const copyButton = event.target.closest('[data-copy-text]');
            if (copyButton) {
                this.copyToClipboard(copyButton.dataset.copyText || '', copyButton.dataset.copyLabel || 'value').catch((error) => notify(error.message, 'error'));
                return;
            }

            const openModalButton = event.target.closest('[data-open-modal]');
            if (openModalButton) {
                this.modalManager.open(openModalButton.dataset.openModal, { account: appState.account, purchaseToken: appState.purchaseToken });
                return;
            }

            const scrollButton = event.target.closest('[data-scroll-to]');
            if (scrollButton) {
                const target = document.getElementById(scrollButton.dataset.scrollTo);
                target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                return;
            }

            const pageButton = event.target.closest('[data-go-page]');
            if (pageButton) {
                this.pageManager.go(pageButton.dataset.goPage);
                return;
            }

            const revokeButton = event.target.closest('[data-action="revoke-license"]');
            if (revokeButton) {
                this.revokeLicense(revokeButton.dataset.licenseKey || '').catch((error) => notify(error.message, 'error'));
                return;
            }

            const actionButton = event.target.closest('[data-action]');
            if (actionButton && actionButton.dataset.action === 'logout') {
                this.handleLogout().catch((error) => notify(error.message, 'error'));
                return;
            }
        });

        // Save button removed from template; editing is done via modal

        el('restartButton')?.addEventListener('click', () => {
            this.restartApp().catch((error) => notify(error.message, 'error'));
        });

        el('refreshTableButton')?.addEventListener('click', () => {
            this.loadActiveTable().catch((error) => notify(error.message, 'error'));
        });

        el('tableSelect')?.addEventListener('change', (event) => {
            appState.activeTable = event.target.value;
            this.loadActiveTable().catch((error) => notify(error.message, 'error'));
        });

        el('tableLimit')?.addEventListener('change', () => {
            this.loadActiveTable().catch((error) => notify(error.message, 'error'));
        });

        el('updateUploadForm')?.addEventListener('submit', (event) => {
            this.uploadVersion(event).catch((error) => notify(error.message, 'error'));
        });

    }

    renderAll() {
        this.renderPublic();
        this.renderUser();
        this.renderLicenses();
        this.renderAdmin();
    }

    async copyToClipboard(text, label) {
        if (!text) throw new Error(`No ${label} value to copy`);

        try {
            if (navigator.clipboard?.writeText) {
                await navigator.clipboard.writeText(text);
            } else {
                throw new Error('Clipboard API unavailable');
            }
        } catch {
            const textarea = document.createElement('textarea');
            textarea.value = text;
            textarea.setAttribute('readonly', 'true');
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';
            textarea.style.pointerEvents = 'none';
            textarea.style.left = '-9999px';
            document.body.appendChild(textarea);
            textarea.select();
            textarea.setSelectionRange(0, textarea.value.length);
            const copied = document.execCommand('copy');
            textarea.remove();
            if (!copied) throw new Error(`Failed to copy ${label}`);
        }

        notify(`${label} copied`, 'success');
    }

    renderPublic() {
        const purchaseMount = el('purchasePanel');
        if (purchaseMount) {
            if (appState.account?.subscriber) {
                purchaseMount.innerHTML = `
                    <div class="panel-header">
                        <div>
                            <p class="eyebrow">Subscription</p>
                        </div>
                        <span class="status-pill positive">Active</span>
                    </div>
                    <div class="empty-state">You already have an active subscription.</div>
                `;
            } else {
                purchaseMount.innerHTML = `
                    <div class="panel-header">
                        <div>
                            <p class="eyebrow">Purchase</p>
                        </div>
                    </div>
                    <div class="empty-state">You haven't completed a purchase yet. To complete a purchase, go to <a href="${escapeHtml(appState.config?.store_url || 'https://google.com').trim()}">the store</a>.</div>.
                `;
            }
        }

        this.renderServers();
    }

    renderUser() {
        const mount = el('userPage');
        if (!mount) return;

        if (!appState.account) {
            mount.innerHTML = '<div class="empty-state">Sign in to view your account details.</div>';
            return;
        }

        const copyableFields = new Set(['username', 'uuid', 'discordAuthCode']);
        const accountEntries = Object.entries(appState.account).map(([key, value]) => `
            ${copyableFields.has(key) ? `
                <div class="user-copy-row">
                    <div class="user-field">
                        <span>${escapeHtml(key)}</span>
                        <strong>${escapeHtml(value ?? '—')}</strong>
                    </div>
                    <button type="button" class="secondary copy-button" data-copy-text="${escapeHtml(value ?? '')}" data-copy-label="${escapeHtml(key)}" aria-label="Copy ${escapeHtml(key)}" title="Copy ${escapeHtml(key)}">
                        <i class="fas fa-clone" aria-hidden="true"></i>
                        <span class="sr-only">Copy ${escapeHtml(key)}</span>
                    </button>
                </div>
            ` : `
                <div class="user-field">
                    <span>${escapeHtml(key)}</span>
                    <strong>${escapeHtml(value ?? '—')}</strong>
                </div>
            `}
        `).join('');

        mount.innerHTML = `
            <div class="panel-header">
                <div>
                    <p class="eyebrow">Account</p>
                    <h2>Your profile</h2>
                </div>
                <div class="button-row">
                    <button type="button" class="nav-link icon-button" data-open-modal="profile" aria-label="Edit profile" title="Edit profile">
                        <i class="fas fa-pencil-alt" aria-hidden="true"></i>
                        <span class="sr-only">Edit profile</span>
                    </button>
                    <button type="button" class="copy-button" data-action="logout" aria-label="Logout" title="Logout" style="background: var(--danger); color: #fff; border-color: var(--danger);">
                        <i class="fas fa-sign-out-alt" aria-hidden="true"></i>
                        <span class="sr-only">Logout</span>
                    </button>
                </div>
            </div>
            <div class="data-grid user-grid">${accountEntries}</div>
        `;

        mount.querySelectorAll('[data-copy-text]').forEach((button) => {
            button.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                this.copyToClipboard(button.dataset.copyText || '', button.dataset.copyLabel || 'value').catch((error) => notify(error.message, 'error'));
            });
        });
    }

    renderLicenses() {
        const mount = el('licensesPage');
        if (!mount) return;

        if (!appState.account) {
            mount.innerHTML = '<div class="empty-state">Sign in to manage your licenses.</div>';
            return;
        }

        if (!appState.account.subscriber) {
            mount.innerHTML = '<div class="empty-state">Your subscription is not active yet. Once Stripe confirms it, this area will unlock automatically.</div>';
            return;
        }

        const rawLicenses = Array.isArray(appState.licenses) ? appState.licenses : [];
        const normalizedLicenses = rawLicenses
            .map((license, index) => {
                const normalized = this.normalizeLicenseEntry(license);
                return normalized ? { ...normalized, _index: index } : null;
            })
            .filter(Boolean);
        const skippedCount = Math.max(0, rawLicenses.length - normalizedLicenses.length);

        const licenseRows = normalizedLicenses.length ? normalizedLicenses.map((license) => {
            const isRevoked = String(license.status || '').toUpperCase() === 'REVOKED';
            const statusLabel = isRevoked
                ? 'Revoked'
                : String(license.status || '').toUpperCase() === 'ACTIVE'
                    ? 'Active'
                    : 'Pending activation';
            return `
                <tr class="license-table-row">
                    <td>
                        <div class="license-cell-main">
                            <strong>${formatValue(license.name)}</strong>
                            <span class="license-cell-subtitle">${escapeHtml(license.productId || '—')}</span>
                        </div>
                    </td>
                    <td>${formatDate(license.createdAt)}</td>
                    <td>${formatDate(license.expiresAt)}</td>
                    <td>
                        <code class="license-key-inline">${escapeHtml(license.licenseKey || '—')}</code>
                    </td>
                    <td class="license-status-cell">
                        <span class="status-pill ${isRevoked ? 'danger' : 'neutral'}">${escapeHtml(statusLabel)}</span>
                    </td>
                    <td class="license-actions-cell">
                        <div class="button-row compact license-actions">
                            ${isRevoked ? '' : `
                            <button type="button" class="secondary copy-button" data-copy-text="${escapeHtml(license.licenseKey || '')}" data-copy-label="license key" aria-label="Copy license key" title="Copy">
                                <i class="fas fa-copy" aria-hidden="true"></i>
                                <span class="sr-only">Copy license key</span>
                            </button>
                            `}
                            ${isRevoked ? '' : `
                            <button type="button" class="danger" data-action="revoke-license" data-license-key="${escapeHtml(license.licenseKey || '')}" aria-label="Delete license" title="Delete">
                                <i class="fas fa-trash-alt" aria-hidden="true"></i>
                                <span class="sr-only">Delete license</span>
                            </button>
                            `}
                        </div>
                    </td>
                </tr>
            `;
        }).join('') : '';

        const licenseContent = normalizedLicenses.length ? `
            <div class="table-wrap license-table-wrap">
                <table class="license-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Created at</th>
                            <th>Expires</th>
                            <th>Key</th>
                            <th class="license-status-header">Status</th>
                            <th class="license-actions-header">Actions</th>
                        </tr>
                    </thead>
                    <tbody>${licenseRows}</tbody>
                </table>
            </div>
        ` : '<div class="empty-state">No readable licenses were found yet.</div>';

        mount.innerHTML = `
            <div class="panel-header">
                <div>
                    <p class="eyebrow">Licenses</p>
                    <h2>Create and manage keys</h2>
                </div>
                <div class="button-row">
                    <button type="button" class="nav-link icon-button" data-open-modal="createLicense" aria-label="Add license" title="Add license">
                        <i class="fas fa-plus" aria-hidden="true"></i>
                        <span class="sr-only">Add license</span>
                    </button>
                    <span class="status-pill positive">Active subscription</span>
                </div>
            </div>

            ${skippedCount > 0 ? `<div class="empty-state" style="margin: 12px 0;">${skippedCount} corrupted license row${skippedCount > 1 ? 's were' : ' was'} skipped while loading.</div>` : ''}
            ${licenseContent}
        `;
    }

    normalizeLicenseEntry(license) {
        if (!license || typeof license !== 'object') return null;

        const normalizeKey = (key) => String(key || '').toLowerCase().replace(/[^a-z0-9]/g, '');
        const isObject = (value) => value && typeof value === 'object' && !Array.isArray(value);
        const hasValue = (value) => value !== undefined && value !== null && String(value).trim() !== '';

        const candidates = [license];
        Object.values(license).forEach((value) => {
            if (isObject(value)) candidates.push(value);
        });

        const valueByKey = new Map();
        candidates.forEach((candidate) => {
            Object.entries(candidate).forEach(([key, value]) => {
                if (!hasValue(value)) return;
                const normalizedKey = normalizeKey(key);
                if (!valueByKey.has(normalizedKey)) valueByKey.set(normalizedKey, value);
            });
        });

        const getFirst = (...aliases) => {
            for (const alias of aliases) {
                const direct = valueByKey.get(normalizeKey(alias));
                if (hasValue(direct)) return direct;
            }
            return null;
        };

        const normalized = {
            licenseKey: getFirst('licenseKey', 'license_key', 'licensekey', 'license_id', 'licenseid', 'key'),
            productId: getFirst('productId', 'product_id', 'productid', 'product'),
            name: getFirst('name', 'customerName', 'customer_name', 'ownerName'),
            customerEmail: getFirst('customerEmail', 'customer_email', 'email', 'mail'),
            status: getFirst('status', 'state'),
            createdAt: getFirst('createdAt', 'created_at', 'issued_at', 'issuedAt', 'created'),
            expiresAt: getFirst('expiresAt', 'expires_at', 'expires', 'expiryAt', 'expirationAt'),
        };

        const hasCoreValue = Boolean(normalized.licenseKey || normalized.productId || normalized.name || normalized.customerEmail || normalized.createdAt || normalized.expiresAt || normalized.status);
        return hasCoreValue ? normalized : null;
    }

    renderAdmin() {
        this.renderConfig();
        this.renderTables();
    }

    renderServers() {
        const list = el('serverList');
        const count = el('serverCount');
        if (!list || !count) return;

        if (!appState.servers.length) {
            list.innerHTML = '<div class="empty-state">No servers were found.</div>';
            const numberEl = count.querySelector('#serverCountNumber');
            if (numberEl) numberEl.textContent = '0';
            else count.textContent = '0';
            return;
        }

        const numberEl = count.querySelector('#serverCountNumber');
        if (numberEl) numberEl.textContent = String(appState.servers.length);
        else count.textContent = `${appState.servers.length} servers`;
        const rows = appState.servers.map((server) => {
            const title = server.serverName || `${server.serverIP || 'Unknown'}:${server.serverPort || '—'}`;
            const address = `${server.serverIP || 'Unknown'}:${server.serverPort || '—'}`;
            const lastSeen = formatDate(server.last_seen_at);
            const motd = formatValue(server.serverMOTD) || '—';
            const site = formatValue(server.site) || '';

            const siteLink = server.site ? String(server.site).trim() : null;
            const discordLink = server.discord ? String(server.discord).trim() : null;
            const actions = [];
            actions.push(`
                <button type="button" class="secondary copy-button" data-copy-text="${escapeHtml(address)}" data-copy-label="server address" aria-label="Copy server address" title="Copy">
                    <i class="fas fa-copy" aria-hidden="true"></i>
                    <span class="sr-only">Copy server address</span>
                </button>
            `);
            if (siteLink) actions.push(`<a class="nav-link icon-button" href="${escapeHtml(siteLink)}" target="_blank" rel="noreferrer" title="Open site"><i class="fas fa-external-link-alt" aria-hidden="true"></i><span class="sr-only">Open site</span></a>`);
            if (discordLink) actions.push(`<a class="nav-link icon-button" href="${escapeHtml(discordLink)}" target="_blank" rel="noreferrer" title="Open Discord"><i class="fab fa-discord" aria-hidden="true"></i><span class="sr-only">Open Discord</span></a>`);

            return `
                <tr class="server-table-row">
                    <td>
                        <div class="license-cell-main">
                            <strong>${escapeHtml(title)}</strong>
                            <span class="license-cell-subtitle">${escapeHtml(address)}</span>
                        </div>
                    </td>
                    <td>${escapeHtml(lastSeen)}</td>
                    <td>${escapeHtml(motd)}</td>
                    <td class="license-actions-cell">
                        <div class="button-row compact license-actions">
                            ${actions.join('\n')}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        list.innerHTML = `
            <div class="table-wrap license-table-wrap">
                <table class="license-table server-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Last seen</th>
                            <th>MOTD</th>
                            <th class="license-actions-header">Actions</th>
                        </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        `;
    }

    renderConfig() {
        const mount = el('configForm');
        if (!mount) return;

        if (!appState.config) {
            if (!appState.token && !appState.account?.administrator) {
                mount.innerHTML = '<div class="empty-state">Sign in to load the editable config.</div>';
                return;
            }

            mount.innerHTML = '<div class="empty-state">Loading config...</div>';
            return;
        }

        const entries = configFields.map(({ key, label }) => `
            <div class="user-field">
                <span>${escapeHtml(label)}</span>
                <strong>${escapeHtml(String(appState.config[key] ?? ''))}</strong>
            </div>
        `).join('');

        mount.innerHTML = `<div class="data-grid user-grid">${entries}</div>`;

        // append an Edit button into the surrounding panel header if present
        const panel = mount.closest('.panel');
        const headerButtons = panel?.querySelector('.panel-header .button-row');
        if (headerButtons && !headerButtons.querySelector('[data-open-modal="config"]')) {
            headerButtons.insertAdjacentHTML('afterbegin', '<button type="button" class="secondary" data-open-modal="config">Edit config</button>');
        }

        // no per-field copy buttons on config page (use read-only rows)
    }

    renderTables() {
        const select = el('tableSelect');
        if (!select) return;

        if (!appState.tables.length) {
            select.innerHTML = '<option value="">No tables available</option>';
            return;
        }

        select.innerHTML = appState.tables.map((table) => `
            <option value="${escapeHtml(table.table)}">${escapeHtml(table.label)}</option>
        `).join('');

        if (!appState.activeTable) appState.activeTable = appState.tables[0].table;
        select.value = appState.activeTable;
    }

    renderTableData(data) {
        const table = el('dataTable');
        const meta = el('tableMeta');
        if (!table || !meta) return;

        if (!data || !data.columns || !data.rows) {
            table.innerHTML = '';
            meta.textContent = 'No table selected.';
            return;
        }

        this.tableColumns = data.columns;
        meta.textContent = `${data.label} · ${data.rows.length} rows loaded`;
        const header = data.columns.map((column) => `<th>${escapeHtml(column)}</th>`).join('');
        const body = data.rows.map((row) => {
            const cells = data.columns.map((column) => `<td>${formatValue(row[column])}</td>`).join('');
            return `<tr>${cells}</tr>`;
        }).join('');

        table.innerHTML = `<thead><tr>${header}</tr></thead><tbody>${body}</tbody>`;
    }

    async loadPublicData() {
        appState.servers = await api('/api/status/servers');
        this.renderPublic();
    }

    async loadAdminData() {
        if (!appState.token && !appState.account?.administrator) {
            appState.account = null;
            appState.config = null;
            appState.tables = [];
            this.renderUser();
            this.renderAdmin();
            this.header.refresh();
            this.refreshPages();
            return;
        }

        appState.account = await api('/api/admin/me');
        appState.config = await api('/api/admin/config');
        appState.tables = await api('/api/admin/tables');
        this.renderUser();
        this.renderAdmin();
        this.header.refresh();
        await this.loadActiveTable();
        this.refreshPages();
    }

    async loadAccountData() {
        if (!appState.userToken) {
            appState.licenses = [];
            this.renderUser();
            this.renderLicenses();
            this.header.refresh();
            this.refreshPages();
            return;
        }

        appState.account = await api('/accounts/me');
        appState.licenses = appState.account?.subscriber ? await api('/accounts/licenses') : [];
        this.renderUser();
        this.renderLicenses();
        this.header.refresh();
        this.refreshPages();
    }

    async loadActiveTable() {
        if ((!appState.token && !appState.account?.administrator) || !appState.activeTable) return;

        const limit = Number(el('tableLimit')?.value || 100);
        const data = await api(`/api/admin/tables/${encodeURIComponent(appState.activeTable)}?limit=${encodeURIComponent(limit)}`);
        this.renderTableData(data);
    }

    async handleLogin(event) {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        const requestBody = new URLSearchParams();
        requestBody.set('email', String(formData.get('email') || '').trim());
        requestBody.set('password', String(formData.get('password') || ''));

        const body = requestBody.toString();
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' };

        const adminResponse = await fetch('/api/admin/login', {
            method: 'POST',
            headers,
            body,
            credentials: 'same-origin',
        });

        if (adminResponse.ok) {
            const adminPayload = await adminResponse.json();
            // Using cookie-based admin session: do not store token in localStorage
            appState.token = '';
            appState.userToken = '';
            appState.account = adminPayload.account || adminPayload;
            localStorage.setItem('photon-account', JSON.stringify(appState.account));
            notify('Signed in as administrator', 'success');
            this.header.refresh();
            this.renderUser();
            await Promise.all([
                this.loadPublicData(),
                this.loadAdminData(),
            ]);
            return;
        }

        const publicResponse = await fetch('/accounts/auth_account', {
            method: 'POST',
            headers,
            body,
        });

        const contentType = publicResponse.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await publicResponse.json() : await publicResponse.text();
        if (!publicResponse.ok) throw new Error(typeof payload === 'string' ? payload : 'Login failed');

        appState.token = '';
        appState.userToken = payload.token || '';
        appState.account = payload.account || payload;
        localStorage.setItem('photon-user-token', appState.userToken);
        localStorage.setItem('photon-account', JSON.stringify(appState.account));
        localStorage.removeItem('photon-admin-token');
        notify('Signed in', 'success');
        this.header.refresh();
        this.renderUser();
        this.renderAdmin();
        await this.loadPublicData();
        await this.loadAccountData();
        this.refreshPages();
    }

    async handlePasswordChange(event) {
        event.preventDefault();

        if (!appState.account?.email) throw new Error('Sign in first');

        const formData = new FormData(event.currentTarget);
        const username = String(formData.get('username') || '').trim();
        const email = String(formData.get('email') || '').trim();
        const currentPassword = String(formData.get('currentPassword') || '');
        const newPassword = String(formData.get('newPassword') || '');
        const confirmPassword = String(formData.get('confirmPassword') || '');
        const feedback = document.getElementById('profileUpdateFeedback');

        if (!username || !email) {
            if (feedback) {
                feedback.textContent = 'Username and email are required.';
                feedback.dataset.state = 'error';
            }
            throw new Error('Username and email are required');
        }

        if ((newPassword || confirmPassword) && newPassword !== confirmPassword) {
            if (feedback) {
                feedback.textContent = 'Passwords do not match.';
                feedback.dataset.state = 'error';
            }
            throw new Error('Passwords do not match');
        }

        if (newPassword && newPassword.length < 8) {
            if (feedback) {
                feedback.textContent = 'Password must be at least 8 characters long.';
                feedback.dataset.state = 'error';
            }
            throw new Error('Password must be at least 8 characters long');
        }

        const account = await api('/accounts/update_profile', {
            method: 'POST',
            body: JSON.stringify({
                uuid: appState.account.uuid,
                currentPassword,
                username,
                email,
                newPassword,
                confirmPassword,
            }),
        });

        appState.account = account;
        localStorage.setItem('photon-account', JSON.stringify(account));

        if (feedback) {
            feedback.textContent = '';
            feedback.dataset.state = 'idle';
        }

        try {
            const formEl = event?.currentTarget ?? document.getElementById('profileForm');
            if (formEl && typeof formEl.reset === 'function') formEl.reset();
        } catch (e) {
            // ignore reset errors
        }
        this.header.refresh();
        this.renderUser();
        this.renderLicenses();
        notify('Profile updated', 'success');
    }

    async handleCreateAccount(event) {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        const password = String(formData.get('password') || '');
        const confirmPassword = String(formData.get('confirmPassword') || '');
        const feedback = document.getElementById('createAccountFeedback');
        const purchaseToken = String(formData.get('token') || appState.purchaseToken || '').trim();

        if (password !== confirmPassword) {
            if (feedback) {
                feedback.textContent = 'Passwords do not match.';
                feedback.dataset.state = 'error';
            }
            throw new Error('Passwords do not match');
        }

        if (feedback) {
            feedback.textContent = '';
            feedback.dataset.state = 'idle';
        }

        const body = new URLSearchParams();
        body.set('username', String(formData.get('username') || '').trim());
        body.set('email', String(formData.get('email') || '').trim());
        body.set('password', password);
        if (purchaseToken) body.set('token', purchaseToken);

        const response = await fetch('/accounts/create_account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body,
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await response.json() : await response.text();
        if (!response.ok) throw new Error(typeof payload === 'string' ? payload : 'Account creation failed');

        appState.userToken = payload.token || '';
        appState.account = payload.account || payload;
        localStorage.setItem('photon-user-token', appState.userToken);
        localStorage.setItem('photon-account', JSON.stringify(appState.account));
        if (purchaseToken) {
            appState.purchaseToken = '';
            window.history.replaceState({}, document.title, window.location.pathname + window.location.hash);
        }
        this.header.refresh();
        this.renderUser();
        this.renderLicenses();
        await this.loadAccountData();
        notify('Account created. You can sign in now.', 'success');
    }

    async handleCreateLicense(event) {
        event.preventDefault();
        if (!appState.userToken) throw new Error('Sign in first');
        if (!appState.account?.subscriber) throw new Error('Active subscription required');

        const form = event.currentTarget instanceof HTMLFormElement
            ? event.currentTarget
            : event.target instanceof HTMLFormElement
                ? event.target
                : null;
        if (!form) throw new Error('License form is not available');

        const formData = new FormData(form);
        const payload = {
            name: String(formData.get('name') || '').trim(),
        };

        const durationDays = String(formData.get('duration_days') || '').trim();
        if (durationDays) payload.duration_days = Number(durationDays);

        const license = await api('/accounts/licenses', {
            method: 'POST',
            body: JSON.stringify(payload),
        });

        appState.licenses = [license, ...(appState.licenses || [])];
        form.reset();
        this.renderLicenses();
        notify('License created', 'success');
    }

    async revokeLicense(licenseKey) {
        if (!appState.userToken) throw new Error('Sign in first');
        if (!licenseKey) throw new Error('Missing license key');
        if (!window.confirm('Revoke this license?')) return;

        const license = await api('/accounts/licenses/revoke', {
            method: 'POST',
            body: JSON.stringify({ license_key: licenseKey }),
        });

        appState.licenses = (appState.licenses || []).map((entry) => {
            const key = entry.licenseKey || entry.license_key;
            if (key !== (license.licenseKey || license.license_key)) return entry;
            return license;
        });
        this.renderLicenses();
        notify('License revoked', 'success');
    }

    async saveConfig(formElement = null) {
        if (!appState.token && !appState.account?.administrator) throw new Error('Sign in first');

        const payload = {};
        const root = formElement ?? el('configForm');
        root?.querySelectorAll('[data-config-key]').forEach((input) => {
            const key = input.dataset.configKey;
            if (input.type === 'number') {
                payload[key] = input.value === '' ? null : Number(input.value);
                return;
            }

            payload[key] = input.value;
        });

        appState.config = await api('/api/admin/config', {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
        this.renderConfig();
        notify('Config saved', 'success');
    }

    async restartApp() {
        if (!appState.token && !appState.account?.administrator) throw new Error('Sign in first');
        if (!window.confirm('Restart Photon now?')) return;

        await api('/api/admin/restart', { method: 'POST' });
        notify('Restart requested', 'success');
    }

    async uploadVersion(event) {
        event.preventDefault();
        if (!appState.token && !appState.account?.administrator) throw new Error('Sign in first');

        const form = event.currentTarget;
        if (!(form instanceof HTMLFormElement)) throw new Error('Invalid upload form');

        const formData = new FormData(form);
        const file = formData.get('file');
        const fileType = String(formData.get('file_type') || '').trim();
        const channel = String(formData.get('channel') || 'STABLE').trim();

        if (!(file instanceof File) || file.size === 0) throw new Error('Please select a .jar file');
        if (!file.name.toLowerCase().endsWith('.jar')) throw new Error('Only .jar files are allowed');
        if (!fileType) throw new Error('File type is required');

        formData.set('file_type', fileType);
        formData.set('channel', channel || 'STABLE');

        const result = await api('/api/admin/updates/upload', {
            method: 'POST',
            body: formData,
        });

        form.reset();
        notify(result?.message || 'Update uploaded successfully', 'success');
    }

    async handleLogout() {
        appState.token = '';
        appState.userToken = '';
        appState.account = null;
        appState.config = null;
        appState.tables = [];
        appState.licenses = [];
        appState.activeTable = null;
        localStorage.removeItem('photon-account');
        localStorage.removeItem('photon-admin-token');
        localStorage.removeItem('photon-user-token');
        this.header.refresh();
        this.renderUser();
        this.renderLicenses();
        this.renderAdmin();
        this.refreshPages();
        notify('Session cleared');
    }

    refreshPages() {
        this.pageManager.go(appState.page, false);
    }
}

export function bootstrapDashboard() {
    const app = new DashboardApp();

    app.mount();

    app.loadPublicData().catch((error) => notify(error.message, 'error'));

    if (appState.token || appState.account?.administrator) {
        app.loadAdminData().catch((error) => notify(error.message, 'error'));
    }

    if (appState.userToken) {
        app.loadAccountData().catch((error) => notify(error.message, 'error'));
    }

    return app;
}