# 🚀 Быстрый старт Editype

## ✅ Проект готов!

Все компоненты созданы и протестированы. Теперь запустим приложение.

## 📦 Что у нас есть

- ✅ **Backend** (Java 17 + Spring Boot 3) - работает
- ✅ **Frontend** (Vue 3 + Vite) - собран
- ✅ **MongoDB** - запущен в Docker
- ✅ **Redis** - запущен в Docker
- ✅ **Docker Compose** - настроен

## 🎯 Самый быстрый способ запуска

### Windows:

```bash
.\start-local.bat
```

Этот скрипт автоматически:
1. ✅ Запустит MongoDB в Docker
2. ✅ Запустит Redis в Docker  
3. ✅ Запустит Backend (Spring Boot на порту 8080)
4. ✅ Запустит Frontend (Vite dev server на порту 5173)

**Время запуска**: ~1-2 минуты

### Linux/macOS:

```bash
# 1. Запустить базы данных
docker run -d --name editype-mongodb -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  mongo:7.0

docker run -d --name editype-redis -p 6379:6379 redis:7-alpine

# 2. Запустить Backend
cd backend
mvn spring-boot:run &

# 3. Запустить Frontend  
cd ../frontend
npm install
npm run dev
```

## 🌐 Доступ к приложению

После запуска откройте в браузере:

### Frontend
```
http://localhost:5173
```
или (если используете Docker)
```
http://localhost:3000
```

### Backend API
```
http://localhost:8080
```

### Проверка Backend
```bash
curl http://localhost:8080/actuator/health
```

## 📝 Первый запуск

### 1. Регистрация нового пользователя

Откройте http://localhost:5173 и:
1. Нажмите "Register"
2. Введите email (например: `admin@editype.com`)
3. Введите пароль (минимум 6 символов)
4. Нажмите "Register"

Вы автоматически войдете в систему!

### 2. Создайте первую публикацию

1. Перейдите в раздел "Publications"
2. Нажмите "Create New Publication"
3. Переключайтесь между языками (RU/EN/CN)
4. Заполните заголовок, метаданные и текст для каждого языка
5. Добавьте DOI (опционально)
6. Сохраните

### 3. Экспортируйте публикацию

1. В списке публикаций нажмите "Export"
2. Выберите язык экспорта
3. Выберите формат (HTML, XML JATS, или TXT)
4. Файл скачается автоматически

## 🐳 Альтернатива: Docker Compose

Если хотите запустить все в Docker:

```bash
docker compose up -d --build
```

⚠️ **Внимание**: Первая сборка займет 5-10 минут (Maven скачивает зависимости)

## 🔧 Текущий статус

### ✅ Запущено
- MongoDB (Docker, порт 27017)
- Redis (Docker, порт 6379)
- Backend (локально, порт 8080)
- Frontend (запускается, порт 5173)

### 🔍 Проверка статуса

```powershell
# Проверить Docker контейнеры
docker ps

# Проверить Backend (должен вернуть JSON)
curl http://localhost:8080/actuator/health

# Проверить Frontend
curl http://localhost:5173
```

## 📡 API Примеры

### Регистрация
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "roles": ["AUTHOR"]
  }'
```

### Вход
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Вернет токен:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "test@example.com",
  "roles": ["AUTHOR"]
}
```

### Использование токена
```bash
curl http://localhost:8080/api/publications \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 🎨 Особенности

### Мультиязычность
- **UI**: Переключается кнопками RU/EN/CN в навигации
- **Контент**: Публикации хранят данные на всех языках
- **Экспорт**: Можно выбрать язык при экспорте

### Роли пользователей
- **AUTHOR** - может создавать публикации
- **EDITOR** - может редактировать
- **REVIEWER** - может рецензировать

### Экспорт
- **HTML** - полностью стилизованный документ
- **XML JATS** - Journal Article Tag Suite формат
- **TXT** - простой текстовый формат

## 🚨 Troubleshooting

### Backend не запускается
```bash
# Проверить порт
netstat -ano | findstr :8080

# Убить процесс если нужно
taskkill /PID <PID> /F

# Проверить MongoDB
docker ps | findstr mongodb
```

### Frontend ошибки
```bash
# Переустановить зависимости
cd frontend
rm -rf node_modules
npm install
npm run dev
```

### MongoDB connection refused
```bash
# Перезапустить MongoDB
docker restart editype-mongodb

# Или пересоздать
docker rm -f editype-mongodb
docker run -d --name editype-mongodb -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  mongo:7.0
```

## 📚 Дополнительная документация

- `README.md` - Обзор проекта
- `ARCHITECTURE.md` - Детальная архитектура
- `TESTING.md` - Руководство по тестированию
- `COMMANDS.md` - Полезные команды
- `STATUS.md` - Текущий статус проекта

## ✨ Готово к работе!

Приложение запущено и готово к использованию! 

🌐 **Откройте**: http://localhost:5173  
🔐 **Зарегистрируйтесь** и начните создавать публикации!

---

**Нужна помощь?** Смотрите `STATUS.md` для диагностики или `COMMANDS.md` для дополнительных команд.




