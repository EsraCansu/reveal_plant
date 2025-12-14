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
    processImage(file) {
        const reader = new FileReader();
        reader.onload = (event) => {
            this.uploadedImage = event.target.result;
            const imagePreview = document.getElementById('imagePreview');
            if (imagePreview) {
                imagePreview.innerHTML = `<img src="${this.uploadedImage}" class="preview-image" alt="Uploaded plant image">`;
            }
            setTimeout(() => this.showResults(), 500);
        };
        reader.readAsDataURL(file);
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

        const diagnosis = {
            id: Date.now(),
            mode: this.selectedMode,
            name: this.selectedMode === 'identify' ? 'Tomato Plant' : 'Early Blight',
            result: this.selectedMode === 'identify' ? 'Tomato Plant (Solanum lycopersicum)' : 'Early Blight - Moderate',
            confidence: this.selectedMode === 'identify' ? 94.5 : 87.2,
            image: this.uploadedImage,
            date: new Date().toISOString(),
            details: this.selectedMode === 'identify' 
                ? 'Identified as Tomato Plant with high confidence'
                : 'Detected Early Blight disease. Recommend immediate treatment'
        };

        diagnoses.push(diagnosis);
        localStorage.setItem('diagnoses', JSON.stringify(diagnoses));
    }

    /**
     * Get identification result template
     */
    getIdentificationResult() {
        return `
            <div class="text-center">
                <img src="${this.uploadedImage}" class="preview-image" alt="Analyzed plant">
                <h3 class="mt-3">Plant Identification Result</h3>
                <div class="alert alert-info mt-3">
                    <h5>Identified Plant: <strong>Tomato Plant</strong></h5>
                    <p class="mb-2"><small>Confidence: <strong>94.5%</strong></small></p>
                    <hr>
                    <p><strong>Scientific Name:</strong> Solanum lycopersicum</p>
                    <p><strong>Family:</strong> Solanaceae</p>
                    <p><strong>Care Tips:</strong></p>
                    <ul class="text-start">
                        <li>Water regularly but keep soil well-drained</li>
                        <li>Requires 6-8 hours of direct sunlight daily</li>
                        <li>Best grown in warm conditions (20-25°C)</li>
                        <li>Support with stakes as plant grows</li>
                    </ul>
                </div>
            </div>
        `;
    }

    /**
     * Get disease detection result template
     */
    getDiseaseResult() {
        return `
            <div class="text-center">
                <img src="${this.uploadedImage}" class="preview-image" alt="Analyzed plant">
                <h3 class="mt-3">Disease Detection Result</h3>
                <div class="alert alert-warning mt-3">
                    <h5><i class="fas fa-exclamation-triangle"></i> Potential Disease Detected</h5>
                    <p class="mb-2"><small>Confidence: <strong>87.2%</strong></small></p>
                    <hr>
                    <p><strong>Disease:</strong> Early Blight</p>
                    <p><strong>Severity:</strong> Moderate</p>
                    <p><strong>Recommendations:</strong></p>
                    <ul class="text-start">
                        <li>Remove infected leaves immediately</li>
                        <li>Improve air circulation around the plant</li>
                        <li>Apply fungicide treatment weekly</li>
                        <li>Avoid watering leaves - water at soil level</li>
                        <li>Monitor for disease spread</li>
                    </ul>
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

// Initialize controller when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.diagnosticsController = new DiagnosticsController();
});