# Frontend - React

Bitki hastalığı tespiti uygulamasının React frontend'i

## 📁 Yapı

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/        # Reusable React components
│   │   ├── Upload.jsx     # Görsel yükleme
│   │   └── Result.jsx     # Tahmin sonuçları
│   ├── pages/            # Page components
│   │   └── Home.jsx      # Ana sayfa
│   ├── services/         # API servisleri
│   │   ├── api.js        # Base API config
│   │   ├── predictionService.js
│   │   └── userService.js
│   ├── styles/           # CSS dosyaları
│   │   ├── upload.css
│   │   ├── result.css
│   │   └── home.css
│   ├── App.jsx          # Main app component
│   ├── index.js         # Entry point
│   └── index.css        # Global styles
├── package.json
└── README.md
```

## 🚀 Başlangıç

### Install
```bash
cd frontend
npm install
```

### Dev Server
```bash
npm start
```

Açılacak: http://localhost:3000

### Build
```bash
npm run build
```

## 🔌 API Bağlantısı

**.env** dosyasında:
```
REACT_APP_API_URL=http://localhost:8080/api
```

## 📚 Bileşenler

### Upload.jsx
- Görsel seçme (drag & drop)
- File preview
- Loading state
- Error handling

### Result.jsx
- Top prediction göster
- Güven oranı göster
- Top 5 tahmin listesi
- Progress bars

### predictionService.js
```javascript
// Tahmin yap
await predictionService.getPrediction(imageFile);

// Geçmiş tahmini getir
await predictionService.getPredictionHistory(limit, page);
```

## 🎨 Design

- Ant Design UI library
- Responsive layout
- Mobile friendly
- Dark mode ready

## 📱 Mobile Support

Bootstrap grid sistemi ile fully responsive
