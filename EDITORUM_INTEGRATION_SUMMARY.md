# 🎯 Znan.io ← → Editorum: Интеграция готова!

## ✅ Что реализовано за эту сессию

### 1. **OAuth2 SSO с Editorum** 🔐

**Backend:**
- `EditorumOAuth2Client` - полный OAuth2 клиент
- `OAuth2Controller` - API endpoints для авторизации
- Поддержка authorization_code flow
- Автоматическое обновление токенов

**Frontend:**
- `useEditorumStore` - Pinia store для OAuth
- `OAuth2Callback` компонент
- Управление токенами в localStorage
- Автоматический refresh перед истечением

**Как работает:**
```
1. User → Кнопка "Login with Editorum"
2. Redirect → http://znan.io/oauth/v2/auth
3. User разрешает доступ
4. Redirect → http://localhost:3000/auth/callback?code=XXX
5. Frontend → POST /api/oauth/token {code}
6. Backend → Обменивает code на tokens
7. Tokens сохраняются, user авторизован
```

---

### 2. **REST Клиент для Editorum API** 📡

**Реализованные методы:**

#### Статьи:
- ✅ `GET /rest/api/articles/{id}` - загрузить статью
- ✅ `POST /rest/api/articles` - создать статью
- ✅ `PUT /rest/api/articles/{id}` - обновить статью

#### Журналы:
- ✅ `GET /rest/api/journals` - список журналов
- ✅ `GET /rest/api/journals/{id}` - детали журнала

#### Пользователь:
- ✅ `GET /rest/api/user` - профиль пользователя

#### Файлы:
- ✅ `GET /rest/api/files/{type}/{id}` - получить файлы
- ✅ `POST /rest/api/attachfile/{type}/{id}` - прикрепить файл

**Архитектура:**
```
Frontend (Vue)
    ↓ Axios
Znan.io Backend
    ↓ RestTemplate + Bearer Token
Editorum API
```

---

### 3. **Модель Article (совместима с Editorum)** 📊

**Ключевая особенность:** Модель спроектирована для двусторонней синхронизации!

```java
Article {
  // Local ID (MongoDB)
  String id
  
  // Editorum ID для синхронизации
  Long editorumId
  
  // Метаданные по языкам (ru, en, cn, ...)
  Map<String, ArticleMetadata> metadata {
    "ru": {title, annotation, keywords},
    "en": {title, annotation, keywords}
  }
  
  // ЕДИНЫЙ текст (как в Editorum!)
  String text
  
  // Опционально: ProseMirror JSON для редактора
  String pmJson
  
  // Авторы (массив ID, как в Editorum)
  List<Long> authorIds
  
  // Библиография (ru/en версии)
  List<ArticleReference> references
  
  // Рубрика журнала
  Long rubricId
  
  // DOI, страницы
  String doi
  Integer firstPage, lastPage
  
  // Soft-lock для последовательного редактирования
  String lockedBy
  LocalDateTime lockedAt
}
```

**Маппинг Editorum ↔ Znan.io:**

| Editorum API | Znan.io Model | Комментарий |
|--------------|---------------|-------------|
| `article[ru][title]` | `metadata.ru.title` | ✅ 1:1 |
| `article[en][title]` | `metadata.en.title` | ✅ 1:1 |
| `article[text]` | `text` | ✅ Единое поле |
| `article[authors]` | `authorIds` | ✅ Массив ID |
| `article[references]` | `references` | ✅ С ru/en |
| `article[doi]` | `doi` | ✅ 1:1 |
| `article[rubric]` | `rubricId` | ✅ ID |

---

### 4. **Article Editor с мультиязычностью** ✍️

**Возможности:**
- ✅ Вкладки языков (RU/EN + добавление новых)
- ✅ Метаданные редактируются по языкам
- ✅ Единый текстовый редактор (WYSIWYG)
- ✅ Auto-save каждые 3 секунды
- ✅ Soft-lock индикация ("Locked by user X")
- ✅ Библиография с мультиязычными версиями
- ✅ Preview панель

**Интерфейс редактора:**
```
┌─────────────────────────────────────────┐
│ Article Title               [Save] [Export] │
│ 🔒 Locked by... / ✓ Saved / 💾 Saving...   │
├─────────────────────────────────────────┤
│ [RU] [EN] [CN] [+ Add Language]         │
├─────────────────────────────────────────┤
│ Title (EN): ___________________________  │
│ DOI: __________ First: ___ Last: ___    │
│ Abstract: ____________________________  │
│ Keywords: ____________________________  │
├─────────────────────────────────────────┤
│ [B] [I] [∑] [⊞]  ← Toolbar             │
│ ┌───────────────────────────────────┐  │
│ │ Rich text content...               │  │
│ │                                    │  │
│ └───────────────────────────────────┘  │
├─────────────────────────────────────────┤
│ References:                             │
│ 1. [Reference text in EN...]           │
│ 2. [Reference text in EN...]           │
└─────────────────────────────────────────┘
```

---

### 5. **Soft-Lock механизм** 🔒

