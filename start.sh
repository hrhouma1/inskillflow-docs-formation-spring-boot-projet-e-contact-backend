#!/bin/bash

echo "============================================"
echo "   DEMARRAGE DE L'API CONTACT"
echo "============================================"
echo ""

echo "[1/3] Verification de Docker..."
if ! command -v docker &> /dev/null; then
    echo "ERREUR: Docker n'est pas installe!"
    echo "Installez Docker: https://docs.docker.com/get-docker/"
    exit 1
fi
echo "OK - Docker est installe"

echo ""
echo "[2/3] Demarrage des services..."
docker-compose up -d

echo ""
echo "[3/3] Attente du demarrage (30 secondes)..."
sleep 30

echo ""
echo "============================================"
echo "   TOUT EST PRET!"
echo "============================================"
echo ""
echo "API:     http://localhost:8080"
echo "Swagger: http://localhost:8080/swagger-ui.html"
echo "Emails:  http://localhost:8025"
echo ""
echo "Admin: admin@example.com / admin123"
echo ""
echo "Pour arreter: docker-compose down"
echo "============================================"

