<template>
  <div class="admin-styles-page">
    <div class="page-header">
      <h1>Управление стилями абзацев</h1>
      <p>Создавайте и настраивайте стили для абзацев документов</p>
    </div>

    <div class="toolbar">
      <button @click="openCreateModal" class="btn-primary">Создать стиль</button>
      <button @click="initializeDefaults" class="btn-secondary">Инициализировать стандартные стили</button>
      <div class="filter-group">
        <label>Категория:</label>
        <select v-model="selectedCategory" @change="filterStyles">
          <option value="">Все</option>
          <option value="headings">Заголовки</option>
          <option value="text">Текст</option>
          <option value="special">Специальные</option>
        </select>
      </div>
    </div>

    <div v-if="stylesStore.loading" class="loading-spinner">
      <div class="spinner"></div>
      <p>Загрузка стилей...</p>
    </div>

    <div v-else-if="filteredStyles.length === 0" class="empty-state">
      <div class="empty-icon">🎨</div>
      <h2>Стили не найдены</h2>
      <p>Создайте свой первый стиль абзаца или инициализируйте стандартные</p>
    </div>

    <div v-else class="styles-grid">
      <div
        v-for="style in filteredStyles"
        :key="style.id"
        class="style-card"
        :class="{ 'system-style': style.isSystemStyle }"
      >
        <div class="style-header">
          <h3>{{ style.name }}</h3>
          <span v-if="style.isSystemStyle" class="system-badge">Системный</span>
        </div>
        <div class="style-info">
          <div class="info-item">
            <strong>Тип:</strong> {{ style.type }}
          </div>
          <div class="info-item">
            <strong>HTML тег:</strong> &lt;{{ style.htmlTag }}&gt;
          </div>
          <div class="info-item">
            <strong>Категория:</strong> {{ getCategoryName(style.category) }}
          </div>
          <div class="info-item">
            <strong>Доступен пользователям:</strong> {{ style.isUserSelectable ? 'Да' : 'Нет' }}
          </div>
        </div>
        <div class="style-preview">
          <div class="preview-label">Предпросмотр:</div>
          <div :style="getCssString(style.cssProperties)" class="preview-text">
            Пример текста с этим стилем
          </div>
        </div>
        <div class="style-css">
          <details>
            <summary>CSS свойства</summary>
            <pre>{{ formatCss(style.cssProperties) }}</pre>
          </details>
        </div>
        <div class="style-actions">
          <button @click="editStyle(style)" class="btn btn-small" :disabled="style.isSystemStyle">
            Редактировать
          </button>
          <button @click="duplicateStyle(style)" class="btn btn-small btn-success">
            Дублировать
          </button>
          <button
            @click="deleteStyle(style.id)"
            class="btn btn-small btn-danger"
            :disabled="style.isSystemStyle"
          >
            Удалить
          </button>
        </div>
      </div>
    </div>

    <!-- Модальное окно для создания/редактирования стиля -->
    <div v-if="showModal" class="modal" @click.self="closeModal">
      <div class="modal-content large-modal">
        <div class="modal-header">
          <h2>{{ editingStyle ? 'Редактировать стиль' : 'Создать стиль' }}</h2>
          <button @click="closeModal" class="close-button">&times;</button>
        </div>

        <form @submit.prevent="saveStyle" class="style-form">
          <div class="form-row">
            <div class="form-group">
              <label for="style-name">Название стиля *</label>
              <input
                type="text"
                id="style-name"
                v-model="styleForm.name"
                required
                placeholder="Например: Заголовок 1"
              />
            </div>

            <div class="form-group">
              <label for="style-type">Технический тип *</label>
              <input
                type="text"
                id="style-type"
                v-model="styleForm.type"
                required
                placeholder="Например: heading1"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="style-description">Описание</label>
            <textarea
              id="style-description"
              v-model="styleForm.description"
              placeholder="Описание стиля"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="html-tag">HTML тег *</label>
              <select id="html-tag" v-model="styleForm.htmlTag" required>
                <option value="">Выберите тег</option>
                <option value="h1">h1 - Заголовок 1</option>
                <option value="h2">h2 - Заголовок 2</option>
                <option value="h3">h3 - Заголовок 3</option>
                <option value="h4">h4 - Заголовок 4</option>
                <option value="h5">h5 - Заголовок 5</option>
                <option value="h6">h6 - Заголовок 6</option>
                <option value="p">p - Параграф</option>
                <option value="blockquote">blockquote - Цитата</option>
                <option value="pre">pre - Код</option>
                <option value="div">div - Блок</option>
              </select>
            </div>

            <div class="form-group">
              <label for="category">Категория *</label>
              <select id="category" v-model="styleForm.category" required>
                <option value="">Выберите категорию</option>
                <option value="headings">Заголовки</option>
                <option value="text">Текст</option>
                <option value="special">Специальные</option>
              </select>
            </div>
          </div>

          <div class="form-group checkbox-group">
            <input type="checkbox" id="user-selectable" v-model="styleForm.isUserSelectable" />
            <label for="user-selectable">Доступен для выбора пользователями</label>
          </div>

          <div class="css-editor">
            <h3>CSS свойства</h3>
            <div class="css-properties">
              <div class="form-row">
                <div class="form-group">
                  <label>Размер шрифта</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['font-size']"
                    placeholder="14px"
                  />
                </div>
                <div class="form-group">
                  <label>Вес шрифта</label>
                  <select v-model="styleForm.cssProperties['font-weight']">
                    <option value="normal">Normal</option>
                    <option value="bold">Bold</option>
                    <option value="lighter">Lighter</option>
                    <option value="bolder">Bolder</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Цвет текста</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['color']"
                    placeholder="#000000"
                  />
                </div>
                <div class="form-group">
                  <label>Цвет фона</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['background-color']"
                    placeholder="#ffffff"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Отступ сверху</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['margin-top']"
                    placeholder="8px"
                  />
                </div>
                <div class="form-group">
                  <label>Отступ снизу</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['margin-bottom']"
                    placeholder="8px"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Внутренний отступ</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['padding']"
                    placeholder="0px"
                  />
                </div>
                <div class="form-group">
                  <label>Межстрочный интервал</label>
                  <input
                    type="text"
                    v-model="styleForm.cssProperties['line-height']"
                    placeholder="1.5"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Выравнивание</label>
                  <select v-model="styleForm.cssProperties['text-align']">
                    <option value="left">Слева</option>
                    <option value="center">По центру</option>
                    <option value="right">Справа</option>
                    <option value="justify">По ширине</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Стиль шрифта</label>
                  <select v-model="styleForm.cssProperties['font-style']">
                    <option value="normal">Normal</option>
                    <option value="italic">Italic</option>
                    <option value="oblique">Oblique</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label>Границы</label>
                <input
                  type="text"
                  v-model="styleForm.cssProperties['border']"
                  placeholder="1px solid #ccc"
                />
              </div>

              <details>
                <summary>Дополнительные CSS свойства (JSON)</summary>
                <textarea
                  v-model="customCssJson"
                  @blur="parseCustomCss"
                  class="css-json-editor"
                  placeholder='{"property": "value"}'
                ></textarea>
              </details>
            </div>
          </div>

          <div class="preview-section">
            <h3>Предпросмотр</h3>
            <div :style="getCssString(styleForm.cssProperties)" class="preview-box">
              Пример текста с применёнными стилями
            </div>
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn-primary">Сохранить</button>
            <button type="button" @click="closeModal" class="btn-secondary">Отмена</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStylesStore } from '@/stores/styles'

