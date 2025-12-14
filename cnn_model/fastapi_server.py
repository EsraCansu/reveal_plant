"""
FastAPI Server - CNN Model Inference
Resim yükleme ve bitki/hastalık tanımlama
"""

import os
import sys
import traceback
import cv2
import numpy as np
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from tensorflow.keras.models import load_model
from tensorflow.keras.applications.resnet import preprocess_input
import uvicorn

# ===================== CONFIG =====================
MODEL_PATH = r'PlantVillage_Resnet101_FineTuning.keras'
IMAGE_SIZE = 224

# Class labels for PlantVillage dataset
CLASS_LABELS = {
    0: "Apple___Apple_scab",
    1: "Apple___Black_rot",
    2: "Apple___Cedar_apple_rust",
    3: "Apple___healthy",
    4: "Blueberry___healthy",
    5: "Cherry_(including_sour)___Powdery_mildew",
    6: "Cherry_(including_sour)___healthy",
    7: "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
    8: "Corn_(maize)___Common_rust",
    9: "Corn_(maize)___Northern_Leaf_Blight",
    10: "Corn_(maize)___healthy",
    # ... Tüm sınıflar
}

# ===================== FastAPI Setup =====================
app = FastAPI(title="PlantVillage CNN API", version="1.0")

# CORS Configuration - Java Backend erişimi için
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000", 
                   "http://localhost:8080", "http://127.0.0.1:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===================== Model Loading =====================
model = None

def load_model_safe():
    global model
    try:
        if os.path.exists(MODEL_PATH):
            model = load_model(MODEL_PATH)
            print(f"✅ Model loaded successfully: {MODEL_PATH}")
            return True
        else:
            print(f"❌ Model file not found: {MODEL_PATH}")
            return False
    except Exception as e:
        print(f"❌ Error loading model: {e}")
        traceback.print_exc()
        return False

# ===================== Prediction Logic =====================
def preprocess_image(image_bytes):
    """Resimi modele göre ön işle"""
    try:
        # Convert bytes to numpy array
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            return None, "Image could not be decoded"
        
        # Resize to 224x224
        img_resized = cv2.resize(img, (IMAGE_SIZE, IMAGE_SIZE))
        
        # Normalize (ResNet expects -1 to 1)
        img_normalized = preprocess_input(img_resized)
        
        # Add batch dimension
        img_batch = np.expand_dims(img_normalized, axis=0)
        
        return img_batch, None
        
    except Exception as e:
        return None, str(e)

def predict(image_batch, mode):
    """CNN modeli ile tahmin yap"""
    try:
        if model is None:
            return None, "Model not loaded"
        
        # Tahmin yap
        predictions = model.predict(image_batch, verbose=0)
        
        # En yüksek confidence'ı bul
        class_idx = np.argmax(predictions[0])
        confidence = float(predictions[0][class_idx])
        class_name = CLASS_LABELS.get(class_idx, f"Class_{class_idx}")
        
        # "healthy" kontrolü
        is_healthy = "healthy" in class_name.lower()
        
        if mode == "identify":
            # Bitki türünü ayıkla (___'dan önceki kısım)
            plant_type = class_name.split("___")[0].replace("_(", " (")
            return {
                "type": "identify",
                "plant": plant_type,
                "plant_id": class_idx,
                "confidence": round(confidence * 100, 2),
                "is_healthy": is_healthy
            }, None
        
        elif mode == "disease":
            # Hastalığı ayıkla
            parts = class_name.split("___")
            plant = parts[0].replace("_(", " (")
            disease = parts[1] if len(parts) > 1 else "Unknown"
            
            return {
                "type": "disease",
                "plant": plant,
                "disease": disease,
                "disease_id": class_idx,
                "confidence": round(confidence * 100, 2),
                "is_healthy": is_healthy
            }, None
        
        else:
            return None, "Invalid mode. Use 'identify' or 'disease'"
            
    except Exception as e:
        return None, str(e)

# ===================== API Endpoints =====================

@app.get("/health")
async def health_check():
    """Server sağlık kontrolü"""
    return {
        "status": "ok",
        "model_loaded": model is not None,
        "timestamp": pd.Timestamp.now().isoformat()
    }

@app.post("/predict")
async def predict_endpoint(
    file: UploadFile = File(...),
    mode: str = Form(...)  # "identify" veya "disease"
):
    """
    Resim yükle ve tahmin yap
    
    Args:
        file: JPG/PNG resim dosyası
        mode: "identify" (bitki tanıma) veya "disease" (hastalık tespiti)
    
    Returns:
        JSON with predictions
    """
    try:
        # Dosya tipi kontrolü
        if file.content_type not in ["image/jpeg", "image/png", "image/jpg"]:
            return JSONResponse(
                status_code=400,
                content={"error": "Only JPEG/PNG images allowed"}
            )
        
        # Resim oku
        image_bytes = await file.read()
        
        # Ön işle
        img_batch, error = preprocess_image(image_bytes)
        if error:
            return JSONResponse(
                status_code=400,
                content={"error": error}
            )
        
        # Tahmin yap
        result, error = predict(img_batch, mode)
        if error:
            return JSONResponse(
                status_code=500,
                content={"error": error}
            )
        
        return JSONResponse(content=result)
        
    except Exception as e:
        print(f"Error: {e}")
        traceback.print_exc()
        return JSONResponse(
            status_code=500,
            content={"error": str(e)}
        )

@app.post("/batch-predict")
async def batch_predict_endpoint(files: list[UploadFile] = File(...), mode: str = Form(...)):
    """Birden fazla resim tahmin et"""
    results = []
    for file in files:
        image_bytes = await file.read()
        img_batch, _ = preprocess_image(image_bytes)
        result, _ = predict(img_batch, mode)
        results.append(result)
    return {"predictions": results}

# ===================== Startup Events =====================

@app.on_event("startup")
async def startup_event():
    """Sunucu başladığında model yükle"""
    print("\n" + "="*50)
    print("🚀 FastAPI Server Starting...")
    print("="*50)
    if load_model_safe():
        print("✅ Server ready at http://localhost:5000")
        print("📚 API Docs: http://localhost:5000/docs")
    else:
        print("⚠️  Server started but model not loaded!")
    print("="*50 + "\n")

# ===================== Main =====================

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=5000,
        log_level="info"
    )