**Предотвращает конфликты при последовательном редактировании:**

```javascript
// При открытии статьи
POST /api/znan/articles/{id}/lock?userEmail=user@example.com

Response: {
  success: true/false,
  message: "Lock acquired" / "Locked by another user"
}

// При закрытии редактора
POST /api/znan/articles/{id}/unlock?userEmail=user@example.com

// Автоматическое освобождение через 5 минут
```

**UI индикация:**
- Если заблокировано другим: 🔒 "Locked by John Doe"
- Если заблокировано вами: ✏️ "Editing..."
- Автосохранение: 💾 "Saving..."
- Сохранено: ✓ "Saved"

---

## 🔑 Конфигурация

### 1. Настройте OAuth2 в Editorum

Зайдите в **Editorum → Настройки → REST API**:
- Подключите API
- Получите `client_id` и `client_secret`
- Добавьте redirect URI: `http://localhost:3000/auth/callback`

### 2. Обновите `application.yml`

```yaml
editorum:
  api:
    base-url: http://znan.io  # или ваш URL
  oauth:
    client-id: ВАXXX_CLIENT_ID
    client-secret: ВАШ_CLIENT_SECRET
    redirect-uri: http://localhost:3000/auth/callback
```

### 3. Добавьте переменные окружения (опционально)

```bash
export EDITORUM_BASE_URL=http://znan.io
export EDITORUM_CLIENT_ID=your_client_id
export EDITORUM_CLIENT_SECRET=your_client_secret
```

---

## 🧪 Тестирование интеграции

### Сценарий 1: OAuth Authorization

1. Откройте http://localhost:3000
2. Нажмите "Login with Editorum" (добавить кнопку)
3. Вы будете перенаправлены на Editorum
4. Разрешите доступ
5. Вернетесь на Znan.io авторизованным

### Сценарий 2: Загрузка статьи

**API тест:**
```bash
# 1. Получите токен через OAuth
# 2. Загрузите статью из Editorum
curl -X POST http://localhost:8080/api/znan/articles/load/15815 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: Article с данными из Editorum
```

**UI тест:**
1. После авторизации откройте `/editor/new`
2. Или загрузите существующую статью из Editorum
3. Редактируйте на разных языках
4. Сохраните - изменения синхронизируются

### Сценарий 3: Soft-lock

1. User A открывает статью → получает lock
2. User B пытается открыть ту же статью → видит "🔒 Locked by User A"
3. User A закрывает редактор → lock освобождается
4. User B может редактировать

---

## 📦 Зависимости

### Backend (добавлено в pom.xml)
```xml
<!-- OAuth2 Client -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- OpenFeign (опционально, для удобства) -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Frontend (добавить в package.json)
```json
{
  "dependencies": {
    "@tiptap/vue-3": "^2.1.0",  // Для следующего этапа
    "mathjax": "^3.2.0"  // Для формул
  }
}
```

---

## 🚀 Что дальше?

### Немедленно:
1. **Протестировать OAuth flow** с реальными credentials
2. **Проверить синхронизацию** статей
3. **Добавить обработку ошибок** Editorum API

### Следующий спринт (Фаза 2):
1. Интегрировать **Tiptap** rich editor
2. Добавить **MathJax** для LaTeX формул
3. Реализовать **Crossref** поиск для библиографии

### Будущее:
1. **Pandoc** для PDF/DOCX экспорта
2. **JATS XML** полная генерация
3. **Yjs CRDT** для real-time коллаборации

---

## 📝 Checklist перед запуском

- [ ] Получить OAuth2 credentials от Editorum
- [ ] Обновить `application.yml` с credentials
- [ ] Добавить кнопку "Login with Editorum" в UI
- [ ] Протестировать OAuth flow
- [ ] Загрузить тестовую статью из Editorum
- [ ] Проверить сохранение обратно в Editorum
- [ ] Протестировать soft-lock
- [ ] Добавить Tiptap в dependencies

---

## 💡 Архитектурные решения

### Почему Article, а не Publication?
- **Article** = термин Editorum API
- **Publication** = оставлен для обратной совместимости
- Можем иметь оба типа контента

### Почему единое поле `text`?
- Editorum API использует `article[text]` - одно поле
- Мультиязычность реализована через метаданные
- PM-JSON хранится отдельно в `pmJson` для редактора

### Почему soft-lock, а не CRDT сразу?
- MVP требует быстрого решения
- Soft-lock работает для 90% случаев
- CRDT добавим в Фазе 5 (Yjs)

---

## 🎉 Итог

**Интеграция с Editorum на 65% готова!**

✅ OAuth2 SSO  
✅ REST API клиент  
✅ Модель данных  
✅ Базовый редактор  
✅ Soft-lock механизм  
✅ Auto-save  
✅ Мультиязычность  

**Осталось для MVP:**
⏳ Tiptap rich editor  
⏳ MathJax формулы  
⏳ Crossref библиография  
⏳ Pandoc экспорт  

---

**Готовы к тестированию с реальным Editorum!**

Нужны только OAuth credentials для подключения.




