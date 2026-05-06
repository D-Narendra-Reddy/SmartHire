const API_BASE_URL = 'http://localhost:8080/api';

// --- Auth Utils ---
function setToken(token) {
    localStorage.setItem('jwtToken', token);
}

function getToken() {
    return localStorage.getItem('jwtToken');
}

function setUser(user) {
    localStorage.setItem('user', JSON.stringify(user));
}

function getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

function logout() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

function isAuthenticated() {
    return !!getToken();
}

function hasRole(roleName) {
    const user = getUser();
    if (!user || !user.roles) return false;
    return user.roles.includes(roleName);
}

// --- Fetch Wrapper ---
async function apiCall(endpoint, method = 'GET', body = null, isMultipart = false) {
    const headers = {};
    const token = getToken();
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (!isMultipart && body) {
        headers['Content-Type'] = 'application/json';
    }

    const options = {
        method,
        headers
    };

    if (body) {
        options.body = isMultipart ? body : JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        const data = await response.json();
        
        if (!response.ok) {
            // Handle specific errors like 401 Unauthorized
            if (response.status === 401) {
                logout();
            }
            throw new Error(data.message || 'Something went wrong');
        }
        
        return data;
    } catch (error) {
        throw error;
    }
}

// --- Formatting Utils ---
function formatEmploymentType(type) {
    if (!type) return '';
    return type.replace('_', ' ').replace(/\w\S*/g, (w) => (w.replace(/^\w/, (c) => c.toUpperCase())));
}

function formatExperience(exp) {
    if (!exp) return '';
    if (!isNaN(exp)) return `${exp} Year${exp > 1 ? 's' : ''} Exp`;
    return String(exp).toLowerCase().includes('exp') || String(exp).toLowerCase().includes('year') ? exp : `${exp} Exp`;
}

function formatSalary(salary) {
    if (!salary) return '';
    if (!isNaN(salary)) return `₹${Number(salary).toLocaleString()}`;
    return String(salary).includes('$') || String(salary).includes('₹') ? salary : `₹${salary}`;
}

// --- Resume Viewer ---
function viewResume(fileUrl) {
    if (!fileUrl) return;
    // Use our backend proxy which re-serves the PDF with Content-Disposition: inline
    const previewUrl = `${API_BASE_URL}/files/preview?url=${encodeURIComponent(fileUrl)}`;
    window.open(previewUrl, '_blank');
}

// --- UI Utils ---
function showMessage(elementId, message, isError = false) {
    const el = document.getElementById(elementId);
    if (!el) return;
    
    el.textContent = message;
    el.className = `alert ${isError ? 'alert-error' : 'alert-success'}`;
    el.classList.remove('hidden');
    
    setTimeout(() => {
        el.classList.add('hidden');
    }, 5000);
}

function renderNavbar() {
    const authLinks = document.getElementById('auth-links');
    if (!authLinks) return;

    if (isAuthenticated()) {
        const user = getUser();
        let dashboardLink = '';
        
        if (hasRole('ROLE_CANDIDATE')) dashboardLink = 'candidate-dashboard.html';
        else if (hasRole('ROLE_RECRUITER')) dashboardLink = 'recruiter-dashboard.html';
        else if (hasRole('ROLE_ADMIN')) dashboardLink = 'admin-dashboard.html';

        authLinks.innerHTML = `
            <a href="jobs.html">Find Jobs</a>
            <a href="${dashboardLink}">Dashboard</a>
            <span style="color: var(--text-muted)">Hi, ${user.name}</span>
            <button onclick="logout()" class="btn btn-outline">Logout</button>
        `;
    } else {
        authLinks.innerHTML = `
            <a href="jobs.html">Find Jobs</a>
            <a href="login.html" class="btn btn-outline">Login</a>
            <a href="register.html" class="btn btn-primary">Sign Up</a>
        `;
    }
}

// Run on page load
document.addEventListener('DOMContentLoaded', () => {
    renderNavbar();
});
