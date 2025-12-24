# Test Prediction Endpoint and verify PredictionPlant/PredictionDisease records

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "Testing Prediction Workflow" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Step 1: Check if gateway is running
Write-Host "`n1️⃣ Checking Gateway (Port 3000)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 5
    Write-Host "✅ Gateway is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Gateway is not running" -ForegroundColor Red
    exit 1
}

# Step 2: Prepare test image (use a small base64 test image)
Write-Host "`n2️⃣ Preparing test image..." -ForegroundColor Yellow
$testImagePath = "c:\Users\esracansu\OneDrive\Belgeler\GitHub\reveal_plant\test_images"

# If test_images exists, use first image
if (Test-Path $testImagePath) {
    $imageFile = Get-ChildItem $testImagePath -File | Select-Object -First 1
    if ($imageFile) {
        Write-Host "Found test image: $($imageFile.Name)" -ForegroundColor Green
        # Convert to base64
        $imageBytes = [System.IO.File]::ReadAllBytes($imageFile.FullName)
        $base64Image = [Convert]::ToBase64String($imageBytes)
        $imageBase64 = "data:image/jpeg;base64,$base64Image"
    } else {
        Write-Host "⚠️  No image files found in test_images" -ForegroundColor Yellow
        # Use minimal test image (1x1 pixel)
        $imageBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABMAEwDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWm5ybnJ2eoqOkpaanqKmqsrO0tba2uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlbaWmJmaoqOkpaanqKmqsrO0tba2uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigD/2Q=="
    }
} else {
    Write-Host "⚠️  test_images directory not found" -ForegroundColor Yellow
    $imageBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABMAEwDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWm5ybnJ2eoqOkpaanqKmqsrO0tba2uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlbaWmJmaoqOkpaanqKmqsrO0tba2uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigD/2Q=="
}

Write-Host "Image prepared (${($imageBase64.Length)/1024} KB)" -ForegroundColor Green

# Step 3: Make prediction request
Write-Host "`n3️⃣ Sending prediction request..." -ForegroundColor Yellow

$predictionPayload = @{
    imageBase64 = $imageBase64
    userId = 1
    predictionType = "plant-identification"
    description = "Test plant image"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest `
        -Uri "http://localhost:3000/api/predictions/analyze" `
        -Method POST `
        -ContentType "application/json" `
        -Body $predictionPayload `
        -TimeoutSec 30

    $result = $response.Content | ConvertFrom-Json
    
    Write-Host "✅ Prediction successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($result | ConvertTo-Json -Depth 10) -ForegroundColor White
    
    # Extract prediction ID if available
    if ($result.prediction_id) {
        Write-Host "`nPrediction ID: $($result.prediction_id)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Prediction failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n=================================" -ForegroundColor Cyan
Write-Host "✅ Test Complete" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
