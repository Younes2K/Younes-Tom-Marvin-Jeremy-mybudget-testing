# Script de lancement de l'application Budget Personnel
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Budget Personnel - Lancement" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Lancer le backend
Write-Host "[1/2] Démarrage du backend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; npm run dev" -WorkingDirectory $PSScriptRoot

Start-Sleep -Seconds 2

# Lancer le frontend
Write-Host "[2/2] Démarrage du frontend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev" -WorkingDirectory $PSScriptRoot

Write-Host ""
Write-Host "✓ Application lancée avec succès!" -ForegroundColor Green
Write-Host ""
Write-Host "Backend:  http://localhost:3001" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:5173" -ForegroundColor Cyan
Write-Host ""
Write-Host "Les deux terminaux vont s'ouvrir dans des fenêtres séparées." -ForegroundColor Gray
Write-Host "Appuyez sur une touche pour fermer ce message..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
