# ========================
# Frontend (Node.js)
# ========================
<<<<<<< HEAD
FROM node:18-alpine AS frontend
=======
FROM node:18-alpine as frontend
>>>>>>> 1bcfebd949ca5169c9d152616eca3d47c0bb7e14

WORKDIR /app

# Package dosyalarını kopyala
COPY package*.json ./

# Bağımlılıkları yükle
RUN npm ci --only=production

# Uygulama dosyalarını kopyala
COPY . .

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000/api/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

# Sunucuyu başlat
EXPOSE 3000
CMD ["npm", "start"]

# ========================
# CNN Model Server (Python)
# ========================
<<<<<<< HEAD
FROM python:3.11-slim AS fastapi
=======
FROM python:3.11-slim as fastapi
>>>>>>> 1bcfebd949ca5169c9d152616eca3d47c0bb7e14

WORKDIR /app/cnn_model

# Sistem bağımlılıkları
RUN apt-get update && apt-get install -y \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && rm -rf /var/lib/apt/lists/*

# Python bağımlılıklarını yükle
COPY cnn_model/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Uygulama dosyalarını kopyala
COPY cnn_model/ .

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8000/health')" || exit 1

# FastAPI sunucusunu başlat
EXPOSE 8000
CMD ["uvicorn", "fastapi_server:app", "--host", "0.0.0.0", "--port", "8000"]
