/**
 * DiagnosticsController
 * Handles all user interactions and workflow logic
 */

// Backend API URL Configuration
const BACKEND_URL = window.BACKEND_URL || 'http://localhost:8080';

class DiagnosticsController {
    constructor() {
        this.uploadedImage = null;
        this.selectedMode = null;
        this.initializeEventListeners();
    }

    /**
     * Initialize all event listeners
     */
    initializeEventListeners() {
        const uploadBtn = document.getElementById('uploadBtn');
        const fileInput = document.getElementById('fileInput');
        const uploadArea = document.querySelector('.upload-area');

        // File upload
        uploadBtn?.addEventListener('click', () => fileInput?.click());
        fileInput?.addEventListener('change', (e) => this.handleFileUpload(e));

        // Drag and drop
        uploadArea?.addEventListener('dragover', (e) => this.handleDragOver(e));
        uploadArea?.addEventListener('dragleave', (e) => this.handleDragLeave(e));
        uploadArea?.addEventListener('drop', (e) => this.handleDrop(e));
    }

    /**
     * Handle file upload
     */
    handleFileUpload(event) {
        const file = event.target.files[0];
        if (file && file.type.startsWith('image/')) {
            this.processImage(file);
        }
    }

    /**
     * Process image file
     */
    async processImage(file) {
        const reader = new FileReader();
        reader.onload = async (event) => {
            this.uploadedImage = event.target.result;
            const imagePreview = document.getElementById('imagePreview');
            if (imagePreview) {
                imagePreview.innerHTML = `<img src="${this.uploadedImage}" class="preview-image" alt="Uploaded plant image"><div class="text-center mt-3"><div class="spinner-border text-success" role="status"><span class="visually-hidden">Analyzing...</span></div><p class="mt-2">Analyzing your plant...</p></div>`;
            }
            
            // ✅ DÜZELTME: Backend'e gerçek istek gönder
            await this.sendToBackend(file);
        };
        reader.readAsDataURL(file);
    }

    /**
     * ✅ YENİ: Backend'e görüntü gönder ve gerçek sonuç al
     */
    async sendToBackend(file) {
        try {
            // Convert file to base64
            const base64Image = await this.fileToBase64(file);
            
            // Get current user info - use guest ID if not logged in
            let userId = null;
            try {
                if (typeof getCurrentUser === 'function') {
                    const user = getCurrentUser();
                    userId = user?.id || null;
                }
            } catch (e) {
                console.log('[API] User not logged in, using guest mode');
            }
            
            // If no user, use guest ID (1 for now - should be a real guest user in DB)
            if (!userId) {
                userId = 1; // Default guest user ID
            }
            
            const requestBody = {
                imageBase64: base64Image,
                predictionType: this.selectedMode === 'identify' ? 'identify-plant' : 'detect-disease',
                userId: userId,
                description: `Uploaded ${this.selectedMode} image`
            };

            console.log('[API] Sending request to:', `${BACKEND_URL}/api/predictions/analyze`);
            console.log('[API] User ID:', userId);
            console.log('[API] Prediction Type:', requestBody.predictionType);
            
            const response = await fetch(`${BACKEND_URL}/api/predictions/analyze`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            console.log('[API] Response status:', response.status);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('[API] Error response:', errorText);
                throw new Error(`Prediction failed: ${response.status} - ${errorText}`);
            }

            const result = await response.json();
            console.log('[API] Success:', result);
            this.predictionResult = result;
            setTimeout(() => this.showResults(), 500);
        } catch (error) {
            console.error('[API] Backend error:', error);
            alert(`Analysis failed: ${error.message}`);
            this.goBack('step-mode');
        }
    }

    /**
     * Convert file to base64
     */
    fileToBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    /**
     * Handle drag over
     */
    handleDragOver(event) {
        event.preventDefault();
        event.currentTarget.style.backgroundColor = 'rgba(103, 166, 75, 0.15)';
    }

