const API_BASE = '/api';
let authToken = localStorage.getItem('token') || '';
let currentUser = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadOpenRequests();
    loadBloodBanks();
    if (authToken) {
        restoreSession();
    } else {
        showView('home');
    }
});

// Toast notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('alert-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span> ${message}`;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Show/Hide views
function showView(viewName) {
    document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
    document.querySelectorAll('nav a').forEach(link => link.classList.remove('active'));

    const activeView = document.getElementById(`${viewName}-view`);
    if (activeView) activeView.classList.add('active');

    const activeLink = document.getElementById(`nav-${viewName}`);
    if (activeLink) activeLink.classList.add('active');

    // Load dynamic data on view change
    if (viewName === 'home') {
        loadOpenRequests();
        loadBloodBanks();
    } else if (viewName === 'dashboard' && currentUser) {
        renderDashboard();
    } else if (viewName === 'auth') {
        switchLoginTab('user');
    }
}

function scrollToSection(id) {
    document.getElementById(id).scrollIntoView({ behavior: 'smooth' });
}

// Toggle blood group field in registration depending on role chosen
function toggleBloodGroupField() {
    const role = document.getElementById('reg-role').value;
    const bloodGroupDiv = document.getElementById('reg-bloodgroup-group');
    if (role === 'RECIPIENT' || role === 'DONOR') {
        bloodGroupDiv.style.display = 'block';
    } else {
        bloodGroupDiv.style.display = 'none'; // Admin
    }
}

// Session restore
async function restoreSession() {
    try {
        const response = await fetch(`${API_BASE}/users/me`, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        if (response.ok) {
            const result = await response.json();
            currentUser = result.data;
            updateNavState(true);
            showView('dashboard');
        } else {
            logout();
        }
    } catch (err) {
        console.error("Session restore failed", err);
        logout();
    }
}

// Navigation visibility update
function updateNavState(isLoggedIn) {
    const authLink = document.getElementById('nav-auth');
    const dashLink = document.getElementById('nav-dashboard');
    const logoutBtn = document.getElementById('nav-logout-btn');

    if (isLoggedIn && currentUser) {
        authLink.style.display = 'none';
        dashLink.style.display = 'block';
        logoutBtn.style.display = 'block';
    } else {
        authLink.style.display = 'block';
        dashLink.style.display = 'none';
        logoutBtn.style.display = 'none';
    }
}

// Toggle between User and Admin login tabs
function switchLoginTab(tab) {
    const userTab = document.getElementById('tab-user-login');
    const adminTab = document.getElementById('tab-admin-login');
    const userSection = document.getElementById('user-login-section');
    const adminSection = document.getElementById('admin-login-section');

    if (tab === 'user') {
        userTab.classList.add('active');
        adminTab.classList.remove('active');
        userSection.style.display = 'block';
        adminSection.style.display = 'none';
    } else {
        adminTab.classList.add('active');
        userTab.classList.remove('active');
        adminSection.style.display = 'block';
        userSection.style.display = 'none';
    }
}

// Auth handlers
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const result = await response.json();

        if (response.ok) {
            authToken = result.data.token;
            currentUser = result.data.user;
            localStorage.setItem('token', authToken);
            showToast("Login successful!", "success");
            updateNavState(true);
            showView('dashboard');
        } else {
            showToast(result.message || "Invalid credentials", "error");
        }
    } catch (err) {
        showToast("Server connection error", "error");
    }
}

async function handleAdminLogin(e) {
    e.preventDefault();
    const email = document.getElementById('admin-login-email').value;
    const password = document.getElementById('admin-login-password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/admin/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const result = await response.json();

        if (response.ok) {
            authToken = result.data.token;
            currentUser = result.data.user;
            localStorage.setItem('token', authToken);
            showToast("Admin login successful!", "success");
            updateNavState(true);
            showView('dashboard');
        } else {
            showToast(result.message || "Invalid credentials", "error");
        }
    } catch (err) {
        showToast("Server connection error", "error");
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const phone = document.getElementById('reg-phone').value;
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;
    const bloodGroup = role !== 'ADMIN' ? document.getElementById('reg-bloodgroup').value : null;
    const city = document.getElementById('reg-city').value;
    const state = document.getElementById('reg-state').value;

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, phone, password, role, bloodGroup, city, state })
        });
        const result = await response.json();

        if (response.ok) {
            authToken = result.data.token;
            currentUser = result.data.user;
            localStorage.setItem('token', authToken);
            showToast("Registration successful!", "success");
            updateNavState(true);
            showView('dashboard');
        } else {
            showToast(result.message || "Registration failed", "error");
        }
    } catch (err) {
        showToast("Server connection error", "error");
    }
}

