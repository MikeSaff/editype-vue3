@echo off
REM Скрипт запуска проекта Editype для Windows

echo 🚀 Starting Editype platform...
echo.

REM Проверка Docker
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Docker не установлен. Установите Docker Desktop.
    exit /b 1
)

REM Остановить существующие контейнеры
echo 📦 Stopping existing containers...
docker compose down

REM Собрать и запустить
echo 🔨 Building and starting containers...
docker compose up -d --build

REM Ожидание запуска
echo.
echo ⏳ Waiting for services to start...
timeout /t 10 /nobreak >nul

REM Проверка статуса
echo.
echo 📊 Service status:
docker compose ps

echo.
echo ✅ Editype platform is running!
echo.
echo 🌐 Access points:
echo    Frontend:  http://localhost:3000
echo    Backend:   http://localhost:8080
echo    MongoDB:   localhost:27017
echo    Redis:     localhost:6379
echo.
echo 📝 To view logs: docker compose logs -f
echo 🛑 To stop: docker compose down
echo.

pause