    /**
     * Handle drag leave
     */
    handleDragLeave(event) {
        event.currentTarget.style.backgroundColor = 'var(--surface)';
    }

    /**
     * Handle drop
     */
    handleDrop(event) {
        event.preventDefault();
        const file = event.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            this.processImage(file);
        }
    }

    /**
     * Select mode (identify or disease detection)
     */
    selectMode(mode) {
        this.selectedMode = mode;
        this.showStep('step-upload');
    }

    /**
     * Show results based on selected mode
     */
    showResults() {
        if (!this.uploadedImage || !this.selectedMode) return;

        const resultContent = document.getElementById('resultContent');
        if (!resultContent) return;

        if (this.selectedMode === 'identify') {
            resultContent.innerHTML = this.getIdentificationResult();
        } else if (this.selectedMode === 'disease') {
            resultContent.innerHTML = this.getDiseaseResult();
        }

        // Save diagnosis to localStorage
        this.saveDiagnosis();
        this.showStep('step-results');
    }

    /**
     * Save diagnosis to localStorage
     */
    saveDiagnosis() {
        const diagnoses = JSON.parse(localStorage.getItem('diagnoses') || '[]');
        const isLoggedIn = localStorage.getItem('user_logged_in');
        
        if (!isLoggedIn) return; // Only save if user is logged in

        // ✅ DÜZELTME: Backend'den gelen gerçek veriyi kullan
        const result = this.predictionResult || {};
        
        const diagnosis = {
            id: Date.now(),
            mode: this.selectedMode,
            name: result.predicted_class || result.plantName || 'Unknown',
            result: result.predicted_class || result.diseaseName || 'Unknown',
            confidence: result.confidence ? (result.confidence * 100) : 0,
            image: this.uploadedImage,
            date: new Date().toISOString(),
            details: result.description || result.recommendedAction || 'No details available'
        };

        diagnoses.push(diagnosis);
        localStorage.setItem('diagnoses', JSON.stringify(diagnoses));
    }

    /**
     * Parse plant name from ML model class (e.g., "Strawberry___healthy" → "Strawberry")
     * Also handles special cases like "Cherry_(including_sour)" → "Cherry"
     */
    parsePlantName(mlClass) {
        // Extract plant name (before "___" or "_(")
        let plantName = mlClass.split('___')[0]; // "Strawberry___healthy" → "Strawberry"
        plantName = plantName.split('_(')[0];     // "Cherry_(including_sour)" → "Cherry"
        return plantName;
    }

    /**
     * ✅ DÜZELTME: Backend'den gelen gerçek veriyle identification sonucu göster
     * 🌱 Sadece bitki adını göster (hastalık adını değil)
     */
    getIdentificationResult() {
        const result = this.predictionResult || {};
        const rawClass = result.predicted_class || 'Unknown Plant';
        const predictedClass = this.parsePlantName(rawClass); // "Strawberry___healthy" → "Strawberry"
        const confidence = result.confidence ? (result.confidence * 100).toFixed(1) : '0';
        const predictionId = result.prediction_id || 0;
        const isLoggedIn = getCookie('userEmail') !== null;
        const userId = parseInt(getCookie('userEmail') ? sessionStorage.getItem('userId') || localStorage.getItem('user_id') : null) || 1;
        
        console.log('[FEEDBACK] PredictionResult:', result);
        console.log('[FEEDBACK] PredictionId:', predictionId, 'UserId:', userId, 'IsLoggedIn:', isLoggedIn);
        
        return `
            <div class="text-center">
                <img src="${this.uploadedImage}" class="preview-image" alt="Analyzed plant">
                <h3 class="mt-3">Plant Identification Result</h3>
                <div class="alert alert-success mt-3">
                    <h5>Identified Plant: <strong>${predictedClass}</strong></h5>
                    <p class="mb-2"><small>Confidence: <strong>${confidence}%</strong></small></p>
                    <hr>
                    ${result.description ? `<p>${result.description}</p>` : ''}
                    ${result.top_predictions ? `
                        <p><strong>Other Possibilities:</strong></p>
                        <ul class="text-start">
                            ${result.top_predictions.slice(1, 4).map(p => 
                                `<li>${this.parsePlantName(p.class_name)}: ${(p.probability * 100).toFixed(1)}%</li>`
                            ).join('')}
                        </ul>
                    ` : ''}
                </div>
                ${isLoggedIn && predictionId ? `
                    <div class="feedback-section mt-3" id="feedback-section-${predictionId}">
                        <p class="text-muted mb-2">Was this prediction correct?</p>
                        <div class="btn-group" role="group">
                            <button class="btn btn-outline-success" onclick="app.submitFeedback(${predictionId}, true, '${rawClass}', ${userId})">
                                <i class="fas fa-thumbs-up"></i> Correct
                            </button>
                            <button class="btn btn-outline-danger" onclick="app.submitFeedback(${predictionId}, false, '${rawClass}', ${userId})">
                                <i class="fas fa-thumbs-down"></i> Incorrect
                            </button>
                        </div>
                        <div id="feedback-message-${predictionId}" class="mt-2"></div>
                    </div>
                ` : ''}
            </div>
        `;
    }

    /**
     * ✅ DÜZELTME: Backend'den gelen gerçek veriyle disease sonucu göster
     */
    getDiseaseResult() {
        const result = this.predictionResult || {};
        const predictedClass = result.predicted_class || 'Unknown Disease';
        const confidence = result.confidence ? (result.confidence * 100).toFixed(1) : '0';
        const isHealthy = predictedClass.toLowerCase().includes('healthy');
        const predictionId = result.prediction_id || 0;
        const isLoggedIn = getCookie('userEmail') !== null; // Check cookie instead of localStorage
        const userId = parseInt(getCookie('userEmail') ? sessionStorage.getItem('userId') || localStorage.getItem('user_id') : null) || 1;
        
        console.log('[FEEDBACK] PredictionResult:', result);
        console.log('[FEEDBACK] PredictionId:', predictionId, 'UserId:', userId, 'IsLoggedIn:', isLoggedIn);
        
        return `
            <div class="text-center">
                <img src="${this.uploadedImage}" class="preview-image" alt="Analyzed plant">
                <h3 class="mt-3">Disease Detection Result</h3>
                <div class="alert ${isHealthy ? 'alert-success' : 'alert-warning'} mt-3">
                    <h5>
                        <i class="fas ${isHealthy ? 'fa-check-circle' : 'fa-exclamation-triangle'}"></i> 
                        ${isHealthy ? 'Plant is Healthy!' : 'Disease Detected'}
                    </h5>
                    <p class="mb-2"><small>Confidence: <strong>${confidence}%</strong></small></p>
                    <hr>
                    <p><strong>Result:</strong> ${predictedClass}</p>
                    ${result.description ? `<p>${result.description}</p>` : ''}
                    ${result.top_predictions && !isHealthy ? `
                        <p><strong>Other Possibilities:</strong></p>
                        <ul class="text-start">
                            ${result.top_predictions.slice(1, 4).map(p => 
                                `<li>${p.class_name}: ${(p.probability * 100).toFixed(1)}%</li>`
                            ).join('')}
                        </ul>
                    ` : ''}
                </div>
                ${isLoggedIn && predictionId ? `
                    <div class="feedback-section mt-3" id="feedback-section-${predictionId}">
                        <p class="text-muted mb-2">Was this prediction correct?</p>
                        <div class="btn-group" role="group">
                            <button class="btn btn-outline-success" onclick="app.submitFeedback(${predictionId}, true, '${predictedClass}', ${userId})">
                                <i class="fas fa-thumbs-up"></i> Correct
                            </button>
                            <button class="btn btn-outline-danger" onclick="app.submitFeedback(${predictionId}, false, '${predictedClass}', ${userId})">
                                <i class="fas fa-thumbs-down"></i> Incorrect
                            </button>
                        </div>
                        <div id="feedback-message-${predictionId}" class="mt-2"></div>
                    </div>
                ` : ''}
            </div>
        `;
    }

    /**
     * Navigate between steps
     */
    showStep(stepId) {
        document.querySelectorAll('.step-container').forEach(step => {
            step.classList.remove('active');
        });
        const targetStep = document.getElementById(stepId);
        if (targetStep) {
            targetStep.classList.add('active');
        }
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    /**
     * Submit feedback for a prediction
     */
    async submitFeedback(predictionId, isCorrect, predictedClass, userId = null) {
        console.log('[FEEDBACK-DEBUG] submitFeedback called!');
        console.log('[FEEDBACK-DEBUG] predictionId:', predictionId);
        console.log('[FEEDBACK-DEBUG] isCorrect:', isCorrect);
        console.log('[FEEDBACK-DEBUG] predictedClass:', predictedClass);
        console.log('[FEEDBACK-DEBUG] userId:', userId);
        
        try {
            console.log(`[Feedback] Submitting: predictionId=${predictionId}, isCorrect=${isCorrect}`);
            
            // HEMEN butonları gizle ve loading mesajı göster
            const feedbackSection = document.getElementById(`feedback-section-${predictionId}`);
            const messageDiv = document.getElementById(`feedback-message-${predictionId}`);
            
            console.log('[FEEDBACK-DEBUG] feedbackSection:', feedbackSection);
            console.log('[FEEDBACK-DEBUG] messageDiv:', messageDiv);
            
            const buttons = feedbackSection?.querySelectorAll('button');
            
            if (buttons) {
                console.log('[FEEDBACK-DEBUG] Found buttons:', buttons.length);
                buttons.forEach(btn => {
                    btn.style.display = 'none'; // Butonları hemen gizle
                });
            } else {
                console.error('[FEEDBACK-DEBUG] No buttons found!');
            }
            
            if (messageDiv) {
                messageDiv.innerHTML = `<small class="text-info"><i class="fas fa-spinner fa-spin"></i> Sending feedback...</small>`;
            } else {
                console.error('[FEEDBACK-DEBUG] messageDiv not found!');
            }
            
            // Get userId from cookie or fall back to parameter
            if (!userId) {
                // Try to get from cookie system first
                const userEmail = getCookie('userEmail');
                if (userEmail) {
                    // User is logged in, get userId from sessionStorage or backend
                    userId = parseInt(sessionStorage.getItem('userId')) || parseInt(localStorage.getItem('user_id'));
                    console.log('[FEEDBACK-DEBUG] Got userId from session/local:', userId);
                }
                // If still no userId, default to guest (1)
                if (!userId) {
                    userId = 1;
                    console.log('[FEEDBACK-DEBUG] Using guest userId:', userId);
                }
            }
            
            console.log('[FEEDBACK-DEBUG] Final userId:', userId);
            
            const feedback = {
                predictionId: predictionId,
                userId: userId,
                isCorrect: isCorrect,
                predictedClass: predictedClass,
                feedbackText: isCorrect ? 'Prediction is correct' : 'Prediction is incorrect'
            };

            const response = await fetch(`${BACKEND_URL}/api/predictions/feedback`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(feedback)
            });

            const result = await response.json();
            
            if (response.ok) {
                messageDiv.innerHTML = `<small class="text-success"><i class="fas fa-check-circle"></i> Thank you for your feedback!</small>`;
                
                // Tüm feedback section'ı 2 saniye sonra kaldır
                setTimeout(() => {
                    if (feedbackSection) {
                        feedbackSection.style.transition = 'opacity 0.5s';
                        feedbackSection.style.opacity = '0';
                        setTimeout(() => feedbackSection.remove(), 500);
                    }
                }, 2000);
                
                console.log('[Feedback] Submitted successfully:', result);
            } else {
                messageDiv.innerHTML = `<small class="text-danger"><i class="fas fa-exclamation-circle"></i> Failed to submit feedback</small>`;
                // Hata durumunda butonları tekrar göster
                if (buttons) {
                    buttons.forEach(btn => btn.style.display = 'inline-block');
                }
                console.error('[Feedback] Error:', result);
            }
        } catch (error) {
            console.error('[Feedback] Network error:', error);
            const messageDiv = document.getElementById(`feedback-message-${predictionId}`);
            const feedbackSection = document.getElementById(`feedback-section-${predictionId}`);
            const buttons = feedbackSection?.querySelectorAll('button');
            
            if (messageDiv) {
                messageDiv.innerHTML = `<small class="text-danger"><i class="fas fa-exclamation-circle"></i> Network error. Please try again.</small>`;
            }
            
            // Hata durumunda butonları tekrar göster
            if (buttons) {
                buttons.forEach(btn => btn.style.display = 'inline-block');
            }
        }
    }

    /**
     * Go back to previous step
     */
    goBack(stepId) {
        if (stepId === 'step-mode') {
            const fileInput = document.getElementById('fileInput');
            const imagePreview = document.getElementById('imagePreview');
            if (fileInput) fileInput.value = '';
            if (imagePreview) imagePreview.innerHTML = '';
            this.uploadedImage = null;
            this.selectedMode = null;
        }
        this.showStep(stepId);
    }

    /**
     * Download report
     */
    downloadReport() {
        alert('Report download feature will be available soon!');
    }
}

