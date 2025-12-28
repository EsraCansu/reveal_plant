"""
Pydantic schemas for request/response validation
"""

from pydantic import BaseModel, Field
from typing import List, Optional
from PIL.Image import Image
import base64

class PredictionResult(BaseModel):
    """Prediction result"""
    disease: str = Field(..., description="Class name (Java backend: disease field)")
    confidence_score: float = Field(..., description="Confidence score (0-1)")
    confidence_percent: float = Field(..., description="Confidence percentage")


class PredictionResponse(BaseModel):
    """API prediction response"""
    success: bool = Field(..., description="Whether operation succeeded")
    image_name: Optional[str] = Field(None, description="Image name")
    top_prediction: Optional[PredictionResult] = Field(None, description="Highest prediction")
    all_predictions: Optional[List[PredictionResult]] = Field(None, description="All predictions")
    error: Optional[str] = Field(None, description="Error message")
    processing_time: float = Field(..., description="Processing time (seconds)")


class HealthResponse(BaseModel):
    """Health check response"""
    status: str = Field(..., description="Service status")
    model_loaded: bool = Field(..., description="Whether model is loaded")
    version: str = Field(..., description="API version")


class FastAPIResponseFormat(BaseModel):
    """FastAPI response format expected by Java Backend"""
    status: str = Field(..., description="Status (success/error)")
    message: str = Field(..., description="Status message")
    top_prediction: str = Field(..., description="Highest prediction class")
    top_confidence: float = Field(..., description="Confidence score (0-1)")
    recommended_action: Optional[str] = Field(None, description="Recommended action")
    predictions: List[PredictionResult] = Field(..., description="All predictions")
