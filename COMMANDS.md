# Полезные команды Editype

## 🚀 Быстрый старт

### Windows
```bash
.\start.bat
```

### Linux/macOS
```bash
chmod +x start.sh
./start.sh
```

### Вручную
```bash
docker compose up -d --build
```

## 🐳 Docker команды

### Управление контейнерами
```bash
# Запуск
docker compose up -d

# Пересборка и запуск
docker compose up -d --build

# Остановка
docker compose down

# Остановка с удалением volumes
docker compose down -v

# Перезапуск
docker compose restart

# Перезапуск конкретного сервиса
docker compose restart backend
```

### Логи
```bash
# Все сервисы
docker compose logs -f

# Конкретный сервис
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mongodb

# Последние 100 строк
docker compose logs --tail=100 backend
```

### Статус и мониторинг
```bash
# Статус контейнеров
docker compose ps

# Использование ресурсов
docker stats

# Health check
curl http://localhost:8080/actuator/health
```

## 🔧 Backend команды

### Локальная разработка
```bash
cd backend

# Запуск
./mvnw spring-boot:run

# Запуск с профилем
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Сборка
./mvnw clean package

# Пропуск тестов
./mvnw clean package -DskipTests

# Запуск тестов
./mvnw test

# Очистка
./mvnw clean
```

### Docker backend
```bash
# Пересборка только backend
docker compose build backend

# Запуск только backend
docker compose up backend

# Войти в контейнер
docker exec -it editype-backend sh

# Просмотр логов приложения
docker exec editype-backend cat /app/logs/application.log
```

## 🎨 Frontend команды

### Локальная разработка
```bash
cd frontend

# Установка зависимостей
npm install

# Dev server
npm run dev

# Сборка
npm run build

# Preview production build
npm run preview
```

### Docker frontend
```bash
# Пересборка только frontend
docker compose build frontend

# Запуск только frontend
docker compose up frontend

# Войти в контейнер
docker exec -it editype-frontend sh

# Проверить nginx конфигурацию
docker exec editype-frontend nginx -t
```

## 💾 MongoDB команды

### Подключение
```bash
# Войти в MongoDB shell
docker exec -it editype-mongodb mongosh -u admin -p admin123 --authenticationDatabase admin

# Использовать БД editype
use editype

# Показать коллекции
show collections

# Найти всех пользователей
db.users.find().pretty()

# Найти все публикации
db.publications.find().pretty()

# Удалить все публикации
db.publications.deleteMany({})

# Создать индекс
db.users.createIndex({ email: 1 }, { unique: true })
```

### Бэкап и восстановление
```bash
# Бэкап
docker exec editype-mongodb mongodump --uri="mongodb://admin:admin123@localhost:27017/editype?authSource=admin" --out=/backup

# Копировать бэкап из контейнера
docker cp editype-mongodb:/backup ./mongodb-backup

# Восстановление
docker exec editype-mongodb mongorestore --uri="mongodb://admin:admin123@localhost:27017/?authSource=admin" /backup
```

## 🔴 Redis команды

### Подключение
```bash
# Войти в Redis CLI
docker exec -it editype-redis redis-cli

# Проверить все ключи
KEYS *

# Получить значение
GET key_name

# Установить значение
SET key_name "value"

# Удалить все данные
FLUSHALL

# Информация
INFO
```

## 🌐 API тестирование

### Auth
```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "roles": ["AUTHOR"]
  }'

# Логин
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Users (с токеном)
```bash
# Получить всех пользователей
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"

# Создать пользователя
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "roles": ["AUTHOR"]
  }'
```

### Publications
```bash
# Получить все публикации
curl http://localhost:8080/api/publications \
  -H "Authorization: Bearer YOUR_TOKEN"

# Создать публикацию
curl -X POST http://localhost:8080/api/publications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "titles": {
      "en": "My Publication",
      "ru": "Моя публикация"
    },
    "texts": {
      "en": "Content here",
      "ru": "Содержание здесь"
    },
    "doi": "10.1000/test"
  }'
```

### Export
```bash
# Экспорт в HTML
curl http://localhost:8080/api/export/{id}/html?lang=en \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o publication.html

# Экспорт в XML
curl http://localhost:8080/api/export/{id}/jats?lang=en \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o publication.xml

# Экспорт в TXT
curl http://localhost:8080/api/export/{id}/txt?lang=en \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o publication.txt
```

## 🧹 Очистка

### Очистка Docker
```bash
# Удалить остановленные контейнеры
docker container prune

# Удалить неиспользуемые образы
docker image prune -a

# Удалить неиспользуемые volumes
docker volume prune

# Полная очистка
docker system prune -a --volumes
```

### Очистка проекта
```bash
# Backend
cd backend
./mvnw clean
rm -rf target/

# Frontend
cd frontend
rm -rf node_modules/ dist/
npm install
```

## 🔍 Отладка

### Проверка портов
```bash
# Windows
netstat -ano | findstr :3000
netstat -ano | findstr :8080

# Linux/macOS
lsof -i :3000
lsof -i :8080
```

### Проверка сети Docker
```bash
# Список сетей
docker network ls

# Инспектирование сети
docker network inspect editype-vue3_editype-network

# Проверка DNS
docker exec editype-backend ping mongodb
docker exec editype-frontend ping backend
```

### Проверка переменных окружения
```bash
# В backend контейнере
docker exec editype-backend env | grep MONGODB

# В frontend контейнере
docker exec editype-frontend env
```

## 📊 Мониторинг

### Health checks
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:3000

# MongoDB
docker exec editype-mongodb mongosh --eval "db.adminCommand('ping')" --quiet
```

### Метрики (если добавлен Prometheus)
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🚨 Troubleshooting

### Backend не запускается
```bash
# Проверить логи
docker compose logs backend

# Пересоздать контейнер
docker compose down backend
docker compose up -d --build backend
```

### Frontend показывает ошибки API
```bash
# Проверить proxy в nginx
docker exec editype-frontend cat /etc/nginx/conf.d/default.conf

# Перезагрузить nginx
docker exec editype-frontend nginx -s reload
```

### MongoDB проблемы
```bash
# Проверить подключение
docker exec editype-backend curl mongodb:27017

# Пересоздать с новым volume
docker compose down -v
docker volume rm editype-vue3_mongodb_data
docker compose up -d
```

### Порты заняты
```bash
# Windows - убить процесс на порту
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/macOS
lsof -ti:8080 | xargs kill -9
```

## 🔐 Безопасность

### Изменить JWT secret (production)
```bash
# В docker-compose.yml или .env
JWT_SECRET=your-super-secret-key-at-least-256-bits-long
```

### Изменить MongoDB пароль
```bash
# В docker-compose.yml
MONGO_INITDB_ROOT_PASSWORD=new-secure-password

# Обновить в MONGODB_URI
mongodb://admin:new-secure-password@mongodb:27017/editype
```

## 📝 Git команды

```bash
# Коммит изменений
git add .
git commit -m "feat: add new feature"

# Создать ветку
git checkout -b feature/new-feature

# Пуш
git push origin main
```

## ⚡ Полезные алиасы

Добавьте в `.bashrc` или `.zshrc`:

```bash
alias dc='docker compose'
alias dcu='docker compose up -d'
alias dcd='docker compose down'
alias dcl='docker compose logs -f'
alias dcr='docker compose restart'
alias dps='docker compose ps'
```

---

**Совет**: Сохраните этот файл в закладки для быстрого доступа к командам!