/**
 * WebSocket Client for Real-time Predictions
 * Handles connection to Spring Boot WebSocket endpoint
 */
class PredictionWebSocketClient {
    constructor() {
        this.userId = this.getUserId();  // Get from session/auth
        this.connected = false;
        this.client = null;
        this.subscriptions = {};
        this.connect();
    }

    /**
     * Get current user ID from session/localStorage
     */
    getUserId() {
        // TODO: Get from authentication context
        // For now, use sessionStorage or query parameter
        return sessionStorage.getItem('userId') || new URLSearchParams(window.location.search).get('userId') || 'guest';
    }

    /**
     * Connect to WebSocket server
     */
    connect() {
        // Using SockJS for WebSocket with fallback to HTTP polling
        const socket = new SockJS(`${BACKEND_URL}/ws/predictions`);
        this.client = Stomp.over(socket);

        // Enable logging for debugging
        this.client.debug = (str) => {
            if (str.includes('CONNECTED') || str.includes('SEND') || str.includes('SUBSCRIBE')) {
                console.log('[WebSocket]', str);
            }
        };

        // Connect with headers
        this.client.connect({}, (frame) => {
            console.log('WebSocket connected:', frame);
            this.connected = true;
            this.subscribeToChannels();
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.connected = false;
            // Retry connection after 5 seconds
            setTimeout(() => this.connect(), 5000);
        });
    }

