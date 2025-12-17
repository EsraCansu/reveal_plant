# Reveal Plant - ML API

Bitki hastalığı tespiti için ResNet101 modeline dayanan REST API

## 📁 Dosya Yapısı

```
ml-api/
├── model/
│   └── model.keras          # Eğitilmiş ResNet101 modeli
│
├── app/
│   ├── main.py             # FastAPI uygulaması
│   └── schema.py           # Pydantic şemaları
│
├── requirements.txt         # Bağımlılıklar
├── test_api.py             # API test dosyası
└── README.md               # Bu dosya
```

## 🚀 Başlangıç

### 1. Bağımlılıkları Yükle

```bash
pip install -r requirements.txt
```

### 2. Modeli Ekle

Kaggle'dan indirilen `model.keras` dosyasını `model/` klasörüne koy:

```bash
# Linux/Mac
cp /path/to/model.keras model/

# Windows
copy C:\path\to\model.keras model\
```

### 3. API'yi Başlat

```bash
python -m uvicorn app.main:app --reload
```

API şu adresler üzerinde çalışacak:
- **API**: http://localhost:8000
- **Docs (Swagger UI)**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## 📡 API Endpoints

### 1. Health Check
```bash
curl http://localhost:8000/health
```

**Yanıt:**
```json
{
  "status": "healthy",
  "model_loaded": true,
  "version": "1.0.0"
}
```

### 2. Sınıflar Listesi
```bash
curl http://localhost:8000/classes
```

**Yanıt:**
```json
{
  "total_classes": 38,
  "classes": ["Apple___Apple_scab", "Apple___Black_rot", ...]
}
```

### 3. Tahmin Yap
```bash
curl -X POST \
  -F "file=@/path/to/image.jpg" \
  http://localhost:8000/predict
```

**Yanıt:**
```json
{
  "success": true,
  "image_name": "dom.jpg",
  "top_prediction": {
    "class_name": "Apple___healthy",
    "confidence": 0.95,
    "confidence_percent": 95.0
  },
  "all_predictions": [
    {
      "class_name": "Apple___healthy",
      "confidence": 0.95,
      "confidence_percent": 95.0
    },
    {
      "class_name": "Apple___Apple_scab",
      "confidence": 0.04,
      "confidence_percent": 4.0
    },
    {
      "class_name": "Apple___Black_rot",
      "confidence": 0.01,
      "confidence_percent": 1.0
    }
  ],
  "processing_time": 0.234
}
```

## 🧪 Testler

### API Test Dosyasını Çalıştır

```bash
# API'nin çalışıyor olduğundan emin ol (başka bir terminalden)
python -m uvicorn app.main:app --reload

# Başka bir terminalden:
python test_api.py
```

### Manuel Test (Python)

```python
import requests

# Sağlık kontrolü
response = requests.get("http://localhost:8000/health")
print(response.json())

# Tahmin yap
with open("test_images/dom.jpg", "rb") as f:
    files = {"file": ("dom.jpg", f, "image/jpeg")}
    response = requests.post("http://localhost:8000/predict", files=files)
    print(response.json())
```

### Swagger UI ile Test

1. http://localhost:8000/docs adresine git
2. **POST /predict** kısmını aç
3. "Try it out" butonuna tıkla
4. Görsel yükle ve "Execute" tıkla

## 🔄 İş Akışı

```
1. Kaggle'da Model Eğitildi
   └─ .keras formatında kaydedildi
   
2. Model ml-api/model/ klasörüne eklendi
   
3. FastAPI Servisi Başlatıldı
   └─ app/main.py çalışıyor
   
4. Görsel POST isteği ile gönderiliyor
   ├─ Görsel alınıyor
   ├─ Ön işlenir (224x224, normalize)
   └─ Model tahmin yapıyor
   
5. Tahmin sonucu JSON olarak döndürülüyor
   └─ top_prediction + all_predictions
```

## 📊 Model Bilgileri

- **Type**: ResNet101
- **Framework**: TensorFlow/Keras
- **Input Size**: 224x224x3
- **Output Classes**: 38 (Bitki hastalıkları)
- **Format**: .keras

## ⚙️ Konfigürasyon

`app/main.py` dosyasında değiştirebileceğiniz ayarlar:

```python
# Model yolu
MODEL_PATH = Path(__file__).parent.parent / "model" / "model.keras"

# Sınıflar listesi
CLASS_NAMES = [...]

# Server ayarları (en altta)
uvicorn.run(
    "main:app",
    host="0.0.0.0",  # Bağlantı adresi
    port=8000,       # Port
    reload=True      # Dosya değişikliğinde otomatik reload
)
```

## 🐳 Docker ile Çalıştırma (Opsiyonel)

```dockerfile
FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0"]
```

```bash
docker build -t reveal-plant-api .
docker run -p 8000:8000 reveal-plant-api
```

## 🔍 Sorun Giderme

### Model yüklenemedi
```
ERROR: Model bulunamadı: /path/to/model/model.keras
```
**Çözüm:** `model.keras` dosyasını `ml-api/model/` klasörüne koy

### Port kullanımda
```
ERROR: Address already in use
```
**Çözüm:** Port değiştir: `--port 8001`

### TensorFlow Hataları
```
ERROR: cannot import name 'keras'
```
**Çözüm:** `pip install --upgrade tensorflow`

## 📝 Lisans

MIT License

## 👨‍💻 Geliştirici

Reveal Plant - Plant Disease Detection API
