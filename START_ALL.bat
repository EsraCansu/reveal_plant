@echo off
REM ============================================
REM   REVEAL PLANT - ALL SERVICES STARTER
REM ============================================
REM   Frontend: Port 3000 (Node.js)
REM   Java API: Port 8080 (Spring Boot)
REM   FastAPI: Port 8000 (TensorFlow/ResNet101)
REM ============================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     REVEAL PLANT - STARTING ALL SERVICES               ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Terminal 1: FastAPI (ML Server) - Port 8000
echo [1/3] Starting FastAPI ML Server (Port 8000)...
start "FastAPI-ML-8000" cmd /k "cd ml-api && conda activate myenv && python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload"
timeout /t 4 /nobreak

REM Terminal 2: Java API (Spring Boot) - Port 8080
echo [2/3] Starting Java Spring Boot API (Port 8080)...
start "Java-API-8080" cmd /k "cd plant_village && java -jar target/plant-village-backend-0.0.1-SNAPSHOT.jar"
timeout /t 4 /nobreak

REM Terminal 3: Frontend (Node.js) - Port 3000
echo [3/3] Starting Frontend Server (Port 3000)...
start "Frontend-3000" cmd /k "npm start"
timeout /t 3 /nobreak

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║          ALL SERVICES STARTED!                         ║
echo ╠════════════════════════════════════════════════════════╣
echo ║                                                        ║
echo ║  - Frontend:      http://localhost:3000                ║
echo ║  - Java API:      http://localhost:8080                ║
echo ║  - FastAPI (ML):  http://localhost:8000                ║
echo ║                                                        ║
echo ║  - API Docs:                                           ║
echo ║     - Java:    http://localhost:8080/swagger-ui.html   ║
echo ║     - FastAPI: http://localhost:8000/docs              ║
echo ║                                                        ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo ║  Press Enter to close this window (services continue)      ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
pause
