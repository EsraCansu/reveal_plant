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
            
            // ✅ FIX: Send actual request to backend
            await this.sendToBackend(file);
        };
        reader.readAsDataURL(file);
    }

    /**
     * ✅ NEW: Send image to backend and get real result
     */
    async sendToBackend(file) {
        try {
            // Convert file to base64
            const base64Image = await this.fileToBase64(file);
            
            // Get current user ID or fall back to guest (0)
            let userId = null;
            try {
                if (typeof getCurrentUserId === 'function') {
                    userId = getCurrentUserId();
                } else if (typeof getCurrentUser === 'function') {
                    const user = getCurrentUser();
                    userId = Number.isInteger(user?.id) ? user.id : null;
                }
            } catch (e) {
                console.log('[API] User not logged in, using guest mode');
            }

            const resolvedUserId = Number.isInteger(userId) ? userId : 0;
            
            const requestBody = {
                imageBase64: base64Image,
                predictionType: this.selectedMode === 'identify' ? 'identify-plant' : 'detect-disease',
                userId: resolvedUserId,
                description: `Uploaded ${this.selectedMode} image`
            };

            console.log('[API] Sending request to:', `${BACKEND_URL}/api/predictions/analyze`);
            console.log('[API] User ID:', resolvedUserId);
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
            console.log('[API] Treatment Info:', {
                symptom_description: result.symptom_description,
                treatment: result.treatment,
                recommended_medicines: result.recommended_medicines
            });
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

        // ✅ FIX: Use real data from backend
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
     * ✅ FIX: Show identification result with real data from backend
     * 🌱 Only show plant name (not disease name)
     */
    getIdentificationResult() {
        const result = this.predictionResult || {};
        const rawClass = result.predicted_class || 'Unknown Plant';
        const predictedClass = this.parsePlantName(rawClass);
        const confidence = result.confidence ? (result.confidence * 100).toFixed(1) : '0';
        const predictionId = result.prediction_id || 0;
        const storedUserId = typeof getCurrentUserId === 'function' ? getCurrentUserId() : null;
        const userId = Number.isInteger(storedUserId) ? storedUserId : 0;
        const isLoggedIn = getCookie('userEmail') !== null && userId > 0;
        
        console.log('[FEEDBACK] PredictionResult:', result);
        console.log('[FEEDBACK] PredictionId:', predictionId, 'UserId:', userId, 'IsLoggedIn:', isLoggedIn);
        console.log('[PLANT INFO] Scientific Name:', result.scientific_name, 'Category:', result.category);
        console.log('[PLANT INFO] Description:', result.plant_description);
        
        return `
            <div class="text-center">
                <img src="${this.uploadedImage}" class="preview-image" alt="Analyzed plant">
                <h3 class="mt-3">Plant Identification Result</h3>
                <div class="alert alert-success mt-3">
                    <h5>Identified Plant: <strong>${predictedClass}</strong></h5>
                    ${result.scientific_name ? `<p class="text-muted mb-1"><em>${result.scientific_name}</em></p>` : ''}
                    <p class="mb-2"><small>Confidence: <strong>${confidence}%</strong></small></p>
                    <hr>
                    ${result.plant_description ? `
                        <div class="text-start mb-3">
                            <h6><i class="fas fa-info-circle"></i> Plant Information:</h6>
                            <p class="mb-1">${result.plant_description}</p>
                        </div>
                    ` : ''}
                    ${(result.care_tips || result.watering_frequency || result.sunlight_requirement || result.soil_type) ? `
                        <div class="text-start mb-3">
                            <h6><i class="fas fa-seedling"></i> Care Guide:</h6>
                            <div class="row">
                                ${result.watering_frequency ? `
                                    <div class="col-md-6 mb-2">
                                        <strong><i class="fas fa-tint text-primary"></i> Watering:</strong>
                                        <p class="mb-0 small">${result.watering_frequency}</p>
                                    </div>
                                ` : ''}
                                ${result.sunlight_requirement ? `
                                    <div class="col-md-6 mb-2">
                                        <strong><i class="fas fa-sun text-warning"></i> Sunlight:</strong>
                                        <p class="mb-0 small">${result.sunlight_requirement}</p>
                                    </div>
                                ` : ''}
                                ${result.soil_type ? `
                                    <div class="col-md-6 mb-2">
                                        <strong><i class="fas fa-mountain text-secondary"></i> Soil:</strong>
                                        <p class="mb-0 small">${result.soil_type}</p>
                                    </div>
                                ` : ''}
                                ${result.hardiness_zone ? `
                                    <div class="col-md-6 mb-2">
                                        <strong><i class="fas fa-thermometer-half text-danger"></i> Hardiness:</strong>
                                        <p class="mb-0 small">${result.hardiness_zone}</p>
                                    </div>
                                ` : ''}
                            </div>
                            ${result.care_tips ? `
                                <div class="mt-2">
                                    <strong><i class="fas fa-lightbulb text-success"></i> Care Tips:</strong>
                                    <p class="mb-0 small">${result.care_tips}</p>
                                </div>
                            ` : ''}
                        </div>
                    ` : ''}
                    ${(() => {
                        // Filter out predictions with the same plant name as the main result
                        const mainPlantName = predictedClass.toLowerCase();
                        const filteredPredictions = (result.top_predictions || []).filter(p => {
                            const altPlantName = this.parsePlantName(p.class_name).toLowerCase();
                            return altPlantName !== mainPlantName;
                        });
                        
                        if (filteredPredictions.length > 0) {
                            return `
                                <div class="text-start">
                                    <h6><i class="fas fa-list"></i> Other Possibilities:</h6>
                                    <ul>
                                        ${filteredPredictions.slice(0, 3).map(p => 
                                            `<li>${this.parsePlantName(p.class_name)}: ${(p.probability * 100).toFixed(1)}%</li>`
                                        ).join('')}
                                    </ul>
                                </div>
                            `;
                        }
                        return '';
                    })()}
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
     * ✅ FIX: Show disease result with real data from backend
     * Clean and organized format like Plant Identification
     */
    getDiseaseResult() {
        const result = this.predictionResult || {};
        const predictedClass = result.predicted_class || 'Unknown Disease';
        const confidence = result.confidence ? (result.confidence * 100).toFixed(1) : '0';
        const isHealthy = predictedClass.toLowerCase().includes('healthy');
        const predictionId = result.prediction_id || 0;
        const storedUserId = typeof getCurrentUserId === 'function' ? getCurrentUserId() : null;
        const userId = Number.isInteger(storedUserId) ? storedUserId : 0;
        const isLoggedIn = getCookie('userEmail') !== null && userId > 0;
        
        console.log('[FEEDBACK] PredictionResult:', result);
        console.log('[FEEDBACK] PredictionId:', predictionId, 'UserId:', userId, 'IsLoggedIn:', isLoggedIn);
        console.log('[DISEASE INFO] Symptoms:', result.symptom_description);
        console.log('[DISEASE INFO] Treatment:', result.treatment);
        console.log('[DISEASE INFO] Medicines:', result.recommended_medicines);
        
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
                    <p class="mb-1"><strong>Result:</strong> ${predictedClass}</p>
                    ${!isHealthy && (result.symptom_description || result.treatment || result.recommended_medicines) ? `
                        <div class="text-start mt-3 mb-3">
                            ${result.symptom_description ? `
                                <h6><i class="fas fa-info-circle"></i> About Disease:</h6>
                                <p class="mb-2">${result.symptom_description}</p>
                            ` : ''}
                            ${result.treatment ? `
                                <h6><i class="fas fa-medkit"></i> Treatment:</h6>
                                <p class="mb-2">${result.treatment}</p>
                            ` : ''}
                            ${result.recommended_medicines ? `
                                <h6><i class="fas fa-pills"></i> Recommended Medicines:</h6>
                                <p class="mb-2">${result.recommended_medicines}</p>
                            ` : ''}
                        </div>
                    ` : ''}
                    ${result.top_predictions && result.top_predictions.length > 1 ? `
                        <div class="text-start">
                            <h6><i class="fas fa-list"></i> Other Possibilities:</h6>
                            <ul>
                                ${result.top_predictions.slice(1, 4).map(p => 
                                    `<li>${p.class_name}: ${(p.probability * 100).toFixed(1)}%</li>`
                                ).join('')}
                            </ul>
                        </div>
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
            
            // IMMEDIATELY hide buttons and show loading message
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
            
            // Resolve userId from helper, cookies, or fallback to guest
            if (!userId) {
                const storedUserId = typeof getCurrentUserId === 'function' ? getCurrentUserId() : null;
                if (Number.isInteger(storedUserId)) {
                    userId = storedUserId;
                    console.log('[FEEDBACK-DEBUG] Got userId from helper:', userId);
                } else if (getCookie('userEmail')) {
                    userId = Number.parseInt(sessionStorage.getItem('userId')) || Number.parseInt(localStorage.getItem('user_id'));
                    console.log('[FEEDBACK-DEBUG] Got userId from storage:', userId);
                }
            }
            if (!Number.isInteger(userId)) {
                userId = 0;
                console.log('[FEEDBACK-DEBUG] Using guest userId:', userId);
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
                // Show buttons again on error
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
            
            // Show buttons again on error
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
        const stored = typeof getCurrentUserId === 'function' ? getCurrentUserId() : null;
        if (Number.isInteger(stored)) {
            return stored;
        }

        const sessionId = sessionStorage.getItem('userId');
        const queryId = new URLSearchParams(window.location.search).get('userId');
        const candidate = sessionId || queryId;
        const parsed = Number.parseInt(candidate, 10);
        return Number.isNaN(parsed) ? 0 : parsed;
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
    window.controller = window.diagnosticsController; // ✅ FIX: alias for onclick in index.html
    window.app = window.diagnosticsController; // ✅ FIX: alias for feedback onclick
    window.webSocketClient = new PredictionWebSocketClient();

    // Send heartbeat every 30 seconds
    setInterval(() => window.webSocketClient?.sendHeartbeat(), 30000);
});
