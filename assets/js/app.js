/**
 * DiagnosticsController
 * Handles all user interactions and workflow logic
 */
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
            const formData = new FormData();
            formData.append('image', file);
            formData.append('mode', this.selectedMode === 'identify' ? 'identify-plant' : 'detect-disease');

            const response = await fetch('/api/predict', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error('Prediction failed');
            }

            const result = await response.json();
            this.predictionResult = result; // Sonucu sakla
            setTimeout(() => this.showResults(), 500);
        } catch (error) {
            console.error('Backend error:', error);
            alert('Analysis failed. Please try again.');
            this.goBack('step-mode');
        }
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
     * ✅ DÜZELTME: Backend'den gelen gerçek veriyle identification sonucu göster
     */
    getIdentificationResult() {
        const result = this.predictionResult || {};
        const predictedClass = result.predicted_class || 'Unknown Plant';
        const confidence = result.confidence ? (result.confidence * 100).toFixed(1) : '0';
        
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
                                `<li>${p.class_name}: ${(p.probability * 100).toFixed(1)}%</li>`
                            ).join('')}
                        </ul>
                    ` : ''}
                </div>
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
        const socket = new SockJS('http://localhost:8080/ws/predictions');
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
    window.webSocketClient = new PredictionWebSocketClient();

    // Send heartbeat every 30 seconds
    setInterval(() => window.webSocketClient?.sendHeartbeat(), 30000);
});
