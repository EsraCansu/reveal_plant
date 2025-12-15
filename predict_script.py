"""
reveal_plant - Kaggle Model Prediction Script
Author: muhammetyusufyilmaz
Kaggle Dataset: muhammetyusufyilmaz/resnet101
"""

import os
import sys
import json
import cv2
import numpy as np
import shutil

# TensorFlow/Keras import
try:
    from keras.models import load_model
    KERAS_AVAILABLE = True
except ImportError:
    try:
        from tensorflow import keras
        load_model = keras.models.load_model
        KERAS_AVAILABLE = True
    except (ImportError, AttributeError):
        print("⚠️  TensorFlow/Keras yüklenmedi!")
        KERAS_AVAILABLE = False

# Kaggle API'si için (optional)
try:
    from kaggle.api.kaggle_api_extended import KaggleApi
    KAGGLE_AVAILABLE = True
except ImportError:
    KAGGLE_AVAILABLE = False
    print("⚠️  Uyarı: Kaggle API yüklenmedi. Modeli manuel olarak indirmelisiniz.")

# Sınıf isimleri
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

def download_model_from_kaggle(dataset_name="muhammetyusufyilmaz/resnet101", download_path="./models"):
    """Kaggle'dan modeli indir"""
    if not KAGGLE_AVAILABLE:
        print(f"\n⚠️  HATA: Kaggle API yüklenmedi!")
        print(f"   Fix: pip install kaggle")
        return False
    
    # Kaggle credentials kontrolü
    kaggle_username = os.getenv('KAGGLE_USERNAME')
    kaggle_key = os.getenv('KAGGLE_KEY')
    
    if not kaggle_username or not kaggle_key:
        print(f"\n⚠️  HATA: Kaggle credentials bulunamadı!")
        print(f"   KAGGLE_USERNAME ve KAGGLE_KEY environment variables'ini ayarla")
        print(f"   veya ~/.kaggle/kaggle.json dosyasını oluştur")
        return False
    
    print(f"\n📥 Kaggle'dan model indiriliyor...")
    print(f"Dataset: {dataset_name}")
    
    try:
        # Kaggle API'sini başlat
        api = KaggleApi()
        api.authenticate()
        print("✓ Kaggle API'ye bağlandı\n")
        
        # Model klasörünü oluştur
        os.makedirs(download_path, exist_ok=True)
        
        # Dataset'i indir
        api.dataset_download_files(dataset_name, path=download_path, unzip=True)
        print(f"✓ Model indirildi: {download_path}\n")
        return True
        
    except Exception as e:
        print(f"✗ Kaggle'dan indirme başarısız: {e}")
        print("💡 İpucu:")
        print(f"  1. Kaggle API key'ini kontrol et: ~/.kaggle/kaggle.json")
        print(f"  2. Dataset adını kontrol et: {dataset_name}")
        return False

def find_model_file(search_dir):
    """Model dosyasını bul"""
    print(f"Model aranıyor: {search_dir}")
    extensions = ['.keras', '.h5', '.hdf5']
    
    for root, dirs, files in os.walk(search_dir):
        for file in files:
            if any(file.endswith(ext) for ext in extensions):
                model_path = os.path.join(root, file)
                print(f"✓ Model bulundu: {model_path}")
                return model_path
    
    raise FileNotFoundError(f"Model bulunamadı: {search_dir}")

def preprocess_image(image_path, target_size=(224, 224)):
    """Görüntüyü model için hazırla"""
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError(f"Görüntü okunamadı: {image_path}")
    
    img = cv2.resize(img, target_size)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img.astype('float32') / 255.0
    img = np.expand_dims(img, axis=0)
    return img

