# RevealPlant - AI Plant Diagnostics Platform

## 🧪 Demo & Test

**Sityi hemen test etmek için:**
- 👉 `/demo.html` sayfasına gidin
- 4 demo kullanıcıdan birini seçin (bir tık ile giriş yapın)
- Önceden yüklü verileri keşfedin:
  - 6 tanılama örneği (3 bitki tanımlama + 3 hastalık tespiti)
  - Tam profil bilgileri
  - Detaylı istatistikler
  - Tüm özellikleri test edin

### Demo Kullanıcıları
```
1. John Doe        → john@example.com    / demo123
2. Sarah Smith     → sarah@example.com   / demo123
3. Mike Johnson    → mike@example.com    / demo123
4. Emma Williams   → emma@example.com    / demo123
```

---

## 📁 Dizin Yapısı

```
plantreval/
├── index.html                # Ana giriş noktası
├── demo.html                 # Demo & Test sayfası
├── README.md                 # Proje dokümantasyonu
│
├── app/                      # Uygulama mantığı (MVC)
│   ├── controllers/
│   │   └── dummyData.js     # Demo veri ve kullanıcılar
│   ├── models/              # Veri modelleri (hazırlanıyor)
│   ├── routes/
│   │   ├── routes.js        # Frontend router
│   │   └── api.routes.js    # API uç noktaları tanımı
│   └── views/               # HTML sayfaları
│       ├── about.html       # Hakkımızda
│       ├── contact.html     # İletişim
│       ├── login.html       # Giriş
│       ├── register.html    # Kayıt
│       ├── dashboard.html   # Kullanıcı paneli
│       ├── diagnoses.html   # Tanılama geçmişi
│       ├── profile.html     # Profil yönetimi
│       └── settings.html    # Ayarlar
│
├── assets/                  # Statik dosyalar
│   ├── css/
│   │   └── style.css        # Tüm stilleri
│   ├── js/
│   │   └── app.js           # Ana uygulama denetleyicisi
│   └── images/
│       └── logo.png         # Marka logosu
│
└── public/                  # Herkese açık dosyalar
```

---

## 🏗️ Mimari

### Model-View-Controller (MVC)

**Model (Veri)**
- Bitki tanımlama verileri
- Hastalık tespiti verileri
- Kullanıcı profilleri
- Tanılama geçmişi

**View (Görüntüleme)**
- 8 HTML sayfası (responsive Bootstrap)
- CSS stil sistemi (CSS Variables)
- Mobil-friendly tasarım

**Controller (Kontrol)**
- `DiagnosticsController` → Ana iş mantığı
- `Router` → Sayfa yönlendirmesi
- `API Routes` → Backend entegrasyonu hazırı

---

## 🚀 Özellikler

### Giriş Yapan Kullanıcılar İçin

#### 📊 Dashboard
- Tanılama istatistikleri
- Son 5 işlem gösterimi
- Hızlı linkler

#### 📋 Tanılama Geçmişi
- Tüm tanılamaların listesi (filtreleme)
- Detay görüntüleme (modal)
- Rapor indirme
- Silme işlemi

#### 👤 Profil Yönetimi
- Avatar yükleme
- Kişisel bilgiler güncelleme
- Şifre değiştirme
- Hesap istatistikleri

#### ⚙️ Ayarlar
- Bildirim tercihleri
- Gizlilik ayarları
- Görünüm tercihleri
- Veri yönetimi (export, delete)

### Plant Diagnostics Workflow
1. **Adım 1**: Mod seçimi (Bitki Tanıma / Hastalık Tespiti)
2. **Adım 2**: Resim yükleme (Drag & Drop destekli)
3. **Adım 3**: Sonuç gösterme ve indir
4. **Otomatik kayıt**: Tanılamalar kullanıcı hesabında saklanır

---

## 🔐 Veri Depolaması

Şu anda **localStorage** kullanıyor:
- ✅ Kullanıcı giriş bilgileri
- ✅ Profil bilgileri
- ✅ Tanılama geçmişi (6 örnek)
- ✅ Kullanıcı tercihleri

### Gelecekte Backend:
```javascript
// app/routes/api.routes.js'de tanımlı:
- POST   /auth/login
- POST   /auth/register
- POST   /diagnostics/identify
- POST   /diagnostics/detect-disease
- GET    /diagnostics/history
- PUT    /profile
```

### Kullanıcı İnteraksiyonları
- Dosya seçimi ve upload
- Drag & Drop desteği
- Geriye gitme seçeneği
- Rapor indirme

## 📝 CSS Organizasyonu

```css
:root { }              /* Renk değişkenleri */
body { }               /* Genel stilleri */
/* Navbar */
/* Butonlar */
/* Kartlar & Konteynerler */
/* Adımlar & İlerleme */
/* Mod Seçici */
/* Resimler */
/* Footer */
/* Responsive Tasarımlar */
```

## 🔧 JavaScript Organizasyonu

### DiagnosticsController Sınıfı

```javascript
class DiagnosticsController {
    - constructor()           // Başlatma
    - initializeEventListeners()
    - handleFileUpload()
    - processImage()
    - handleDragOver/Leave/Drop()
    - selectMode()
    - showResults()
    - showStep()
    - goBack()
    - downloadReport()
}
```

## 🎨 Renk Şeması

```css
--brand-dark: #2D7A4A      /* Ana yeşil */
--brand-light: #67A64B     /* Açık yeşil */
--accent: #F4D05E          /* Sarı aksan */
--earth: #733702           /* Kahverengi */
--bg: #F6F8F5              /* Arka plan */
--surface: #FFFFFF         /* Yüzey */
--text: #122017            /* Metin */
--muted: #7A8B7A           /* Yumuşak metin */
```

## 📱 Responsive Breakpoints

- Desktop: 1200px+
- Tablet: 768px - 1199px
- Mobile: < 768px

## 🔌 Genişletme Noktaları

1. **Yeni Modeller Ekleme**
   - `app/models/` klasörüne yeni dosya ekle

2. **Yeni Kontrolör İşlevleri**
   - `DiagnosticsController` sınıfına metod ekle

3. **Yeni Sayfalar/Bölümler**
   - `app/views/` klasörüne HTML şablonu ekle
   - CSS stillerini `assets/css/style.css`'ye ekle

4. **API Entegrasyonu**
   - `app/models/` içinde API hizmetleri oluştur
   - `DiagnosticsController`'da metodlara çağrı ekle

## 💡 Best Practices

✅ CSS değişkenleri kullan - kolay tema değişimi
✅ Sınıf tabanlı JavaScript - daha iyi organizasyon
✅ HTML semantik - erişilebilirlik ve SEO
✅ Mobile-first - responsive tasarım
✅ Moduler yapı - kolay bakım ve güncelleme

## 🔄 Bakım İş Akışı

1. **Stil Güncellemeleri** → `assets/css/style.css`
2. **İş Mantığı Güncellemeleri** → `assets/js/app.js` (DiagnosticsController)
3. **HTML Yapı Değişiklikleri** → `index.html`
4. **Veri Modelleri** → `app/models/`
