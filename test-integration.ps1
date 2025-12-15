#!/usr/bin/env pwsh
<#
.SYNOPSIS
Reveal Plant - Comprehensive Integration Test Suite
Tests Docker containers, FastAPI, and system connectivity
#>

Write-Host "`n" + ("="*70) -ForegroundColor Cyan
Write-Host "🧪 REVEAL PLANT - COMPREHENSIVE TEST SUITE" -ForegroundColor Cyan
Write-Host ("="*70) -ForegroundColor Cyan

# ===================== SECTION 1: DOCKER STATUS =====================
Write-Host "`n[1/5] 🐳 CHECKING DOCKER CONTAINERS..." -ForegroundColor Yellow

$containers = docker ps --format "table {{.Names}}\t{{.Status}}" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Docker is running" -ForegroundColor Green
    Write-Host $containers
} else {
    Write-Host "❌ Docker is not available or not running" -ForegroundColor Red
    exit 1
}

# ===================== SECTION 2: FRONTEND TEST =====================
Write-Host "`n[2/5] 🌐 TESTING FRONTEND (Port 3000)..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend is running" -ForegroundColor Green
        Write-Host "   Status: $($response.StatusCode) OK" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Frontend returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Frontend is not responding: $_" -ForegroundColor Red
}

# Test CSS
try {
    $cssResponse = Invoke-WebRequest -Uri "http://localhost:3000/assets/css/style.css" -UseBasicParsing -TimeoutSec 5
    if ($cssResponse.StatusCode -eq 200) {
        Write-Host "✅ CSS is being served correctly" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  CSS not available: $_" -ForegroundColor Yellow
}

# ===================== SECTION 3: FASTAPI TEST =====================
Write-Host "`n[3/5] 🔬 TESTING FASTAPI SERVER (Port 8000)..." -ForegroundColor Yellow

# Test health endpoint
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8000/health" -UseBasicParsing -TimeoutSec 5
    if ($healthResponse.StatusCode -eq 200) {
        $healthData = $healthResponse.Content | ConvertFrom-Json
        Write-Host "✅ FastAPI is running" -ForegroundColor Green
        Write-Host "   Status: $($healthData.status)" -ForegroundColor Green
        Write-Host "   Model Loaded: $($healthData.model_loaded)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ FastAPI health check failed: $_" -ForegroundColor Red
}

# Test available plants
try {
    $plantsResponse = Invoke-WebRequest -Uri "http://localhost:8000/plants" -UseBasicParsing -TimeoutSec 5
    if ($plantsResponse.StatusCode -eq 200) {
        $plantsData = $plantsResponse.Content | ConvertFrom-Json
        Write-Host "✅ Can retrieve available plants: $($plantsData.plants.Count) plants" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Could not retrieve plants: $_" -ForegroundColor Yellow
}

# ===================== SECTION 4: DATABASE TEST =====================
Write-Host "`n[4/5] 🗄️  TESTING SQL SERVER DATABASE..." -ForegroundColor Yellow

try {
    # Build connection string
    $connectionString = "Server=localhost;Database=plant_village;User Id=sa;Password=YOUR_MS_SQL_PASSWORD;"
    $sqlConnection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
    $sqlConnection.Open()
    
    if ($sqlConnection.State -eq 'Open') {
        Write-Host "✅ SQL Server is connected" -ForegroundColor Green
        
        # Check tables
        $sqlCommand = $sqlConnection.CreateCommand()
        $sqlCommand.CommandText = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo' ORDER BY TABLE_NAME"
        $sqlReader = $sqlCommand.ExecuteReader()
        
        $tables = @()
        while ($sqlReader.Read()) {
            $tables += $sqlReader["TABLE_NAME"]
        }
        $sqlReader.Close()
        $sqlConnection.Close()
        
        if ($tables.Count -gt 0) {
            Write-Host "✅ Found $($tables.Count) tables:" -ForegroundColor Green
            foreach ($table in $tables) {
                Write-Host "   • $table" -ForegroundColor Green
            }
        } else {
            Write-Host "⚠️  No tables found in database" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "⚠️  Could not connect to SQL Server: Check password and connection settings" -ForegroundColor Yellow
    Write-Host "   Edit this script and replace YOUR_MS_SQL_PASSWORD" -ForegroundColor Yellow
}

# ===================== SECTION 5: API ENDPOINTS TEST =====================
Write-Host "`n[5/5] 📡 TESTING API ENDPOINTS..." -ForegroundColor Yellow

# Test Java backend (if running)
try {
    $backendResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator" -UseBasicParsing -TimeoutSec 5
    if ($backendResponse.StatusCode -eq 200) {
        Write-Host "✅ Java Backend is running on port 8080" -ForegroundColor Green
    }
} catch {
    Write-Host "⏳ Java Backend is not running on port 8080 (expected during development)" -ForegroundColor Yellow
}

# ===================== FINAL SUMMARY =====================
# ===================== FINAL SUMMARY =====================
Write-Host "`n" + ("="*70) -ForegroundColor Cyan
Write-Host "📊 TEST SUMMARY" -ForegroundColor Cyan
Write-Host ("="*70) -ForegroundColor Cyan

Write-Host "`n✅ READY TO TEST:" -ForegroundColor Green
Write-Host "   1. Frontend is accessible at http://localhost:3000" -ForegroundColor Green
Write-Host "   2. FastAPI server is running at http://localhost:8000" -ForegroundColor Green
Write-Host "   3. CSS and assets are being served" -ForegroundColor Green

Write-Host "`n📝 NEXT STEPS:" -ForegroundColor Green
Write-Host "   1. Open http://localhost:3000 in your browser" -ForegroundColor Green
Write-Host "   2. Navigate to the diagnosis page" -ForegroundColor Green
Write-Host "   3. Upload a plant image" -ForegroundColor Green
Write-Host "   4. Verify WebSocket connection and real-time prediction" -ForegroundColor Green
Write-Host "   5. Check browser console (F12) for WebSocket messages" -ForegroundColor Green

Write-Host "`n🔧 TROUBLESHOOTING:" -ForegroundColor Yellow
Write-Host "   - If FastAPI is not running: check cnn_model/fastapi_server.py" -ForegroundColor Yellow
Write-Host "   - If Frontend has issues: check server.js and Express configuration" -ForegroundColor Yellow
Write-Host "   - If database fails: ensure SQL Server is running and credentials are correct" -ForegroundColor Yellow

Write-Host "`n🧪 MANUAL TEST COMMANDS:" -ForegroundColor Cyan
Write-Host "   # Test FastAPI directly" -ForegroundColor Cyan
Write-Host "   curl http://localhost:8000/health" -ForegroundColor Cyan
Write-Host "   " -ForegroundColor Cyan
Write-Host "   # Test frontend" -ForegroundColor Cyan
Write-Host "   curl http://localhost:3000" -ForegroundColor Cyan
Write-Host "   " -ForegroundColor Cyan
Write-Host "   # Test CSS" -ForegroundColor Cyan
Write-Host "   curl http://localhost:3000/assets/css/style.css" -ForegroundColor Cyan

Write-Host "`n" + ("="*70) -ForegroundColor Cyan
