# 🚀 Быстрая установка и запуск Editype 2.0

## 📋 Предварительные требования

- Docker Desktop (Windows/Mac) или Docker Engine + Docker Compose (Linux)
- Git
- 8 GB RAM минимум (16 GB рекомендуется)
- 10 GB свободного места на диске

---

## ⚡ Быстрый старт (3 команды)

```bash
# 1. Клонировать репозиторий (если еще не клонировано)
git clone <your-repo-url>
cd editype-vue3

# 2. Установить frontend зависимости
cd frontend
npm install
cd ..

# 3. Запустить все сервисы
docker compose up -d
```

**Готово!** 🎉

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MongoDB: localhost:27017

---

## 📦 Что установится

### Docker контейнеры:
- **MongoDB** - База данных
- **Redis** - Кеш (опционально)
- **Backend** - Spring Boot + TeX Live + Pandoc + Vivliostyle
- **Frontend** - Vue 3 + Vite + Nginx

### NPM пакеты (автоматически):
- Tiptap + ProseMirror
- KaTeX
- Citation.js
- Vue 3, Vue Router, Pinia
- Axios, Vue i18n

### Backend (в Docker):
- TeX Live (XeLaTeX, LuaLaTeX)
- Pandoc
- Biber (библиография)
- Vivliostyle CLI
- Шрифты (Liberation, Cyrillic)

---

## 🔍 Проверка установки

### 1. Проверьте Docker контейнеры

```bash
docker compose ps
```

Должны быть запущены:
- `editype-backend`
- `editype-frontend`
- `editype-mongodb`
- `editype-redis` (опционально)

### 2. Проверьте логи

```bash
# Backend
docker compose logs backend | grep -i "started"
# Должно быть: "Application started successfully"

# Frontend
docker compose logs frontend
# Должно быть: "server running at http://localhost:3000"
```

### 3. Проверьте LaTeX

```bash
docker compose exec backend xelatex --version
# Должно вывести версию XeTeX

docker compose exec backend pandoc --version
# Должно вывести версию Pandoc 3.x

docker compose exec backend vivliostyle --version
# Должно вывести версию Vivliostyle
```

---

## 🧪 Первый тест

### 1. Откройте браузер

```
http://localhost:3000
```

### 2. Зарегистрируйтесь

- Email: `test@example.com`
- Password: `password123`

### 3. Создайте статью

1. Нажмите "Create Article"
2. Введите название
3. Попробуйте:
   - Написать текст
   - Вставить формулу (кнопка ∑): `E=mc^2`
   - Добавить ссылку (References → Add Reference)

### 4. Экспортируйте в PDF

1. Сохраните статью (Save)
2. Нажмите "Export"
3. Выберите "PDF (Vivliostyle)"
4. Скачается PDF файл!

---

## 🛠️ Разработка (локально)

Если хотите разрабатывать без Docker:

### Frontend

```bash
cd frontend
npm install
npm run dev
# Откройте http://localhost:5173
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
# Запустится на http://localhost:8080
```

**Важно:** LaTeX и Pandoc должны быть установлены на вашей машине!

---

## 🔄 Обновление

```bash
# Остановить контейнеры
docker compose down

# Обновить код
git pull

# Обновить зависимости
cd frontend && npm install && cd ..

# Пересобрать и запустить
docker compose up -d --build
```

---

## 🗑️ Полная очистка

```bash
# Остановить и удалить контейнеры
docker compose down -v

# Удалить node_modules
rm -rf frontend/node_modules

# Удалить Docker образы
docker rmi editype-backend editype-frontend

# Свежая установка
cd frontend && npm install && cd ..
docker compose up -d --build
```

---

## ⚙️ Конфигурация

### Переменные окружения

Создайте `.env` файл в корне проекта:

```env
# Backend
SPRING_PROFILES_ACTIVE=dev
MONGODB_URI=mongodb://admin:admin123@mongodb:27017/editype?authSource=admin
JWT_SECRET=your-secret-key-change-in-production

# LaTeX
LATEX_ENGINE=xelatex
PDF_EXPORT_TIMEOUT=60000

# Frontend
VITE_API_URL=http://localhost:8080
VITE_EDITORUM_URL=https://editorum.test
```

### Порты (docker-compose.yml)

Если порты заняты, измените:

```yaml
services:
  frontend:
    ports:
      - "3000:80"  # Поменяйте 3000 на свободный порт
  
  backend:
    ports:
      - "8080:8080"  # Поменяйте 8080 на свободный порт
```

---

## 🐛 Частые проблемы

### Проблема: Port already in use

**Решение:**
```bash
# Найдите процесс
netstat -ano | findstr :3000   # Windows
lsof -i :3000                  # Mac/Linux

# Убейте процесс или измените порт в docker-compose.yml
```

### Проблема: npm install fails

**Решение:**
```bash
# Очистите кеш
npm cache clean --force

# Удалите node_modules и package-lock
rm -rf frontend/node_modules frontend/package-lock.json

# Установите заново
cd frontend && npm install
```

### Проблема: Docker build fails

**Решение:**
```bash
# Очистите Docker кеш
docker system prune -a

# Пересоберите с нуля
docker compose build --no-cache
```

### Проблема: MongoDB connection error

**Решение:**
```bash
# Проверьте запущен ли MongoDB
docker compose ps mongodb

# Проверьте логи
docker compose logs mongodb

# Перезапустите
docker compose restart mongodb
```

### Проблема: LaTeX не работает

**Решение:**
```bash
# Зайдите в контейнер
docker compose exec backend sh

# Проверьте установку
which xelatex
which pandoc

# Если нет - пересоберите backend
docker compose build --no-cache backend
docker compose up -d
```

---

## 📞 Поддержка

### Документация

- `LATEX_INTEGRATION_COMPLETE.md` - Полная документация интеграции
- `ARCHITECTURE.md` - Архитектура проекта
- `TESTING.md` - Тестирование
- `RUN.md` - Детальное руководство запуска

### Логи

```bash
# Все логи
docker compose logs -f

# Только backend
docker compose logs -f backend

# Только frontend
docker compose logs -f frontend

# Последние 100 строк
docker compose logs --tail=100
```

---

## ✅ Checklist после установки

- [ ] Docker контейнеры запущены
- [ ] Frontend открывается (http://localhost:3000)
- [ ] Backend отвечает (http://localhost:8080/actuator/health)
- [ ] MongoDB работает
- [ ] Можно зарегистрироваться
- [ ] Можно создать статью
- [ ] Редактор Tiptap загружается
- [ ] Можно вставить формулу
- [ ] Можно добавить ссылку
- [ ] Экспорт в PDF работает

**Если все галочки - поздравляем! 🎉**

---

## 🚀 Что дальше?

1. Прочитайте `LATEX_INTEGRATION_COMPLETE.md` для деталей
2. Попробуйте все функции редактора
3. Создайте тестовую статью с формулами
4. Экспортируйте в разные форматы
5. Интегрируйтесь с Editorum (если нужно)

**Happy coding! 💻✨**


