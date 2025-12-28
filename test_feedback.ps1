$response = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -ContentType 'application/json' -Body '{"userName":"Admin","password":"Password123"}'
$token = $response.accessToken
Write-Output "Token received"

$headers = @{
    'Authorization' = "Bearer $token"
}

$feedback = Invoke-RestMethod -Uri 'http://localhost:8080/api/predictions/feedback/all' -Method GET -Headers $headers
if ($feedback.Count -gt 0) {
    Write-Output "First feedback record:"
    $feedback[0] | ConvertTo-Json -Depth 5
} else {
    Write-Output "No feedback found"
}