def predict_batch(model, image_folder, output_file='results/predictions.json'):
    """Klasördeki tüm görüntüler için tahmin yap"""
    print(f"\n{'='*60}")
    print("TAHMİN BAŞLIYOR")
    print(f"{'='*60}")
    
    extensions = ('.jpg', '.jpeg', '.png', '.bmp')
    image_files = [f for f in os.listdir(image_folder) 
                   if f.lower().endswith(extensions)]
    
    if not image_files:
        print(f"⚠️  Görüntü bulunamadı: {image_folder}")
        return []
    
    print(f"Toplam {len(image_files)} görüntü bulundu\n")
    results = []
    
    for i, img_file in enumerate(image_files, 1):
        img_path = os.path.join(image_folder, img_file)
        print(f"[{i}/{len(image_files)}] {img_file}")
        
        try:
            processed_img = preprocess_image(img_path)
            predictions = model.predict(processed_img, verbose=0)
            top_3_idx = np.argsort(predictions[0])[::-1][:3]
            
            predictions_list = [
                {
                    'class': CLASS_NAMES[idx],
                    'confidence': float(predictions[0][idx]),
                    'confidence_percent': float(predictions[0][idx] * 100)
                }
                for idx in top_3_idx
            ]
            
            result = {
                'image': img_file,
                'success': True,
                'predictions': predictions_list,
                'top_prediction': CLASS_NAMES[top_3_idx[0]],
                'top_confidence': float(predictions[0][top_3_idx[0]])
            }
            
            print(f"  ✓ {result['top_prediction']}")
            print(f"    Güven: %{result['top_confidence']*100:.2f}\n")
            
        except Exception as e:
            result = {
                'image': img_file,
                'success': False,
                'error': str(e)
            }
            print(f"  ✗ Hata: {e}\n")
        
        results.append(result)
    
    # Sonuçları kaydet
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    print(f"{'='*60}")
    print("ÖZET")
    print(f"{'='*60}")
    print(f"Toplam: {len(results)}")
    print(f"Başarılı: {sum(1 for r in results if r['success'])}")
    print(f"Sonuç: {output_file}")
    print(f"{'='*60}\n")
    
    return results

def main():
    print("\n🌱 REVEAL PLANT - Bitki Hastalığı Tespiti\n")
    
    if not KERAS_AVAILABLE:
        print("✗ HATA: TensorFlow/Keras yüklenmedi!")
        print("Kurmak için: pip install tensorflow")
        sys.exit(1)
    
    # Model klasörü
    model_dir = "./models"
    model_needs_cleanup = False
    
    try:
        # Model klasörü kontrol et
        if not os.path.exists(model_dir):
            print(f"📂 {model_dir} klasörü bulunamadı")
            download_model_from_kaggle()
            model_needs_cleanup = True
        else:
            # Klasörde model var mı kontrol et
            model_files = [f for f in os.listdir(model_dir) 
                          if f.endswith(('.keras', '.h5', '.hdf5'))]
            if not model_files:
                print(f"📦 {model_dir} klasörü boş, model indiriliyor...")
                download_model_from_kaggle()
                model_needs_cleanup = True
        
        # Model yükle
        try:
            model_path = find_model_file(model_dir)
            print(f"\nModel yükleniyor...")
            model = load_model(model_path)
            print(f"✓ Model yüklendi: {model.output_shape[-1]} sınıf\n")
        except FileNotFoundError as e:
            print(f"\n✗ {e}")
            print("💡 Kaggle'dan model indirmek için: pip install kaggle")
            sys.exit(1)
        
        # Tahmin yap
        test_folder = "./test_images"
        if not os.path.exists(test_folder):
            print(f"✗ Klasör bulunamadı: {test_folder}")
            sys.exit(1)
        
        results = predict_batch(model, test_folder)
        
        if results:
            print("✅ Tahminler tamamlandı!")
        else:
            print("⚠️  Hiç tahmin yapılamadı!")
            sys.exit(1)
    
    finally:
        # Modeli sil (cleanup)
        if model_needs_cleanup and os.path.exists(model_dir):
            try:
                shutil.rmtree(model_dir)
                print(f"\n🗑️  Model klasörü silindi: {model_dir}")
            except Exception as e:
                print(f"\n⚠️  Model klasörü silinemedi: {e}")

if __name__ == "__main__":
    # Command line arguments
    download_only = '--download-only' in sys.argv
    
    try:
        if download_only:
            print("\n📥 Model indirme modu...")
            if download_model_from_kaggle():
                print("✅ Model başarıyla indirildi")
                sys.exit(0)
            else:
                print("❌ Model indirme başarısız")
                sys.exit(1)
        else:
            main()
    except Exception as e:
        print(f"\n✗ Hata: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)