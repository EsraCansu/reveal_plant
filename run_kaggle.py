#!/usr/bin/env python3
"""
Kaggle Model Tetikleyici Script
1. Lokal test görseli seçer
2. Kaggle notebook'a kopyalar
3. Kaggle'a gönderir ve çalıştırır
4. Sonuçları indirir
"""

import os
import sys
import json
import shutil
import subprocess
from pathlib import Path
from typing import Optional

def select_test_image() -> Optional[Path]:
    """Kullanıcıdan test görseli seçmesini iste"""
    print("\n" + "="*60)
    print("🖼️  TEST GÖRSELI SEÇİMİ")
    print("="*60)
    
    # Mevcut görsel dosyalarını listele
    test_dirs = [
        Path.cwd() / "test_images",
        Path.cwd() / "sample_images",
        Path.home() / "Pictures"
    ]
    
    image_files = []
    for test_dir in test_dirs:
        if test_dir.exists():
            image_files.extend(test_dir.glob("*.jpg"))
            image_files.extend(test_dir.glob("*.png"))
            image_files.extend(test_dir.glob("*.jpeg"))
    
    if not image_files:
        print("⚠️  Görsel bulunamadı!")
        print("   test_images/ klasörüne PNG veya JPG ekle")
        return None
    
    # GitHub Actions'te veya interaktif olmayan modda first image'ı seç
    if not sys.stdin.isatty():  # CI/CD environment
        selected = image_files[0]
        print(f"✓ CI Mode: İlk görsel seçildi: {selected.name}")
        return selected
    
    # Görselleri listele
    print(f"\nBulunan görseller ({len(image_files)}):")
    for i, img in enumerate(image_files[:10], 1):
        print(f"  {i}. {img.name} ({img.parent.name}/)")
    
    # Kullanıcı seçimi
    while True:
        try:
            choice = input("\nGörsel numarası seçin (1-" + str(min(10, len(image_files))) + "): ").strip()
            idx = int(choice) - 1
            if 0 <= idx < len(image_files):
                selected = image_files[idx]
                print(f"✓ Seçilen: {selected.name}")
                return selected
            else:
                print("⚠️  Geçersiz seçim!")
        except (ValueError, IndexError):
            print("⚠️  Geçersiz input!")

def copy_image_to_notebook(image_path: Path) -> bool:
    """Test görseli notebook klasörüne kopyala"""
    print("\n" + "="*60)
    print("📁 GÖRSEL KOPYALANIYOR")
    print("="*60)
    
    notebook_dir = Path.cwd() / "kaggle_notebook"
    notebook_dir.mkdir(exist_ok=True)
    
    # Eski görselleri temizle
    for img in notebook_dir.glob("test_image.*"):
        img.unlink()
    
    # Yeni görseli kopyala
    dest = notebook_dir / f"test_image{image_path.suffix}"
    shutil.copy2(image_path, dest)
    
    print(f"✓ Görsel kopyalandı: {dest}")
    return True

def push_to_kaggle() -> bool:
    """Notebook'u Kaggle'a gönder"""
    print("\n" + "="*60)
    print("🚀 KAGGLE'A GÖNDERİLİYOR")
    print("="*60)
    
    # Kaggle credentials kontrol
    username = os.getenv('KAGGLE_USERNAME')
    key = os.getenv('KAGGLE_KEY')
    
    if not username or not key:
        print("⚠️  Kaggle credentials bulunamadı!")
        print("   KAGGLE_USERNAME ve KAGGLE_KEY environment variable'ları gerekli")
        return False
    
    # Kaggle config oluştur
    kaggle_dir = Path.home() / ".kaggle"
    kaggle_dir.mkdir(exist_ok=True)
    
    config = {"username": username, "key": key}
    config_path = kaggle_dir / "kaggle.json"
    
    with open(config_path, 'w') as f:
        json.dump(config, f)
    
    # Permissions ayarla (Unix)
    if os.name != 'nt':  # Windows değilse
        config_path.chmod(0o600)
    
    print(f"✓ Kaggle config oluşturuldu: {config_path}")
    
    notebook_dir = Path.cwd() / "kaggle_notebook"
    
    # Kaggle CLI komutu
    cmd = ["kaggle", "kernels", "push", "-p", str(notebook_dir)]
    
    try:
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            print("✓ Notebook Kaggle'a gönderildi")
            print(result.stdout)
            return True
        else:
            print(f"⚠️  Hata: {result.stderr}")
            return False
    except FileNotFoundError:
        print("⚠️  Kaggle CLI yüklenmedi!")
        print("   Kur: pip install kaggle")
        return False

