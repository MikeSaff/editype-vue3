<template>
  <div class="paragraph-editor" :class="`paragraph-${paragraph.type}`">
    <!-- Селектор типа абзаца -->
    <div class="paragraph-toolbar" v-if="!readonly">
      <select 
        v-model="localParagraph.styleId" 
        @change="onStyleChange"
        class="style-selector"
      >
        <option 
          v-for="style in availableStyles" 
          :key="style.id"
          :value="style.id"
        >
          {{ style.name }}
        </option>
      </select>
      
      <div class="paragraph-actions">
        <button @click="insertFormula" title="Вставить формулу" class="btn-icon">∑</button>
        <button @click="insertTable" title="Вставить таблицу" class="btn-icon">⊞</button>
        <button @click="insertImage" title="Вставить изображение" class="btn-icon">🖼</button>
        <button @click="moveParagraphUp" title="Переместить вверх" class="btn-icon">↑</button>
        <button @click="moveParagraphDown" title="Переместить вниз" class="btn-icon">↓</button>
        <button @click="deleteParagraph" title="Удалить абзац" class="btn-icon btn-danger">×</button>
      </div>
    </div>
    
    <!-- Контент абзаца в зависимости от типа -->
    <div class="paragraph-content" :style="getAppliedStyles()">
      <!-- Обычный текст -->
      <div 
        v-if="localParagraph.contentType === 'TEXT'"
        contenteditable="true"
        @input="onContentChange"
        @blur="saveContent"
        class="text-content"
        ref="contentEl"
        v-html="localParagraph.content"
      ></div>
      
      <!-- Inline формула -->
      <div 
        v-else-if="localParagraph.contentType === 'FORMULA_INLINE'"
        class="formula-inline"
      >
        <span class="formula-wrapper">
          <input 
            v-model="localParagraph.content"
            @input="onContentChange"
            @blur="saveContent"
            placeholder="E=mc^2"
            class="formula-input"
          />
          <span class="formula-preview">\( {{ localParagraph.content }} \)</span>
        </span>
      </div>
      
      <!-- Block формула -->
      <div 
        v-else-if="localParagraph.contentType === 'FORMULA_BLOCK'"
        class="formula-block"
      >
        <textarea 
          v-model="localParagraph.content"
          @input="onContentChange"
          @blur="saveContent"
          placeholder="\\int_{0}^{\\infty} e^{-x} dx"
          class="formula-textarea"
          rows="3"
        ></textarea>
        <div class="formula-preview">\[ {{ localParagraph.content }} \]</div>
      </div>
      
      <!-- Таблица -->
      <div 
        v-else-if="localParagraph.contentType === 'TABLE'"
        class="table-content"
      >
        <table class="editable-table">
          <tbody>
            <tr v-for="(row, rowIndex) in tableData" :key="rowIndex">
              <td 
                v-for="(cell, cellIndex) in row" 
                :key="cellIndex"
                contenteditable="true"
                @input="onTableCellChange(rowIndex, cellIndex, $event)"
                @blur="saveContent"
              >
                {{ cell }}
              </td>
            </tr>
          </tbody>
        </table>
        <div class="table-controls">
          <button @click="addTableRow" class="btn-small">+ Строка</button>
          <button @click="addTableColumn" class="btn-small">+ Столбец</button>
        </div>
      </div>
      
      <!-- Изображение -->
      <div 
        v-else-if="localParagraph.contentType === 'IMAGE'"
        class="image-content"
      >
        <img v-if="localParagraph.content" :src="localParagraph.content" alt="Image" />
        <div v-else class="image-placeholder">
          <input 
            type="file" 
            @change="onImageUpload" 
            accept="image/*"
            ref="imageInput"
          />
          <p>Загрузите изображение</p>
        </div>
        <input 
          v-model="imageCap tion"
          @blur="saveCaption"
          placeholder="Подпись к изображению"
          class="image-caption"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

