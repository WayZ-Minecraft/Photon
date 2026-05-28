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
    }

    wireChromeEvents() {
        if (document.body.dataset.photonChromeBound === 'true') return;
        document.body.dataset.photonChromeBound = 'true';

        document.addEventListener('click', (event) => {
            const openModalButton = event.target.closest('[data-open-modal]');
            if (openModalButton) {
                this.modalManager.open(openModalButton.dataset.openModal);
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

            const actionButton = event.target.closest('[data-action]');
            if (actionButton && actionButton.dataset.action === 'logout') {
                this.handleLogout().catch((error) => notify(error.message, 'error'));
                return;
            }
        });

        el('saveConfigButton')?.addEventListener('click', () => {
            this.saveConfig().catch((error) => notify(error.message, 'error'));
        });

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
    }

    renderAll() {
        this.renderPublic();
        this.renderAdmin();
    }

    renderPublic() {
        this.renderServers();
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
            count.textContent = '0 servers';
            return;
        }

        count.textContent = `${appState.servers.length} servers`;
        list.innerHTML = appState.servers.map((server) => {
            const title = server.serverName || `${server.serverIP || 'Unknown'}:${server.serverPort || '—'}`;
            const subtitle = `${server.serverIP || 'Unknown'}:${server.serverPort || '—'} · queue ${server.queuePort ?? '—'}`;
            return `
                <article class="data-card">
                    <div class="data-card-top">
                        <div>
                            <h3>${escapeHtml(title)}</h3>
                            <p>${escapeHtml(subtitle)}</p>
                        </div>
                        <span class="status-pill neutral">${formatDate(server.last_seen_at)}</span>
                    </div>
                    <div class="data-grid compact-grid">
                        <div><span>MOTD</span><strong>${formatValue(server.serverMOTD)}</strong></div>
                        <div><span>Site</span><strong>${formatValue(server.site)}</strong></div>
                        <div><span>Discord</span><strong>${formatValue(server.discord)}</strong></div>
                    </div>
                </article>
            `;
        }).join('');
    }

    renderConfig() {
        const form = el('configForm');
        if (!form) return;

        if (!appState.config) {
            form.innerHTML = '<div class="empty-state">Sign in to load the editable config.</div>';
            return;
        }

        form.innerHTML = configFields.map(({ key, label, type }) => `
            <label class="config-field">
                <span>${escapeHtml(label)}</span>
                <input type="${type}" data-config-key="${key}" value="${escapeHtml(appState.config[key] ?? '')}">
            </label>
        `).join('');
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
        if (!appState.token) {
            appState.account = null;
            appState.config = null;
            appState.tables = [];
            this.renderAdmin();
            this.renderHeaderState();
            this.refreshPages();
            return;
        }

        appState.account = await api('/api/admin/me');
        appState.config = await api('/api/admin/config');
        appState.tables = await api('/api/admin/tables');
        this.renderAdmin();
        this.renderHeaderState();
        await this.loadActiveTable();
        this.refreshPages();
    }

    async loadActiveTable() {
        if (!appState.token || !appState.activeTable) return;

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
        });

        if (adminResponse.ok) {
            const adminPayload = await adminResponse.json();
            appState.token = adminPayload.token;
            appState.account = adminPayload.account;
            localStorage.setItem('photon-admin-token', appState.token);
            localStorage.setItem('photon-account', JSON.stringify(appState.account));
            notify('Signed in', 'success');
            this.header.refresh();
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
        appState.account = payload;
        localStorage.setItem('photon-account', JSON.stringify(payload));
        localStorage.removeItem('photon-admin-token');
        notify('Signed in', 'success');
        this.header.refresh();
        this.renderAdmin();
        await this.loadPublicData();
        this.refreshPages();
    }

    async handleCreateAccount(event) {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        const body = new URLSearchParams();
        body.set('username', String(formData.get('username') || '').trim());
        body.set('email', String(formData.get('email') || '').trim());
        body.set('password', String(formData.get('password') || ''));

        const response = await fetch('/accounts/create_account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body,
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await response.json() : await response.text();
        if (!response.ok) throw new Error(typeof payload === 'string' ? payload : 'Account creation failed');

        notify('Account created. You can sign in now.', 'success');
    }

    async saveConfig() {
        if (!appState.token) throw new Error('Sign in first');

        const payload = {};
        el('configForm')?.querySelectorAll('[data-config-key]').forEach((input) => {
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
        if (!appState.token) throw new Error('Sign in first');
        if (!window.confirm('Restart Photon now?')) return;

        await api('/api/admin/restart', { method: 'POST' });
        notify('Restart requested', 'success');
    }

    async handleLogout() {
        appState.token = '';
        appState.account = null;
        appState.config = null;
        appState.tables = [];
        appState.activeTable = null;
        localStorage.removeItem('photon-account');
        localStorage.removeItem('photon-admin-token');
        this.header.refresh();
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

    const template = el('appTemplate');
    el('appShell').appendChild(template.content.cloneNode(true));
    app.pageManager.bind();
    app.header.refresh();
    app.footer.refresh();

    if (document.body.dataset.photonChromeBound !== 'true') {
        document.body.dataset.photonChromeBound = 'true';
        document.addEventListener('click', (event) => {
            const openModalButton = event.target.closest('[data-open-modal]');
            if (openModalButton) {
                app.modalManager.open(openModalButton.dataset.openModal);
                return;
            }

            const pageButton = event.target.closest('[data-go-page]');
            if (pageButton) {
                app.pageManager.go(pageButton.dataset.goPage);
                return;
            }

            const actionButton = event.target.closest('[data-action]');
            if (actionButton && actionButton.dataset.action === 'logout') {
                app.handleLogout().catch((error) => notify(error.message, 'error'));
                return;
            }
        });
    }

    el('saveConfigButton')?.addEventListener('click', () => {
        app.saveConfig().catch((error) => notify(error.message, 'error'));
    });

    el('restartButton')?.addEventListener('click', () => {
        app.restartApp().catch((error) => notify(error.message, 'error'));
    });

    el('refreshTableButton')?.addEventListener('click', () => {
        app.loadActiveTable().catch((error) => notify(error.message, 'error'));
    });

    el('tableSelect')?.addEventListener('change', (event) => {
        appState.activeTable = event.target.value;
        app.loadActiveTable().catch((error) => notify(error.message, 'error'));
    });

    el('tableLimit')?.addEventListener('change', () => {
        app.loadActiveTable().catch((error) => notify(error.message, 'error'));
    });

    app.renderAll();
    app.refreshPages();

    app.loadPublicData().catch((error) => notify(error.message, 'error'));

    if (appState.token) {
        app.loadAdminData().catch((error) => notify(error.message, 'error'));
    }

    return app;
}