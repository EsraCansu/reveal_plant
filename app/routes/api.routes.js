/**
 * API Routes Configuration
 * Defines all backend API endpoints
 * To be used with Express.js or similar backend framework
 */

// Base API URL
const API_BASE_URL = process.env.API_URL || 'http://localhost:3000/api';

/**
 * Authentication Routes
 */
const authRoutes = {
    // POST /auth/register - Register new user
    register: `${API_BASE_URL}/auth/register`,
    
    // POST /auth/login - Login user
    login: `${API_BASE_URL}/auth/login`,
    
    // POST /auth/logout - Logout user
    logout: `${API_BASE_URL}/auth/logout`,
    
    // POST /auth/refresh-token - Refresh access token
    refreshToken: `${API_BASE_URL}/auth/refresh-token`,
    
    // POST /auth/forgot-password - Request password reset
    forgotPassword: `${API_BASE_URL}/auth/forgot-password`,
    
    // POST /auth/reset-password - Reset password with token
    resetPassword: `${API_BASE_URL}/auth/reset-password`
};

/**
 * Plant Diagnostics Routes
 */
const diagnosticsRoutes = {
    // POST /diagnostics/identify - Identify plant from image
    identifyPlant: `${API_BASE_URL}/diagnostics/identify`,
    
    // POST /diagnostics/detect-disease - Detect disease from plant image
    detectDisease: `${API_BASE_URL}/diagnostics/detect-disease`,
    
    // GET /diagnostics/history - Get user's diagnosis history
    getHistory: `${API_BASE_URL}/diagnostics/history`,
    
    // GET /diagnostics/:id - Get specific diagnosis details
    getDiagnosis: (id) => `${API_BASE_URL}/diagnostics/${id}`,
    
    // DELETE /diagnostics/:id - Delete a diagnosis
    deleteDiagnosis: (id) => `${API_BASE_URL}/diagnostics/${id}`,
    
    // POST /diagnostics/:id/download - Download diagnosis report
    downloadReport: (id) => `${API_BASE_URL}/diagnostics/${id}/download`
};

/**
 * User Profile Routes
 */
const profileRoutes = {
    // GET /profile - Get user profile
    getProfile: `${API_BASE_URL}/profile`,
    
    // PUT /profile - Update user profile
    updateProfile: `${API_BASE_URL}/profile`,
    
    // PUT /profile/password - Change password
    changePassword: `${API_BASE_URL}/profile/password`,
    
    // GET /profile/preferences - Get user preferences
    getPreferences: `${API_BASE_URL}/profile/preferences`,
    
    // PUT /profile/preferences - Update user preferences
    updatePreferences: `${API_BASE_URL}/profile/preferences`
};

/**
 * Plant Database Routes
 */
const plantRoutes = {
    // GET /plants - Get all plants
    getAllPlants: `${API_BASE_URL}/plants`,
    
    // GET /plants/search - Search plants
    searchPlants: `${API_BASE_URL}/plants/search`,
    
    // GET /plants/:id - Get specific plant details
    getPlant: (id) => `${API_BASE_URL}/plants/${id}`,
    
    // GET /plants/:id/diseases - Get diseases for a plant
    getPlantDiseases: (id) => `${API_BASE_URL}/plants/${id}/diseases`
};

/**
 * Disease Database Routes
 */
const diseaseRoutes = {
    // GET /diseases - Get all diseases
    getAllDiseases: `${API_BASE_URL}/diseases`,
    
    // GET /diseases/search - Search diseases
    searchDiseases: `${API_BASE_URL}/diseases/search`,
    
    // GET /diseases/:id - Get specific disease details
    getDisease: (id) => `${API_BASE_URL}/diseases/${id}`,
    
    // GET /diseases/:id/treatments - Get treatments for a disease
    getTreatments: (id) => `${API_BASE_URL}/diseases/${id}/treatments`
};

/**
 * Utility function to make API calls
 */
async function apiCall(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('access_token')}`
        }
    };

    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    try {
        const response = await fetch(url, mergedOptions);
        
        if (!response.ok) {
            if (response.status === 401) {
                // Token expired, redirect to login
                localStorage.removeItem('access_token');
                window.location.href = 'app/views/login.html';
            }
            throw new Error(`API Error: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Call Error:', error);
        throw error;
    }
}

/**
 * Export all routes
 */
const API_ROUTES = {
    auth: authRoutes,
    diagnostics: diagnosticsRoutes,
    profile: profileRoutes,
    plants: plantRoutes,
    diseases: diseaseRoutes,
    apiCall: apiCall
};

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = API_ROUTES;
}