    /**
     * Subscribe to WebSocket channels
     */
    subscribeToChannels() {
        // Subscribe to user-specific prediction results
        this.subscriptions.predictions = this.client.subscribe(
            `/user/${this.userId}/queue/predictions`,
            (message) => this.handlePredictionResponse(JSON.parse(message.body))
        );

        // Subscribe to prediction status updates
        this.subscriptions.status = this.client.subscribe(
            `/user/${this.userId}/queue/status`,
            (message) => this.handleStatusUpdate(JSON.parse(message.body))
        );

        // Subscribe to error messages
        this.subscriptions.errors = this.client.subscribe(
            `/user/${this.userId}/queue/errors`,
            (message) => this.handleError(JSON.parse(message.body))
        );

        // Subscribe to broadcast predictions
        this.subscriptions.broadcast = this.client.subscribe(
            '/topic/predictions',
            (message) => this.handleBroadcastPrediction(JSON.parse(message.body))
        );
    }

    /**
     * Send prediction request to server
     */
    sendPrediction(plantId, imageBase64, description = '') {
        if (!this.connected) {
            console.error('WebSocket not connected');
            return;
        }

        const request = {
            userId: this.userId,
            plantId: plantId,
            imageBase64: imageBase64,
            description: description,
            requestedAt: new Date().toISOString()
        };

        this.client.send(
            `/app/predict/${this.userId}`,
            {},
            JSON.stringify(request)
        );

        console.log('Prediction request sent');
    }

