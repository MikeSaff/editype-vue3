@echo off
REM Локальный запуск без Docker

echo 🚀 Starting Editype locally (without Docker)...
echo.

REM Проверка MongoDB
echo 📦 Checking MongoDB...
docker ps | findstr editype-mongodb >nul
if %errorlevel% neq 0 (
    echo Starting MongoDB in Docker...
    docker start editype-mongodb 2>nul || docker run -d --name editype-mongodb -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin123 mongo:7.0
)

REM Проверка Redis  
echo 📦 Checking Redis...
docker ps | findstr editype-redis >nul
if %errorlevel% neq 0 (
    echo Starting Redis in Docker...
    docker start editype-redis 2>nul || docker run -d --name editype-redis -p 6379:6379 redis:7-alpine
)

echo.
echo ✅ Database services are running
echo.

REM Backend
echo 🔧 Starting Backend...
start cmd /k "cd backend && mvn spring-boot:run"

REM Ждем немного
timeout /t 5 /nobreak >nul

REM Frontend
echo 🎨 Starting Frontend...
start cmd /k "cd frontend && npm install && npm run dev"

echo.
echo ✅ Services are starting...
echo.
echo 🌐 Access points (when ready):
echo    Frontend:  http://localhost:5173 (Vite dev server)
echo    Backend:   http://localhost:8080
echo    MongoDB:   localhost:27017
echo    Redis:     localhost:6379
echo.

pause




