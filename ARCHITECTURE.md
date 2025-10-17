# Архитектура проекта Editype

## 📐 Общая архитектура

Editype построен на микросервисной архитектуре с четким разделением на:
- **Backend** (Spring Boot) - бизнес-логика и API
- **Frontend** (Vue 3) - пользовательский интерфейс
- **Database** (MongoDB) - хранение данных
- **Cache** (Redis) - кеширование (готов к использованию)

## 🏗️ Backend Architecture

### Слои приложения

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
MongoDB Database
```

### Модули

#### 1. **Auth Module** (`com.editype.auth`)
- **Назначение**: Аутентификация и авторизация
- **Компоненты**:
  - `AuthController` - REST endpoints для login/register
  - `AuthService` - логика аутентификации
  - DTOs: `LoginRequestDTO`, `RegisterRequestDTO`, `AuthResponseDTO`

#### 2. **User Module** (`com.editype.user`)
- **Назначение**: Управление пользователями
- **Компоненты**:
  - `User` entity с ролями (AUTHOR, EDITOR, REVIEWER)
  - `UserController` - CRUD операции
  - `UserService` - бизнес-логика
  - `UserRepository` - доступ к данным
  - DTOs: `UserRequestDTO`, `UserResponseDTO`

#### 3. **Publication Module** (`com.editype.publication`)
- **Назначение**: Управление публикациями
- **Компоненты**:
  - `Publication` entity с мультиязычными полями (Map<String, String>)
  - `PublicationController` - CRUD операции
  - `PublicationService` - бизнес-логика
  - `PublicationRepository` - доступ к данным
  - DTOs: `PublicationRequestDTO`, `PublicationResponseDTO`

#### 4. **Export Module** (`com.editype.export`)
- **Назначение**: Экспорт публикаций в различные форматы
- **Компоненты**:
  - `ExportService` - генерация файлов
  - `ExportController` - endpoints для экспорта
- **Форматы**: HTML, XML JATS, TXT

#### 5. **Security Module** (`com.editype.security`)
- **Компоненты**:
  - `JwtUtil` - генерация и валидация JWT токенов
  - `JwtAuthenticationFilter` - фильтр для проверки токенов
  - `SecurityConfig` - конфигурация Spring Security

#### 6. **Config Module** (`com.editype.config`)
- `SecurityConfig` - настройки безопасности

#### 7. **Exception Module** (`com.editype.exception`)
- `GlobalExceptionHandler` - глобальная обработка ошибок
- `ResourceNotFoundException` - кастомное исключение

### Безопасность

- **JWT Authentication**: Stateless аутентификация
- **BCrypt**: Хеширование паролей
- **Role-based access**: Роли пользователей
- **Protected endpoints**: Защита через Spring Security

### API Design

- **RESTful**: Следует REST принципам
- **Versioning**: Готово к версионированию (префикс /api)
- **Status Codes**: Правильное использование HTTP статусов
- **Error Handling**: Консистентная обработка ошибок

## 🎨 Frontend Architecture

### Структура Vue 3 приложения

```
Components
    ↓
Views (Pages)
    ↓
API Layer
    ↓
