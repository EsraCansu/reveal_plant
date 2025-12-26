"""
FastAPI Server - CNN Model Inference
Resim yükleme ve bitki/hastalık tanımlama
"""

import os
import sys
import traceback
import base64
import json
from datetime import datetime
try:
    import cv2
except ImportError:
    cv2 = None
import numpy as np
from fastapi import FastAPI, UploadFile, File, Form, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
try:
    from tensorflow.keras.models import load_model
    from tensorflow.keras.applications.resnet import preprocess_input
except ImportError:
    load_model = None
    preprocess_input = None
import uvicorn

# ===================== CONFIG =====================
MODEL_PATH = r'C:\Users\myy\Documents\GitHub\reveal_plant\ml-api\model\PlantVillage_Resnet101_FineTuning.keras'
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

# Disease recommendations mapping
DISEASE_RECOMMENDATIONS = {
    "apple_scab": "Apply fungicide treatments. Remove infected leaves. Improve air circulation.",
    "black_rot": "Prune infected branches. Apply copper-based fungicide. Sanitize tools.",
    "powdery_mildew": "Use sulfur or neem oil spray. Ensure proper spacing between plants.",
    "leaf_spot": "Remove affected leaves. Apply fungicide. Increase air circulation.",
}

# ===================== FastAPI Setup =====================
app = FastAPI(title="PlantVillage CNN API", version="2.0")

# CORS Configuration - Java Backend erişimi için
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000", 
                   "http://localhost:8080", "http://127.0.0.1:8080",
                   "http://localhost:8000", "http://127.0.0.1:8000"],
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
        
        # Top-3 predictions
        top_3_indices = np.argsort(predictions[0])[-3:][::-1]
        top_3_predictions = {}
        
        for idx in top_3_indices:
            class_name = CLASS_LABELS.get(int(idx), f"Class_{idx}")
            confidence = float(predictions[0][idx])
            top_3_predictions[class_name] = confidence
        
        # En yüksek confidence'ı bul
        class_idx = np.argmax(predictions[0])
        confidence = float(predictions[0][class_idx])
        class_name = CLASS_LABELS.get(class_idx, f"Class_{class_idx}")
        
        # "healthy" kontrolü
        is_healthy = "healthy" in class_name.lower()
        
        # Parse plant and disease
        parts = class_name.split("___")
        plant_name = parts[0].replace("_(", " (")
        disease_name = parts[1] if len(parts) > 1 else "Unknown"
        
        return {
            "status": "success",
            "predictions": top_3_predictions,
            "topPrediction": disease_name,
            "topConfidence": round(confidence, 4),
            "plantName": plant_name,
            "diseaseName": disease_name if not is_healthy else "Healthy",
            "isHealthy": is_healthy,
            "recommendedAction": DISEASE_RECOMMENDATIONS.get(
                disease_name.lower().replace(" ", "_"), 
                "Continue monitoring the plant. Apply general plant care practices."
            ),
            "symptoms": [],
            "metadata": {
                "modelVersion": "ResNet101",
                "imageSize": IMAGE_SIZE,
                "classIndex": int(class_idx)
            },
            "processingTimeMs": 0
        }, None
            
    except Exception as e:
        return None, str(e)

# ===================== API Endpoints =====================

@app.get("/health")
async def health_check():
    """Server sağlık kontrolü"""
    return {
        "status": "ok",
        "model_loaded": model is not None,
        "timestamp": datetime.now().isoformat()
    }

@app.post("/predict")
async def predict_endpoint(request: Request):
    """
    REST API - JSON based prediction request
    
    Expected JSON payload:
    {
        "imageBase64": "data:image/jpeg;base64,...",
        "imageType": "jpg",
        "plantId": 1,
        "description": "Yellow spots on leaves"
    }
    
    Returns:
        JSON with predictions matching FastAPIResponse model
    """
# cnn_model/fastapi_server.py (Düzeltilmiş)