function logout() {
    authToken = '';
    currentUser = null;
    localStorage.removeItem('token');
    updateNavState(false);
    showToast("Logged out successfully");
    showView('home');
}

// Public donor search
async function searchDonors(e) {
    e.preventDefault();
    const city = document.getElementById('search-city').value;
    const bloodGroup = document.getElementById('search-blood-group').value;
    
    let url = `${API_BASE}/donors/search?city=${encodeURIComponent(city)}`;
    if (bloodGroup) url += `&bloodGroup=${bloodGroup}`;

    try {
        const response = await fetch(url);
        const result = await response.json();

        if (response.ok) {
            const tableBody = document.getElementById('donor-search-table-body');
            tableBody.innerHTML = '';
            
            const resultsDiv = document.getElementById('donor-search-results');
            resultsDiv.style.display = 'block';

            if (result.data.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" class="text-center">No active donors found in this city.</td></tr>';
                return;
            }

            result.data.forEach(donor => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${donor.name}</strong></td>
                    <td><span style="color:var(--primary); font-weight:600;">${donor.bloodGroup.replace('_', ' ')}</span></td>
                    <td>${donor.city}, ${donor.state}</td>
                    <td><span class="badge" style="background:rgba(16,185,129,0.15); color:var(--success);">Available</span></td>
                `;
                tableBody.appendChild(tr);
            });
        }
    } catch (err) {
        showToast("Error searching donors", "error");
    }
}

// Load public open requests
async function loadOpenRequests() {
    try {
        const response = await fetch(`${API_BASE}/requests`);
        const result = await response.json();
        
        const tableBody = document.getElementById('open-requests-table-body');
        tableBody.innerHTML = '';

        if (result.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No open blood requests at the moment.</td></tr>';
            return;
        }

        result.data.forEach(req => {
            const isDonor = currentUser && currentUser.role === 'DONOR';
            const actionBtn = isDonor 
                ? `<button class="btn btn-success" style="padding: 6px 12px; font-size:13px;" onclick="fulfillRequest(${req.id})">Fulfill</button>`
                : `<span class="badge badge-open">Open</span>`;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><span style="color:var(--primary); font-weight:bold;">${req.requiredBlood.replace('_', ' ')}</span></td>
                <td><strong>${req.hospitalName}</strong><br/><small style="color:var(--text-muted);">${req.city}</small></td>
                <td><span class="badge badge-${req.urgencyLevel.toLowerCase()}">${req.urgencyLevel}</span></td>
                <td>${req.contactNumber}</td>
                <td>${actionBtn}</td>
            `;
            tableBody.appendChild(tr);
        });
    } catch (err) {
        console.error("Failed to load open requests", err);
    }
}

