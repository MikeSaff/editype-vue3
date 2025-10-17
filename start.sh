#!/bin/bash

# Скрипт запуска проекта Editype

echo "🚀 Starting Editype platform..."
echo ""

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Установите Docker Desktop."
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose не установлен."
    exit 1
fi

# Остановить существующие контейнеры
echo "📦 Stopping existing containers..."
docker compose down

# Собрать и запустить
echo "🔨 Building and starting containers..."
docker compose up -d --build

# Ожидание запуска
echo ""
echo "⏳ Waiting for services to start..."
sleep 10

# Проверка статуса
echo ""
echo "📊 Service status:"
docker compose ps

echo ""
echo "✅ Editype platform is running!"
echo ""
echo "🌐 Access points:"
echo "   Frontend:  http://localhost:3000"
echo "   Backend:   http://localhost:8080"
echo "   MongoDB:   localhost:27017"
echo "   Redis:     localhost:6379"
echo ""
echo "📝 To view logs: docker compose logs -f"
echo "🛑 To stop: docker compose down"
echo ""




