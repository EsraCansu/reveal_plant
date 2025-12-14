import os
import sys
import traceback
import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.applications.resnet import preprocess_input  # ÖNEMLİ!

# Dosya yolları
MODEL_PATH = r'C:\Users\esracansu\OneDrive\Masaüstü\transfer_function\cnn\transfer_function\PlantVillage_Resnet101_FineTuning.keras'
IMAGE_PATH = r'C:\Users\esracansu\OneDrive\Masaüstü\transfer_function\uzum.jpg'

def load_model_checked(model_path):
    model_abspath = os.path.abspath(model_path)
    print(f"[DEBUG] Model yolu: {model_abspath}")
    if not os.path.exists(model_abspath):
        raise FileNotFoundError(f"Model dosyası bulunamadı: {model_abspath}")
    try:
        m = load_model(model_abspath)
        print("[DEBUG] Model başarıyla yüklendi.")
        return m
    except Exception:
        print("[ERROR] Model yüklenirken hata oluştu:")
        traceback.print_exc()
        raise

def load_image_checked(image_path):
    image_abspath = os.path.abspath(image_path)
    print(f"[DEBUG] Görüntü yolu: {image_abspath}")
    
    if not os.path.exists(image_abspath):
        raise FileNotFoundError(f"Görüntü dosyası bulunamadı: {image_abspath}")
    
    try:
        with open(image_abspath, 'rb') as f:
            file_bytes = np.asarray(bytearray(f.read()), dtype=np.uint8)
        frame = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        
        if frame is None:
            raise ValueError(f"Görüntü decode edilemedi: {image_abspath}")
        
        print(f"[DEBUG] Görüntü başarıyla yüklendi. Boyut: {frame.shape}")
        return frame
    except Exception as e:
        print(f"[ERROR] Görüntü yüklenirken hata: {e}")
        raise

try:
    model = load_model_checked(MODEL_PATH)
    num_classes = model.output_shape[-1]
    print(f"[DEBUG] Model {num_classes} sınıf için eğitilmiş\n")
    
    class_names = [
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
    
    if len(class_names) != num_classes:
        print(f"[WARNING] Sınıf sayısı uyumsuz! Model: {num_classes}, Liste: {len(class_names)}")
        class_names = [f'Class_{i}' for i in range(num_classes)]

    def preprocess_image(image, target_size=(224, 224)):
        """
        Model için görüntü ön işleme
        NOT: Modeliniz hangi normalizasyon ile eğitildiyse onu kullanın!
        """
        # Resize
        img = cv2.resize(image, target_size)
        # BGR -> RGB
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        
        # Modelinizin eğitim sırasında kullandığı normalizasyonu seçin:
        # SEÇENEK 1: 0-1 arası (en yaygın)
        img = img.astype('float32') / 255.0
        
        # SEÇENEK 2: ResNet ImageNet standart (eğer transfer learning yaptıysanız)
        # img = img.astype('float32')
        # img = preprocess_input(img)
        
        # SEÇENEK 3: -1 ile 1 arası
        # img = (img.astype('float32') / 127.5) - 1.0
        
        # Batch dimension ekle
        img = np.expand_dims(img, axis=0)
        
        print(f"[DEBUG] Preprocessed - min: {img.min():.2f}, max: {img.max():.2f}")
        
        return img

    def predict_image(image):
        """
        Tahmin yap ve tüm anlamlı sonuçları göster
        """
        display_image = image.copy()
        processed_image = preprocess_image(image)
        
        # Tahmin yap
        predictions = model.predict(processed_image, verbose=0)
        
        # Tüm tahminleri sırala (yüksekten düşüğe)
        sorted_indices = np.argsort(predictions[0])[::-1]
        
        # %0.01'den büyük olan tüm tahminleri göster
        print("\n" + "="*60)
        print("TÜM TAHMİNLER (Güven > %0.01)")
        print("="*60)
        
        significant_predictions = []
        for idx in sorted_indices:
            confidence = predictions[0][idx] * 100
            if confidence > 0.01:  # %0.01'den büyük olanları göster
                class_name = class_names[idx]
                significant_predictions.append((class_name, confidence))
                
                # Görsel çubuk
                bar_length = int(confidence / 2)  # Her 2% için bir ■
                bar = "█" * bar_length
                
                print(f"{class_name:50} | {bar} {confidence:6.2f}%")
        
        print("="*60)
        print(f"Toplam anlamlı tahmin sayısı: {len(significant_predictions)}")
        print("="*60 + "\n")
        
        # En yüksek tahmin
        predicted_class = significant_predictions[0][0]
        confidence = significant_predictions[0][1]
        
        # Görüntü üzerine yaz (en iyi 3 tahmini)
        y_offset = 30
        for i, (cls, conf) in enumerate(significant_predictions[:3]):
            # Sınıf ismini kısalt
            short_name = cls.split('___')[-1][:20]
            text = f"{i+1}. {short_name}: {conf:.1f}%"
            
            # Renk: Yeşil -> Sarı -> Kırmızı
            if i == 0:
                color = (0, 255, 0)  # Yeşil
            elif i == 1:
                color = (0, 255, 255)  # Sarı
            else:
                color = (0, 165, 255)  # Turuncu
            
            cv2.putText(display_image, text, (10, y_offset), 
                       cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
            y_offset += 30
        
        return display_image, predicted_class, confidence, significant_predictions

    # Ana akış
    print("[INFO] Görüntü yükleniyor...")
    frame = load_image_checked(IMAGE_PATH)

    # CROP işlemini kaldır veya daha dikkatli uygula
    # Eğer crop gerekiyorsa:
    """
    crop_factor = 0.8  # Daha az agresif crop
    height, width = frame.shape[:2]
    crop_width = int(width * crop_factor)
    crop_height = int(height * crop_factor)
    start_x = (width - crop_width) // 2
    start_y = (height - crop_height) // 2
    cropped_frame = frame[start_y:start_y + crop_height, start_x:start_x + crop_width]
    frame = cv2.resize(cropped_frame, (width, height))
    """
    
    # Tahmin yap
    print("[INFO] Tahmin yapılıyor...")
    result_image, pred_class, conf = predict_image(frame)
    
    print(f"\n{'='*50}")
    print(f"SONUÇ")
    print(f"{'='*50}")
    print(f"Tahmin: {pred_class}")
    print(f"Güven: {conf:.2f}%")
    print(f"{'='*50}\n")
    
    cv2.imshow('CNN Prediction', result_image)
    print("Pencereyi kapatmak için herhangi bir tuşa basınız...")
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    
    print("\n[SUCCESS] Program başarıyla tamamlandı!")

except Exception as e:
    print(f"\n[ERROR] Bir hata oluştu: {e}")
    traceback.print_exc()
    print("\nProgramı kapatmak için Enter tuşuna basınız...")
    input()