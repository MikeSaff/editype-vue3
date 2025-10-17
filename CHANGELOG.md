# Changelog - Editype

All notable changes to this project will be documented in this file.

## [2.0.0] - 2024-10-16

### 🎉 Major Release: LaTeX & Advanced Editors Integration

#### ✨ Added

**Frontend:**
- Tiptap Rich Text Editor with ProseMirror
- KaTeX для рендеринга LaTeX формул (inline и display)
- Citation.js для управления библиографией
- ReferencesManager компонент для работы с ссылками
- TiptapEditor компонент с полным функционалом
- ArticleEditorNew с интеграцией всех новых фич
- Научные LaTeX макросы (ℝ, ℂ, ℕ, ∇, ∫, etc.)
- Поиск публикаций по DOI
- Импорт/экспорт BibTeX
- Форматирование библиографии (ГОСТ, APA, Vancouver, IEEE, MLA)

**Backend:**
- LatexExportService для экспорта через Pandoc + XeLaTeX
- VivliostyleExportService для красивого PDF
- LaTeX шаблоны для научных статей
- JATS XML шаблоны
- Новые export endpoints (PDF LaTeX, PDF Vivliostyle, LaTeX source)
- Поддержка мультиязычных LaTeX документов
- Автоматическая генерация BibTeX из ссылок

**Infrastructure:**
- TeX Live (XeLaTeX, LuaLaTeX) в Docker
- Pandoc 3.1+ для конвертации документов
- Biber для обработки библиографии
- Vivliostyle CLI для CSS Paged Media
- Liberation и Cyrillic шрифты
- ImageMagick для обработки изображений

**Documentation:**
- LATEX_INTEGRATION_COMPLETE.md - полная документация интеграции
- QUICK_INSTALL.md - быстрая установка
- INTEGRATION_SUMMARY.md - краткое резюме
- CHANGELOG.md - этот файл

#### 🔄 Changed

- Обновлен Dockerfile с полной установкой LaTeX стека
- Расширен ExportController новыми endpoints
- Обновлен README с информацией о новых фичах
- Улучшен articles.js API с дополнительными методами
- Переработан package.json с новыми зависимостями

#### 📦 Dependencies

**Added:**
```json
{
  "@tiptap/vue-3": "^2.1.13",
  "@tiptap/starter-kit": "^2.1.13",
  "@tiptap/extension-mathematics": "^2.1.13",
  "@tiptap/extension-table": "^2.1.13",
  "@tiptap/extension-link": "^2.1.13",
  "@tiptap/extension-image": "^2.1.13",
  "katex": "^0.16.9",
  "citation-js": "^0.7.9",
  "@citation-js/plugin-bibtex": "^0.7.9",
  "@citation-js/plugin-csl": "^0.7.9"
}
```

#### 🚀 New Features

1. **Rich Text Editing**
   - Структурированный контент с ProseMirror
   - Заголовки (H1-H6)
   - Форматирование (bold, italic, strikethrough, code)
   - Списки (bullet, ordered)
   - Таблицы с редактированием
   - Изображения
   - Ссылки
   - Undo/Redo

2. **LaTeX Формулы**
   - Inline формулы: `$E=mc^2$`
   - Display формулы: `$$\int...$$`
   - Научные макросы
   - Мгновенный preview
   - Экспорт в LaTeX и PDF

3. **Библиография**
   - Поиск по DOI с автозаполнением
   - Ручной ввод ссылок
   - Импорт BibTeX файлов
   - Экспорт в BibTeX
   - Множество стилей форматирования
   - Проверка дубликатов
   - Сортировка

4. **Экспорт**
   - PDF через LaTeX (академический стандарт)
   - PDF через Vivliostyle (типографика)
   - LaTeX source код
   - HTML
   - JATS XML
   - Plain text

#### 🔧 API Changes

**New Endpoints:**
```
GET  /api/export/articles/{id}/pdf/latex
GET  /api/export/articles/{id}/pdf/vivliostyle
GET  /api/export/articles/{id}/latex
GET  /api/export/{id}/pdf?engine=latex|vivliostyle
```

#### 📊 Performance

- LaTeX PDF export: 5-20 секунд (в зависимости от размера)
- Vivliostyle PDF export: 2-10 секунд
- LaTeX source generation: < 500ms
- Formula rendering: < 100ms (KaTeX)

#### 🐛 Bug Fixes

- Исправлена работа с мультиязычными метаданными
- Улучшена обработка ошибок при экспорте
- Исправлены проблемы с кодировкой в LaTeX

---

## [1.1.0] - 2024-10-15

### Editorum Integration Complete

#### ✨ Added

- OAuth2 SSO интеграция с Editorum
- REST API клиент для Editorum
- Модель Article совместимая с Editorum
- Двусторонняя синхронизация статей
- Soft-lock механизм
- Auto-save функциональность
- Мультиязычная поддержка (RU/EN/CN)

#### 🔄 Changed

- Переработана архитектура для интеграции с Editorum
- Обновлены API endpoints
- Улучшена модель данных

---

## [1.0.0] - 2024-10-01

### Initial Release

#### ✨ Added

- Базовая архитектура (Spring Boot + Vue 3)
- JWT авторизация
- Управление пользователями
- Базовые операции со статьями
- Экспорт в HTML, XML, TXT
- MongoDB для хранения
- Docker контейнеризация
- Мультиязычный интерфейс (RU/EN/CN)

---

## Unreleased / Planned

### [2.1.0] - В разработке

- [ ] DOCX экспорт через Pandoc
- [ ] Улучшенные LaTeX шаблоны
- [ ] Preview в реальном времени
- [ ] Spell checker
- [ ] Дополнительные стили библиографии

### [3.0.0] - Real-time Collaboration

- [ ] Yjs CRDT интеграция
- [ ] WebSocket gateway
- [ ] Комментарии и рецензирование
- [ ] Version control
- [ ] Conflict resolution

### [4.0.0] - AI & Advanced Features

- [ ] AI-ассистент для написания
- [ ] Автоматическая проверка стиля
- [ ] Plagiarism detection
- [ ] Интеграция с arXiv/PubMed
- [ ] Препринт сервер

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 2.0.0 | 2024-10-16 | LaTeX & Advanced Editors |
| 1.1.0 | 2024-10-15 | Editorum Integration |
| 1.0.0 | 2024-10-01 | Initial Release |

---

## Semantic Versioning

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR** version when you make incompatible API changes
- **MINOR** version when you add functionality in a backwards compatible manner
- **PATCH** version when you make backwards compatible bug fixes

---

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


