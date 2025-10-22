<template>
  <div class="templates-management">
    <div class="admin-header">
      <h1>Управление шаблонами документов</h1>
      <button @click="showCreateModal = true" class="btn-create">
        + Создать шаблон
      </button>
    </div>
    
    <!-- Фильтры -->
    <div class="filters-bar">
      <input 
        v-model="searchQuery"
        @input="onSearch"
        type="text"
        placeholder="Поиск шаблонов..."
        class="search-input"
      />
      
      <select v-model="filterCategory" class="filter-select">
        <option value="">Все категории</option>
        <option value="publication">Публикации</option>
        <option value="book">Книги</option>
        <option value="academic">Академические</option>
        <option value="document">Документы</option>
      </select>
      
      <select v-model="filterDocumentType" class="filter-select">
        <option value="">Все типы документов</option>
        <!-- TODO: Загрузить типы документов -->
      </select>
    </div>
    
    <!-- Таблица шаблонов -->
    <div class="templates-table-container">
      <table class="templates-table">
        <thead>
          <tr>
            <th>Название</th>
            <th>Тип документа</th>
            <th>Категория</th>
            <th>Версия</th>
            <th>По умолчанию</th>
            <th>Статус</th>
            <th>Создан</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="template in filteredTemplates" :key="template.id">
            <td>
              <strong>{{ template.name }}</strong>
              <br>
              <small class="text-muted">{{ template.description }}</small>
            </td>
            <td>{{ template.documentTypeId }}</td>
            <td>
              <span class="badge" :class="`badge-${template.category}`">
                {{ getCategoryName(template.category) }}
              </span>
            </td>
            <td>{{ template.version || '1.0' }}</td>
            <td>
              <span v-if="template.isDefault" class="badge badge-success">Да</span>
              <span v-else class="text-muted">Нет</span>
            </td>
            <td>
              <span v-if="template.active" class="badge badge-active">Активен</span>
              <span v-else class="badge badge-inactive">Неактивен</span>
            </td>
            <td>
              <small>{{ formatDate(template.createdAt) }}</small>
            </td>
            <td>
              <div class="action-buttons">
                <button @click="editTemplate(template)" class="btn-action btn-edit">
                  ✏️
                </button>
                <button @click="viewTemplate(template)" class="btn-action btn-view">
                  👁️
                </button>
                <button @click="deleteTemplate(template.id)" class="btn-action btn-delete">
                  🗑️
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="filteredTemplates.length === 0" class="empty-state">
        <p>Шаблоны не найдены</p>
      </div>
    </div>
    
    <!-- Модальное окно создания/редактирования шаблона -->
    <div v-if="showCreateModal || editingTemplate" class="modal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <h2>{{ editingTemplate ? 'Редактировать шаблон' : 'Создать новый шаблон' }}</h2>
          <button @click="closeModal" class="btn-close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label>Название шаблона *</label>
            <input 
              v-model="templateForm.name"
              type="text"
              placeholder="Стандартная научная статья"
              required
            />
          </div>
          
          <div class="form-group">
            <label>Описание</label>
            <textarea 
              v-model="templateForm.description"
              rows="3"
              placeholder="Описание шаблона..."
            ></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Тип документа *</label>
              <select v-model="templateForm.documentTypeId" required>
                <option value="">Выберите тип</option>
                <option value="scientific-article">Научная статья</option>
                <option value="conference-paper">Статья конференции</option>
                <option value="book-chapter">Глава книги</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>Категория *</label>
              <select v-model="templateForm.category" required>
                <option value="publication">Публикация</option>
                <option value="book">Книга</option>
                <option value="academic">Академический</option>
                <option value="document">Документ</option>
              </select>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Версия</label>
              <input 
                v-model="templateForm.version"
                type="text"
                placeholder="1.0"
              />
            </div>
            
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" v-model="templateForm.isDefault" />
                Шаблон по умолчанию для типа документа
              </label>
              <label>
                <input type="checkbox" v-model="templateForm.active" />
                Активен
              </label>
            </div>
          </div>
          
          <div class="form-group">
            <label>Загрузить файл шаблона</label>
            <div class="file-upload">
              <input 
                type="file"
                @change="onFileSelect"
                accept=".html,.docx,.tex,.zip"
                ref="fileInput"
              />
              <button @click="$refs.fileInput.click()" class="btn-upload">
                Выбрать файл
              </button>
              <span v-if="templateForm.file">{{ templateForm.file.name }}</span>
            </div>
            <small class="help-text">Поддерживаемые форматы: HTML, DOCX, LaTeX, ZIP</small>
          </div>
          
          <div class="form-group">
            <label>CSS стили</label>
            <textarea 
              v-model="templateForm.cssStyles"
              rows="5"
              placeholder=".paragraph { font-size: 1rem; }"
              class="code-textarea"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label>HTML контент (опционально)</label>
            <textarea 
              v-model="templateForm.content"
              rows="10"
              placeholder="<div>...</div>"
              class="code-textarea"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="btn-secondary">Отмена</button>
          <button @click="saveTemplate" class="btn-primary" :disabled="!isFormValid">
            {{ editingTemplate ? 'Сохранить' : 'Создать' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- Модальное окно просмотра шаблона -->
    <div v-if="viewingTemplate" class="modal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <h2>Просмотр шаблона: {{ viewingTemplate.name }}</h2>
          <button @click="viewingTemplate = null" class="btn-close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="template-preview">
            <h3>Информация</h3>
            <dl class="info-list">
              <dt>ID:</dt>
              <dd>{{ viewingTemplate.id }}</dd>
              <dt>Тип документа:</dt>
              <dd>{{ viewingTemplate.documentTypeId }}</dd>
              <dt>Категория:</dt>
              <dd>{{ getCategoryName(viewingTemplate.category) }}</dd>
              <dt>Версия:</dt>
              <dd>{{ viewingTemplate.version }}</dd>
              <dt>Создан:</dt>
              <dd>{{ formatDate(viewingTemplate.createdAt) }}</dd>
            </dl>
            
            <h3>CSS стили</h3>
            <pre class="code-preview">{{ viewingTemplate.cssStyles || 'Нет стилей' }}</pre>
            
            <h3>HTML контент</h3>
            <pre class="code-preview">{{ viewingTemplate.content || 'Нет контента' }}</pre>
            
            <h3>Стили абзацев</h3>
            <div v-if="viewingTemplate.paragraphStyles && viewingTemplate.paragraphStyles.length > 0">
              <div 
                v-for="style in viewingTemplate.paragraphStyles" 
                :key="style.id"
                class="style-item"
              >
                <strong>{{ style.name }}</strong> ({{ style.type }})
                <pre>{{ style.cssStyles }}</pre>
              </div>
            </div>
            <p v-else class="text-muted">Стили абзацев не определены</p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="viewingTemplate = null" class="btn-primary">Закрыть</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTemplatesStore } from '@/stores/templates'

const templatesStore = useTemplatesStore()

const showCreateModal = ref(false)
const editingTemplate = ref(null)
const viewingTemplate = ref(null)
const searchQuery = ref('')
const filterCategory = ref('')
const filterDocumentType = ref('')

const templateForm = ref({
  name: '',
  description: '',
  documentTypeId: '',
  category: 'publication',
  version: '1.0',
  isDefault: false,
  active: true,
  file: null,
  cssStyles: '',
  content: ''
})

onMounted(async () => {
  await templatesStore.loadAllTemplates()
})

const filteredTemplates = computed(() => {
  let templates = templatesStore.templates
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    templates = templates.filter(t => 
      t.name.toLowerCase().includes(query) || 
      t.description?.toLowerCase().includes(query)
    )
  }
  
  if (filterCategory.value) {
    templates = templates.filter(t => t.category === filterCategory.value)
  }
  
  if (filterDocumentType.value) {
    templates = templates.filter(t => t.documentTypeId === filterDocumentType.value)
  }
  
  return templates
})