    /**
     * Handle individual prediction response
     */
    handlePredictionResponse(response) {
        console.log('Prediction response received:', response);

        // Update UI with prediction results
        const resultDiv = document.getElementById('predictionResult');
        if (resultDiv) {
            resultDiv.innerHTML = `
                <div class="prediction-card">
                    <h3>Plant: ${response.plantName}</h3>
                    <h4>Disease: ${response.diseaseName}</h4>
                    <p>Confidence: ${(response.confidence * 100).toFixed(2)}%</p>
                    <p><strong>Recommendation:</strong> ${response.recommendedAction}</p>
                    <p><small>Predicted at: ${response.predictedAt}</small></p>
                </div>
            `;
        }

        // Trigger callback if exists
        if (window.onPredictionComplete) {
            window.onPredictionComplete(response);
        }
    }

    /**
     * Handle status updates during processing
     */
    handleStatusUpdate(update) {
        console.log('Status update:', update);

        // Update progress bar
        const progressBar = document.getElementById('predictionProgress');
        if (progressBar) {
            progressBar.style.width = `${update.progressPercentage}%`;
            progressBar.textContent = `${update.progressPercentage}%`;
        }

        // Update status message
        const statusDiv = document.getElementById('predictionStatus');
        if (statusDiv) {
            statusDiv.textContent = update.message;
            statusDiv.classList.add(`status-${update.status.toLowerCase()}`);
        }

        // Trigger callback if exists
        if (window.onStatusUpdate) {
            window.onStatusUpdate(update);
        }
    }

