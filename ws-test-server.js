import express from 'express';
import { createServer } from 'http';
import { WebSocketServer } from 'ws';
import { fileURLToPath } from 'url';
import path from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const server = createServer(app);
const wss = new WebSocketServer({ server, path: '/ws/test' });

const PORT = 8081;

// Store connected clients
const clients = new Set();

// WebSocket connection handler
wss.on('connection', (ws) => {
    console.log('📡 Client connected');
    clients.add(ws);

    // Send welcome message
    ws.send(JSON.stringify({
        type: 'welcome',
        message: 'Connected to WebSocket Test Server',
        timestamp: new Date().toISOString()
    }));

    ws.on('message', (data) => {
        try {
            const message = JSON.parse(data);
            console.log('📨 Received:', message);

            // Echo back
            ws.send(JSON.stringify({
                type: 'echo',
                original: message,
                timestamp: new Date().toISOString()
            }));

            // Broadcast to others
            clients.forEach(client => {
                if (client !== ws && client.readyState === 1) {
                    client.send(JSON.stringify({
                        type: 'broadcast',
                        data: message,
                        timestamp: new Date().toISOString()
                    }));
                }
            });

            // Mock prediction response
            if (message.action === 'predict') {
                setTimeout(() => {
                    ws.send(JSON.stringify({
                        type: 'prediction',
                        status: 'success',
                        plantName: 'Apple',
                        diseaseName: 'Apple Scab',
                        confidence: 0.92,
                        recommendation: 'Apply fungicide treatment',
                        timestamp: new Date().toISOString()
                    }));
                }, 2000);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    });

    ws.on('close', () => {
        console.log('❌ Client disconnected');
        clients.delete(ws);
    });

    ws.on('error', (error) => {
        console.error('WebSocket error:', error);
    });
});

// Express routes
app.get('/', (req, res) => {
    res.send('WebSocket Test Server - Use ws://localhost:8081/ws/test');
});

app.get('/health', (req, res) => {
    res.json({
        status: 'ok',
        clients: clients.size,
        timestamp: new Date().toISOString()
    });
});

server.listen(PORT, () => {
    console.log(`🚀 WebSocket Test Server running on ws://localhost:${PORT}/ws/test`);
});
