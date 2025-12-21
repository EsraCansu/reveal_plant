"""
Test script to verify prediction flow:
1. Load image
2. Convert to base64
3. Send to FastAPI
4. Check response format
5. Display results
"""

import base64
import requests
import json
from pathlib import Path

# Test configuration
FASTAPI_URL = "http://localhost:8000/predict/base64"
TEST_IMAGE_PATH = r"C:\Users\myy\Documents\GitHub\reveal_plant\cnn_model\test_image.jpg"

def load_and_encode_image(image_path):
    """Load image and convert to base64"""
    print("\n" + "="*60)
    print("STEP 1: LOADING IMAGE")
    print("="*60)
    
    if not Path(image_path).exists():
        print(f"❌ Image not found: {image_path}")
        print("\nUsing a simple test image instead...")
        # Create a minimal 1x1 red pixel image
        import io
        from PIL import Image
        img = Image.new('RGB', (224, 224), color='red')
        buffer = io.BytesIO()
        img.save(buffer, format='JPEG')
        image_bytes = buffer.getvalue()
    else:
        print(f"✓ Image found: {image_path}")
        with open(image_path, 'rb') as f:
            image_bytes = f.read()
        print(f"✓ Image loaded: {len(image_bytes)} bytes")
    
    # Encode to base64
    base64_string = base64.b64encode(image_bytes).decode('utf-8')
    print(f"✓ Base64 encoded: {len(base64_string)} characters")
    print(f"  First 50 chars: {base64_string[:50]}...")
    
    return base64_string

def send_to_fastapi(base64_image):
    """Send image to FastAPI and get prediction"""
    print("\n" + "="*60)
    print("STEP 2: SENDING TO FASTAPI")
    print("="*60)
    
    payload = {
        "imageBase64": base64_image,
        "mode": "identify",
        "description": "Test prediction"
    }
    
    print(f"✓ Request URL: {FASTAPI_URL}")
    print(f"✓ Payload keys: {list(payload.keys())}")
    print(f"✓ Base64 length: {len(payload['imageBase64'])} chars")
    
    try:
        response = requests.post(
            FASTAPI_URL,
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        print(f"✓ Response status: {response.status_code}")
        
        if response.status_code != 200:
            print(f"❌ Error response: {response.text}")
            return None
        
        return response.json()
    
    except Exception as e:
        print(f"❌ Request failed: {e}")
        return None

def analyze_response(response):
    """Analyze and display response format"""
    print("\n" + "="*60)
    print("STEP 3: ANALYZING RESPONSE FORMAT")
    print("="*60)
    
    if not response:
        print("❌ No response received")
        return
    
    print("✓ Response received!")
    print(f"\nResponse keys: {list(response.keys())}")
    
    # Check status
    status = response.get('status', 'unknown')
    print(f"\n📊 Status: {status}")
    
    if status == 'error':
        print(f"❌ Error message: {response.get('message', 'Unknown error')}")
        return
    
    # Check prediction
    top_pred = response.get('top_prediction', 'Unknown')
    confidence = response.get('top_confidence', 0.0)
    
    print(f"\n🎯 Top Prediction: {top_pred}")
    print(f"📈 Confidence: {confidence:.4f} ({confidence*100:.2f}%)")
    
    # Check all predictions
    all_preds = response.get('predictions', [])
    print(f"\n📋 All Predictions: {len(all_preds)} results")
    
    if all_preds:
        print("\nTop 3 Predictions:")
        for i, pred in enumerate(all_preds[:3], 1):
            disease = pred.get('disease', 'Unknown')
            conf_percent = pred.get('confidence_percent', 0.0)
            print(f"  {i}. {disease:50} {conf_percent:6.2f}%")

def display_final_format(response):
    """Display how the response would be shown to user"""
    print("\n" + "="*60)
    print("STEP 4: FINAL DISPLAY FORMAT")
    print("="*60)
    
    if not response or response.get('status') == 'error':
        print("\n❌ ANALYSIS FAILED")
        print("   Would show error message to user")
        return
    
    top_pred = response.get('top_prediction', 'Unknown')
    confidence = response.get('top_confidence', 0.0)
    all_preds = response.get('predictions', [])
    
    print("\n" + "─"*60)
    print("│  PLANT IDENTIFICATION RESULT")
    print("─"*60)
    
    # Extract plant name (remove disease part)
    plant_name = top_pred.split('___')[0] if '___' in top_pred else top_pred
    plant_name = plant_name.replace('_', ' ')
    
    print(f"│  Identified Plant: {plant_name}")
    print(f"│  Confidence: {confidence*100:.1f}%")
    print("─"*60)
    
    if all_preds and len(all_preds) > 1:
        print("\n│  Other Possibilities:")
        for i, pred in enumerate(all_preds[1:4], 2):
            disease = pred.get('disease', '').replace('_', ' ')
            conf = pred.get('confidence_percent', 0.0)
            print(f"│    {i}. {disease:40} {conf:5.1f}%")
        print("─"*60)

def main():
    print("\n🧪 FASTAPI PREDICTION TEST")
    print("Testing complete prediction flow...\n")
    
    # Step 1: Load and encode image
    base64_image = load_and_encode_image(TEST_IMAGE_PATH)
    
    # Step 2: Send to FastAPI
    response = send_to_fastapi(base64_image)
    
    # Step 3: Analyze response
    analyze_response(response)
    
    # Step 4: Display final format
    display_final_format(response)
    
    # Print raw JSON
    print("\n" + "="*60)
    print("RAW JSON RESPONSE")
    print("="*60)
    if response:
        print(json.dumps(response, indent=2))
    
    print("\n✓ Test complete!")

if __name__ == "__main__":
    main()
