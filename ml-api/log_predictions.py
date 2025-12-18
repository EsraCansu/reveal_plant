"""
Tahmin sonuçlarını API'den alıp veritabanına kaydetme
"""

import json
import requests
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional

# Database ayarları
DB_PATH = Path(__file__).parent.parent / "predictions.db"


class PredictionLogger:
    """API tahminlerini veritabanına kaydetme"""
    
    def __init__(self, db_path: str = str(DB_PATH)):
        self.db_path = db_path
        self.init_db()
    
    def init_db(self):
        """Veritabanını ve tabloları oluştur"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Ana tahmin tablosu
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS prediction_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                image_name TEXT NOT NULL,
                top_class_name TEXT NOT NULL,
                top_confidence REAL NOT NULL,
                processing_time REAL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # Tüm tahminleri kaydetmek için detay tablosu
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS prediction_details (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                prediction_log_id INTEGER NOT NULL,
                class_name TEXT NOT NULL,
                confidence REAL NOT NULL,
                confidence_percent REAL NOT NULL,
                rank INTEGER,
                FOREIGN KEY (prediction_log_id) REFERENCES prediction_log(id)
            )
        ''')
        
        conn.commit()
        conn.close()
        print(f"✓ Veritabanı oluşturuldu: {self.db_path}")
    
    def save_prediction(self, json_response: Dict) -> Optional[int]:
        """
        API yanıtını veritabanına kaydet
        
        Args:
            json_response: API'nin döndürdüğü JSON yanıt
        
        Returns:
            Kaydedilen tahmin ID'si veya None
        """
        if not json_response.get("success"):
            print(f"✗ Tahmin başarısız: {json_response.get('error')}")
            return None
        
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            # Ana tabloyu kaydet
            image_name = json_response.get("image_name", "unknown")
            top_pred = json_response.get("top_prediction", {})
            
            cursor.execute('''
                INSERT INTO prediction_log (
                    image_name, 
                    top_class_name, 
                    top_confidence, 
                    processing_time
                ) VALUES (?, ?, ?, ?)
            ''', (
                image_name,
                top_pred.get("class_name"),
                top_pred.get("confidence"),
                json_response.get("processing_time")
            ))
            
            prediction_id = cursor.lastrowid
            
            # Detay tablosunu kaydet
            all_predictions = json_response.get("all_predictions", [])
            for rank, pred in enumerate(all_predictions, 1):
                cursor.execute('''
                    INSERT INTO prediction_details (
                        prediction_log_id,
                        class_name,
                        confidence,
                        confidence_percent,
                        rank
                    ) VALUES (?, ?, ?, ?, ?)
                ''', (
                    prediction_id,
                    pred.get("class_name"),
                    pred.get("confidence"),
                    pred.get("confidence_percent"),
                    rank
                ))
            
            conn.commit()
            conn.close()
            
            print(f"✓ Tahmin kaydedildi (ID: {prediction_id})")
            print(f"  - Görsel: {image_name}")
            print(f"  - En yüksek tahmin: {top_pred.get('class_name')}")
            print(f"  - Güven: {top_pred.get('confidence_percent'):.2f}%")
            
            return prediction_id
        
        except Exception as e:
            print(f"✗ Veritabanı hatası: {e}")
            return None
    
    def get_predictions(self, limit: int = 10) -> List[Dict]:
        """Son N tahmini getir"""
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        
        cursor.execute('''
            SELECT * FROM prediction_log 
            ORDER BY created_at DESC 
            LIMIT ?
        ''', (limit,))
        
        rows = cursor.fetchall()
        conn.close()
        
        return [dict(row) for row in rows]
    
    def get_prediction_with_details(self, prediction_id: int) -> Optional[Dict]:
        """Tahmin ve tüm detaylarını getir"""
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        
        # Ana tahmin
        cursor.execute('SELECT * FROM prediction_log WHERE id = ?', (prediction_id,))
        main = cursor.fetchone()
        
        if not main:
            return None
        
        # Detaylar
        cursor.execute('''
            SELECT * FROM prediction_details 
            WHERE prediction_log_id = ? 
            ORDER BY rank
        ''', (prediction_id,))
        
        details = [dict(row) for row in cursor.fetchall()]
        conn.close()
        
        return {
            **dict(main),
            "details": details
        }
    
    def get_stats(self) -> Dict:
        """İstatistikleri getir"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute('SELECT COUNT(*) FROM prediction_log')
        total = cursor.fetchone()[0]
        
        cursor.execute('''
            SELECT TOP_CLASS_NAME, COUNT(*) as count 
            FROM prediction_log 
            GROUP BY TOP_CLASS_NAME 
            ORDER BY count DESC 
            LIMIT 5
        ''')
        top_classes = cursor.fetchall()
        
        cursor.execute('SELECT AVG(processing_time) FROM prediction_log')
        avg_time = cursor.fetchone()[0]
        
        conn.close()
        
        return {
            "total_predictions": total,
            "top_5_classes": [{"class": c[0], "count": c[1]} for c in top_classes],
            "average_processing_time": avg_time
        }


# Kullanım Örneği
if __name__ == "__main__":
    logger = PredictionLogger()
    
    # Örnek 1: API'den tahmin al ve kaydet
    print("\n" + "="*60)
    print("ÖRNEK 1: Curl ile tahmin al ve kaydet")
    print("="*60)
    
    # Curl ile tahmini JSON dosyasına kaydet:
    # curl.exe -X POST -F "file=@C:/Users/.../uzum.jpg" http://localhost:8000/predict > prediction.json
    # Sonra bu dosyayı yükle:
    
    try:
        with open("prediction_response.json", "r") as f:
            response = json.load(f)
            logger.save_prediction(response)
    except FileNotFoundError:
        print("⚠️  prediction_response.json dosyası bulunamadı")
    
    # Örnek 2: Requests kütüphanesi ile
    print("\n" + "="*60)
    print("ÖRNEK 2: Python Requests ile tahmin al ve kaydet")
    print("="*60)
    
    image_path = "test_images/uzum.jpg"
    if Path(image_path).exists():
        try:
            with open(image_path, "rb") as f:
                files = {"file": (image_path, f, "image/jpeg")}
                response = requests.post(
                    "http://localhost:8000/predict",
                    files=files
                )
                
                if response.status_code == 200:
                    json_data = response.json()
                    logger.save_prediction(json_data)
                else:
                    print(f"✗ API hatası: {response.status_code}")
        except Exception as e:
            print(f"✗ Hata: {e}")
    else:
        print(f"⚠️  {image_path} bulunamadı")
    
    # Örnek 3: Geçmiş tahmini getir
    print("\n" + "="*60)
    print("ÖRNEK 3: Geçmiş Tahminler")
    print("="*60)
    
    predictions = logger.get_predictions(limit=5)
    for pred in predictions:
        print(f"\nID: {pred['id']}")
        print(f"  Görsel: {pred['image_name']}")
        print(f"  Tahmin: {pred['top_class_name']}")
        print(f"  Güven: {pred['top_confidence']:.2f}")
        print(f"  Saat: {pred['created_at']}")
    
    # Örnek 4: İstatistikler
    print("\n" + "="*60)
    print("İSTATİSTİKLER")
    print("="*60)
    
    stats = logger.get_stats()
    print(f"Toplam tahmin: {stats['total_predictions']}")
    print(f"Ortalama işlem süresi: {stats['average_processing_time']:.3f}s")
    print(f"En sık tahmin edilen 5 sınıf:")
    for i, cls in enumerate(stats['top_5_classes'], 1):
        print(f"  {i}. {cls['class']}: {cls['count']} kez")
