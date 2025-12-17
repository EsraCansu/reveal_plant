"""
Pydantic schemas for request/response validation
"""

from pydantic import BaseModel, Field
from typing import List, Optional
from PIL.Image import Image
import base64

class PredictionResult(BaseModel):
    """Tahmin sonucu"""
    class_name: str = Field(..., description="Sınıf adı")
    confidence: float = Field(..., description="Güven skoru (0-1)")
    confidence_percent: float = Field(..., description="Güven yüzdesi")


class PredictionResponse(BaseModel):
    """API tahmin yanıtı"""
    success: bool = Field(..., description="İşlem başarılı mı")
    image_name: Optional[str] = Field(None, description="Görsel adı")
    top_prediction: Optional[PredictionResult] = Field(None, description="En yüksek tahmin")
    all_predictions: Optional[List[PredictionResult]] = Field(None, description="Tüm tahminler")
    error: Optional[str] = Field(None, description="Hata mesajı")
    processing_time: float = Field(..., description="İşlem süresi (saniye)")


class HealthResponse(BaseModel):
    """Sağlık kontrol yanıtı"""
    status: str = Field(..., description="Servis durumu")
    model_loaded: bool = Field(..., description="Model yüklendi mi")
    version: str = Field(..., description="API sürümü")
