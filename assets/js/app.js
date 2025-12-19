/**
 * DiagnosticsController - UI ve Akış Kontrolü
 */
class DiagnosticsController {
    constructor() {
        this.uploadedImage = null;
        this.selectedMode = null;
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        const uploadBtn = document.getElementById('uploadBtn');
        const fileInput = document.getElementById('fileInput');
        const uploadArea = document.querySelector('.upload-area');

        uploadBtn?.addEventListener('click', () => fileInput?.click());
        fileInput?.addEventListener('change', (e) => this.handleFileUpload(e));

        uploadArea?.addEventListener('dragover', (e) => {
            e.preventDefault();
            e.currentTarget.style.backgroundColor = 'rgba(103, 166, 75, 0.15)';
        });

        uploadArea?.addEventListener('dragleave', (e) => {
            e.currentTarget.style.backgroundColor = 'transparent';
        });

        uploadArea?.addEventListener('drop', (e) => {
            e.preventDefault();
            const file = e.dataTransfer.files[0];
            if (file && file.type.startsWith('image/')) this.processImage(file);
        });
    }

    handleFileUpload(event) {
        const file = event.target.files[0];
        if (file) this.processImage(file);
    }

    processImage(file) {
        const reader = new FileReader();
        reader.onload = (event) => {
            this.uploadedImage = event.target.result; // Base64 veri
            const imagePreview = document.getElementById('imagePreview');
            if (imagePreview) {
                imagePreview.innerHTML = `<img src="${this.uploadedImage}" class="preview-image" style="max-width:100%">`;
            }
            
            // ANALİZİ BAŞLAT: WebSocket üzerinden backend'e gönder
            if (window.webSocketClient && window.webSocketClient.connected) {
                const base64Content = this.uploadedImage.split(',')[1]; // Sadece data kısmını al
                window.webSocketClient.sendPrediction(1, base64Content, "User analysis request");
            } else {
                alert("Backend bağlantısı hazır değil! Lütfen bekleyin.");
            }
        };
        reader.readAsDataURL(file);
    }

    selectMode(mode) {
        console.log("Mod seçildi:", mode);
        this.selectedMode = mode;
        this.showStep('step-upload');
    }

    showStep(stepId) {
        document.querySelectorAll('.step-container').forEach(s => s.classList.remove('active'));
        document.getElementById(stepId)?.classList.add('active');
    }
}

/**
 * WebSocket Client - Backend ile Gerçek Zamanlı İletişim
 */
class PredictionWebSocketClient {
    constructor() {
        this.userId = 1; // Şimdilik sabit, login sonrası dinamikleşecek
        this.connected = false;
        this.client = null;
        this.connect();
    }

    connect() {
        // ADRES DÜZELTİLDİ: Backend WebSocketConfig ile uyumlu
        const socket = new SockJS('http://127.0.0.1:8080/ws-predictions');
        this.client = Stomp.over(socket);

        this.client.connect({}, (frame) => {
            console.log('WebSocket Bağlandı!');
            this.connected = true;
            this.subscribeToChannels();
        }, (error) => {
            console.error('WebSocket Hatası:', error);
            this.connected = false;
            setTimeout(() => this.connect(), 5000); // Koptukça dene
        });
    }

    subscribeToChannels() {
        // Backend'den gelecek tahmin sonucunu dinle
        this.client.subscribe(`/user/${this.userId}/queue/predictions`, (message) => {
            const response = JSON.parse(message.body);
            console.log("Tahmin Geldi:", response);
            alert("Analiz Tamamlandı: " + response.plantName);
        });
    }

    sendPrediction(plantId, imageBase64, description) {
        const request = {
            plantId: plantId,
            imageBase64: imageBase64,
            description: description
        };
        // YOL DÜZELTİLDİ: Başına /app eklendi
        this.client.send(`/app/predict/${this.userId}`, {}, JSON.stringify(request));
    }
}

// BAŞLATICI - window.controller ismi index.html ile eşlendi
document.addEventListener('DOMContentLoaded', () => {
    window.controller = new DiagnosticsController(); 
    window.webSocketClient = new PredictionWebSocketClient();
});