@app.post("/predict")
async def predict_endpoint(request: Request):
    
    try:
        # Parse JSON request
        body = await request.json()
        
        # ⬅️ Kritik Düzeltme: Hem camelCase hem de snake_case'i kontrol et.
        image_base64 = body.get("imageBase64") or body.get("image_base64", "")
        
        image_type = body.get("imageType", "jpg")
        plant_id = body.get("plantId")
        description = body.get("description", "")
        
        # Validate image
        if not image_base64:
            return JSONResponse(
                status_code=400,
                content={"status": "error", "message": "Missing imageBase64 or image_base64"} # Hata mesajını da güncelle
            )
        
        
        # Extract base64 data from data URI
        if image_base64.startswith("data:image/"):
            image_base64 = image_base64.split(",")[1]
        
        # Decode base64 to bytes
        try:
            image_bytes = base64.b64decode(image_base64)
        except Exception as e:
            return JSONResponse(
                status_code=400,
                content={"status": "error", "message": f"Invalid base64 encoding: {str(e)}"}
            )
        
        # Preprocess image
        img_batch, error = preprocess_image(image_bytes)
        if error:
            return JSONResponse(
                status_code=400,
                content={"status": "error", "message": error}
            )
        
        # Predict
        result, error = predict(img_batch, "disease")
        if error:
            return JSONResponse(
                status_code=500,
                content={"status": "error", "message": error}
            )
        
        return JSONResponse(content=result)
        
    except Exception as e:
        print(f"Error: {e}")
        traceback.print_exc()
        return JSONResponse(
            status_code=500,
            content={"status": "error", "message": str(e)}
        )

@app.post("/predict-file")
async def predict_file_endpoint(
    file: UploadFile = File(...),
    mode: str = Form(default="disease")
):
    """
    File upload based prediction (original endpoint)
    
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
                content={"status": "error", "message": "Only JPEG/PNG images allowed"}
            )
        
        # Resim oku
        image_bytes = await file.read()
        
        # Ön işle
        img_batch, error = preprocess_image(image_bytes)
        if error:
            return JSONResponse(
                status_code=400,
                content={"status": "error", "message": error}
            )
        
        # Tahmin yap
        result, error = predict(img_batch, mode)
        if error:
            return JSONResponse(
                status_code=500,
                content={"status": "error", "message": error}
            )
        
        return JSONResponse(content=result)
        
    except Exception as e:
        print(f"Error: {e}")
        traceback.print_exc()
        return JSONResponse(
            status_code=500,
            content={"status": "error", "message": str(e)}
        )

@app.get("/plants")
async def get_available_plants():
    """Available plants list"""
    plants = sorted(list(set([
        CLASS_LABELS[idx].split("___")[0] 
        for idx in CLASS_LABELS.keys()
    ])))
    return {"plants": plants}

@app.get("/diseases")
async def get_available_diseases():
    """Available diseases list"""
    diseases = sorted(list(set([
        CLASS_LABELS[idx].split("___")[1] 
        for idx in CLASS_LABELS.keys()
        if "___" in CLASS_LABELS[idx]
    ])))
    return {"diseases": diseases}

# ===================== Startup Events =====================

@app.on_event("startup")
async def startup_event():
    """Sunucu başladığında model yükle"""
    print("\n" + "="*50)
    print("🚀 FastAPI Server Starting...")
    print("="*50)
    if load_model_safe():
        print("✅ Server ready at http://localhost:8000")
        print("📚 API Docs: http://localhost:8000/docs")
        print("📊 Endpoints:")
        print("  POST /predict - JSON based prediction")
        print("  POST /predict-file - File upload prediction")
        print("  GET /health - Health check")
        print("  GET /plants - Available plants")
        print("  GET /diseases - Available diseases")
    else:
        print("⚠️  Server started but model not loaded!")
    print("="*50 + "\n")

# ===================== Main =====================

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,        log_level="info"
    )