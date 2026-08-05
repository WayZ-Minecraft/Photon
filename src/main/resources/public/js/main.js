/**
 * Core Application State
 */
const State = {
    token: '',
    userToken: localStorage.getItem('photon-user-token') || '',
    account: JSON.parse(localStorage.getItem('photon-account')) || null,
    purchaseToken: new URLSearchParams(window.location.search).get('token') || '',
    activePage: 'overview',
    config: null,
    configSchema: [
        { key: 'bot_activity', label: 'Bot Activity', type: 'text' },
        { key: 'discord_bot_token', label: 'Discord Bot Token', type: 'password' },
        { key: 'discord_bot_id', label: 'Discord Bot ID', type: 'text' },
        { key: 'official_discord_server_id', label: 'Official Discord Server ID', type: 'text' },
        { key: 'network_console_channel_id', label: 'Console Channel ID', type: 'text' },
        { key: 'server_creator_role_id', label: 'Server Creator Role ID', type: 'text' },
        { key: 'webserver_port', label: 'Webserver Port', type: 'number' },
        { key: 'license_product_id', label: 'License Product ID', type: 'text' },
        { key: 'license_default_duration_days', label: 'Default License Duration (days)', type: 'number' },
        { key: 'stripe_api_key', label: 'Stripe API Key', type: 'password' },
        { key: 'stripe_webhook_secret', label: 'Stripe Webhook Secret', type: 'password' },
        { key: 'api_version', label: 'API Version', type: 'text' },
        { key: 'mod_version', label: 'Mod Version', type: 'text' },
        { key: 'launcher_version', label: 'Launcher Version', type: 'text' },
        { key: 'store_url', label: 'Store URL', type: 'url' },
        { key: 'terms_of_service_url', label: "Terms of Service", type: 'url' },
        { key: 'terms_of_sale_url', label: "Terms of Sale", type: 'url' },
        { key: 'privacy_policy_url', label: 'Privacy Policy', type: 'url' }
    ]
};

/**
 * API Client
 */
