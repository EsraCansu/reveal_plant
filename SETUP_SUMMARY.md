# 🌱 Reveal Plant - Fullstack Kurulum Özeti

## ✅ Tamamlanan Yapı

### 1️⃣ **Frontend (React)**
```
frontend/
├── src/
│   ├── components/
│   │   ├── Upload.jsx          ← Görsel yükleme & drag-drop
│   │   └── Result.jsx          ← Tahmin sonuçları göster
│   ├── pages/
│   │   └── Home.jsx            ← Ana sayfa
│   ├── services/
│   │   ├── api.js              ← API client (axios)
│   │   ├── predictionService.js ← Tahmin API calls
│   │   └── userService.js      ← Kullanıcı API calls
│   ├── styles/
│   │   ├── upload.css
│   │   ├── result.css
│   │   └── home.css
│   ├── App.jsx
│   └── index.js
├── package.json                ← React, Ant Design, axios
├── Dockerfile                  ← Production image
└── README.md
```

**Özellikler:**
- ✓ Ant Design UI components
- ✓ Responsive layout
- ✓ Drag & drop file upload
- ✓ Image preview
- ✓ Progress bars (tahmin güven oranı)
- ✓ Top 5 tahmin gösterimi

---

### 2️⃣ **Backend (Java Spring Boot)**
```
plant_village/
├── src/main/java/plant_village/
│   ├── config/
│   │   └── WebConfig.java      ← CORS yapılandırması
│   ├── controller/
│   │   ├── PredictionController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── PredictionService.java
│   │   └── UserService.java
│   ├── repository/             ← JPA repositories
│   ├── model/                  ← JPA entities
│   └── exception/              ← Error handling
├── pom.xml                     ← Maven dependencies
└── Dockerfile
```

**Özellikler:**
- ✓ CORS enabled (localhost:3000)
- ✓ REST API endpoints (/api/predictions, /api/users)
- ✓ JPA/Hibernate ORM
- ✓ MySQL database
- ✓ ML API entegrasyonu

---

### 3️⃣ **ML API (Python FastAPI)**
```
ml-api/
├── app/
│   ├── main.py                 ← FastAPI app
│   └── schema.py               ← Pydantic models
├── model/
│   └── PlantVillage_Resnet101_FineTuning.keras
├── requirements.txt            ← TensorFlow, Keras, OpenCV
├── Dockerfile
├── QUICK_START.md
└── log_predictions.py          ← Veritabanı loglama
```

**Özellikler:**
- ✓ ResNet101 model
- ✓ 38 bitki hastalığı sınıfı
- ✓ Top 5 tahmin
- ✓ Health check endpoint
- ✓ Sağlık kontrollü docker

---

## 🔄 Frontend-Backend Akışı

```
┌─────────────────────────────────────────────────────────┐
│                    REACT FRONTEND                        │
│                   (Port 3000)                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Upload Component                                  │   │
│  │ - Drag & drop file                                │   │
│  │ - Select image                                    │   │
│  └───────────────┬──────────────────────────────────┘   │
│                  │                                        │
│  ┌──────────────▼──────────────────────────────────┐   │
│  │ predictionService.getPrediction(file)           │   │
│  │ POST /api/predictions (form-data)               │   │
│  └───────────────┬──────────────────────────────────┘   │
│                  │                                        │
│  ┌──────────────▼──────────────────────────────────┐   │
│  │ Result Component                                 │   │
│  │ - Show top prediction                           │   │
│  │ - Show top 5 predictions                        │   │
│  │ - Show confidence %                              │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                   HTTP/CORS
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                SPRING BOOT BACKEND                       │
│                  (Port 8080)                             │
│  ┌──────────────────────────────────────────────────┐   │
│  │ PredictionController                             │   │
│  │ POST /api/predictions                            │   │
│  │ GET  /api/predictions                            │   │
│  │ GET  /api/predictions/{id}                       │   │
│  └───────────────┬──────────────────────────────────┘   │
│                  │                                        │
│  ┌──────────────▼──────────────────────────────────┐   │
│  │ PredictionService                               │   │
│  │ - ML API'ye tahmin iste                         │   │
│  │ - Sonuçları işle                                 │   │
│  │ - Veritabanına kaydet                           │   │
│  └───────────────┬──────────────────────────────────┘   │
│                  │                                        │
│  ┌──────────────▼──────────────────────────────────┐   │
│  │ PredictionRepository (JPA)                       │   │
│  │ - prediction_log tablosu                        │   │
│  │ - prediction_details tablosu                    │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                      REST API
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  FASTAPI ML SERVER                       │
│                   (Port 8000)                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │ POST /predict                                    │   │
│  │ - Görsel al                                      │   │
│  │ - Preprocess et (224x224)                        │   │
│  │ - Model tahmin yap                              │   │
│  │ - Top 5 döndür                                  │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Kullanım

### Local Development (3 Terminal)

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
npm install  # İlk seferinde
npm start
```

