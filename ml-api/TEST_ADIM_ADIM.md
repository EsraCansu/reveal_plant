# ML-API Test Adım Adım Rehberi

Model: **PlantVillage ResNet101 Fine-Tuning**
API Framework: **FastAPI**
Python Versiyonu: **3.10+**

---

## ✅ ÖN KONTROL

Model dosyasının doğru yolda olup olmadığını kontrol et:

```
ml-api/
└── model/
    └── PlantVillage_Resnet101_FineTuning.keras  ✓ OLMALI
```

Eğer dosya adı farklıysa, `app/main.py` dosyasındaki bu satırı güncelle:
```python
MODEL_PATH = Path(__file__).parent.parent / "model" / "PlantVillage_Resnet101_FineTuning.keras"
```

---

## ADIM 1️⃣: Bağımlılıkları Yükle

```bash
cd ml-api
pip install -r requirements.txt
```

**Kontrol et:**
```bash
pip list | findstr tensorflow fastapi uvicorn opencv
```

Çıktısı olmalı:
```
fastapi
opencv-python
tensorflow
uvicorn
```

---

## ADIM 2️⃣: API'yi Başlat

**Terminal 1'de** (API sunucusu):
```bash
cd ml-api
python -m uvicorn app.main:app --reload --port 8000
```

**Beklenen çıktı:**
```
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
INFO:     Application startup complete
INFO:     Model yüklendi: C:\Users\...\ml-api\model\PlantVillage_Resnet101_FineTuning.keras
INFO:     Model durumu: Yüklendi ✓
```

---

## ADIM 3️⃣: API Sağlık Kontrolü

**Terminal 2'de** (başka bir terminal açın):

```bash
curl http://localhost:8000/health
```

**Beklenen yanıt:**
```json
{
  "status": "healthy",
  "model_loaded": true,
  "version": "1.0.0"
}
```

---

## ADIM 4️⃣: Sınıfları Listele

```bash
curl http://localhost:8000/classes
```

**Beklenen yanıt:**
```json
{
  "total_classes": 38,
  "classes": [
    "Apple___Apple_scab",
    "Apple___Black_rot",
    ...
    "Tomato___healthy"
  ]
}
```

---

## ADIM 5️⃣: Test Görseli ile Tahmin Yap

### Seçenek A: Command Line (curl)

**Test görselinizin yolu:**
```
C:\Users\esracansu\OneDrive\Masaüstü\transfer_function\uzum.jpg
```

```bash
curl -X POST -F "file=@C:/Users/esracansu/OneDrive/Masaüstü/transfer_function/uzum.jpg" http://localhost:8000/predict
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
    {
      "class_name": "Grape___Black_rot",
      "confidence": 0.04,
      "confidence_percent": 4.0
    },
    ...
  ],
  "processing_time": 0.234
}
```

### Seçenek B: Python Script

```bash
python test_api.py
```

Bu otomatik olarak:
- ✓ Health check
- ✓ Classes listesi
- ✓ Tahmin testi

yapacak ve güzel formatlanmış çıktı verecek.

### Seçenek C: Swagger UI (Arayüz)

Tarayıcıda açın: **http://localhost:8000/docs**

Oradan:
1. **POST /predict** kısmını aç
2. **"Try it out"** tıkla
3. Görsel seç (jpeg/png)
4. **"Execute"** tıkla
5. Sonucu gör

---

## ADIM 6️⃣: Birden Fazla Görsel Test Et

Test görselleri hazırla:

```
test_images/
├── dom.jpg          (elma)
├── uzum.jpg         (üzüm)
├── domates.jpg      (domates)
└── biber.jpg        (biber)
```

Her bir görsel için:
```bash
curl -X POST -F "file=@test_images/dom.jpg" http://localhost:8000/predict | python -m json.tool
```

---

## ADIM 7️⃣: Hata Test Et

### Yanlış Dosya Türü

```bash
curl -X POST -F "file=@test.txt" http://localhost:8000/predict
```

**Beklenen hata:**
```json
{
  "detail": "Sadece JPG, JPEG, PNG dosyaları kabul edilir"
}
```

### Model Olmadan

`model/` klasörünü geçici olarak sil ve başla:

```bash
move model model_backup
python -m uvicorn app.main:app --reload
```

```bash
curl http://localhost:8000/health
```

**Beklenen yanıt:**
```json
{
  "status": "unhealthy",
  "model_loaded": false,
  "version": "1.0.0"
}
```

