#!/usr/bin/env python3
"""
Quick test script to verify full prediction workflow
"""
import base64
import json
from pathlib import Path
import sys

# Add ml-api to path
sys.path.insert(0, r'C:\Users\myy\Documents\GitHub\reveal_plant\ml-api')

def test_workflow():
    print("\n" + "="*70)
    print("🔬 PREDICTION WORKFLOW TEST")
    print("="*70)
    
    # Step 1: Test FastAPI connection
    print("\n1️⃣ Testing FastAPI connection...")
    try:
        import requests
        response = requests.get("http://localhost:8000/docs", timeout=5)
        print(f"   ✅ FastAPI is running on port 8000")
    except Exception as e:
        print(f"   ❌ FastAPI connection failed: {e}")
        return False
    
    # Step 2: Test Spring Boot connection
    print("\n2️⃣ Testing Spring Boot connection...")
    try:
        response = requests.get("http://localhost:8080/api/health", timeout=5)
        print(f"   ✅ Spring Boot is running on port 8080")
    except Exception as e:
        print(f"   ⚠️ Spring Boot health check: {e}")
    
    # Step 3: Create a test image and send prediction
    print("\n3️⃣ Creating test image and sending prediction...")
    try:
        from PIL import Image
        import io
        
        # Create a red image (tomato-like)
        img = Image.new('RGB', (224, 224), color=(255, 50, 50))
        buffer = io.BytesIO()
        img.save(buffer, format='JPEG')
        image_bytes = buffer.getvalue()
        base64_image = base64.b64encode(image_bytes).decode('utf-8')
        
        print(f"   ✓ Test image created: {len(image_bytes)} bytes")
        
        # Send to Java backend /analyze endpoint
        payload = {
            "userId": 1,
            "imageBase64": base64_image,
            "description": "Test red tomato image",
            "plantId": None
        }
        
        headers = {"Content-Type": "application/json"}
        response = requests.post(
            "http://localhost:8080/api/predictions/analyze",
            json=payload,
            headers=headers,
            timeout=30
        )
        
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"   ✅ Prediction successful!")
            print(f"\n   Response:")
            print(json.dumps(result, indent=2, ensure_ascii=False))
            return True
        else:
            print(f"   ❌ Prediction failed: {response.text}")
            return False
            
    except Exception as e:
        print(f"   ❌ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = test_workflow()
    print("\n" + "="*70)
    if success:
        print("✅ WORKFLOW TEST PASSED!")
    else:
        print("❌ WORKFLOW TEST FAILED!")
    print("="*70 + "\n")
    sys.exit(0 if success else 1)
