/**
 * Dummy Data Generator for RevelPlant
 * Creates sample users and diagnosis data for testing
 */

const dummyUsers = [
    {
        email: 'john@example.com',
        password: 'demo123',
        name: 'John Doe'
    },
    {
        email: 'sarah@example.com',
        password: 'demo123',
        name: 'Sarah Smith'
    },
    {
        email: 'mike@example.com',
        password: 'demo123',
        name: 'Mike Johnson'
    },
    {
        email: 'emma@example.com',
        password: 'demo123',
        name: 'Emma Williams'
    }
];

const dummyDiagnoses = [
    {
        id: 1,
        mode: 'identify',
        name: 'Tomato Plant',
        result: 'Tomato Plant (Solanum lycopersicum)',
        confidence: 94.5,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23e8f5e9%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E🍅 Tomato%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 3).toISOString(),
        details: 'Healthy tomato plant identified with high confidence. Suitable for cultivation.'
    },
    {
        id: 2,
        mode: 'disease',
        name: 'Early Blight',
        result: 'Early Blight - Moderate Severity',
        confidence: 87.2,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23fff3e0%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E⚠️ Disease%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 2).toISOString(),
        details: 'Early Blight detected on tomato leaves. Recommend fungicide treatment and removal of infected leaves.'
    },
    {
        id: 3,
        mode: 'identify',
        name: 'Rose Plant',
        result: 'Rose Plant (Rosa spp.)',
        confidence: 96.3,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23fce4ec%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E🌹 Rose%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 1).toISOString(),
        details: 'Beautiful rose plant identified. Recommended care: regular watering, pruning, and sunlight exposure.'
    },
    {
        id: 4,
        mode: 'disease',
        name: 'Powdery Mildew',
        result: 'Powdery Mildew - Light Severity',
        confidence: 82.1,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23f3e5f5%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E🦠 Mildew%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 0.5).toISOString(),
        details: 'Powdery Mildew detected. Apply sulfur-based fungicide and ensure proper air circulation.'
    },
    {
        id: 5,
        mode: 'identify',
        name: 'Basil Plant',
        result: 'Basil Plant (Ocimum basilicum)',
        confidence: 91.8,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23e8f5e9%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E🌿 Basil%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 5).toISOString(),
        details: 'Healthy basil plant. Perfect for culinary use. Harvest regularly to encourage growth.'
    },
    {
        id: 6,
        mode: 'disease',
        name: 'Leaf Spot',
        result: 'Leaf Spot - Moderate Severity',
        confidence: 85.4,
        image: 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22%3E%3Crect fill=%22%23ffebee%22 width=%22200%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 font-size=%2224%22%3E🍂 Leaf Spot%3C/text%3E%3C/svg%3E',
        date: new Date(Date.now() - 86400000 * 7).toISOString(),
        details: 'Bacterial Leaf Spot detected. Isolate the plant and apply copper fungicide.'
    }
];

const dummyProfiles = {
    'john@example.com': {
        fullName: 'John Doe',
        phone: '+1 (555) 123-4567',
        location: 'New York, USA',
        bio: 'Plant enthusiast and hobby gardener. Growing vegetables in my backyard.',
        avatar: 'https://via.placeholder.com/120/4CAF50/ffffff?text=JD'
    },
    'sarah@example.com': {
        fullName: 'Sarah Smith',
        phone: '+1 (555) 234-5678',
        location: 'Los Angeles, USA',
        bio: 'Professional botanist interested in plant disease detection.',
        avatar: 'https://via.placeholder.com/120/FF69B4/ffffff?text=SS'
    },
    'mike@example.com': {
        fullName: 'Mike Johnson',
        phone: '+1 (555) 345-6789',
        location: 'Chicago, USA',
        bio: 'Urban farmer and sustainability advocate.',
        avatar: 'https://via.placeholder.com/120/2196F3/ffffff?text=MJ'
    },
    'emma@example.com': {
        fullName: 'Emma Williams',
        phone: '+1 (555) 456-7890',
        location: 'Seattle, USA',
        bio: 'Gardening educator and plant care specialist.',
        avatar: 'https://via.placeholder.com/120/FF9800/ffffff?text=EW'
    }
};

/**
 * Initialize dummy data for a specific user
 */
function initializeDummyDataForUser(email) {
    // Set account creation date
    if (!localStorage.getItem('account_created_at')) {
        localStorage.setItem('account_created_at', new Date(Date.now() - 86400000 * 30).toISOString());
    }
    
    // Set profile data
    const profile = dummyProfiles[email];
    if (profile) {
        localStorage.setItem('profile', JSON.stringify(profile));
    }
    
    // Set diagnoses data (each user gets their own copy)
    const userDiagnoses = JSON.parse(JSON.stringify(dummyDiagnoses));
    localStorage.setItem('diagnoses', JSON.stringify(userDiagnoses));
    
    // Set settings
    localStorage.setItem('settings', JSON.stringify({
        emailNotif: true,
        pushNotif: false,
        newsNotif: true,
        careNotif: true,
        profileVisibility: 'private',
        shareData: false,
        language: 'en',
        theme: 'light'
    }));
}

/**
 * Get list of all dummy users
 */
function getAllDummyUsers() {
    return dummyUsers;
}

/**
 * Get dummy user by email
 */
function getDummyUser(email) {
    return dummyUsers.find(u => u.email === email);
}

/**
 * Verify dummy user credentials
 */
function verifyDummyUser(email, password) {
    const user = getDummyUser(email);
    return user && user.password === password;
}

/**
 * Display dummy users info
 */
function displayDummyUsersInfo() {
    console.log('%c=== RevelPlant Dummy Users ===', 'color: #2D7A4A; font-size: 16px; font-weight: bold;');
    console.log('%cYou can use any of these accounts to test the application:', 'color: #666;');
    
    dummyUsers.forEach((user, index) => {
        console.log(`\n%c${index + 1}. ${user.name}`, 'color: #2D7A4A; font-weight: bold;');
        console.log(`   Email: ${user.email}`);
        console.log(`   Password: ${user.password}`);
    });
    
    console.log('\n%cTip: After logging in, you will see:', 'color: #FF9800; font-weight: bold;');
    console.log('   - Dashboard with statistics');
    console.log('   - 6 sample diagnoses (3 plant IDs + 3 disease detections)');
    console.log('   - Complete profile information');
    console.log('   - Customizable settings');
}

// Auto-display info on page load
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('index.html') || window.location.pathname.endsWith('/')) {
        displayDummyUsersInfo();
    }
});