### Docker Compose (1 Komut)

```bash
docker-compose up --build
```

Erişim:
- **Frontend:** http://localhost:3000
- **Backend:** http://localhost:8080
- **ML API:** http://localhost:8000
- **DB Admin:** http://localhost:3306 (mysql -u root)

---

## 🔌 API Endpoints

### Frontend → Backend

```
POST   /api/predictions              Yeni tahmin
GET    /api/predictions              Tüm tahminler
GET    /api/predictions/{id}         Tekil tahmin
DELETE /api/predictions/{id}         Tahmin sil
```

### Backend → ML API

```
POST   /predict                      Tahmin yap
GET    /health                       Sağlık kontrol
GET    /classes                      Sınıfları listele
```

---

## 📊 Veritabanı

**MySQL Tables:**

1. **prediction_log**
   - id, image_name, top_class_name, top_confidence, processing_time, created_at

2. **prediction_details**
   - id, prediction_log_id, class_name, confidence, confidence_percent, rank

---

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
ml.api.url=http://localhost:8000
```

### ML API (.env)
```
MODEL_PATH=./model/PlantVillage_Resnet101_FineTuning.keras
```

---

## ✅ Kontrol Listesi

- [x] React Frontend kuruldu
- [x] Spring Boot Backend kuruldu
- [x] FastAPI ML server kuruldu
- [x] CORS konfigürasyonu yapıldı
- [x] API services yazıldı
- [x] React components oluşturuldu
- [x] Docker images tanımlandı
- [x] docker-compose.yml hazırlandı
- [x] Environment variables dökümente edildi

---

## 📁 Dosya Kontrol Listesi

Frontend:
- ✓ frontend/src/services/api.js
- ✓ frontend/src/services/predictionService.js
- ✓ frontend/src/services/userService.js
- ✓ frontend/src/components/Upload.jsx
- ✓ frontend/src/components/Result.jsx
- ✓ frontend/src/pages/Home.jsx
- ✓ frontend/package.json
- ✓ frontend/Dockerfile

Backend:
- ✓ plant_village/config/WebConfig.java (CORS)
- ✓ plant_village/pom.xml (Maven dependencies)

ML API:
- ✓ ml-api/app/main.py
- ✓ ml-api/app/schema.py
- ✓ ml-api/requirements.txt
- ✓ ml-api/Dockerfile
- ✓ ml-api/log_predictions.py

---

## 🎯 Sonraki Adımlar

1. **Veritabanı Entegrasyonu**
   - Backend'de PredictionRepository ayarla
   - Tahmin sonuçlarını kaydet

2. **Authentication**
   - JWT token ekle
   - Login/Register endpoints

3. **Real-time Updates**
   - WebSocket ekle
   - Live prediction updates

4. **Cloud Deployment**
   - AWS/Azure container registry
   - Kubernetes orchestration

5. **Monitoring**
   - ELK stack (logs)
   - Prometheus (metrics)

---

## 📚 Dökümentasyon

- [SETUP_GUIDE.md](./SETUP_GUIDE.md) - Kurulum rehberi
- [FULLSTACK_INTEGRATION.md](./FULLSTACK_INTEGRATION.md) - Entegrasyon detayları
- [ml-api/QUICK_START.md](./ml-api/QUICK_START.md) - ML API kullanımı
- [frontend/README.md](./frontend/README.md) - Frontend kurulum

---

## 🆘 Sorun Giderme

### CORS Hatası
→ WebConfig.java'da allowedOrigins kontrol et

### ML API bağlantı hatası
→ FastAPI servisi çalışıyor mu? `curl http://localhost:8000/health`

### React build error
→ `npm install` ve `npm start` yeniden çalıştır

### Port conflict
→ Port değiştir veya process kill et: `lsof -i :8080`

---

**Kurulum tamamlandı! 🎉**

Şimdi test edilebilir:
1. http://localhost:3000 (Frontend)
2. Görsel yükle
3. Tahmin sonuçlarını gör