const props = defineProps({
  paragraph: {
    type: Object,
    required: true
  },
  availableStyles: {
    type: Array,
    default: () => []
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update', 'delete', 'move-up', 'move-down'])

const localParagraph = reactive({ ...props.paragraph })
const contentEl = ref(null)
const imageInput = ref(null)
const imageCaption = ref('')
const tableData = ref([
  ['Ячейка 1', 'Ячейка 2'],
  ['Ячейка 3', 'Ячейка 4']
])

// Применение стилей из шаблона
const getAppliedStyles = () => {
  const style = props.availableStyles.find(s => s.id === localParagraph.styleId)
  return style?.cssStyles || ''
}

// Обработка изменения контента
const onContentChange = (event) => {
  if (event.target) {
    localParagraph.content = event.target.innerHTML || event.target.value
    localParagraph.plainText = event.target.innerText || event.target.value
  }
  emit('update', localParagraph)
}

// Сохранение контента
const saveContent = () => {
  emit('update', localParagraph)
}

// Изменение стиля абзаца
const onStyleChange = () => {
  const style = props.availableStyles.find(s => s.id === localParagraph.styleId)
  if (style) {
    localParagraph.type = style.type
  }
  emit('update', localParagraph)
}

// Вставка формулы
const insertFormula = () => {
  const isBlock = confirm('Вставить блочную формулу? (Отмена = строчная)')
  localParagraph.contentType = isBlock ? 'FORMULA_BLOCK' : 'FORMULA_INLINE'
  localParagraph.content = ''
  emit('update', localParagraph)
}

// Вставка таблицы
const insertTable = () => {
  localParagraph.contentType = 'TABLE'
  localParagraph.content = JSON.stringify(tableData.value)
  emit('update', localParagraph)
}

// Вставка изображения
const insertImage = () => {
  localParagraph.contentType = 'IMAGE'
  imageInput.value?.click()
}

// Загрузка изображения
const onImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      localParagraph.content = e.target.result
      emit('update', localParagraph)
    }
    reader.readAsDataURL(file)
  }
}

// Сохранение подписи к изображению
const saveCaption = () => {
  if (!localParagraph.properties) {
    localParagraph.properties = {}
  }
  localParagraph.properties.caption = imageCaption.value
  emit('update', localParagraph)
}

// Изменение ячейки таблицы
const onTableCellChange = (rowIndex, cellIndex, event) => {
  tableData.value[rowIndex][cellIndex] = event.target.innerText
  localParagraph.content = JSON.stringify(tableData.value)
  emit('update', localParagraph)
}

// Добавление строки в таблицу
const addTableRow = () => {
  const colCount = tableData.value[0]?.length || 2
  tableData.value.push(Array(colCount).fill(''))
  localParagraph.content = JSON.stringify(tableData.value)
  emit('update', localParagraph)
}

// Добавление столбца в таблицу
const addTableColumn = () => {
  tableData.value.forEach(row => row.push(''))
  localParagraph.content = JSON.stringify(tableData.value)
  emit('update', localParagraph)
}

// Перемещение абзаца
const moveParagraphUp = () => {
  emit('move-up', localParagraph.id)
}

const moveParagraphDown = () => {
  emit('move-down', localParagraph.id)
}

// Удаление абзаца
const deleteParagraph = () => {
  if (confirm('Удалить этот абзац?')) {
    emit('delete', localParagraph.id)
  }
}

// Синхронизация с props
watch(() => props.paragraph, (newVal) => {
  Object.assign(localParagraph, newVal)
}, { deep: true })
</script>

<style scoped>
.paragraph-editor {
  border: 1px solid transparent;
  border-radius: 4px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s;
}

.paragraph-editor:hover {
  border-color: #e5e7eb;
  background: #f9fafb;
}

.paragraph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background: #f3f4f6;
  border-radius: 4px;
}

.style-selector {
  padding: 0.25rem 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: white;
  font-size: 0.9rem;
}

.paragraph-actions {
  display: flex;
  gap: 0.25rem;
}

.btn-icon {
  padding: 0.25rem 0.5rem;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.btn-icon:hover {
  background: #e5e7eb;
}

.btn-danger {
  color: #dc2626;
}

.btn-danger:hover {
  background: #fee2e2;
}

.paragraph-content {
  min-height: 2rem;
}

.text-content {
  outline: none;
  padding: 0.5rem;
  line-height: 1.8;
}

.text-content:focus {
  background: white;
  border-radius: 4px;
}

.formula-inline,
.formula-block {
  padding: 0.5rem;
}

.formula-wrapper {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.formula-input,
.formula-textarea {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.formula-preview {
  flex: 1;
  padding: 0.5rem;
  background: #f9fafb;
  border-radius: 4px;
}

.editable-table {
  width: 100%;
  border-collapse: collapse;
}

.editable-table td {
  border: 1px solid #d1d5db;
  padding: 0.5rem;
  min-width: 100px;
}

.editable-table td:focus {
  outline: 2px solid #667eea;
}

.table-controls {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
}

.btn-small {
  padding: 0.25rem 0.75rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-small:hover {
  background: #e5e7eb;
}

.image-content {
  text-align: center;
}

.image-content img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.image-placeholder {
  padding: 2rem;
  border: 2px dashed #d1d5db;
  border-radius: 4px;
  text-align: center;
}

.image-caption {
  width: 100%;
  margin-top: 0.5rem;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.9rem;
  font-style: italic;
}
</style>




