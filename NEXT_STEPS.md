# 🚀 Следующие шаги разработки Znan.io

## 📋 Текущий статус

✅ **Фаза 1 завершена:**
- OAuth2 интеграция
- REST клиент Editorum
- Модель Article
- Базовый редактор
- Soft-lock механизм

⏳ **Что делать дальше:**

---

## 🎯 Приоритет 1: Настроить OAuth2

### Действия:
1. Получить credentials из Editorum:
   - Зайти в настройки юрлица
   - Меню "REST API" → Подключить
   - Скопировать `client_id` и `client_secret`

2. Обновить конфигурацию:
```bash
# backend/src/main/resources/application.yml
editorum:
  api:
    base-url: http://znan.io  # или ваш URL
  oauth:
    client-id: {ваш_client_id}
    client-secret: {ваш_client_secret}
    redirect-uri: http://localhost:3000/auth/callback
```

3. Добавить кнопку "Login with Editorum" в UI

4. Протестировать:
   - Нажать "Login with Editorum"
   - Авторизоваться на Editorum
   - Вернуться на Znan.io
   - Проверить что токен сохранен

---

## 🎯 Приоритет 2: Rich Text Editor (Tiptap)

### Почему Tiptap?
- ✅ Основан на ProseMirror (индустриальный стандарт)
- ✅ Vue 3 нативная поддержка
- ✅ Расширяемость (плагины)
- ✅ Markdown/LaTeX поддержка
- ✅ Collaborative editing ready (Yjs)

### Установка:
```bash
cd frontend
npm install @tiptap/vue-3 @tiptap/starter-kit @tiptap/extension-mathematics
```

### Интеграция:
- Заменить `contenteditable` на Tiptap компонент
- Добавить расширения: Bold, Italic, Heading, Table, Code Block
- Добавить Mathematics extension для LaTeX формул

### Примерный код:
```vue
<template>
  <editor-content :editor="editor" />
</template>

<script setup>
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Mathematics from '@tiptap/extension-mathematics'

const editor = useEditor({
  extensions: [
    StarterKit,
    Mathematics
  ],
  content: article.text,
  onUpdate: ({ editor }) => {
    article.text = editor.getHTML()
    article.pmJson = JSON.stringify(editor.getJSON())
    triggerAutoSave()
  }
})
</script>
```

---

## 🎯 Приоритет 3: MathJax для формул

### Установка:
```bash
npm install mathjax@3
```

### Интеграция в preview:
```vue
<template>
  <div ref="previewEl" class="preview-content" v-html="article.text"></div>
</template>

<script setup>
import { onMounted, watch, ref } from 'vue'
import { mathjax } from 'mathjax-full/js/mathjax'

const previewEl = ref(null)

watch(() => article.text, () => {
  // Render MathJax
  if (previewEl.value) {
    mathjax.typesetPromise([previewEl.value])
  }
})
</script>
```

---

## 🎯 Приоритет 4: Crossref для библиографии

### API:
```javascript
// Поиск по DOI
const searchByDOI = async (doi) => {
  const response = await fetch(`https://api.crossref.org/works/${doi}`)
  const data = await response.json()
  return mapToCSL(data.message)
}

// Поиск по названию
const searchByTitle = async (title) => {
  const response = await fetch(
    `https://api.crossref.org/works?query.title=${encodeURIComponent(title)}`
  )
  const data = await response.json()
  return data.message.items.map(mapToCSL)
}
```

### CSL-JSON формат:
```json
{
  "id": "ref1",
  "type": "article-journal",
  "author": [{"family": "Smith", "given": "John"}],
  "title": "Article title",
  "container-title": "Journal name",
  "volume": "10",
  "issue": "2",
  "page": "123-456",
  "issued": {"date-parts": [[2024]]},
  "DOI": "10.1234/example"
}
```

---

## 🎯 Приоритет 5: Pandoc для экспорта

### Установка Pandoc в Docker:
```dockerfile
# backend/Dockerfile
RUN apk add --no-cache pandoc texlive wkhtmltopdf
```

### Использование:
```java
// ExportService.java
public byte[] exportToDOCX(Article article) {
  // Сохранить HTML в temp файл
  File htmlFile = File.createTempFile("article", ".html");
  Files.writeString(htmlFile.toPath(), article.getText());
  
  // Запустить Pandoc
  Process process = new ProcessBuilder(
    "pandoc",
    htmlFile.getAbsolutePath(),
    "-o", "output.docx",
    "--from", "html",
    "--to", "docx"
  ).start();
  
  // Прочитать результат
  byte[] docx = Files.readAllBytes(Path.of("output.docx"));
  return docx;
}
```

---

## 📦 Обновление зависимостей

### Frontend (`package.json`):
```json
{
  "dependencies": {
    "@tiptap/vue-3": "^2.1.13",
    "@tiptap/starter-kit": "^2.1.13",
    "@tiptap/extension-mathematics": "^2.1.13",
    "mathjax-full": "^3.2.2",
    "citation-js": "^0.7.0"  // для CSL форматирования
  }
}
```

### Backend (`pom.xml`):
Уже добавлены:
- ✅ spring-boot-starter-oauth2-client
- ✅ spring-cloud-starter-openfeign

Добавить для Pandoc wrapper:
```xml
<!-- Apache Commons Exec для запуска Pandoc -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-exec</artifactId>
  <version>1.3</version>
</dependency>
```

---

## 🔄 Workflow разработки

### Ежедневно:
1. Тестировать OAuth flow с Editorum
2. Загружать статьи из Editorum
3. Редактировать и сохранять обратно
4. Проверять синхронизацию

### Еженедельно:
1. Code review
2. Обновление документации
3. Тестирование новых features
4. Обсуждение следующих шагов

---

## 🎯 Критерии готовности MVP

- [ ] OAuth2 работает с production Editorum
- [ ] Статья загружается из Editorum корректно
- [ ] Редактор с Tiptap работает плавно
- [ ] LaTeX формулы рендерятся (MathJax)
- [ ] Auto-save работает без потери данных
- [ ] Сохранение в Editorum проходит успешно
- [ ] Crossref поиск находит ссылки
- [ ] ГОСТ форматирование работает
- [ ] Экспорт PDF/DOCX/JATS функционален
- [ ] Файлы прикрепляются к Editorum (attachfile)
- [ ] Soft-lock предотвращает конфликты

---

## 📞 Контакты для получения

1. **OAuth2 credentials** (client_id, client_secret)
2. **Production URL** Editorum
3. **Тестовый аккаунт** для разработки
4. **Примеры статей** для тестирования
5. **Обратная связь** по API изменениям

---

## 💻 Команды для продолжения разработки

```bash
# Установить новые зависимости (Tiptap)
cd frontend
npm install @tiptap/vue-3 @tiptap/starter-kit mathjax-full

# Перезапустить backend (если обновили application.yml)
cd backend
mvn spring-boot:run

# Перезапустить frontend
cd frontend
npm run dev
```

---

**Готовы к следующему этапу!** 🚀

Ждем OAuth credentials для полного тестирования интеграции.




