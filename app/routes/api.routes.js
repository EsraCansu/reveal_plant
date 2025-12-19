// 1. Portu 8080 olarak düzelttik
const API_BASE_URL = 'http://127.0.0.1:8080/api';

const authRoutes = {
    // 2. Java Controller (UserController) ile yolu eşitledik
    register: `${API_BASE_URL}/users/register`,
    login: `${API_BASE_URL}/users/login`, // Java'da login metodunu yazınca burası çalışacak
    logout: `${API_BASE_URL}/users/logout`
};

const diagnosticsRoutes = {
    // Java tarafında PredictionController yollarını buraya yazmalısın
    identifyPlant: `${API_BASE_URL}/predictions/identify`,
    detectDisease: `${API_BASE_URL}/predictions/disease`
};

/**
 * Geliştirilmiş apiCall fonksiyonu (JSON hatasını önler)
 */
async function apiCall(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
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
        
        // Yanıt boş mu kontrolü
        const text = await response.text(); 
        const data = text ? JSON.parse(text) : {}; 

        if (!response.ok) {
            throw new Error(data.message || `Hata Kodu: ${response.status}`);
        }

        return data;
    } catch (error) {
        console.error('API Call Error:', error);
        throw error;
    }
}

const API_ROUTES = {
    auth: authRoutes,
    diagnostics: diagnosticsRoutes,
    apiCall: apiCall
};

if (typeof module !== 'undefined' && module.exports) {
    module.exports = API_ROUTES;
}