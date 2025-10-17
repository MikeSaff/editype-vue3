# Znan.io - Scientific Writing & Publishing Platform

**Платформа для научного письма и публикаций** с интеграцией Editorum.io

Znan.io - онлайн-платформа для научного письма (WYSIWYG/LaTeX), совместного редактирования, управления библиографией и публикации препринтов. Интегрируется с издательской системой Editorum через REST API и OAuth2.

## 🏗️ Архитектура

- **Backend**: Java 17, Spring Boot 3 (Web, Data MongoDB, Security), REST API
- **Frontend**: Vue 3 + Vite, Composition API, Pinia, Vue Router, i18n
- **Database**: MongoDB
- **Cache**: Redis
- **Infrastructure**: Docker Compose, Nginx

## 📋 Возможности

### Фаза 1: Интеграция с Editorum ✅
- ✅ **OAuth2 SSO** с Editorum (authorization_code flow)
- ✅ **REST API клиент** для Editorum (статьи, журналы, файлы)
- ✅ **Модель Article** совместимая с Editorum API
- ✅ **Двусторонняя синхронизация** Znan.io ↔ Editorum
- ✅ **Мультиязычность** (RU/EN/CN) для метаданных
- ✅ **Soft-lock** механизм для последовательного редактирования
- ✅ **Auto-save** каждые 3 секунды
- ✅ **Библиография** с мультиязычными версиями

### Базовый функционал ✅
- ✅ Управление пользователями (CRUD)
- ✅ JWT авторизация (для standalone режима)
- ✅ Экспорт в HTML, XML JATS, TXT
- ✅ Заготовка Pandoc (DOCX/PDF)
- ✅ Мультиязычный интерфейс
- ✅ Контейнеризация Docker

### Фаза 2: LaTeX & Advanced Editors ✅ ЗАВЕРШЕНО
- ✅ **Tiptap + ProseMirror** - Rich text editor с структурированным контентом
- ✅ **KaTeX** - Быстрый рендеринг LaTeX формул (inline и display)
- ✅ **Citation.js** - Управление библиографией (DOI, BibTeX, ГОСТ, APA)
- ✅ **LaTeX Export** - PDF через Pandoc + XeLaTeX (высокое качество)
- ✅ **Vivliostyle** - PDF с профессиональной типографикой (CSS Paged Media)
- ✅ **Научные макросы** - ℝ, ℂ, ℕ, ∇, ∫, и другие
- ✅ **LaTeX templates** - Шаблоны для научных статей
- ✅ **Full Docker stack** - TeX Live, Pandoc, Biber, Vivliostyle CLI

### Запланировано (Фаза 3-5)
- 🔄 **DOCX экспорт** через Pandoc (в разработке)
- 🔄 **Yjs CRDT** для real-time коллаборации
- 🔄 **WebSocket** gateway для совместного редактирования
- 🔄 **Version control** для статей
- 🔄 **Комментарии и рецензирование**

### API Endpoints

#### OAuth2 (Editorum SSO)
- `GET /api/oauth/authorize-url` - Получить URL авторизации Editorum
- `POST /api/oauth/token` - Обменять code на tokens
- `POST /api/oauth/refresh` - Обновить access token

#### Export (NEW! ✨)
- `GET /api/export/articles/{id}/pdf/latex` - 📄 PDF через LaTeX (высокое качество)
- `GET /api/export/articles/{id}/pdf/vivliostyle` - 📘 PDF через Vivliostyle (типографика)
- `GET /api/export/articles/{id}/latex` - 📝 LaTeX source код
- `GET /api/export/{id}/html` - 🌐 HTML
- `GET /api/export/{id}/jats` - 📋 JATS XML
- `GET /api/export/{id}/txt` - 📄 Plain text
- `GET /api/export/{id}/pdf?engine=latex|vivliostyle` - PDF (выбор движка)

#### Articles (Znan.io)
- `GET /api/znan/articles` - Все статьи
- `GET /api/znan/articles/{id}` - Статья по ID
- `POST /api/znan/articles/load/{editorumId}` - Загрузить из Editorum
- `PUT /api/znan/articles/{id}` - Сохранить (+ sync с Editorum)
- `POST /api/znan/articles/{id}/lock` - Получить блокировку для редактирования
- `POST /api/znan/articles/{id}/unlock` - Освободить блокировку
- `DELETE /api/znan/articles/{id}` - Удалить статью

