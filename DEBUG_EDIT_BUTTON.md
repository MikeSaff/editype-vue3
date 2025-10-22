# 🐛 Диагностика проблемы с кнопкой "Редактировать"

## Проверьте следующее:

### 1. Откройте консоль браузера (F12)

Перейдите на страницу со списком статей: http://localhost:3000/articles

### 2. Проверьте, загружаются ли статьи

В консоли выполните:
```javascript
// Проверить статьи
const articlesStore = await import('/src/stores/articles.js').then(m => m.useArticlesStore())
console.log('Статьи:', articlesStore.articles)
console.log('Количество:', articlesStore.articles?.length)

// Проверить первую статью
if (articlesStore.articles?.length > 0) {
  const firstArticle = articlesStore.articles[0]
  console.log('ID первой статьи:', firstArticle.id)
  console.log('Полная статья:', firstArticle)
}
```

### 3. Проверьте роутер

```javascript
// Проверить текущий роутер
const router = window.$router || (await import('vue-router').then(m => m.useRouter()))
console.log('Роутер:', router)

// Попробовать перейти вручную (замените YOUR_ARTICLE_ID на реальный ID)
router.push('/editor/YOUR_ARTICLE_ID')
```

### 4. Проверьте ошибки в консоли

Есть ли красные ошибки типа:
- `Failed to load article`
- `Cannot read property 'id' of undefined`
- `Network error`
- `404 Not Found`

---

## Возможные причины проблемы:

### Причина 1: Статьи не загружаются
**Проверка**: В консоли должны быть статьи с полями `id`, `title`

**Решение**: Проверить API endpoint `/api/znan/articles`

### Причина 2: У статей нет ID
**Проверка**: `article.id` должно быть определено

**Решение**: Проверить backend, возвращается ли поле `id`

### Причина 3: Ошибка JavaScript
**Проверка**: Красные ошибки в консоли

**Решение**: Смотреть текст ошибки

### Причина 4: Проблема с аутентификацией
**Проверка**: `authStore.isAuthenticated()` должно быть `true`

**Решение**: Перелогиниться

---

## Быстрое исправление

### Если кнопка не реагирует - попробуйте:

1. **Очистить кеш браузера**: Ctrl+Shift+Delete
2. **Перезагрузить страницу**: Ctrl+F5
3. **Проверить сеть**: Network tab в DevTools
4. **Перелогиниться**: Выйти и войти снова

---

## Отчет об ошибке

Пожалуйста, предоставьте:
1. Скриншот консоли (F12)
2. Текст ошибки (если есть)
3. Результат команды из пункта 2 выше
4. Работает ли кнопка "Создать статью"?

---

## Альтернативный способ открыть редактор

Если кнопка "Редактировать" не работает, попробуйте:

1. Скопируйте URL статьи из списка
2. Замените `/articles` на `/editor/ARTICLE_ID`
3. Или выполните в консоли:
```javascript
// Замените ARTICLE_ID на реальный ID вашей статьи
window.location.href = '/editor/ARTICLE_ID'
```

---

Сообщите результаты проверки, чтобы я мог точно определить проблему! 🔍