const stylesStore = useStylesStore()

const selectedCategory = ref('')
const showModal = ref(false)
const editingStyle = ref(null)
const customCssJson = ref('')

const styleForm = ref({
  name: '',
  type: '',
  description: '',
  htmlTag: '',
  category: '',
  isUserSelectable: true,
  cssProperties: {}
})

const filteredStyles = computed(() => {
  if (!selectedCategory.value) {
    return stylesStore.styles
  }
  return stylesStore.getStylesByCategory(selectedCategory.value)
})

onMounted(async () => {
  await stylesStore.loadStyles()
})

const filterStyles = () => {
  // Filtering is handled by computed property
}

const getCategoryName = (category) => {
  const categories = {
    headings: 'Заголовки',
    text: 'Текст',
    special: 'Специальные'
  }
  return categories[category] || category
}

const getCssString = (cssProperties) => {
  if (!cssProperties) return ''
  return Object.entries(cssProperties)
    .map(([key, value]) => `${key}: ${value}`)
    .join('; ')
}

const formatCss = (cssProperties) => {
  if (!cssProperties) return 'Нет CSS свойств'
  return JSON.stringify(cssProperties, null, 2)
}

const openCreateModal = () => {
  editingStyle.value = null
  styleForm.value = {
    name: '',
    type: '',
    description: '',
    htmlTag: '',
    category: '',
    isUserSelectable: true,
    cssProperties: {}
  }
  customCssJson.value = ''
  showModal.value = true
}

