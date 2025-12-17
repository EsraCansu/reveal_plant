"""
Reveal Plant - REST API for Plant Disease Detection
Model: ResNet101 Fine-Tuning (PlantVillage)
"""

import os
import time
import logging
import numpy as np
import cv2
from pathlib import Path
from typing import Optional
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from .schema import PredictionResponse, PredictionResult, HealthResponse

# Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI uygulaması
app = FastAPI(
    title="Reveal Plant API",
    description="Bitki hastalığı tespiti - ResNet101 Fine-Tuning Modeli",
    version="1.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Model ve konfigürasyon
MODEL_PATH = Path(__file__).parent.parent / "model" / "PlantVillage_Resnet101_FineTuning.keras"
MODEL = None
MODEL_LOADED = False

# Model konfigürasyonu
INPUT_SIZE = 224
NUM_CLASSES = 38

CLASS_NAMES = [
    'Apple___Apple_scab', 'Apple___Black_rot', 'Apple___Cedar_apple_rust', 'Apple___healthy',
    'Blueberry___healthy', 'Cherry_(including_sour)___Powdery_mildew', 
    'Cherry_(including_sour)___healthy', 'Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot', 
    'Corn_(maize)___Common_rust_', 'Corn_(maize)___Northern_Leaf_Blight', 'Corn_(maize)___healthy', 
    'Grape___Black_rot', 'Grape___Esca_(Black_Measles)', 'Grape___Leaf_blight_(Isariopsis_Leaf_Spot)', 
    'Grape___healthy', 'Orange___Haunglongbing_(Citrus_greening)', 'Peach___Bacterial_spot',
    'Peach___healthy', 'Pepper,_bell___Bacterial_spot', 'Pepper,_bell___healthy', 
    'Potato___Early_blight', 'Potato___Late_blight', 'Potato___healthy', 
    'Raspberry___healthy', 'Soybean___healthy', 'Squash___Powdery_mildew', 
    'Strawberry___Leaf_scorch', 'Strawberry___healthy', 'Tomato___Bacterial_spot', 
    'Tomato___Early_blight', 'Tomato___Late_blight', 'Tomato___Leaf_Mold', 
    'Tomato___Septoria_leaf_spot', 'Tomato___Spider_mites Two-spotted_spider_mite', 
    'Tomato___Target_Spot', 'Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'Tomato___Tomato_mosaic_virus',
    'Tomato___healthy'
]


def load_model():
    """Modeli yükle"""
    global MODEL, MODEL_LOADED
    
    try:
        from tensorflow.keras.models import load_model
        
        if not MODEL_PATH.exists():
            logger.error(f"Model bulunamadı: {MODEL_PATH}")
            MODEL_LOADED = False
            return False
        
        MODEL = load_model(str(MODEL_PATH))
        
        # Model bilgileri
        output_classes = MODEL.output_shape[-1]
        logger.info(f"Model yüklendi: {MODEL_PATH}")
        logger.info(f"Çıkış sınıfları: {output_classes}")
        
        if output_classes != NUM_CLASSES:
            logger.warning(f"Sınıf sayısı uyumsuz: {output_classes} vs {NUM_CLASSES}")
        
        MODEL_LOADED = True
        return True
    except Exception as e:
        logger.error(f"Model yükleme hatası: {e}")
        MODEL_LOADED = False
        return False


def preprocess_image(image_path: str, target_size: tuple = (224, 224)) -> Optional[np.ndarray]:
    """
    Görseli modele uygun şekilde ön işle
    - Resize: 224x224
    - BGR -> RGB dönüşümü
    - Normalizasyon: 0-1 aralığı (float32)
    """
    try:
        # Görseli oku
        img = cv2.imread(image_path)
        if img is None:
            raise ValueError(f"Görsel okunamadı: {image_path}")
        
        # Resize
        img = cv2.resize(img, target_size)
        
        # BGR -> RGB
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        # Normalizasyon (0-1 arası)
        img = img.astype('float32') / 255.0
        
        # Batch dimension ekle
        img = np.expand_dims(img, axis=0)
        
        logger.debug(f"Görsel ön işlendi: min={img.min():.2f}, max={img.max():.2f}")
        return img
    except Exception as e:
        logger.error(f"Ön işleme hatası: {e}")
        return None


@app.on_event("startup")
async def startup_event():
    """Uygulama başlangıcında model yükle"""
    logger.info("API başlatılıyor...")
    load_model()
    logger.info(f"Model durumu: {'Yüklendi ✓' if MODEL_LOADED else 'Yüklenemedi ✗'}")


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Servis sağlık kontrolü"""
    return {
        "status": "healthy" if MODEL_LOADED else "unhealthy",
        "model_loaded": MODEL_LOADED,
        "version": "1.0.0"
    }


@app.post("/predict", response_model=PredictionResponse)
async def predict(file: UploadFile = File(...)):
    """
    Görseli analiz et ve tahmin yap
    
    **Parametre:**
    - file: Yüklenecek görsel dosyası (JPG, PNG)
    
    **Yanıt:**
    - success: İşlem başarılı mı
    - top_prediction: En yüksek tahmin
    - all_predictions: Top 3 tahmin
    - processing_time: İşlem süresi
    """
    start_time = time.time()
    
    # Model kontrol
    if not MODEL_LOADED:
        raise HTTPException(status_code=503, detail="Model yüklenmedi. API başlatma sırasında hata oluştu.")
    
    # Dosya uzantı kontrol
    if file.filename.lower().split('.')[-1] not in ['jpg', 'jpeg', 'png']:
        raise HTTPException(status_code=400, detail="Sadece JPG, JPEG, PNG dosyaları kabul edilir")
    
    try:
        # Görsel dosyasını temp klasörüne kaydet
        temp_dir = Path("/tmp") if os.name != 'nt' else Path(os.environ.get('TEMP', '.'))
        temp_dir.mkdir(exist_ok=True)
        temp_path = temp_dir / file.filename
        
        # Dosyayı oku ve kaydet
        content = await file.read()
        with open(temp_path, "wb") as f:
            f.write(content)
        
        # Görseli ön işle
        processed_img = preprocess_image(str(temp_path))
        if processed_img is None:
            raise ValueError("Görsel ön işleme başarısız")
        
        logger.info(f"Tahmin yapılıyor: {file.filename}")
        
        # Tahmin yap
        predictions = MODEL.predict(processed_img, verbose=0)
        
        # Top 5 tahmini al
        top_5_idx = np.argsort(predictions[0])[::-1][:5]
        
        predictions_list = [
            PredictionResult(
                class_name=CLASS_NAMES[idx],
                confidence=float(predictions[0][idx]),
                confidence_percent=float(predictions[0][idx] * 100)
            )
            for idx in top_5_idx
        ]
        
        processing_time = time.time() - start_time
        
        logger.info(f"Tahmin tamamlandı - Top: {predictions_list[0].class_name} ({predictions_list[0].confidence_percent:.2f}%)")
        
        return {
            "success": True,
            "image_name": file.filename,
            "top_prediction": predictions_list[0],
            "all_predictions": predictions_list,
            "processing_time": processing_time
        }
    
    except Exception as e:
        logger.error(f"Tahmin hatası: {e}")
        processing_time = time.time() - start_time
        return {
            "success": False,
            "image_name": file.filename,
            "error": str(e),
            "processing_time": processing_time
        }
    
    finally:
        # Temp dosyasını sil
        if temp_path.exists():
            try:
                temp_path.unlink()
            except Exception as e:
                logger.warning(f"Temp dosya silinemedi: {e}")


@app.get("/classes")
async def get_classes():
    """Mevcut sınıfları listele"""
    return {
        "total_classes": len(CLASS_NAMES),
        "classes": CLASS_NAMES
    }


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