    /**
     * Handle error messages
     */
    handleError(error) {
        console.error('WebSocket error:', error);

        const errorDiv = document.getElementById('predictionError');
        if (errorDiv) {
            errorDiv.style.display = 'block';
            errorDiv.textContent = `Error: ${error.errorMessage}`;
        }

        // Trigger callback if exists
        if (window.onPredictionError) {
            window.onPredictionError(error);
        }
    }

    /**
     * Handle broadcast predictions from other users
     */
    handleBroadcastPrediction(prediction) {
        console.log('Broadcast prediction received:', prediction);

        // Update live feed or notification center
        const feedDiv = document.getElementById('predictionFeed');
        if (feedDiv) {
            const item = document.createElement('div');
            item.className = 'feed-item';
            item.innerHTML = `
                <p><strong>${prediction.plantName}</strong> - ${prediction.diseaseName}</p>
                <p>Confidence: ${(prediction.confidence * 100).toFixed(2)}%</p>
            `;
            feedDiv.prepend(item);
        }

        // Trigger callback if exists
        if (window.onBroadcastPrediction) {
            window.onBroadcastPrediction(prediction);
        }
    }

    /**
     * Send heartbeat to keep connection alive
     */
    sendHeartbeat() {
        if (this.connected) {
            this.client.send('/app/heartbeat', {}, JSON.stringify({ clientId: this.userId }));
        }
    }

    /**
     * Disconnect from WebSocket
     */
    disconnect() {
        if (this.client) {
            this.client.disconnect(() => {
                console.log('WebSocket disconnected');
                this.connected = false;
            });
        }
    }
}

// Initialize controller when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.diagnosticsController = new DiagnosticsController();
    window.controller = window.diagnosticsController; // ✅ DÜZELTME: index.html'deki onclick için alias
    window.app = window.diagnosticsController; // ✅ DÜZELTME: feedback onclick için alias
    window.webSocketClient = new PredictionWebSocketClient();

    // Send heartbeat every 30 seconds
    setInterval(() => window.webSocketClient?.sendHeartbeat(), 30000);
});
