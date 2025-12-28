@echo off
chcp 65001 >nul
title Reveal Plant - System Shutdown
color 0C

echo ============================================================
echo           REVEAL PLANT - SYSTEM SHUTDOWN
echo ============================================================
echo.

echo [1/3] Java (Spring Boot) durduruluyor...
taskkill /F /IM java.exe 2>nul
echo       ✓ Java durduruldu
echo.

echo [2/3] Node.js durduruluyor...
taskkill /F /IM node.exe 2>nul
echo       ✓ Node.js durduruldu
echo.

echo [3/3] Python (FastAPI) durduruluyor...
taskkill /F /IM python.exe 2>nul
taskkill /F /IM uvicorn.exe 2>nul
echo       ✓ Python durduruldu
echo.

echo ============================================================
echo             TUM SERVISLER DURDURULDU!
echo ============================================================
echo.

timeout /t 3
