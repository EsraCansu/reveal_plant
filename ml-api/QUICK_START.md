# Reveal Plant API - Hızlı Başlangıç

## 📋 Gereksinimler

- Python 3.10+
- Conda ortamı: `myenv`
- Model dosyası: `ml-api/model/PlantVillage_Resnet101_FineTuning.keras`

---

## 🚀 ADIM 1: Bağımlılıkları Yükle

```bash
cd ml-api
pip install -r requirements.txt
```

✓ **Kontrol et:**
```bash
pip list | findstr fastapi uvicorn tensorflow opencv requests
```

---

## 🚀 ADIM 2: API'yi Başlat

**Terminal 1'de:**//conda prompt

```bash
conda activate myenv
cd C:\Users\esracansu\OneDrive\Belgeler\GitHub\reveal_plant\ml-api
python -m uvicorn app.main:app --reload
```

**Beklenen çıktı:**
```
INFO:     Uvicorn running on http://127.0.0.1:8000
INFO:     Application startup complete
INFO:app.main:Model yüklendi: ...PlantVillage_Resnet101_FineTuning.keras
INFO:app.main:Model durumu: Yüklendi ✓
```

---

## ✅ ADIM 3: API Testleri

**Terminal 2'de** aşağıdaki komutları çalıştır:

### Test 1: Health Check
```bash
curl.exe http://localhost:8000/health
```

**Beklenen yanıt:**
```json
{"status":"healthy","model_loaded":true,"version":"1.0.0"}
```

---

### Test 2: Tahmin Yap (Uzum Görseli)
```bash
curl.exe -X POST -F "file=@C:/Users/esracansu/OneDrive/Masaüstü/transfer_function/uzum.jpg" http://localhost:8000/predict
```

**Beklenen yanıt:**
```json
{
  "success": true,
  "image_name": "uzum.jpg",
  "top_prediction": {
    "class_name": "Grape___healthy",
    "confidence": 0.95,
    "confidence_percent": 95.0
  },
  "all_predictions": [
    {
      "class_name": "Grape___healthy",
      "confidence": 0.95,
      "confidence_percent": 95.0
    },
    ...
  ],
  "processing_time": 0.234
}
```

---

### Test 3: Sınıfları Listele
```bash
curl.exe http://localhost:8000/classes
```

**Yanıt:**
```json
{
  "total_classes": 38,
  "classes": ["Apple___Apple_scab", "Apple___Black_rot", ..., "Tomato___healthy"]
}
```

---

### Test 4: Python ile Otomatik Test
```bash
pip install requests
python test_api.py
```

**Çıktı örneği:**
```
============================================================
REVEAL PLANT - API TEST SUITE
============================================================

✓ HEALTH CHECK TEST
Status: 200 - Model Yüklendi ✓

✓ CLASSES TEST
Total Classes: 38

✓ PREDICTION TEST
✓ Tahmin başarılı!
  Görsel: uzum.jpg
  En yüksek tahmin: Grape___healthy
  Güven: 95.00%
  İşlem süresi: 0.234s

  Top 5 tahmin:
    1. Grape___healthy: 95.00%
    2. Grape___Black_rot: 4.00%
    ...

============================================================
TEST ÖZETI
============================================================
Health Check: ✓ PASS
Classes List: ✓ PASS
Prediction: ✓ PASS
============================================================
```

---

## 🌐 ADIM 4: Web Arayüzü (Swagger UI)

Tarayıcıda aç: **http://localhost:8000/docs**

Oradan:
1. **POST /predict** kısmını aç
2. **"Try it out"** tıkla
3. Görsel seç
4. **"Execute"** tıkla

---

## 📊 Farklı Görseller ile Test

```bash
# Apple
curl.exe -X POST -F "file=@test_images/dom.jpg" http://localhost:8000/predict

# Tomato
curl.exe -X POST -F "file=@test_images/domates.jpg" http://localhost:8000/predict

# Pepper
curl.exe -X POST -F "file=@test_images/biber.jpg" http://localhost:8000/predict
```

---

## 🔴 API'yi Durdur

Terminal 1'de:
```
CTRL + C
```

---

## 📝 Model Bilgileri

| Bilgi | Değer |
|-------|-------|
| **Model Adı** | PlantVillage ResNet101 Fine-Tuning |
| **Format** | .keras |
| **Input Size** | 224x224x3 (RGB) |
| **Çıkış Sınıfları** | 38 bitki hastalığı |
| **Framework** | TensorFlow/Keras |
| **Processing Time** | ~0.2-5 saniye |

---

## 🐛 Sorun Giderme

### Port kullanımda
```bash
python -m uvicorn app.main:app --port 8001 --reload
```

### Model bulunamadı
```
✓ ml-api/model/PlantVillage_Resnet101_FineTuning.keras var mı?
```

### Requests modülü yok
```bash
pip install requests
```

---

## ✨ Başarılı Test Kontrol Listesi

- [ ] API başlatıldı (Terminal 1)
- [ ] Health check 200 OK
- [ ] Tahmin başarılı oldu
- [ ] Top prediction görüldü
- [ ] Processing time < 10 saniye
- [ ] test_api.py tüm testleri geçti

---

## 🎯 Sonraki Adımlar

✅ **Tamamlandı:**
- FastAPI REST servisi
- Model entegrasyonu
- Local testing

📋 **Gelecek:**
1. Web Frontend (HTML/React)
2. Docker containerization
3. Cloud deployment (Azure/AWS)
4. Veritabanı entegrasyonu
