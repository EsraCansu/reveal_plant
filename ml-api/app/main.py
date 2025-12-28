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
from fastapi.responses import HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from tensorflow.keras.applications.resnet import preprocess_input as resnet_preprocess_input

from .schema import PredictionResponse, PredictionResult, HealthResponse, FastAPIResponseFormat

# Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI application
app = FastAPI(
    title="Reveal Plant API",
    description="Plant disease detection - ResNet101 Fine-Tuning Model",
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

# Model and configuration
MODEL_PATH = Path(__file__).parent.parent / "model" / "PlantVillage_Resnet101_FineTuning.keras"
MODEL = None
MODEL_LOADED = False

# Model configuration
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
    """Load the model"""
    global MODEL, MODEL_LOADED
    
    try:
        import tensorflow as tf
        
        logger.info(f"TensorFlow version: {tf.__version__}")
        
        from tensorflow.keras.models import load_model as keras_load_model
        
        if not MODEL_PATH.exists():
            logger.error(f"Model not found: {MODEL_PATH}")
            MODEL_LOADED = False
            return False
        
        logger.info(f"Loading model: {MODEL_PATH}")
        logger.info(f"Model file size: {MODEL_PATH.stat().st_size / (1024*1024):.2f} MB")
        
        MODEL = keras_load_model(str(MODEL_PATH))
        
        # Model info
        output_classes = MODEL.output_shape[-1]
        logger.info(f"Model loaded: {MODEL_PATH}")
        logger.info(f"Output classes: {output_classes}")
        
        if output_classes != NUM_CLASSES:
            logger.warning(f"Class count mismatch: {output_classes} vs {NUM_CLASSES}")
        
        MODEL_LOADED = True
        return True
    except Exception as e:
        import traceback
        logger.error(f"Model loading error: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        MODEL_LOADED = False
        return False


def preprocess_image(image_path: str, target_size: tuple = (224, 224)) -> Optional[np.ndarray]:
    """
    Preprocess image for ResNet101 model
    
    Model training normalization: (image / 127.5) - 1.0 [-1, 1] range
    - Resize: 224x224 (ResNet101 input size)
    - BGR -> RGB conversion (OpenCV BGR, model expects RGB)
    - Normalization: (pixel / 127.5) - 1.0 (ImageNet ResNet normalization)
    - Add batch dimension
    
    ResNet101 specifications (plant_village_optimized.ipynb):
    - Input size: 224x224x3 (RGB)
    - Output: 38 plant disease classes
    - Normalization: [-1, 1] range: (image / 127.5) - 1.0
    - Normalization used in CELL 4
    """
    try:
        # 1. Read image (OpenCV -> BGR format)
        img = cv2.imread(image_path)
        if img is None:
            raise ValueError(f"Could not read image: {image_path}")
        
        logger.debug(f"Original image size: {img.shape}")
        
        # 2. Resize to 224x224 (ResNet101 standart input size)
        img = cv2.resize(img, target_size)
        
        # 3. BGR -> RGB conversion (OpenCV reads BGR format, model expects RGB)
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        # 4. Convert to Float32
        img = img.astype('float32')
        
        # 5. ResNet101 normalization: [-1, 1] range
        # plant_village_optimized.ipynb CELL 4 - Data Preprocessing
        # Model was trained with this normalization: (image / 127.5) - 1.0
        img = (img / 127.5) - 1.0
        
        # Check value range (for debugging)
        logger.debug(f"After normalization - Min: {img.min():.4f}, Max: {img.max():.4f}, "
                    f"Mean: {img.mean():.4f}, Std: {img.std():.4f}")
        
        # 6. Add batch dimension (model expects batch input)
        img = np.expand_dims(img, axis=0)
        
        logger.debug(f"Final shape: {img.shape} (batch, height, width, channels)")
        return img
        
    except Exception as e:
        logger.error(f"Preprocessing error: {e}")
        return None


@app.on_event("startup")
async def startup_event():
    """Load model on application startup"""
    logger.info("Starting API...")
    load_model()
    logger.info(f"Model status: {'Loaded ✓' if MODEL_LOADED else 'Failed to load ✗'}")


@app.get("/", tags=["Root"], response_class=HTMLResponse)
async def root():
    """Root endpoint - API welcome page"""
    status_color = "green" if MODEL_LOADED else "red"
    status_text = "✓ Loaded" if MODEL_LOADED else "✗ Failed to load"
    
    return f"""
    <!DOCTYPE html>
    <html lang="tr">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Reveal Plant API</title>
        <style>
            * {{
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }}
            body {{
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 20px;
            }}
            .container {{
                background: white;
                border-radius: 10px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
                max-width: 800px;
                width: 100%;
                padding: 40px;
            }}
            h1 {{
                color: #333;
                margin-bottom: 10px;
                text-align: center;
            }}
            .status {{
                text-align: center;
                margin-bottom: 30px;
                padding: 15px;
                background: #f5f5f5;
                border-radius: 5px;
            }}
            .status-indicator {{
                display: inline-block;
                width: 12px;
                height: 12px;
                border-radius: 50%;
                margin-right: 8px;
                background-color: {status_color};
            }}
            .endpoints {{
                margin-top: 30px;
            }}
            .endpoint {{
                margin-bottom: 15px;
                padding: 12px;
                background: #f9f9f9;
                border-left: 4px solid #667eea;
                border-radius: 3px;
            }}
            .endpoint-method {{
                display: inline-block;
                padding: 3px 8px;
                background: #667eea;
                color: white;
                border-radius: 3px;
                font-size: 12px;
                font-weight: bold;
                margin-right: 10px;
            }}
            .endpoint-url {{
                color: #666;
                font-family: monospace;
                font-size: 14px;
            }}
            .buttons {{
                display: flex;
                gap: 10px;
                margin-top: 30px;
                flex-wrap: wrap;
                justify-content: center;
            }}
            .button {{
                padding: 10px 20px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                font-size: 14px;
                text-decoration: none;
                display: inline-block;
                transition: all 0.3s ease;
            }}
            .button-primary {{
                background: #667eea;
                color: white;
            }}
            .button-primary:hover {{
                background: #5568d3;
                transform: translateY(-2px);
            }}
            .button-secondary {{
                background: #f0f0f0;
                color: #333;
                border: 1px solid #ddd;
            }}
            .button-secondary:hover {{
                background: #e0e0e0;
            }}
            .info {{
                background: #e7f3ff;
                border-left: 4px solid #2196F3;
                padding: 12px;
                border-radius: 3px;
                margin-top: 20px;
                color: #1565C0;
                font-size: 13px;
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <h1>🌿 Reveal Plant API</h1>
            <p style="text-align: center; color: #666; margin-bottom: 20px;">Plant Disease Detection System</p>
            
            <div class="status">
                <span class="status-indicator"></span>
                <strong>Model Status:</strong> {status_text}
            </div>
            
            <div class="endpoints">
                <h3 style="color: #333; margin-bottom: 15px;">📡 API Endpoints</h3>
                
                <div class="endpoint">
                    <span class="endpoint-method">GET</span>
                    <span class="endpoint-url">/health</span>
                    <p style="color: #999; font-size: 12px; margin-top: 5px;">Service health check</p>
                </div>
                
                <div class="endpoint">
                    <span class="endpoint-method">POST</span>
                    <span class="endpoint-url">/predict</span>
                    <p style="color: #999; font-size: 12px; margin-top: 5px;">Predict with image file</p>
                </div>
                
                <div class="endpoint">
                    <span class="endpoint-method">POST</span>
                    <span class="endpoint-url">/predict/base64</span>
                    <p style="color: #999; font-size: 12px; margin-top: 5px;">Predict with Base64 encoded image</p>
                </div>
                
                <div class="endpoint">
                    <span class="endpoint-method">GET</span>
                    <span class="endpoint-url">/classes</span>
                    <p style="color: #999; font-size: 12px; margin-top: 5px;">List available disease classes</p>
                </div>
            </div>
            
            <div class="buttons">
                <a href="/docs" class="button button-primary">📚 API Documentation</a>
                <a href="/redoc" class="button button-secondary">📖 ReDoc Documentation</a>
                <a href="/health" class="button button-secondary">🏥 Health Check</a>
            </div>
            
            <div class="info">
                💡 <strong>Tip:</strong> You can test API endpoints and view detailed documentation on the /docs page.
            </div>
        </div>
    </body>
    </html>
    """


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Service health check"""
    return {
        "status": "healthy" if MODEL_LOADED else "unhealthy",
        "model_loaded": MODEL_LOADED,
        "version": "1.0.0"
    }


@app.post("/predict", response_model=PredictionResponse)
async def predict(file: UploadFile = File(...)):
    """
    Analyze image and make prediction
    
    **Parameter:**
    - file: Image file to upload (JPG, PNG)
    
    **Response:**
    - success: Whether operation succeeded
    - top_prediction: Highest prediction
    - all_predictions: Top 3 predictions
    - processing_time: Processing time
    """
    start_time = time.time()
    
    # Model check
    if not MODEL_LOADED:
        raise HTTPException(status_code=503, detail="Model not loaded. Error occurred during API startup.")
    
    # File extension check
    if file.filename.lower().split('.')[-1] not in ['jpg', 'jpeg', 'png']:
        raise HTTPException(status_code=400, detail="Only JPG, JPEG, PNG files are accepted")
    
    try:
        # Save image file to temp folder
        temp_dir = Path("/tmp") if os.name != 'nt' else Path(os.environ.get('TEMP', '.'))
        temp_dir.mkdir(exist_ok=True)
        temp_path = temp_dir / file.filename
        
        # Read and save file
        content = await file.read()
        with open(temp_path, "wb") as f:
            f.write(content)
        
        # Preprocess image
        processed_img = preprocess_image(str(temp_path))
        if processed_img is None:
            raise ValueError("Image preprocessing failed")
        
        logger.info(f"Making prediction: {file.filename}")
        
        # Tahmin yap
        predictions = MODEL.predict(processed_img, verbose=0)
        
        # Softmax may already be applied, if not apply it
        # Model output is already normalized with softmax (categorical output)
        predictions = predictions[0]  # Remove batch dimension
        
        # Get top 5 predictions
        top_5_idx = np.argsort(predictions)[::-1][:5]
        
        predictions_list = [
            PredictionResult(
                disease=CLASS_NAMES[idx],
                confidence_score=float(predictions[idx]),
                confidence_percent=float(predictions[idx] * 100)
            )
            for idx in top_5_idx
        ]
        
        processing_time = time.time() - start_time
        
        logger.info(f"Prediction completed - Top: {predictions_list[0].disease} ({predictions_list[0].confidence_percent:.2f}%)")
        
        return {
            "success": True,
            "image_name": file.filename,
            "top_prediction": predictions_list[0],
            "all_predictions": predictions_list,
            "processing_time": processing_time
        }
    
    except Exception as e:
        logger.error(f"Prediction error: {e}")
        processing_time = time.time() - start_time
        return {
            "success": False,
            "image_name": file.filename,
            "error": str(e),
            "processing_time": processing_time
        }
    
    finally:
        # Delete temp file
        if temp_path.exists():
            try:
                temp_path.unlink()
            except Exception as e:
                logger.warning(f"Could not delete temp file: {e}")


@app.get("/classes")
async def get_classes():
    """List available classes"""
    return {
        "total_classes": len(CLASS_NAMES),
        "classes": CLASS_NAMES
    }


@app.post("/predict/base64", response_model=FastAPIResponseFormat)
async def predict_base64(image_data: dict):
    """
    Predict with Base64 encoded image (endpoint that handles requests from Java Backend)
    
    **Request Body:**
    - imageBase64: Base64 encoded image string
    - mode: Prediction mode (optional)
    - description: Image description (optional)
    
    **Response:** FastAPIResponseFormat expected by Java Backend
    """
    start_time = time.time()
    
    # Model check
    if not MODEL_LOADED:
        raise HTTPException(status_code=503, detail="Model not loaded. Error occurred during API startup.")
    
    try:
        import base64
        from io import BytesIO
        from PIL import Image as PILImage
        
        # Decode Base64 string
        if "imageBase64" not in image_data:
            raise ValueError("imageBase64 parameter is required")
        
        image_base64 = image_data.get("imageBase64", "")
        
        # Remove data URL prefix if present (e.g., "data:image/jpeg;base64,")
        if "," in image_base64 and image_base64.startswith("data:"):
            image_base64 = image_base64.split(",", 1)[1]
            logger.info(f"🔧 Stripped data URL prefix from base64 string")
        
        # Base64 decode
        image_bytes = base64.b64decode(image_base64)
        image = PILImage.open(BytesIO(image_bytes))
        
        # Convert RGBA to RGB if needed (for PNG transparency)
        if image.mode in ('RGBA', 'LA', 'P'):
            # Create white background
            background = PILImage.new('RGB', image.size, (255, 255, 255))
            if image.mode == 'P':
                image = image.convert('RGBA')
            background.paste(image, mask=image.split()[-1] if image.mode == 'RGBA' else None)
            image = background
        elif image.mode != 'RGB':
            image = image.convert('RGB')
        
        # Save to temp folder
        temp_dir = Path("/tmp") if os.name != 'nt' else Path(os.environ.get('TEMP', '.'))
        temp_dir.mkdir(exist_ok=True)
        temp_path = temp_dir / "temp_predict.jpg"
        image.save(temp_path, 'JPEG')
        
        # Preprocess image
        processed_img = preprocess_image(str(temp_path))
        if processed_img is None:
            raise ValueError("Image preprocessing failed")
        
        logger.info("Making prediction for Base64 image")
        
        # Tahmin yap
        predictions = MODEL.predict(processed_img, verbose=0)
        
        # Get softmax output and normalize
        predictions = predictions[0]  # Remove batch dimension
        
        # Get top 5 predictions
        top_5_idx = np.argsort(predictions)[::-1][:5]
        
        predictions_list = [
            PredictionResult(
                disease=CLASS_NAMES[idx],
                confidence_score=float(predictions[idx]),
                confidence_percent=float(predictions[idx] * 100)
            )
            for idx in top_5_idx
        ]
        
        top_pred = predictions_list[0]
        processing_time = time.time() - start_time
        
        # Java Backend'in beklediği format
        return {
            "status": "success",
            "message": "Prediction completed successfully",
            "top_prediction": top_pred.disease,
            "top_confidence": top_pred.confidence_score,
            "recommended_action": f"Analysis suggests {top_pred.disease.replace('_', ' ')} with {top_pred.confidence_percent:.1f}% confidence",
            "predictions": predictions_list
        }
    
    except Exception as e:
        logger.error(f"Base64 prediction error: {e}")
        return {
            "status": "error",
            "message": str(e),
            "top_prediction": "Unknown",
            "top_confidence": 0.0,
            "recommended_action": None,
            "predictions": []
        }
    
    finally:
        # Delete temp file
        try:
            if temp_path.exists():
                temp_path.unlink()
        except:
            pass


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
