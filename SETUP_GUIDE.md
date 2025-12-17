# Reveal Plant - Fullstack Setup

## 🎯 Amaç

Java Spring Boot Backend + React Frontend + FastAPI ML servisi entegrasyonu

## 📁 Proje Yapısı (Güncellenmiş)

```
reveal_plant/
│
├── 🖥️  frontend/                    # React (Port 3000)
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/              # API calls (predictionService, userService)
│   │   └── styles/
│   ├── package.json
│   └── Dockerfile
│
├── 🔧 plant_village/               # Java Spring Boot Backend (Port 8080)
│   ├── src/main/java/
│   │   └── plant_village/
│   │       ├── controller/        # REST API endpoints
│   │       ├── service/           # Business logic
│   │       ├── repository/        # Database (JPA)
│   │       ├── model/             # Entities
│   │       └── config/            # CORS setup (WebConfig.java)
│   ├── pom.xml
│   └── Dockerfile
│
├── 🤖 ml-api/                      # FastAPI ML Service (Port 8000)
│   ├── app/
│   │   ├── main.py               # FastAPI app
│   │   └── schema.py             # Pydantic models
│   ├── model/
│   │   └── PlantVillage_Resnet101_FineTuning.keras
│   ├── requirements.txt
│   └── Dockerfile
│
└── 📦 docker-compose.yml           # Multi-container setup
```

## 🚀 Başlatma Seçenekleri

### Seçenek 1: Docker Compose (Üretim)

```bash
docker-compose up --build
```

Erişim:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- ML API: http://localhost:8000
- Database: localhost:3306

### Seçenek 2: Local Development

**Terminal 1 - ML API:**
```bash
cd ml-api
conda activate myenv
python -m uvicorn app.main:app --reload
```

**Terminal 2 - Backend:**
```bash
cd plant_village
mvn spring-boot:run
```

**Terminal 3 - Frontend:**
```bash
cd frontend
npm install
npm start
```

## 🔌 API Entegrasyonu

### Frontend → Backend

**API Client Ayarı:**
```javascript
// frontend/src/services/api.js
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

**Service Örneği:**
```javascript
// frontend/src/services/predictionService.js
export const predictionService = {
  getPrediction: async (imageFile) => {
    const formData = new FormData();
    formData.append('file', imageFile);
    
    const response = await apiClient.post('/predictions', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  }
};
```

### Backend CORS Konfigürasyonu

```java
// plant_village/src/main/java/plant_village/config/WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### Backend → ML API

Backend, tahmin isteklerini ML API'ye iletir:
```java
// plant_village/src/main/java/plant_village/service/PredictionServiceImpl.java
String mlApiUrl = environment.getProperty("ml.api.url");
// POST /predict çağrısı
```

## 📊 Veri Akışı

```
1. Kullanıcı görseli yükler
   ↓
2. React Component (Upload.jsx)
   ↓
3. predictionService.getPrediction(file)
   ↓
4. POST http://localhost:8080/api/predictions
   ↓
5. Spring Boot Backend
   └─ PredictionController.predict()
   ↓
6. Backend → ML API POST /predict
   ├─ Model tahmin yapıyor
   ↓
7. Backend → Database
   └─ prediction_log tablosuna kaydediyor
   ↓
8. Response JSON → Frontend
   ↓
9. Result Component (göster)
```

## 🗄️ Veritabanı Şeması

**prediction_log Tablosu:**
```sql
CREATE TABLE prediction_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  image_name VARCHAR(255),
  top_class_name VARCHAR(255),
  top_confidence DECIMAL(5,4),
  processing_time DECIMAL(10,3),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**prediction_details Tablosu:**
```sql
CREATE TABLE prediction_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prediction_log_id BIGINT,
  class_name VARCHAR(255),
  confidence DECIMAL(5,4),
  confidence_percent DECIMAL(5,2),
  rank INT,
  FOREIGN KEY (prediction_log_id) REFERENCES prediction_log(id)
);
```

## 🔑 Environment Variables

### Frontend (.env)
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Backend (application.properties)
```
spring.datasource.url=jdbc:mysql://localhost:3306/plant_village
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
ml.api.url=http://localhost:8000
cors.allowed-origins=http://localhost:3000
```

### ML API (.env)
```
MODEL_PATH=./model/PlantVillage_Resnet101_FineTuning.keras
```

## ✅ Sağlık Kontrolleri

```bash
# Frontend
curl http://localhost:3000

