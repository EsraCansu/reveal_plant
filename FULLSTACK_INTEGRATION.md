# Fullstack Project - Backend & Frontend Entegrasyon

## 📋 Proje Yapısı

```
reveal_plant/
├── plant_village/              # Backend (Java Spring Boot)
│   ├── src/main/java/
│   │   └── plant_village/
│   │       ├── config/         # CORS, Security config
│   │       ├── controller/     # REST endpoints
│   │       ├── service/        # Business logic
│   │       ├── repository/     # Database
│   │       └── model/          # Entities
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                   # Frontend (React)
│   ├── src/
│   │   ├── components/        # UI components
│   │   ├── services/          # API clients
│   │   ├── pages/            # Pages
│   │   └── styles/           # CSS
│   ├── package.json
│   └── Dockerfile
│
├── ml-api/                    # ML API (FastAPI)
│   ├── app/
│   │   ├── main.py          # FastAPI app
│   │   └── schema.py        # Models
│   ├── model/               # TensorFlow model
│   └── requirements.txt
│
└── docker-compose.yml        # Multi-container orchestration
```

## 🔌 API Endpoints

### Backend (Java - Port 8080)

**Base URL:** `http://localhost:8080/api`

#### Predictions
```
POST   /api/predictions           - Tahmin yap
GET    /api/predictions           - Geçmiş tahminleri getir
GET    /api/predictions/{id}      - Tekil tahmin detayı
DELETE /api/predictions/{id}      - Tahmin sil
```

#### Users
```
POST   /api/auth/login            - Oturum aç
POST   /api/auth/register         - Kayıt ol
GET    /api/users/profile         - Profil bilgisi
PUT    /api/users/profile         - Profil güncelle
```

#### Health
```
GET    /api/health                - Servis durumu
```

### ML API (Python - Port 8000)

**Base URL:** `http://localhost:8000`

```
POST   /predict                   - Tahmin yap (görsel)
GET    /health                    - Sağlık kontrolü
GET    /classes                   - Sınıfları listele
```

## 🚀 Başlatma

### 1. Docker Compose ile (Önerilir)

```bash
docker-compose up --build
```

Hizmetler:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- ML API: http://localhost:8000

### 2. Local Development

#### Terminal 1: Backend
```bash
cd plant_village
mvn spring-boot:run
```

#### Terminal 2: ML API
```bash
cd ml-api
python -m uvicorn app.main:app --reload
```

#### Terminal 3: Frontend
```bash
cd frontend
npm install
npm start
```

## 🔗 Frontend → Backend Communication

### API Client Setup

**frontend/src/services/api.js**
```javascript
const API_BASE_URL = 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

### Example Service Call

**frontend/src/services/predictionService.js**
```javascript
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

## 🔐 CORS Configuration

Backend CORS yapılandırması:
```java
// plant_village/src/main/java/plant_village/config/WebConfig.java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000")
    .allowedMethods("GET", "POST", "PUT", "DELETE")
    .allowCredentials(true);
```

## 🗂️ Veri Akışı

```
React Component
    ↓
Upload Component (görsel seç)
    ↓
predictionService.getPrediction(file)
    ↓
axios POST /api/predictions
    ↓
Backend Controller (PredictionController)
    ↓
PredictionService (business logic)
    ↓
ML API Call (tahmin)
    ↓
PredictionRepository (veritabanı kaydet)
    ↓
Response JSON → Frontend
    ↓
Result Component (göster)
```

## 📦 Docker Compose

**docker-compose.yml**
```yaml
version: '3.8'

services:
  # Frontend
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      - REACT_APP_API_URL=http://localhost:8080/api

  # Backend
  backend:
    build: ./plant_village
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/plant_village
      - ML_API_URL=http://ml-api:8000

  # ML API
  ml-api:
    build: ./ml-api
    ports:
      - "8000:8000"

  # Database (optional)
  db:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=plant_village
```

## 🛠️ Development Workflow

1. **Değişiklik Yap**
   - React: `src/` klasörü
   - Java: `src/main/java/`
   - Python: `ml-api/app/`

2. **Auto-reload Etkin**
   - Frontend: npm start (HMR)
   - Backend: Spring DevTools
   - ML API: --reload flag

3. **Test**
   ```bash
   Frontend: npm test
   Backend: mvn test
   ```

4. **Deploy**
   ```bash
   docker-compose up --build
   ```

## 📝 Environment Variables

### Frontend (.env)
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Backend (application.properties)
```
spring.datasource.url=jdbc:mysql://localhost:3306/plant_village
spring.datasource.username=root
spring.datasource.password=root
ml.api.url=http://localhost:8000
```

### ML API (.env)
```
MODEL_PATH=/app/model/model.keras
```

## ✅ Checklist

Backend Setup:
- [ ] Java Spring Boot projesine CORS yapılandırması var
- [ ] `/api/predictions` endpoint'i var
- [ ] Veritabanı bağlantısı kurulu

Frontend Setup:
- [ ] React projesine axios kurulu
- [ ] `services/api.js` oluşturuldu
- [ ] `services/predictionService.js` oluşturuldu
- [ ] Upload ve Result komponentleri hazır

ML API:
- [ ] FastAPI servisi çalışıyor
- [ ] Model yüklendi
- [ ] `/predict` endpoint'i test edildi

Integration:
- [ ] Frontend → Backend iletişim test edildi
- [ ] CORS hataları yok
- [ ] Tahmin API'si çalışıyor

## 📚 Referanslar

- [React Documentation](https://react.dev)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Ant Design](https://ant.design)
- [Axios Documentation](https://axios-http.com)
