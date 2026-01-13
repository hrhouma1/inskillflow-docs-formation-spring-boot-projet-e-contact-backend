@echo off
echo ============================================
echo    DEMARRAGE DE L'API CONTACT
echo ============================================
echo.

echo [1/3] Verification de Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Docker n'est pas installe ou pas demarre!
    echo Telechargez Docker Desktop: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)
echo OK - Docker est installe

echo.
echo [2/3] Demarrage des services...
docker-compose up -d

echo.
echo [3/3] Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak >nul

echo.
echo ============================================
echo    TOUT EST PRET!
echo ============================================
echo.
echo API:     http://localhost:8080
echo Swagger: http://localhost:8080/swagger-ui.html
echo Emails:  http://localhost:8025
echo.
echo Admin: admin@example.com / admin123
echo.
echo Pour arreter: docker-compose down
echo ============================================

pause