# Backend
curl http://localhost:8080/api/health

# ML API
curl http://localhost:8000/health

# Database
mysql -h localhost -u root -p plant_village
```

## 🛠️ Geliştirme Talimatları

### Yeni Endpoint Eklemek

1. **Backend Controller:**
```java
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    @PostMapping
    public ResponseEntity<?> predict(@RequestParam("file") MultipartFile file) {
        // ...
    }
}
```

2. **Frontend Service:**
```javascript
export const predictionService = {
    getPrediction: async (imageFile) => {
        // ...
    }
};
```

3. **Frontend Component:**
```javascript
const [result, setResult] = useState(null);
const handleUpload = async (file) => {
    const response = await predictionService.getPrediction(file);
    setResult(response);
};
```

## 📝 Dosya Kontrol Listesi

Kurulumun tamamlanıp tamamlanmadığını kontrol et:

**Frontend:**
- [ ] `frontend/src/services/api.js` - API client
- [ ] `frontend/src/services/predictionService.js` - Tahmin servisi
- [ ] `frontend/src/components/Upload.jsx` - Upload componenti
- [ ] `frontend/src/components/Result.jsx` - Sonuç componenti
- [ ] `frontend/src/pages/Home.jsx` - Ana sayfa
- [ ] `frontend/package.json` - Bağımlılıklar

**Backend:**
- [ ] `plant_village/src/main/java/plant_village/config/WebConfig.java` - CORS
- [ ] `plant_village/src/main/java/plant_village/controller/PredictionController.java` - Endpoints
- [ ] `plant_village/src/main/java/plant_village/service/PredictionService.java` - Business logic

**ML API:**
- [ ] `ml-api/app/main.py` - FastAPI app
- [ ] `ml-api/model/PlantVillage_Resnet101_FineTuning.keras` - Model dosyası

**Docker:**
- [ ] `docker-compose.yml` - Servis tanımları
- [ ] `frontend/Dockerfile` - React build
- [ ] `plant_village/Dockerfile` - Java build
- [ ] `ml-api/Dockerfile` - Python build

## 🐛 Sorun Giderme

### CORS Hatası
```
Access to XMLHttpRequest blocked by CORS policy
```
→ WebConfig.java'da allowedOrigins kontrol et

### ML API Bağlanamaması
```
Connection refused: localhost:8000
```
→ `python -m uvicorn app.main:app --reload` çalışıyor mu?

### Database Bağlantısı
```
No suitable driver found for jdbc:mysql
```
→ `mvn dependency:resolve` çalıştır

### Port Kullanımda
```
Port 8080 is already in use
```
→ `lsof -i :8080` ile process bulup `kill` et

## 📚 İlgili Dosyalar

- [FULLSTACK_INTEGRATION.md](./FULLSTACK_INTEGRATION.md) - Detaylı entegrasyon rehberi
- [ml-api/QUICK_START.md](./ml-api/QUICK_START.md) - ML API test rehberi
- [frontend/README.md](./frontend/README.md) - Frontend kurulum

## 🎓 Sonraki Adımlar

1. **Authentication** → JWT token ekle
2. **Real-time Updates** → WebSocket ekle
3. **Image Storage** → Cloud storage (AWS S3) ekle
4. **Analytics** → Dashboard ekle
5. **Mobile App** → React Native