const isFormValid = computed(() => {
  return templateForm.value.name && 
         templateForm.value.documentTypeId && 
         templateForm.value.category
})

const editTemplate = (template) => {
  editingTemplate.value = template
  templateForm.value = {
    name: template.name,
    description: template.description,
    documentTypeId: template.documentTypeId,
    category: template.category,
    version: template.version,
    isDefault: template.isDefault,
    active: template.active,
    file: null,
    cssStyles: template.cssStyles || '',
    content: template.content || ''
  }
}

const viewTemplate = (template) => {
  viewingTemplate.value = template
}

const deleteTemplate = async (id) => {
  if (confirm('Вы уверены, что хотите удалить этот шаблон?')) {
    const result = await templatesStore.deleteTemplate(id)
    if (result.success) {
      alert('Шаблон успешно удален')
    } else {
      alert(`Ошибка: ${result.error}`)
    }
  }
}

const onFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    templateForm.value.file = file
  }
}

const saveTemplate = async () => {
  try {
    let result
    
    if (editingTemplate.value) {
      result = await templatesStore.updateTemplate(editingTemplate.value.id, templateForm.value)
    } else {
      result = await templatesStore.createTemplate(templateForm.value)
    }
    
    if (result.success) {
      alert('Шаблон успешно сохранен')
      closeModal()
    } else {
      alert(`Ошибка: ${result.error}`)
    }
  } catch (error) {
    console.error('Ошибка сохранения шаблона:', error)
    alert('Произошла ошибка при сохранении шаблона')
  }
}