const Api = async (path, options = {}) => {
    const headers = new Headers(options.headers || {});
    if (State.token) headers.set('Authorization', `Bearer ${State.token}`);
    if (State.userToken) headers.set('X-Photon-User-Token', State.userToken);
    
    const method = (options.method || 'GET').toUpperCase();
    if (path.startsWith('/api/admin') && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
        const cookie = document.cookie.split(';').map(c => c.trim()).find(c => c.startsWith('photon_csrf='));
        if (cookie) headers.set('X-CSRF-Token', decodeURIComponent(cookie.split('=')[1] || ''));
    }
    
    const isFormData = options.body instanceof FormData;
    if (options.body && !isFormData && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

    const res = await fetch(path, { credentials: 'same-origin', ...options, headers });
    const contentType = res.headers.get('content-type') || '';
    const payload = contentType.includes('json') ? await res.json() : await res.text();

    if (!res.ok) throw new Error(typeof payload === 'string' ? payload : payload?.message || res.statusText);
    return payload;
};

/**
 * Formats
 */
const formatVal = (v) => v == null || v === '' ? '—' : (typeof v === 'object' ? JSON.stringify(v) : String(v));
const formatDate = (v) => {
    if (!v) return '—';
    const d = new Date(v);
    return isNaN(d.getTime()) ? v : d.toLocaleString();
};

/**
 * UI & DOM Manipulation
 */
const UI = {
    init() {
        // Disconnect active user session if a URL token is detected
        if (State.purchaseToken && (State.userToken || State.account || State.token)) App.logout();

        // Routing
        document.querySelectorAll('.nav-link').forEach(el => {
            el.addEventListener('click', (e) => this.navigate(e.target.dataset.target));
        });
        
        // Theme setup
        const btn = document.getElementById('themeToggle');
        const html = document.documentElement;
        const updateThemeIcon = () => {
            const isDark = html.getAttribute('data-theme') === 'dark' || (html.getAttribute('data-theme') === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
            btn.innerHTML = isDark ? '<i class="fa-solid fa-sun"></i>' : '<i class="fa-solid fa-moon"></i>';
        };
        
        btn.addEventListener('click', () => {
            const current = html.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', next);
            localStorage.setItem('theme-preference', next);
            updateThemeIcon();
        });
        
        const savedTheme = localStorage.getItem('theme-preference');
        if(savedTheme) html.setAttribute('data-theme', savedTheme);
        updateThemeIcon();

        // Handle Purchase Token UI state
        if (State.purchaseToken && !State.account) {
            document.getElementById('purchaseAlert').classList.remove('hidden');
            document.getElementById('registerModalSubtitle').innerHTML = '<span class="text-accent"><i class="fa-solid fa-link"></i> Purchase linked automatically.</span>';
            this.switchAuthTab('register');
        }

        if (State.userToken && !State.token) {
            Api('/accounts/me')
                .then((account) => {
                    State.account = account;
                    localStorage.setItem('photon-account', JSON.stringify(account));
                    this.updateAuthVisbility();
                })
                .catch(() => {});
        }

        this.updateAuthVisbility();
    },

    toggleMobileMenu() {
        const menu = document.getElementById('navMenu');
        const btnIcon = document.querySelector('.mobile-menu-btn i');
        if (!menu) return;
        const isOpen = menu.classList.toggle('mobile-open');
        if (btnIcon) btnIcon.className = isOpen ? 'fa-solid fa-xmark' : 'fa-solid fa-bars';
    },

    navigate(pageId) {
        if(!pageId) return;
        
        // Close mobile menu if open
        const menu = document.getElementById('navMenu');
        const btnIcon = document.querySelector('.mobile-menu-btn i');
        if (menu) menu.classList.remove('mobile-open');
        if (btnIcon) btnIcon.className = 'fa-solid fa-bars';

        document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        
        const targetPage = document.getElementById(`page-${pageId}`);
        if (targetPage) targetPage.classList.add('active');
        
        const targetLink = document.querySelector(`.nav-link[data-target="${pageId}"]`);
        if (targetLink) targetLink.classList.add('active');
        
        const profileBtn = document.getElementById('navProfileBtn');
        if (profileBtn) {
            if (pageId === 'user') {
                profileBtn.style.color = 'var(--accent-color)';
                profileBtn.style.borderColor = 'var(--accent-color)';
            } else {
                profileBtn.style.color = '';
                profileBtn.style.borderColor = '';
            }
        }
        
        State.activePage = pageId;

        // Lazy load logic
        if(pageId === 'overview') App.loadPublicServers();
        if(pageId === 'licenses' && State.account?.subscriber) App.loadLicenses();
        if(pageId === 'tables') App.loadTablesList();
    },

    updateAuthVisbility() {
        const isAdmin = !!State.token || State.account?.administrator;
        const isUser = !!State.userToken || !!State.account;
        const isSub = isUser && (State.account?.subscriber || State.account?.subscriptionStatus === 'ACTIVE');

        document.querySelectorAll('.guest-only').forEach(el => el.classList.toggle('hidden', isUser));
        document.querySelectorAll('.auth-required').forEach(el => el.classList.toggle('hidden', !isUser));
        document.querySelectorAll('.sub-required').forEach(el => el.classList.toggle('hidden', !isSub));
        document.querySelectorAll('.admin-required').forEach(el => el.classList.toggle('hidden', !isAdmin));

        if (isUser && State.account) {
            // Populate Edit Form
            const editUser = document.getElementById('editUsername');
            const editEmail = document.getElementById('editEmail');
            if(editUser) editUser.value = State.account.username || '';
            if(editEmail) editEmail.value = State.account.email || '';

            // Build Dynamic Profile Grid
            const grid = document.getElementById('profileDetailsGrid');
            if (grid) {
                const copyable = ['username', 'uuid', 'email', 'discordAuthCode'];
                grid.innerHTML = Object.entries(State.account).map(([key, val]) => {
                    const v = formatVal(val);
                    const isCopyable = copyable.includes(key) && val;
                    return `
                        <div class="card">
                            <span class="text-secondary text-sm" style="text-transform: capitalize;"><i class="fa-solid fa-tag text-accent" style="margin-right: 6px;"></i>${this.escapeHTML(key)}</span>
                            <div class="card-body" style="margin-top: 0.5rem;">
                                <strong class="${key==='uuid'?'font-mono text-sm':''}" style="word-break: break-all; color: var(--text-primary); font-size: 0.95rem;">${this.escapeHTML(v)}</strong>
                            </div>
                            ${isCopyable ? `
                            <div class="card-footer">
                                <button class="btn icon-btn" title="Copy" onclick="UI.copy('${this.escapeHTML(String(val))}')"><i class="fa-regular fa-copy"></i></button>
                            </div>
                            ` : ''}
                        </div>
                    `;
                }).join('');
            }

            // Subscription Overview Update
            const subBox = document.getElementById('subscriptionStatusBox');
            if (subBox) {
                if (State.account.subscriber) {
                    subBox.innerHTML = `
                        <div><p class="text-secondary" style="margin:0">Your subscription is active and in good standing.</p></div>
                        <span class="badge active" style="font-size: 0.9rem; padding: 0.5rem 1rem;"><i class="fa-solid fa-check"></i> Active</span>
                    `;
                } else {
                    const storeUrl = State.config?.store_url || 'https://google.com';
                    subBox.innerHTML = `
                        <div><p class="text-secondary" style="margin:0">You do not have an active subscription.</p></div>
                        <a href="${this.escapeHTML(storeUrl)}" target="_blank" class="btn primary"><i class="fa-solid fa-cart-shopping"></i> Go to Store</a>
                    `;
                }
            }

            const profileSubCard = document.getElementById('profileSubscriptionCard');
            if (profileSubCard) {
                const status = State.account.subscriptionStatus || (isSub ? 'ACTIVE' : 'EXPIRED');
                const expiresAt = State.account.subscriptionExpiresAt ? formatDate(State.account.subscriptionExpiresAt) : 'No expiry date';
                profileSubCard.innerHTML = `
                    <div class="card-header">
                        <span style="text-transform: capitalize;"><i class="fa-solid fa-credit-card text-accent" style="margin-right: 6px;"></i>Subscription</span>
                    </div>
                    <div class="card-body">
                        <strong style="color: var(--text-primary); font-size: 0.95rem;">${this.escapeHTML(status)}</strong>
                        <p class="text-secondary text-sm" style="margin: 0.5rem 0 0;">Expires: ${this.escapeHTML(expiresAt)}</p>
                    </div>
                    <div class="card-footer">
                        <span class="badge ${isSub ? 'active' : 'inactive'}">${isSub ? 'Active' : 'Inactive'}</span>
                    </div>
                `;
            }

            document.getElementById('purchaseAlert').classList.add('hidden');
        } else {
                const subBox = document.getElementById('subscriptionStatusBox');
                if(subBox) {
                    subBox.innerHTML = `
                        <div><p class="text-secondary" style="margin:0">Sign in to view your subscription status.</p></div>
                        <button onclick="UI.openModal('authModal')" class="btn primary"><i class="fa-solid fa-right-to-bracket"></i> Sign In</button>
                    `;
                }
        }
    },

    openModal(id) { document.getElementById(id).classList.add('open'); },
    
    closeModal(event, force=false) {
        if (force || (event && event.target && event.target.classList.contains('modal-backdrop'))) {
            document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('open'));
        }
    },

    switchAuthTab(tab) {
        document.querySelectorAll('.modal-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.modal-panel').forEach(p => p.classList.remove('active'));
        
        const tabs = document.querySelectorAll('.modal-tab');
        if (tab === 'login' && tabs.length > 0) tabs[0].classList.add('active');
        if (tab === 'register' && tabs.length > 1) tabs[1].classList.add('active');
        
        const formId = tab === 'login' ? 'loginForm' : 'registerForm';
        const formEl = document.getElementById(formId);
        if (formEl) formEl.classList.add('active');
    },

    toast(msg, type='info') {
        const container = document.getElementById('toast-container');
        const el = document.createElement('div');
        el.className = `toast ${type}`;
        const icon = type === 'success' ? 'check-circle' : type === 'error' ? 'circle-exclamation' : 'circle-info';
        el.innerHTML = `<i class="fa-solid fa-${icon}"></i> ${this.escapeHTML(msg)}`;
        container.appendChild(el);
        setTimeout(() => { el.style.opacity = '0'; setTimeout(()=>el.remove(), 300); }, 3000);
    },

    copy(text) {
        navigator.clipboard.writeText(text)
            .then(() => this.toast('Copied to clipboard', 'success'))
            .catch(() => this.toast('Failed to copy', 'error'));
    },

    escapeHTML(str) {
        return String(str||'').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[m]));
    }
};