// Load public blood banks
async function loadBloodBanks() {
    const city = document.getElementById('bank-city-filter').value;
    let url = `${API_BASE}/banks`;
    if (city) url += `?city=${encodeURIComponent(city)}`;

    try {
        const response = await fetch(url);
        const result = await response.json();
        const container = document.getElementById('blood-banks-list');
        container.innerHTML = '';

        if (result.data.length === 0) {
            container.innerHTML = '<p style="color:var(--text-muted); text-align:center; padding: 20px 0;">No blood banks found.</p>';
            return;
        }

        // populate select dropdown in admin dashboard as well
        const adminSelect = document.getElementById('admin-bank-select');
        if (adminSelect) {
            // Keep default option
            adminSelect.innerHTML = '<option value="">Choose a Blood Bank...</option>';
            result.data.forEach(bank => {
                const opt = document.createElement('option');
                opt.value = bank.id;
                opt.textContent = `${bank.name} (${bank.city})`;
                adminSelect.appendChild(opt);
            });
        }

        result.data.forEach(bank => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                    <div>
                        <h4 style="margin-bottom: 5px;">${bank.name}</h4>
                        <p style="font-size:13px; color:var(--text-muted); margin-bottom: 3px;">📍 ${bank.address}, ${bank.city}</p>
                        <p style="font-size:13px; color:var(--text-muted);">📞 Phone: ${bank.phone || 'N/A'}</p>
                    </div>
                    <button class="btn btn-secondary" style="padding:6px 12px; font-size:12px;" onclick="viewBankInventory(${bank.id}, '${bank.name}')">Stock</button>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        console.error("Failed to load blood banks", err);
    }
}

// View blood bank inventory in modal
async function viewBankInventory(bankId, bankName) {
    try {
        const response = await fetch(`${API_BASE}/banks/${bankId}/inventory`);
        const result = await response.json();
        
        document.getElementById('view-inventory-bank-name').textContent = `${bankName} Stock`;
        const tbody = document.getElementById('view-inventory-table-body');
        tbody.innerHTML = '';

        if (!result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No stock levels recorded for this bank.</td></tr>';
        } else {
            result.data.forEach(item => {
                const date = new Date(item.lastUpdated).toLocaleDateString();
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${item.bloodGroup.replace('_', ' ')}</strong></td>
                    <td><span class="badge" style="background:rgba(255,255,255,0.05); color:white; font-size:14px;">${item.unitsAvailable} Units</span></td>
                    <td>${date}</td>
                `;
                tbody.appendChild(tr);
            });
        }
        document.getElementById('view-inventory-modal').style.display = 'flex';
    } catch (err) {
        showToast("Failed to fetch inventory", "error");
    }
}

function closeViewInventoryModal() {
    document.getElementById('view-inventory-modal').style.display = 'none';
}

// Render dynamic panels in Dashboard based on user role
function renderDashboard() {
    document.getElementById('welcome-name').textContent = `Welcome Back, ${currentUser.name}!`;
    document.getElementById('welcome-role').textContent = currentUser.role;
    document.getElementById('welcome-email-text').textContent = currentUser.email;

    const recipientPanel = document.getElementById('recipient-panel');
    const donorPanel = document.getElementById('donor-panel');
    const adminPanel = document.getElementById('admin-panel');
    const availContainer = document.getElementById('donor-availability-toggle-container');

    recipientPanel.style.display = 'none';
    donorPanel.style.display = 'none';
    adminPanel.style.display = 'none';
    availContainer.style.display = 'none';

    if (currentUser.role === 'RECIPIENT') {
        recipientPanel.style.display = 'block';
        loadMyRequests();
    } else if (currentUser.role === 'DONOR') {
        donorPanel.style.display = 'block';
        availContainer.style.display = 'block';
        
        // set availability btn style
        const btn = document.getElementById('availability-toggle-btn');
        btn.textContent = `Status: ${currentUser.available ? 'Available to Donate' : 'Unavailable'}`;
        btn.className = currentUser.available ? 'btn btn-success' : 'btn btn-secondary';

        loadMyDonations();
    } else if (currentUser.role === 'ADMIN') {
        adminPanel.style.display = 'block';
    }
}

// Toggle availability status for donor
async function toggleAvailability() {
    try {
        const response = await fetch(`${API_BASE}/users/me/availability`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();

        if (response.ok) {
            currentUser.available = result.data.available;
            showToast("Availability status updated!", "success");
            renderDashboard();
        } else {
            showToast(result.message || "Failed to update status", "error");
        }
    } catch (err) {
        showToast("Error updating status", "error");
    }
}

// Recipient requests fetcher
async function loadMyRequests() {
    try {
        const response = await fetch(`${API_BASE}/requests/my`, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();
        const tbody = document.getElementById('my-requests-table-body');
        tbody.innerHTML = '';

        if (!result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">You haven\'t posted any blood requests yet.</td></tr>';
            return;
        }

        result.data.forEach(req => {
            const statusClass = req.status.toLowerCase();
            const actionBtn = req.status === 'OPEN' 
                ? `<button class="btn btn-secondary" style="padding:4px 8px; font-size:12px; background:var(--critical); color:white;" onclick="cancelRequest(${req.id})">Cancel</button>`
                : 'N/A';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><span style="color:var(--primary); font-weight:bold;">${req.requiredBlood.replace('_', ' ')}</span></td>
                <td><strong>${req.hospitalName}</strong><br/><small style="color:var(--text-muted);">${req.city}</small></td>
                <td><span class="badge badge-${req.urgencyLevel.toLowerCase()}">${req.urgencyLevel}</span></td>
                <td><span class="badge badge-${statusClass}">${req.status}</span></td>
                <td>${actionBtn}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Failed to load requests", err);
    }
}

// Donor history fetcher
async function loadMyDonations() {
    try {
        const response = await fetch(`${API_BASE}/donors/history`, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();
        const tbody = document.getElementById('my-donations-table-body');
        tbody.innerHTML = '';

        if (!result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No donation records found.</td></tr>';
            return;
        }

        result.data.forEach(d => {
            const date = new Date(d.donatedAt).toLocaleDateString();
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${date}</td>
                <td><strong>${d.request.hospitalName}</strong><br/><small style="color:var(--text-muted);">${d.request.city}</small></td>
                <td><span style="color:var(--primary); font-weight:bold;">${d.request.requiredBlood.replace('_', ' ')}</span></td>
                <td><span class="badge" style="background:rgba(16,185,129,0.15); color:var(--success);">${d.status}</span></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Failed to load history", err);
    }
}

// Fulfill Request
async function fulfillRequest(requestId) {
    if (!currentUser || currentUser.role !== 'DONOR') {
        showToast("Please log in as a Donor to fulfill requests", "error");
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/requests/${requestId}/fulfill?donorId=${currentUser.id}`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();

        if (response.ok) {
            showToast("Thank you! Blood request marked as Fulfilled.", "success");
            loadOpenRequests();
            if (currentUser.role === 'DONOR') loadMyDonations();
        } else {
            showToast(result.message || "Failed to fulfill request", "error");
        }
    } catch (err) {
        showToast("Error updating request", "error");
    }
}

// Cancel Request
async function cancelRequest(requestId) {
    if (!confirm("Are you sure you want to cancel this request?")) return;

    try {
        const response = await fetch(`${API_BASE}/requests/${requestId}/cancel`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();

        if (response.ok) {
            showToast("Request cancelled.", "success");
            if (currentUser.role === 'RECIPIENT') loadMyRequests();
            loadOpenRequests();
        } else {
            showToast(result.message || "Failed to cancel request", "error");
        }
    } catch (err) {
        showToast("Error cancelling request", "error");
    }
}

// Post Request
async function submitBloodRequest(e) {
    e.preventDefault();
    const requiredBlood = document.getElementById('req-blood').value;
    const hospitalName = document.getElementById('req-hospital').value;
    const city = document.getElementById('req-city-input').value;
    const contactNumber = document.getElementById('req-contact').value;
    const urgencyLevel = document.getElementById('req-urgency').value;

    try {
        const response = await fetch(`${API_BASE}/requests`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ requiredBlood, hospitalName, city, contactNumber, urgencyLevel })
        });
        const result = await response.json();

        if (response.ok) {
            showToast("Blood request posted successfully!", "success");
            closeRequestModal();
            loadMyRequests();
            loadOpenRequests();
        } else {
            showToast(result.message || "Failed to post request", "error");
        }
    } catch (err) {
        showToast("Error connecting to server", "error");
    }
}

// Modal Toggle Helpers
function openRequestModal() { document.getElementById('request-modal').style.display = 'flex'; }
function closeRequestModal() { document.getElementById('request-modal').style.display = 'none'; }

// Admin Inventory management loader
async function loadAdminBankInventory() {
    const bankId = document.getElementById('admin-bank-select').value;
    const section = document.getElementById('admin-inventory-section');
    if (!bankId) {
        section.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/banks/${bankId}/inventory`);
        const result = await response.json();
        
        section.style.display = 'block';
        const tbody = document.getElementById('admin-inventory-table-body');
        tbody.innerHTML = '';

        if (!result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No stock levels recorded for this bank.</td></tr>';
        } else {
            result.data.forEach(item => {
                const date = new Date(item.lastUpdated).toLocaleString();
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${item.bloodGroup.replace('_', ' ')}</strong></td>
                    <td><span class="badge" style="background:rgba(255,255,255,0.05); color:white; font-size:14px;">${item.unitsAvailable} Units</span></td>
                    <td>${date}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (err) {
        showToast("Failed to fetch inventory data", "error");
    }
}

function openInventoryUpdateModal() {
    document.getElementById('inventory-modal').style.display = 'flex';
}

function closeInventoryUpdateModal() {
    document.getElementById('inventory-modal').style.display = 'none';
}

// Admin Update Inventory submitter
async function submitInventoryUpdate(e) {
    e.preventDefault();
    const bankId = document.getElementById('admin-bank-select').value;
    const bloodGroup = document.getElementById('inv-blood').value;
    const units = document.getElementById('inv-units').value;

    try {
        const response = await fetch(`${API_BASE}/banks/${bankId}/inventory?bloodGroup=${bloodGroup}&units=${units}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        const result = await response.json();

        if (response.ok) {
            showToast("Inventory stock updated successfully!", "success");
            closeInventoryUpdateModal();
            loadAdminBankInventory();
        } else {
            showToast(result.message || "Failed to update inventory", "error");
        }
    } catch (err) {
        showToast("Error updating stock", "error");
    }
}