const editStyle = (style) => {
  if (style.isSystemStyle) {
    alert('Системные стили нельзя редактировать')
    return
  }

  editingStyle.value = style
  styleForm.value = {
    name: style.name,
    type: style.type,
    description: style.description || '',
    htmlTag: style.htmlTag,
    category: style.category,
    isUserSelectable: style.isUserSelectable,
    cssProperties: { ...style.cssProperties }
  }
  customCssJson.value = ''
  showModal.value = true
}

const duplicateStyle = (style) => {
  editingStyle.value = null
  styleForm.value = {
    name: `${style.name} (копия)`,
    type: `${style.type}_copy`,
    description: style.description || '',
    htmlTag: style.htmlTag,
    category: style.category,
    isUserSelectable: style.isUserSelectable,
    cssProperties: { ...style.cssProperties }
  }
  customCssJson.value = ''
  showModal.value = true
}

const parseCustomCss = () => {
  if (!customCssJson.value.trim()) return

  try {
    const customProps = JSON.parse(customCssJson.value)
    styleForm.value.cssProperties = { ...styleForm.value.cssProperties, ...customProps }
  } catch (err) {
    alert('Ошибка парсинга JSON: ' + err.message)
  }
}

const saveStyle = async () => {
  let result
  if (editingStyle.value) {
    result = await stylesStore.updateStyle(editingStyle.value.id, styleForm.value)
  } else {
    result = await stylesStore.createStyle(styleForm.value)
  }

  if (result.success) {
    alert('Стиль успешно сохранён!')
    closeModal()
  } else {
    alert(`Ошибка сохранения стиля: ${result.error}`)
  }
}

const deleteStyle = async (id) => {
  if (!confirm('Вы уверены, что хотите удалить этот стиль?')) return

  const result = await stylesStore.deleteStyle(id)
  if (result.success) {
    alert('Стиль удалён!')
  } else {
    alert(`Ошибка удаления стиля: ${result.error}`)
  }
}

const initializeDefaults = async () => {
  if (!confirm('Инициализировать стандартные стили? Это создаст базовый набор стилей.')) return

  const result = await stylesStore.initializeDefaults()
  if (result.success) {
    alert('Стандартные стили успешно созданы!')
  } else {
    alert(`Ошибка инициализации стилей: ${result.error}`)
  }
}

const closeModal = () => {
  showModal.value = false
}
</script>

<style scoped>
.admin-styles-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  font-size: 14px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.filter-group label {
  font-size: 14px;
  color: #555;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
}

.empty-state p {
  color: #666;
  font-size: 14px;
}

.styles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.style-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: box-shadow 0.2s;
}

.style-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.style-card.system-style {
  border-left: 4px solid #4caf50;
}

.style-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.style-header h3 {
  font-size: 18px;
  margin: 0;
}

.system-badge {
  background: #4caf50;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
}

.style-info {
  margin-bottom: 16px;
}

.info-item {
  font-size: 13px;
  margin-bottom: 6px;
  color: #555;
}

.info-item strong {
  color: #333;
}

.style-preview {
  margin-bottom: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 4px;
}

.preview-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.preview-text {
  padding: 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.style-css {
  margin-bottom: 16px;
}

.style-css summary {
  cursor: pointer;
  font-size: 13px;
  color: #3498db;
  margin-bottom: 8px;
}

.style-css pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

.style-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  overflow-y: auto;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-content.large-modal {
  max-width: 1000px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.close-button {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-button:hover {
  color: #333;
}

.style-form {
  padding: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group textarea {
  resize: vertical;
  min-height: 60px;
}

.checkbox-group {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.css-editor {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #e0e0e0;
}

.css-editor h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.css-properties {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 4px;
}

.css-json-editor {
  width: 100%;
  min-height: 100px;
  font-family: monospace;
  font-size: 12px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 8px;
}

.preview-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #e0e0e0;
}

.preview-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.preview-box {
  padding: 20px;
  background: white;
  border: 2px dashed #ddd;
  border-radius: 4px;
  min-height: 60px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.btn-primary,
.btn-secondary,
.btn-success,
.btn-danger,
.btn-small,
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #7f8c8d;
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #229954;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #c0392b;
}

.btn-small {
  padding: 6px 12px;
  font-size: 13px;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>




