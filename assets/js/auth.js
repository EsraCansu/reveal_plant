/**
 * Cookie Utility Functions
 * Secure cookie management for authentication
 */

/**
 * Get cookie by name
 */
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        const cookieValue = parts.pop().split(';').shift();
        // URL decode cookie value (backend URL encode yapıyor)
        // + karakterlerini boşluğa çevir (URL encoding)
        try {
            const decodedValue = decodeURIComponent(cookieValue.replace(/\+/g, ' '));
            return decodedValue;
        } catch (e) {
            return cookieValue.replace(/\+/g, ' ');
        }
    }
    return null;
}

/**
 * Set cookie
 */
function setCookie(name, value, days = 1) {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
}

/**
 * Delete cookie
 */
function deleteCookie(name) {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/`;
}

/**
 * Check if user is logged in (via cookie)
 */
function isLoggedIn() {
    return getCookie('userEmail') !== null;
}

/**
 * Get current user info from cookie
 */
function getCurrentUser() {
    return {
        email: getCookie('userEmail'),
        name: getCookie('userName'),
        role: getCookie('userRole')
    };
}

/**
 * Logout user (clear cookies and redirect)
 */
async function logout() {
    try {
        // Backend'e logout isteği gönder (cookie'leri sunucu tarafında temizlemek için)
        await fetch('http://localhost:8080/api/users/auth/logout', {
            method: 'POST',
            credentials: 'include' // Cookie'leri gönder
        });
    } catch (error) {
        console.error('Logout error:', error);
    }
    
    // Client-side cookie'leri temizle
    deleteCookie('userEmail');
    deleteCookie('userName');
    deleteCookie('userRole');
    deleteCookie('jwt_token');
    deleteCookie('remember_me');
    
    // localStorage'ı da temizle (eski veri varsa)
    localStorage.removeItem('user_logged_in');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_remember');
    
    // Login sayfasına yönlendir
    window.location.href = '/app/views/login.html';
}

/**
 * XSS Protection: Sanitize HTML
 */
function sanitizeHTML(str) {
    const temp = document.createElement('div');
    temp.textContent = str;
    return temp.innerHTML;
}

/**
 * XSS Protection: Escape HTML entities
 */
function escapeHTML(str) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        '/': '&#x2F;'
    };
    return str.replace(/[&<>"'/]/g, (char) => map[char]);
}

/**
 * Validate email format
 */
function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

/**
 * Check authentication before page load
 */
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = '/app/views/login.html';
        return false;
    }
    return true;
}

/**
 * Check admin role
 */
function requireAdmin() {
    const user = getCurrentUser();
    if (!isLoggedIn() || user.role?.toLowerCase() !== 'admin') {
        window.location.href = '/app/views/login.html';
        return false;
    }
    return true;
}

/**
 * Make authenticated API request with credentials
 */
async function authenticatedFetch(url, options = {}) {
    const defaultOptions = {
        credentials: 'include', // Cookie'leri otomatik gönder
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };
    
    return fetch(url, { ...defaultOptions, ...options });
}
