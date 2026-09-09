function createModal(id, formContent) {
    return `
        <div class="modal-backdrop" id="${id}" onclick="UI.closeModal(event)">
            <div class="modal">
                <button class="modal-close icon-btn" onclick="UI.closeModal(null, true)"><i class="fa-solid fa-xmark"></i></button>
                ${formContent}
            </div>
        </div>
    `;
}

class IncAuthModal extends HTMLElement {
    connectedCallback() {
        this.innerHTML = createModal('authModal', `
            <div class="modal-tabs">
                <div class="modal-tab active" onclick="UI.switchAuthTab('login')">Login</div>
                <div class="modal-tab" onclick="UI.switchAuthTab('register')">Register</div>
            </div>
            
            <!-- Login Form -->
            <form id="loginForm" class="modal-panel active" onsubmit="App.login(event)">
                <div style="text-align: center; margin-bottom: 1.5rem;">
                    <h2><i class="fa-solid fa-fingerprint text-accent"></i> Authentication</h2>
                    <p class="text-sm text-secondary">Sign in to your account</p>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <div class="input-wrapper">
                        <i class="fa-regular fa-envelope left-icon"></i>
                        <input type="email" name="email" required placeholder="joe@gmail.com">
                    </div>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <div class="input-wrapper">
                        <i class="fa-solid fa-lock left-icon"></i>
                        <input type="password" name="password" required placeholder="••••••••">
                    </div>
                </div>
                <button type="submit" class="btn primary" style="width: 100%; margin-top: 1rem;">Login</button>
            </form>

            <!-- Register Form -->
            <form id="registerForm" class="modal-panel" onsubmit="App.register(event)">
                <div style="text-align: center; margin-bottom: 1.5rem;">
                    <h2><i class="fa-solid fa-user-plus text-accent"></i> Create Account</h2>
                    <p class="text-sm text-secondary" id="registerModalSubtitle">Join the network</p>
                </div>
                <div class="form-group">
                    <label>Username</label>
                    <div class="input-wrapper">
                        <i class="fa-regular fa-user left-icon"></i>
                        <input type="text" name="username" required placeholder="JoeDalton_">
                    </div>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <div class="input-wrapper">
                        <i class="fa-regular fa-envelope left-icon"></i>
                        <input type="email" name="email" required placeholder="joe@gmail.com">
                    </div>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <div class="input-wrapper">
                        <i class="fa-solid fa-lock left-icon"></i>
                        <input type="password" name="password" required minlength="8" placeholder="••••••••">
                    </div>
                </div>
                <button type="submit" class="btn primary" style="width: 100%; margin-top: 1rem;">Register</button>
            </form>
        `);
    }
}
customElements.define('inc-auth-modal', IncAuthModal);

class IncCreateLicenseModal extends HTMLElement {
    connectedCallback() {
        this.innerHTML = createModal('createLicenseModal', `
            <form onsubmit="App.createLicense(event)">
                <h2 style="margin-bottom: 1.5rem;"><i class="fa-solid fa-key text-accent"></i> New License</h2>
                <div class="form-group">
                    <label>License Name / Identifier</label>
                    <input type="text" name="name" required placeholder="e.g. Production Server">
                </div>
                <div class="form-group">
                    <label>Product</label>
                    <select name="product_id" id="licenseProductSelect" required>
                        <option value="">Loading products...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Duration (Days)</label>
                    <input type="number" name="duration_days" placeholder="30" min="1">
                    <span class="text-sm text-secondary">Leave blank for default.</span>
                </div>
                <button type="submit" class="btn primary" style="width: 100%; margin-top: 1rem;">Generate Key</button>
            </form>
        `);
    }
}
customElements.define('inc-create-license-modal', IncCreateLicenseModal); 

class IncEditProfileModal extends HTMLElement {
    connectedCallback() {
        this.innerHTML = createModal('editProfileModal', `
            <form onsubmit="App.updateProfile(event)">
                <h2 style="margin-bottom: 1.5rem;"><i class="fa-solid fa-user-pen text-accent"></i> Edit Details</h2>
                <div class="grid">
                    <div class="grid grid-cols-2">
                        <div class="form-group">
                            <label>Username</label>
                            <input type="text" name="username" id="editUsername" required>
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" name="email" id="editEmail" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>New Password <span class="text-secondary">(Optional)</span></label>
                        <input type="password" name="newPassword" minlength="8" placeholder="Leave blank to keep current">
                    </div>
                    <div class="form-group">
                        <label>Current Password <span class="text-danger">*</span></label>
                        <input type="password" name="currentPassword" required placeholder="Required to save changes">
                    </div>
                    <button type="submit" class="btn primary" style="width: 100%; margin-top: 1rem;">Save Changes</button>
                </div>
            </form>
        `);
    }
}
customElements.define('inc-edit-profile-modal', IncEditProfileModal); 