---

## ADIM 8️⃣: Python ile Otomatik Test

```bash
python test_api.py
```

Çıktı örneği:
```
============================================================
REVEAL PLANT - API TEST SUITE
============================================================

============================================================
🏥 HEALTH CHECK TEST
============================================================
Status: 200
Response: {
  "status": "healthy",
  "model_loaded": true,
  "version": "1.0.0"
}

============================================================
📋 CLASSES TEST
============================================================
Status: 200
Total Classes: 38
First 5 Classes: ['Apple___Apple_scab', 'Apple___Black_rot', ...]

============================================================
🔮 PREDICTION TEST
============================================================
Status: 200
✓ Tahmin başarılı!
  Görsel: uzum.jpg
  En yüksek tahmin: Grape___healthy
  Güven: 95.00%
  İşlem süresi: 0.234s

  Top 5 tahmin:
    1. Grape___healthy: 95.00%
    2. Grape___Black_rot: 4.00%
    3. Grape___Esca_(Black_Measles): 0.99%
    4. Grape___Leaf_blight_(Isariopsis_Leaf_Spot): 0.01%
    5. Orange___Haunglongbing_(Citrus_greening): 0.00%

============================================================
TEST ÖZETI
============================================================
Health Check: ✓ PASS
Classes List: ✓ PASS
Prediction: ✓ PASS
============================================================
```

---

## 🔍 LOG KONTROL

API Terminal'de çalışan logları kontrol et:

```
INFO:     Application startup complete
INFO:     API başlatılıyor...
INFO:     Model yüklendi: .../PlantVillage_Resnet101_FineTuning.keras
INFO:     Çıkış sınıfları: 38
INFO:     Model durumu: Yüklendi ✓
INFO:     127.0.0.1:12345 "POST /predict HTTP/1.1" 200 OK
INFO:     Tahmin yapılıyor: uzum.jpg
INFO:     Tahmin tamamlandı - Top: Grape___healthy (95.00%)
```

---

## ⚡ PERFORMANS TEST

İşlem süresini ölçmek:

```bash
python -c "
import requests
import time
import json

for i in range(5):
    start = time.time()
    r = requests.post('http://localhost:8000/predict', 
                      files={'file': open('test_images/uzum.jpg', 'rb')})
    elapsed = time.time() - start
    data = r.json()
    print(f'{i+1}. {data[\"processing_time\"]:.3f}s')
"
```

**Beklenen:** ~0.2-0.5 saniye

---

## 🐛 SORUN GİDERME

### Problem: Model bulunamadı
```
FileNotFoundError: Model dosyası bulunamadı
```

**Çözüm:**
```bash
# Model dosyasının yolunu kontrol et
ls -la ml-api/model/
# Veya Windows:
dir ml-api\model\
```

### Problem: Port kullanımda
```
OSError: [Errno 48] Address already in use
```

**Çözüm:**
```bash
# Başka bir port kullan:
python -m uvicorn app.main:app --port 8001
```

### Problem: TensorFlow hatası
```
ImportError: cannot import name 'keras'
```

**Çözüm:**
```bash
pip install --upgrade tensorflow
```

### Problem: cv2 (OpenCV) hatası
```
ImportError: opencv_python not installed
```

**Çözüm:**
```bash
pip install opencv-python-headless  # Veya
pip install opencv-python
```

---

## ✨ BAŞARILI TEST KONTROL LİSTESİ

- [ ] Bağımlılıklar yüklendi
- [ ] API başlatıldı (Port 8000)
- [ ] Health check 200 OK
- [ ] Classes listesi 38 sınıf gösteriyor
- [ ] Test görseli başarıyla tahmin yapıldı
- [ ] Top prediction bulundu
- [ ] Processing time < 1 saniye
- [ ] Swagger UI çalışıyor (http://localhost:8000/docs)
- [ ] Errorlar düzgün handle ediliyor
- [ ] Logs API terminalinde görünüyor

---

## 📊 NEXT STEPS

✅ API'nin yerel çalışması tamamlandı

Sonraki adımlar:
1. **Docker'da çalıştırma** (isteğe bağlı)
2. **Üretim sunucusuna dağıtma** (AWS/Azure/Heroku)
3. **Web arayüzü ile entegrasyon** (HTML/React frontend)
4. **Veritabanı entegrasyonu** (tahmin geçmişi)

