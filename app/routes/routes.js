/**
 * Frontend Router
 * Manages page navigation and URL routing
 */

class Router {
    constructor() {
        this.routes = {};
        this.currentPage = null;
    }

    /**
     * Register a route
     */
    register(path, handler) {
        this.routes[path] = handler;
    }

    /**
     * Navigate to a route
     */
    navigate(path) {
        if (this.routes[path]) {
            this.currentPage = path;
            this.routes[path]();
            // Update browser history
            window.history.pushState({ page: path }, '', path);
        } else {
            console.warn(`Route not found: ${path}`);
            this.navigateHome();
        }
    }

    /**
     * Navigate to home page
     */
    navigateHome() {
        window.location.href = 'index.html';
    }

    /**
     * Get current page
     */
    getCurrentPage() {
        return this.currentPage;
    }
}

// Create global router instance
const router = new Router();

/**
 * Initialize all routes
 */
function initializeRoutes() {
    // Home page route
    router.register('home', () => {
        window.location.href = 'index.html';
    });

    // About page route
    router.register('about', () => {
        window.location.href = 'app/views/about.html';
    });

    // Contact page route
    router.register('contact', () => {
        window.location.href = 'app/views/contact.html';
    });

    // Login page route
    router.register('login', () => {
        window.location.href = 'app/views/login.html';
    });

    // Register page route
    router.register('register', () => {
        window.location.href = 'app/views/register.html';
    });

    // Dashboard page (future - for logged in users)
    router.register('dashboard', () => {
        const isLoggedIn = localStorage.getItem('user_logged_in');
        if (isLoggedIn) {
            window.location.href = 'app/views/dashboard.html';
        } else {
            router.navigate('login');
        }
    });

    // Profile page (future - for logged in users)
    router.register('profile', () => {
        const isLoggedIn = localStorage.getItem('user_logged_in');
        if (isLoggedIn) {
            window.location.href = 'app/views/profile.html';
        } else {
            router.navigate('login');
        }
    });

    // Diagnoses history (future - for logged in users)
    router.register('diagnoses', () => {
        const isLoggedIn = localStorage.getItem('user_logged_in');
        if (isLoggedIn) {
            window.location.href = 'app/views/diagnoses.html';
        } else {
            router.navigate('login');
        }
    });

    // Settings page (future - for logged in users)
    router.register('settings', () => {
        const isLoggedIn = localStorage.getItem('user_logged_in');
        if (isLoggedIn) {
            window.location.href = 'app/views/settings.html';
        } else {
            router.navigate('login');
        }
    });
}

// Initialize routes when page loads
document.addEventListener('DOMContentLoaded', () => {
    initializeRoutes();
});

// Handle browser back/forward buttons
window.addEventListener('popstate', (event) => {
    if (event.state && event.state.page) {
        router.navigate(event.state.page);
    }
});
