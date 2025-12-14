import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import axios from 'axios';
import bodyParser from 'body-parser';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';

// Environment değişkenlerini yükle
dotenv.config();

// __dirname ve __filename ES6 modules için
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Express uygulamasını oluştur
const app = express();
const PORT = process.env.PORT || 3000;
const JAVA_API_URL = process.env.JAVA_API_URL || 'http://localhost:8080';
const FASTAPI_URL = process.env.FASTAPI_URL || 'http://localhost:8000';

// ======================== MIDDLEWARE ========================
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Statik dosyaları sunmak için
app.use(express.static(path.join(__dirname, 'public')));

// ======================== ROUTES ========================

// Ana sayfa
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

// Demo sayfası
app.get('/demo', (req, res) => {
  res.sendFile(path.join(__dirname, 'demo.html'));
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    message: 'Frontend server is running',
    timestamp: new Date().toISOString()
  });
});

// ======================== PROXY ENDPOINTS ========================

// Java API proxy'si - Kullanıcı işlemleri
app.post('/api/auth/register', async (req, res) => {
  try {
    const response = await axios.post(`${JAVA_API_URL}/api/auth/register`, req.body);
    res.json(response.data);
  } catch (error) {
    console.error('Registration error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Registration failed' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  try {
    const response = await axios.post(`${JAVA_API_URL}/api/auth/login`, req.body);
    res.json(response.data);
  } catch (error) {
    console.error('Login error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Login failed' });
  }
});

app.get('/api/users/:id', async (req, res) => {
  try {
    const response = await axios.get(`${JAVA_API_URL}/api/users/${req.params.id}`);
    res.json(response.data);
  } catch (error) {
    console.error('User fetch error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to fetch user' });
  }
});

app.put('/api/users/:id', async (req, res) => {
  try {
    const response = await axios.put(`${JAVA_API_URL}/api/users/${req.params.id}`, req.body);
    res.json(response.data);
  } catch (error) {
    console.error('User update error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to update user' });
  }
});

// Tahminleme işlemleri
app.get('/api/predictions', async (req, res) => {
  try {
    const response = await axios.get(`${JAVA_API_URL}/api/predictions`);
    res.json(response.data);
  } catch (error) {
    console.error('Predictions fetch error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to fetch predictions' });
  }
});

app.get('/api/predictions/:id', async (req, res) => {
  try {
    const response = await axios.get(`${JAVA_API_URL}/api/predictions/${req.params.id}`);
    res.json(response.data);
  } catch (error) {
    console.error('Prediction fetch error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to fetch prediction' });
  }
});

// Python FastAPI proxy'si - Model tahminleri
app.post('/api/predict', async (req, res) => {
  try {
    const response = await axios.post(`${FASTAPI_URL}/predict`, req.body);
    res.json(response.data);
  } catch (error) {
    console.error('Prediction error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Prediction failed' });
  }
});

// ======================== ERROR HANDLING ========================

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Server error:', err);
  res.status(500).json({ error: 'Internal server error', message: err.message });
});

// ======================== SERVER START ========================

app.listen(PORT, () => {
  console.log(`✅ Frontend server is running on http://localhost:${PORT}`);
  console.log(`📌 Java API URL: ${JAVA_API_URL}`);
  console.log(`📌 FastAPI URL: ${FASTAPI_URL}`);
  console.log(`🔗 Environment: ${process.env.NODE_ENV || 'development'}`);
});

export default app;
