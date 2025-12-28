import multer from 'multer';
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import axios from 'axios';
import bodyParser from 'body-parser';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';

// Load environment variables
dotenv.config();

// __dirname and __filename for ES6 modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Create Express application
const app = express();
const PORT = process.env.PORT || 3000;
const JAVA_API_URL = process.env.JAVA_API_URL || 'http://localhost:8080';
const FASTAPI_URL = process.env.FASTAPI_URL || 'http://localhost:8000';

// ======================== MIDDLEWARE ========================
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Configuration to store uploaded file in memory
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

// For serving static files
app.use(express.static(path.join(__dirname)));  // Serve all files in root directory
app.use('/assets', express.static(path.join(__dirname, 'assets')));  // Assets folder
app.use('/app', express.static(path.join(__dirname, 'app')));  // App folder

// ======================== ROUTES ========================

// Home page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

// WebSocket Test Suite
app.get('/websocket-test.html', (req, res) => {
  res.sendFile(path.join(__dirname, 'WEBSOCKET_TEST.html'));
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

// Java API proxy - User operations
/*app.post('/api/auth/register', async (req, res) => {
  try {
    const response = await axios.post(`${JAVA_API_URL}/api/users/register`, req.body);
    res.json(response.data);
  } catch (error) {
    console.error('Registration error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Registration failed' });
  }
});*/
// Java API - Prediction (Image Analysis)
// upload.single('image') middleware places the uploaded file in req.file object.
app.post('/api/predict', upload.single('image'), async (req, res) => {
  
  if (!req.file) {
    return res.status(400).json({ error: 'Image file not uploaded. Please upload an image.' });
  }

  try {
    // 1. Convert the uploaded image Buffer (binary data in memory) to Base64 string
    const base64Image = req.file.buffer.toString('base64');
    
    // 2. Create the payload structure expected by Java Backend
    const javaApiPayload = {
      imageBase64: base64Image,
      predictionType: req.body.mode || 'detect-disease',
      userId: req.body.userId || 1,
      description: req.body.description || 'Uploaded plant image'
    };

    console.log(`📤 Sending to Java Backend: ${JAVA_API_URL}/api/predictions/analyze`);

    // 3. Send request to Java Backend
    const response = await axios.post(
      `${JAVA_API_URL}/api/predictions/analyze`, 
      javaApiPayload,
      { headers: { 'Content-Type': 'application/json' } }
    );
    
    console.log('📥 Received from Java:', response.data);
    
    // 4. Return response to client
    res.json(response.data);

  } catch (error) {
    console.error('❌ Prediction error:', error.message);
    if (error.response) {
      console.error('Error response:', error.response.data);
    }
    const errorMessage = error.response?.data?.message || error.response?.data?.error || 'Prediction operation failed';
    res.status(error.response?.status || 500).json({ error: errorMessage });
  }
});

app.post('/api/users/register', async (req, res) => {
  try {
    const response = await axios.post(`${JAVA_API_URL}/api/users/register`, req.body);
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

app.get('/api/predictions/:id', async (req, res) => {
  try {
    const response = await axios.get(`${JAVA_API_URL}/api/predictions/${req.params.id}`);
    res.json(response.data);
  } catch (error) {
    console.error('Prediction fetch error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to fetch prediction' });
  }
});

// Get prediction history for user
app.get('/api/predictions/history/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const response = await axios.get(`${JAVA_API_URL}/api/predictions/history/${userId}`);
    res.json(response.data);
  } catch (error) {
    console.error('Prediction history error:', error.message);
    res.status(error.response?.status || 500).json(error.response?.data || { error: 'Failed to fetch prediction history' });
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
