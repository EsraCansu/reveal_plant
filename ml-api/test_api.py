"""
API Test Dosyası
FastAPI servisini test etmek için kullanın
"""

import requests
import json
from pathlib import Path

# API URL
API_URL = "http://localhost:8000"

def test_health():
    """Sağlık kontrolü testi"""
    print("\n" + "="*60)
    print("🏥 HEALTH CHECK TEST")
    print("="*60)
    
    response = requests.get(f"{API_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    return response.status_code == 200


def test_classes():
    """Sınıflar listesi testi"""
    print("\n" + "="*60)
    print("📋 CLASSES TEST")
    print("="*60)
    
    response = requests.get(f"{API_URL}/classes")
    data = response.json()
    print(f"Status: {response.status_code}")
    print(f"Total Classes: {data['total_classes']}")
    print(f"First 5 Classes: {data['classes'][:5]}")
    return response.status_code == 200


def test_predict(image_path: str):
    """Tahmin testi"""
    print("\n" + "="*60)
    print("🔮 PREDICTION TEST")
    print("="*60)
    
    image_file = Path(image_path)
    if not image_file.exists():
        print(f"⚠️  Görsel bulunamadı: {image_path}")
        return False
    
    with open(image_file, "rb") as f:
        files = {"file": (image_file.name, f, "image/jpeg")}
        response = requests.post(f"{API_URL}/predict", files=files)
    
    print(f"Status: {response.status_code}")
    data = response.json()
    
    if data.get("success"):
        print(f"✓ Tahmin başarılı!")
        print(f"  Görsel: {data['image_name']}")
        print(f"  En yüksek tahmin: {data['top_prediction']['class_name']}")
        print(f"  Güven: {data['top_prediction']['confidence_percent']:.2f}%")
        print(f"  İşlem süresi: {data['processing_time']:.3f}s")
        print(f"\n  Top 3 tahmin:")
        for i, pred in enumerate(data['all_predictions'], 1):
            print(f"    {i}. {pred['class_name']}: {pred['confidence_percent']:.2f}%")
    else:
        print(f"✗ Tahmin başarısız: {data.get('error')}")
    
    return response.status_code == 200


def test_all():
    """Tüm testleri çalıştır"""
    print("\n" + "🌱"*30)
    print("REVEAL PLANT - API TEST SUITE")
    print("🌱"*30)
    
    try:
        # Test 1: Health check
        health_ok = test_health()
        
        # Test 2: Classes
        classes_ok = test_classes()
        
        # Test 3: Prediction
        test_image = Path(__file__).parent / "test_images" / "dom.jpg"
        if test_image.exists():
            predict_ok = test_predict(str(test_image))
        else:
            print(f"\n⚠️  Test görseli bulunamadı: {test_image}")
            predict_ok = False
        
        # Özet
        print("\n" + "="*60)
        print("TEST ÖZETI")
        print("="*60)
        print(f"Health Check: {'✓ PASS' if health_ok else '✗ FAIL'}")
        print(f"Classes List: {'✓ PASS' if classes_ok else '✗ FAIL'}")
        print(f"Prediction: {'✓ PASS' if predict_ok else '⚠️  SKIP/FAIL'}")
        print("="*60)
        
    except requests.exceptions.ConnectionError:
        print("\n✗ HATA: API'ye bağlanılamadı!")
        print(f"  Kontrol edin: {API_URL} çalışıyor mu?")
        print("  Başlat: python -m uvicorn app.main:app --reload")
    except Exception as e:
        print(f"\n✗ HATA: {e}")


if __name__ == "__main__":
    test_all()