/**
 * App Logic & Data Fetching
 */
const App = {
    async init() {
        UI.init();
        UI.navigate(window.location.hash.replace('#','') || 'overview');
        
        const promises = [this.loadPublicServers()];
        if (State.token || State.account?.administrator) promises.push(this.loadAdminConfig());
        
        await Promise.allSettled(promises);
    },

    // --- Authentication ---
    clearPurchaseToken() {
        if (State.purchaseToken) {
            State.purchaseToken = '';
            window.history.replaceState({}, '', window.location.pathname);
        }
    },

    async login(e) {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        const originalText = btn.innerHTML;
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
        }

        const formData = new FormData(e.target);
        const body = new URLSearchParams();
        for (const [key, val] of formData.entries()) {
            body.append(key, val);
        }
        
        // Append the purchase token if it exists
        if (State.purchaseToken) body.set('token', State.purchaseToken);

        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

        try {
            // Pre-emptively notify the stripe session endpoint just like in register
            if (State.purchaseToken) {
                await fetch('/stripe/purchase_session', {
                    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ checkoutSessionId: State.purchaseToken })
                }).catch(()=>{});
            }

            // Try Admin
            const adminRes = await fetch('/api/admin/login', { method: 'POST', headers, body: body.toString(), credentials: 'same-origin' });
            if (adminRes.ok) {
                const payload = await adminRes.json();
                State.token = ''; State.userToken = ''; State.account = payload.account || payload;
                localStorage.setItem('photon-account', JSON.stringify(State.account));
                UI.toast('Signed in as admin', 'success');
                
                // Clear token from URL after successful auth
                this.clearPurchaseToken();
                
                this.onLoginSuccess();
                return;
            }

            // Fallback to User
            const payload = await Api('/accounts/auth_account', { method: 'POST', headers, body: body.toString() });
            
            State.userToken = payload.token || ''; State.account = payload.account || payload;
            localStorage.setItem('photon-user-token', State.userToken);
            localStorage.setItem('photon-account', JSON.stringify(State.account));
            
            // Clear token from URL after successful auth
            this.clearPurchaseToken();

            UI.toast('Signed in', 'success');
            this.onLoginSuccess();
        } catch (err) {
            UI.toast(err.message, 'error');
        } finally {
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        }
    },

    async register(e) {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        const originalText = btn.innerHTML;
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
        }

        const formData = new FormData(e.target);
        const body = new URLSearchParams();
        for (const [key, val] of formData.entries()) {
            body.append(key, val);
        }
        if (State.purchaseToken) body.set('token', State.purchaseToken);

        try {
            if (State.purchaseToken) {
                await fetch('/stripe/purchase_session', {
                    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ checkoutSessionId: State.purchaseToken })
                }).catch(()=>{});
            }

            const res = await Api('/accounts/create_account', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() });
            State.userToken = res.token || ''; State.account = res.account || res;
            localStorage.setItem('photon-user-token', State.userToken);
            localStorage.setItem('photon-account', JSON.stringify(State.account));
            
            if (State.purchaseToken) { State.purchaseToken = ''; window.history.replaceState({}, '', window.location.pathname); }
            
            UI.toast('Account created', 'success');
            this.onLoginSuccess();
        } catch (err) {
            UI.toast(err.message, 'error');
        } finally {
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        }
    },

    async logout() {
        State.token = ''; State.userToken = ''; State.account = null;
        localStorage.removeItem('photon-account');
        localStorage.removeItem('photon-user-token');
        UI.updateAuthVisbility();
        UI.navigate('overview');
        UI.toast('Logged out');
    },

    onLoginSuccess() {
        UI.closeModal(null, true);
        if (State.account?.administrator) this.loadAdminConfig();
        UI.updateAuthVisbility();
        if (State.account?.subscriber) UI.navigate('licenses');
        else UI.navigate('user');
    },

    async updateProfile(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            const payload = {
                uuid: State.account.uuid,
                currentPassword: fd.get('currentPassword'),
                username: fd.get('username'),
                email: fd.get('email')
            };
            if (fd.get('newPassword')) {
                payload.newPassword = fd.get('newPassword');
                payload.confirmPassword = fd.get('newPassword'); 
            }

            const acc = await Api('/accounts/update_profile', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            
            State.account = acc;
            localStorage.setItem('photon-account', JSON.stringify(acc));
            e.target.reset();
            UI.updateAuthVisbility();
            UI.closeModal(null, true);
            UI.toast('Profile updated', 'success');
        } catch (err) { UI.toast(err.message, 'error'); }
    },

    // --- Public ---
    async loadPublicServers() {
        try {
            const servers = await Api('/api/status/servers');
            const grid = document.getElementById('serverGrid');
            document.getElementById('serverCount').innerHTML = `${servers.length} <i class="fa-solid fa-globe"></i>`;
            
            if (!servers.length) { grid.innerHTML = '<p class="text-secondary" style="grid-column: 1/-1;">No servers online.</p>'; return; }

            grid.innerHTML = servers.map(s => `
                <div class="card">
                    <div class="card-header">
                        <span style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${UI.escapeHTML(s.serverName || 'Unknown')}">${UI.escapeHTML(s.serverName || 'Unknown Server')}</span>
                    </div>
                    <div class="card-body">
                        <p style="margin-bottom:0">${UI.escapeHTML(s.serverMOTD || 'No MOTD provided')}</p>
                    </div>
                    <div class="card-footer">
                        <button class="btn icon-btn" title="Copy IP" onclick="UI.copy('${s.serverIP}:${s.serverPort}')"><i class="fa-regular fa-copy"></i></button>
                        ${s.site ? `<a href="${UI.escapeHTML(s.site)}" target="_blank" class="btn icon-btn"><i class="fa-solid fa-link"></i></a>` : ''}
                        ${s.discord ? `<a href="${UI.escapeHTML(s.discord)}" target="_blank" class="btn icon-btn"><i class="fa-brands fa-discord"></i></a>` : ''}
                    </div>
                </div>
            `).join('');
        } catch (e) {
            document.getElementById('serverGrid').innerHTML = `<p class="text-secondary text-danger" style="grid-column: 1/-1;">Failed to load servers.</p>`;
        }
    },

    downloadMod(e) {
        e.preventDefault();
        const chan = document.getElementById('downloadChannel').value;
        window.location.href = `/download/mod?channel=${encodeURIComponent(chan)}`;
    },

    // --- Subscriptions ---
    async loadLicenses() {
        try {
            const licenses = await Api('/accounts/licenses');
            const tbody = document.getElementById('licensesTableBody');
            
            if (!licenses || !licenses.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-secondary text-center">No licenses found.</td></tr>';
                return;
            }

            tbody.innerHTML = licenses.map(l => {
                const status = String(l.status || l.state || 'UNKNOWN').toUpperCase();
                const isRevoked = status === 'REVOKED';
                const badgeClass = status === 'ACTIVE' ? 'active' : (isRevoked ? 'danger' : '');
                
                return `
                <tr>
                    <td>
                        <strong>${UI.escapeHTML(l.name || l.customerName || '—')}</strong><br>
                        <span class="text-secondary text-sm">${UI.escapeHTML(l.productId || l.product_id || '—')}</span>
                    </td>
                    <td>
                        <span class="text-sm">
                            <i class="fa-solid fa-arrow-right-to-bracket text-success" style="margin-right:4px;"></i> ${formatDate(l.createdAt || l.created_at).split(',')[0]}<br>
                            <i class="fa-solid fa-arrow-right-from-bracket text-danger" style="margin-right:4px;"></i> ${formatDate(l.expiresAt || l.expires_at).split(',')[0]}
                        </span>
                    </td>
                    <td><code class="font-mono text-sm" style="background: var(--surface-glass); padding: 0.3rem 0.5rem; border-radius: 6px; border: 1px solid var(--border-color);">${UI.escapeHTML(l.licenseKey || l.key || '—')}</code></td>
                    <td><span class="badge ${badgeClass}">${UI.escapeHTML(status)}</span></td>
                    <td>
                        <div style="display: flex; gap: 0.5rem;">
                            ${!isRevoked ? `
                                <button class="icon-btn" onclick="UI.copy('${l.licenseKey || l.key}')" title="Copy Key"><i class="fa-regular fa-copy"></i></button>
                                <button class="icon-btn" style="color:var(--danger-color)" onclick="App.revokeLicense('${l.licenseKey || l.key}')" title="Revoke"><i class="fa-solid fa-trash"></i></button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `}).join('');
        } catch (e) { UI.toast('Failed to load licenses', 'error'); }
    },

    async createLicense(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        const payload = { name: fd.get('name') };
        if (fd.get('duration_days')) payload.duration_days = Number(fd.get('duration_days'));

        try {
            await Api('/accounts/licenses', { method: 'POST', body: JSON.stringify(payload) });
            UI.closeModal(null, true);
            e.target.reset();
            UI.toast('License created', 'success');
            this.loadLicenses();
        } catch (err) { UI.toast(err.message, 'error'); }
    },

    async revokeLicense(key) {
        if(!confirm('Revoke this license?')) return;
        try {
            await Api('/accounts/licenses/revoke', { method: 'POST', body: JSON.stringify({ license_key: key }) });
            UI.toast('License revoked', 'success');
            this.loadLicenses();
        } catch (err) { UI.toast(err.message, 'error'); }
    },

    // --- Admin & Config ---
    async loadAdminConfig() {
        try {
            State.config = await Api('/api/admin/config');
            const form = document.getElementById('configForm');
            
            form.innerHTML = State.configSchema.map(f => `
                <div class="form-group">
                    <label>${UI.escapeHTML(f.label)}</label>
                    <input type="${f.type}" name="${f.key}" value="${UI.escapeHTML(State.config[f.key] || '')}">
                </div>
            `).join('');

            // Update footer links if available
            if (State.config.store_url) document.getElementById('footerStoreLink').href = State.config.store_url;
            if (State.config.terms_of_service_url) document.getElementById('footerTosLink').href = State.config.terms_of_service_url;
            if (State.config.terms_of_sale_url) document.getElementById('footerTosaleLink').href = State.config.terms_of_sale_url;
            if (State.config.privacy_policy_url) document.getElementById('footerPrivacyLink').href = State.config.privacy_policy_url;

            UI.updateAuthVisbility(); // Re-trigger UI update for overview store button if config changed
        } catch (e) { /* user lacks permission, silently ignore */ }
    },

    async saveConfig(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        const payload = {};
        State.configSchema.forEach(f => {
            const val = fd.get(f.key);
            payload[f.key] = f.type === 'number' ? (val ? Number(val) : null) : val;
        });
        try {
            State.config = await Api('/api/admin/config', { method: 'PUT', body: JSON.stringify(payload) });
            UI.toast('Config saved', 'success');
            this.loadAdminConfig(); // Refresh UI mappings
        } catch (err) { UI.toast(err.message, 'error'); }
    },

    async loadTablesList() {
        try {
            const tables = await Api('/api/admin/tables');
            const sel = document.getElementById('tableSelector');
            
            if (!tables || !tables.length) {
                sel.innerHTML = '<option value="">No tables available</option>';
                document.getElementById('dataTableBody').innerHTML = '<tr><td class="text-secondary">No tables found on server.</td></tr>';
                return;
            }

            sel.innerHTML = tables.map(t => `<option value="${t.table}">${t.label}</option>`).join('');
            this.loadTableData();
        } catch(e) {
            const sel = document.getElementById('tableSelector');
            sel.innerHTML = '<option value="">Error loading tables</option>';
            UI.toast('Failed to load tables list (API Error)', 'error');
        }
    },

    async loadTableData() {
        const table = document.getElementById('tableSelector').value;
        const limit = document.getElementById('tableLimit').value;
        if(!table) return;
        
        try {
            const data = await Api(`/api/admin/tables/${encodeURIComponent(table)}?limit=${limit}`);
            const head = document.getElementById('dataTableHead');
            const body = document.getElementById('dataTableBody');
            
            if(!data.columns || !data.rows.length) {
                head.innerHTML = '<tr><th>Notice</th></tr>';
                body.innerHTML = '<tr><td class="text-secondary">No rows found in this table.</td></tr>';
                return;
            }

            head.innerHTML = `<tr>${data.columns.map(c => `<th>${UI.escapeHTML(c)}</th>`).join('')}</tr>`;
            body.innerHTML = data.rows.map(row => 
                `<tr>${data.columns.map(c => `<td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis;" title="${UI.escapeHTML(formatVal(row[c]))}">${UI.escapeHTML(formatVal(row[c]))}</td>`).join('')}</tr>`
            ).join('');
        } catch (e) { 
            UI.toast('Failed to load table data', 'error');
            document.getElementById('dataTableBody').innerHTML = '<tr><td class="text-danger">Failed to fetch data.</td></tr>';
        }
    },

    async loadTableData() {
        const table = document.getElementById('tableSelector').value;
        const limit = document.getElementById('tableLimit').value;
        if(!table) return;
        
        try {
            const data = await Api(`/api/admin/tables/${encodeURIComponent(table)}?limit=${limit}`);
            const head = document.getElementById('dataTableHead');
            const body = document.getElementById('dataTableBody');
            
            if(!data.columns || !data.rows.length) {
                body.innerHTML = '<tr><td class="text-secondary">No rows found.</td></tr>';
                return;
            }

            head.innerHTML = `<tr>${data.columns.map(c => `<th>${UI.escapeHTML(c)}</th>`).join('')}</tr>`;
            body.innerHTML = data.rows.map(row => 
                `<tr>${data.columns.map(c => `<td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis;" title="${UI.escapeHTML(row[c])}">${UI.escapeHTML(formatVal(row[c]))}</td>`).join('')}</tr>`
            ).join('');
        } catch (e) { UI.toast('Failed to load table data', 'error'); }
    },

    async restartService() {
        if(!confirm('Restart Photon Network now?')) return;
        try {
            await Api('/api/admin/restart', { method: 'POST' });
            UI.toast('Restart requested', 'success');
        } catch(e) { UI.toast(e.message, 'error'); }
    },

    async uploadUpdate(e) {
        e.preventDefault();
        try {
            await Api('/api/admin/updates/upload', { method: 'POST', body: new FormData(e.target) });
            UI.toast('Update uploaded successfully', 'success');
            e.target.reset();
        } catch(err) { UI.toast(err.message, 'error'); }
    }
};

// Boot
document.addEventListener('DOMContentLoaded', () => App.init());