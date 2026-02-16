# Script pour arrêter tous les processus Node.js
Write-Host "Arrêt de tous les processus Node.js..." -ForegroundColor Yellow

Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "✓ Processus arrêtés" -ForegroundColor Green
Write-Host ""
Write-Host "Vous pouvez maintenant relancer l'application avec:" -ForegroundColor Cyan
Write-Host "  .\start.ps1" -ForegroundColor White