#### Editorum Proxy
- `GET /api/editorum/user` - Текущий пользователь из Editorum
- `GET /api/editorum/journals` - Список журналов
- `GET /api/editorum/journals/{id}` - Журнал по ID
- `GET /api/editorum/files/{type}/{id}` - Файлы публикации

#### Authentication (Standalone)
- `POST /api/auth/login` - Вход (JWT для standalone)
- `POST /api/auth/register` - Регистрация

#### Users (Standalone)
- `GET /api/users` - Пользователи (для управления)
- `POST /api/users` - Создать пользователя

#### Publications (Legacy)
- `GET /api/publications` - Старые публикации
- `POST /api/publications` - Создать

#### Export
- `GET /api/export/{id}/html?lang={lang}` - Экспорт в HTML
- `GET /api/export/{id}/jats?lang={lang}` - Экспорт в XML JATS
- `GET /api/export/{id}/txt?lang={lang}` - Экспорт в TXT
- `GET /api/export/{id}/pdf?lang={lang}` - Экспорт в PDF (через Pandoc)
- `GET /api/export/{id}/docx?lang={lang}` - Экспорт в DOCX (через Pandoc)

## 🚀 Быстрый старт

### Требования
- Docker
- Docker Compose

### Запуск проекта

```bash
# Клонировать репозиторий
git clone <repository-url>
cd editype-vue3

# Запустить все сервисы
docker compose up -d

# Проверить статус
docker compose ps
```

После запуска:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **MongoDB**: localhost:27017
- **Redis**: localhost:6379

### Остановка

```bash
docker compose down
```

### Очистка данных

```bash
docker compose down -v
```

## 🛠️ Разработка

### Backend (Spring Boot)

```bash
cd backend

# Запустить локально
./mvnw spring-boot:run

# Сборка
./mvnw clean package
```

### Frontend (Vue 3)

```bash
cd frontend

# Установить зависимости
npm install

# Запустить dev server
npm run dev

# Сборка для production
npm run build
```

## 📁 Структура проекта

```
editype-vue3/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/editype/
│   │   │   │   ├── auth/          # JWT авторизация
│   │   │   │   ├── config/        # Конфигурация
│   │   │   │   ├── exception/     # Обработка ошибок
│   │   │   │   ├── export/        # Экспорт публикаций
│   │   │   │   ├── publication/   # Публикации
│   │   │   │   ├── security/      # Security компоненты
│   │   │   │   └── user/          # Пользователи
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/               # Vue 3 frontend
│   ├── src/
│   │   ├── api/           # API клиенты
│   │   ├── components/    # Vue компоненты
│   │   ├── locales/       # i18n переводы
│   │   ├── router/        # Vue Router
│   │   ├── stores/        # Pinia stores
│   │   ├── views/         # Страницы
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml      # Docker Compose конфигурация
└── README.md
```

## 🔐 Безопасность

- JWT токены для аутентификации
- BCrypt для хеширования паролей
- Spring Security для защиты эндпоинтов
- CORS конфигурация
- Защищенные маршруты на frontend

## 🌐 Мультиязычность

Поддерживаемые языки:
- 🇷🇺 Русский (ru)
- 🇬🇧 Английский (en)
- 🇨🇳 Китайский (cn)

Публикации могут содержать:
- Заголовки на разных языках
- Метаданные на разных языках
- Тексты на разных языках

## 📤 Экспорт

Поддерживаемые форматы экспорта:
- **HTML** - Полностью стилизованный HTML документ
- **XML JATS** - Journal Article Tag Suite XML (упрощенная версия)
- **TXT** - Простой текстовый формат

*Примечание: PDF и DOCX экспорт требуют дополнительных библиотек (iText, Apache POI)*

## 🔄 Future Roadmap

- WebSocket для реального времени редактирования
- Полноценный PDF/DOCX экспорт
- Система комментариев и рецензирования
- Расширенный поиск и фильтрация
- Уведомления

## 📝 Лицензия

MIT License

## 👥 Разработка

Проект создан в соответствии с современными практиками разработки и следует принципам:
- Clean Architecture
- REST API best practices
- Component-based UI
- Reactive programming
- Containerization

---

**Версия**: 1.0.0