def run_notebook() -> bool:
    """Notebook'u çalıştır (lokal fallback)"""
    print("\n" + "="*60)
    print("▶️  NOTEBOOK ÇALIŞTIRILIYYOR")
    print("="*60)
    
    notebook_path = Path.cwd() / "kaggle_notebook" / "notebook.ipynb"
    
    # Notebook var mı kontrol et
    if not notebook_path.exists():
        print(f"⚠️  Notebook bulunamadı: {notebook_path}")
        print("   Fallback moduna geçiliyor...")
        return create_fallback_predictions()
    
    try:
        import nbformat
        from nbconvert.preprocessors import ExecutePreprocessor
        
        # Notebook'u yükle (UTF-8 encoding)
        with open(notebook_path, encoding='utf-8') as f:
            nb = nbformat.read(f, as_version=4)
        
        # Çalıştır (available kernels'den seç)
        try:
            ep = ExecutePreprocessor(timeout=600, kernel_name='python3')
        except:
            ep = ExecutePreprocessor(timeout=600)
        
        ep.preprocess(nb, {'metadata': {'path': str(notebook_path.parent)}})
        
        # Sonucu kaydet (UTF-8 encoding)
        with open(notebook_path, 'w', encoding='utf-8') as f:
            nbformat.write(nb, f)
        
        print("✓ Notebook çalıştırıldı")
        return True
        
    except ImportError as e:
        print(f"⚠️  Gerekli kütüphane yüklenmedi: {e}")
        print("   Kur: pip install nbconvert jupyter nbformat")
        print("   Fallback moduna geçiliyor...")
        return create_fallback_predictions()
        
    except Exception as e:
        print(f"⚠️  Notebook çalıştırma hatası: {e}")
        print("   Fallback moduna geçiliyor...")
        return create_fallback_predictions()

def create_fallback_predictions() -> bool:
    """Notebook çalıştırılamazsa tahmin JSON'ını manuel oluştur"""
    print("\n📋 Fallback: Tahmin sonucu oluşturuluyor...")
    
    try:
        notebook_dir = Path.cwd() / "kaggle_notebook"
        test_image_paths = list(notebook_dir.glob("test_image.*"))
        
        if not test_image_paths:
            print("⚠️  Test görseli bulunamadı")
            return False
        
        test_image = test_image_paths[0]
        
        # Basit dummy tahmin (gerçek model yükleme çalışmıyorsa)
        result = {
            'image': test_image.name,
            'success': False,
            'error': 'Notebook execution failed - model could not be loaded locally',
            'note': 'Bu lokal fallback sonucudur. Gerçek tahmin için Kaggle\'da çalıştırılmalı.',
            'fallback': True
        }
        
        results = [result]
        
        # Sonuçları kaydet
        output_file = notebook_dir / 'predictions.json'
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        
        print(f"✓ Fallback predictions oluşturuldu: {output_file}")
        return True
        
    except Exception as e:
        print(f"⚠️  Fallback da başarısız: {e}")
        import traceback
        traceback.print_exc()
        return False

def download_results() -> bool:
    """Sonuçları indir"""
    print("\n" + "="*60)
    print("📥 SONUÇLAR İNDİRİLİYOR")
    print("="*60)
    
    results_dir = Path.cwd() / "results"
    results_dir.mkdir(exist_ok=True)
    
    predictions_file = Path.cwd() / "kaggle_notebook" / "predictions.json"
    
    if predictions_file.exists():
        dest = results_dir / "predictions.json"
        shutil.copy2(predictions_file, dest)
        print(f"✓ Sonuçlar kaydedildi: {dest}")
        
        # Sonuçları göster
        try:
            with open(dest, encoding='utf-8') as f:
                results = json.load(f)
            
            print(f"\n📊 TAHMIN SONUÇLARI:")
            for result in results:
                if result.get('success'):
                    print(f"  ✓ {result['image']}: {result.get('top_prediction', 'N/A')}")
                    print(f"    Güven: {result.get('top_confidence', 0)*100:.2f}%")
                elif result.get('fallback'):
                    print(f"  ⓘ {result['image']}: Fallback sonucu (lokal çalıştırma başarısız)")
                    print(f"    Not: {result.get('note', '')}")
                else:
                    print(f"  ✗ {result['image']}: {result.get('error', 'Hata')}")
        except Exception as e:
            print(f"⚠️  Sonuçlar okunamadı: {e}")
        
        return True
    else:
        print("⚠️  Tahmin sonuçları bulunamadı!")
        print(f"   Beklenen dosya: {predictions_file}")
        return False

def main():
    print("\n" + "🌱"*30)
    print("REVEAL PLANT - Kaggle Tahmin Pipeline")
    print("🌱"*30)
    
    try:
        # Step 1: Test görseli seç
        image = select_test_image()
        if not image:
            sys.exit(1)
        
        # Step 2: Görseli notebook'a kopyala
        if not copy_image_to_notebook(image):
            sys.exit(1)
        
        # Step 3: Notebook'u çalıştır (lokal)
        success = run_notebook()
        if not success:
            print("⚠️  Lokal çalıştırma başarısız, ancak fallback sonuç oluşturuldu")
        
        # Step 4: Sonuçları indir
        if not download_results():
            print("⚠️  Sonuç indirme başarısız!")
            sys.exit(1)
        
        print("\n" + "="*60)
        print("✅ TAMAMLANDI!")
        print("="*60)
        print(f"Sonuçlar: {Path.cwd() / 'results' / 'predictions.json'}")
        
        if not success:
            print("\n💡 İPUCU:")
            print("   Lokal çalıştırma başarısız oldu. Gerçek tahmin için:")
            print("   1. Kaggle'a gönder: python run_kaggle.py --push")
            print("   2. veya gerekli kütüphaneleri yükle: pip install nbconvert jupyter nbformat")
        
    except KeyboardInterrupt:
        print("\n⚠️  İşlem iptal edildi")
        sys.exit(1)
    except Exception as e:
        print(f"\n✗ Hata: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()