const closeModal = () => {
  showCreateModal.value = false
  editingTemplate.value = null
  templateForm.value = {
    name: '',
    description: '',
    documentTypeId: '',
    category: 'publication',
    version: '1.0',
    isDefault: false,
    active: true,
    file: null,
    cssStyles: '',
    content: ''
  }
}

const onSearch = () => {
  // Поиск происходит автоматически через computed filteredTemplates
}

const getCategoryName = (category) => {
  const categories = {
    'publication': 'Публикация',
    'book': 'Книга',
    'academic': 'Академический',
    'document': 'Документ'
  }
  return categories[category] || category
}

const formatDate = (dateString) => {
  if (!dateString) return 'Не указано'
  return new Date(dateString).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style scoped>
.templates-management {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.admin-header h1 {
  color: #333;
  margin: 0;
}

.btn-create {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-create:hover {
  background: #059669;
}

.filters-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-input,
.filter-select {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
}

.search-input {
  flex: 2;
}

.filter-select {
  flex: 1;
}

.templates-table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
}

.templates-table {
  width: 100%;
  border-collapse: collapse;
}

.templates-table thead {
  background: #f9fafb;
}

.templates-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
}

.templates-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.text-muted {
  color: #9ca3af;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge-publication {
  background: #dbeafe;
  color: #1e40af;
}

.badge-book {
  background: #fef3c7;
  color: #92400e;
}

.badge-academic {
  background: #e0e7ff;
  color: #3730a3;
}

.badge-document {
  background: #f3f4f6;
  color: #1f2937;
}

.badge-success {
  background: #d1fae5;
  color: #065f46;
}

.badge-active {
  background: #d1fae5;
  color: #065f46;
}

.badge-inactive {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: background 0.3s;
}

.btn-action:hover {
  background: #f3f4f6;
}

.btn-edit:hover {
  background: #dbeafe;
}

.btn-delete:hover {
  background: #fee2e2;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #9ca3af;
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
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #9ca3af;
  line-height: 1;
}

.btn-close:hover {
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #374151;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-family: inherit;
  font-size: 1rem;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: normal;
}

.file-upload {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.file-upload input[type="file"] {
  display: none;
}

.btn-upload {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
}

.btn-upload:hover {
  background: #e5e7eb;
}

.help-text {
  color: #9ca3af;
  font-size: 0.85rem;
}

.code-textarea {
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.template-preview h3 {
  color: #333;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}

.info-list {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 0.5rem 1rem;
}

.info-list dt {
  font-weight: 600;
  color: #6b7280;
}

.info-list dd {
  margin: 0;
  color: #333;
}

.code-preview {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  overflow-x: auto;
  font-size: 0.85rem;
}

.style-item {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 0.5rem;
}

.style-item pre {
  margin-top: 0.5rem;
  background: white;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}
</style>