Backend REST API
```

### Технологический стек

- **Vue 3**: Composition API с `<script setup>`
- **Pinia**: Управление состоянием
- **Vue Router**: Маршрутизация
- **Vue i18n**: Интернационализация
- **Axios**: HTTP клиент

### Ключевые компоненты

#### 1. **API Layer** (`src/api/`)
- `axios.js` - настроенный axios instance с interceptors
- `users.js` - API для пользователей
- `publications.js` - API для публикаций
- `auth.js` - API для авторизации
- `export.js` - API для экспорта

#### 2. **Stores** (`src/stores/`)
- `auth.js` - хранилище аутентификации (Pinia)

#### 3. **Views** (`src/views/`)
- `Login.vue` - страница входа
- `Register.vue` - страница регистрации
- `UsersList.vue` - управление пользователями
- `PublicationsList.vue` - управление публикациями

#### 4. **Router** (`src/router/`)
- Navigation guards для защиты маршрутов
- Редиректы для авторизованных/неавторизованных пользователей

#### 5. **i18n** (`src/locales/`)
- `en.json` - английский
- `ru.json` - русский
- `cn.json` - китайский

### State Management (Pinia)

```javascript
// Auth Store
{
  user: Object,
  token: String,
  login(): Promise,
  register(): Promise,
  logout(): void,
  isAuthenticated(): Boolean
}
```

### Routing Strategy

- **Protected routes**: требуют авторизации
- **Public routes**: `/login`, `/register`
- **Redirects**: автоматические перенаправления

## 🗄️ Data Model

### User Collection (MongoDB)

```javascript
{
  _id: ObjectId,
  email: String (unique),
  password: String (hashed),
  roles: Array<String>, // ['AUTHOR', 'EDITOR', 'REVIEWER']
  createdAt: DateTime,
  updatedAt: DateTime
}
```

### Publication Collection (MongoDB)

```javascript
{
  _id: ObjectId,
  titles: {
    ru: String,
    en: String,
    cn: String
  },
  metadata: {
    ru: { author: String, keywords: String, ... },
    en: { author: String, keywords: String, ... },
    cn: { author: String, keywords: String, ... }
  },
  texts: {
    ru: String,
    en: String,
    cn: String
  },
  doi: String,
  createdAt: DateTime,
  updatedAt: DateTime
}
```

## 🐳 Docker Architecture

### Контейнеры

1. **MongoDB** (mongo:7.0)
   - Port: 27017
   - Volume: mongodb_data
   - Credentials: admin/admin123

2. **Redis** (redis:7-alpine)
   - Port: 6379
   - Volume: redis_data

3. **Backend** (Java 17)
   - Port: 8080
   - Multi-stage build (Maven → JRE)
   - Health check: /actuator/health

4. **Frontend** (Node 18 → Nginx)
   - Port: 3000
   - Multi-stage build (npm → nginx)
   - Proxy to backend через /api

### Networking

- Custom bridge network: `editype-network`
- Service discovery по имени контейнера
- Nginx reverse proxy для API

### Volumes

- **mongodb_data**: персистентное хранилище БД
- **redis_data**: кеш данных

## 🔄 Data Flow

### Авторизация

```
User → Frontend (Login) 
  → POST /api/auth/login 
  → Backend (AuthService) 
  → MongoDB (check credentials) 
  → JWT Token 
  → Frontend (save to localStorage) 
  → Authenticated requests
```

### CRUD Operations

```
User Action → Frontend Component 
  → API Call (with JWT) 
  → Backend Controller 
  → Service Layer 
  → Repository 
  → MongoDB 
  → Response 
  → Update UI
```

### Export Flow

```
User clicks Export 
  → Select format & language 
  → GET /api/export/{id}/{format}?lang=X 
  → ExportService generates content 
  → Download file
```

## 🌐 Multilingual Support

### Стратегия

1. **UI Language**: Vue i18n для интерфейса
2. **Content Language**: Map структуры в MongoDB
3. **Export Language**: Параметр запроса

### Реализация

```javascript
// Frontend - UI
{{ t('nav.users') }} // Из локализации

// Backend - Content
publication.titles.get(language) // Из Map<String, String>
```

## 🔒 Security Architecture

### Layers

1. **Network**: Docker network изоляция
2. **Application**: Spring Security
3. **Authentication**: JWT tokens
4. **Authorization**: Role-based access
5. **Data**: BCrypt password hashing

### JWT Flow

```
Login → Generate JWT (secret + expiration) 
  → Return to client 
  → Client stores in localStorage 
  → Include in Authorization header 
  → Backend validates 
  → Extract user info 
  → Check permissions
```

## 📈 Scalability Considerations

### Готово к масштабированию

- ✅ Stateless backend (JWT)
- ✅ Контейнеризация
- ✅ Разделение на сервисы
- ✅ Redis для кеширования
- ✅ MongoDB для горизонтального масштабирования

### Будущие улучшения

- Load balancer для backend
- Read replicas для MongoDB
- CDN для статики frontend
- Message queue (RabbitMQ/Kafka)
- WebSocket для real-time

## 🧪 Testing Strategy

### Backend
- Unit tests: JUnit 5
- Integration tests: Spring Boot Test
- API tests: REST Assured

### Frontend
- Unit tests: Vitest
- E2E tests: Cypress (будущее)

## 📊 Monitoring & Observability

### Текущая реализация
- Spring Boot Actuator (health, info)
- Docker health checks
- Container logs

### Рекомендации для production
- Prometheus для метрик
- Grafana для визуализации
- ELK stack для логов
- Distributed tracing (Zipkin/Jaeger)

## 🚀 Deployment Strategy

### Development
```bash
docker compose up -d
```

### Production (рекомендации)
- Kubernetes для оркестрации
- CI/CD pipeline (GitLab CI, GitHub Actions)
- Blue-green deployment
- Автоматические бэкапы MongoDB
- SSL/TLS сертификаты
- Environment-based конфигурация

---

**Автор**: Editype Team  
**Версия**: 1.0.0  
**Дата**: 2024




