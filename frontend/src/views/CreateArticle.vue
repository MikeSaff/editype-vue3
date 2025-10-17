<template>
  <div class="create-article-page">
    <div class="page-header">
      <h1>Создать новую статью</h1>
      <button @click="goBack" class="btn-back">← Назад</button>
    </div>
    
    <div class="creation-flow">
      <!-- Шаг 1: Выбор типа документа -->
      <div class="step" :class="{ active: currentStep === 1 }">
        <div class="step-header">
          <div class="step-number">1</div>
          <h2>Выберите тип документа</h2>
          <p>Выберите подходящий тип документа из доступных в системе Editorum</p>
        </div>
        
        <div v-if="documentTypesStore.loading" class="loading">
          <div class="spinner"></div>
          <p>Загрузка типов документов...</p>
        </div>
        
        <div v-else class="document-types-grid">
          <div 
            v-for="docType in documentTypesStore.documentTypes" 
            :key="docType.id"
            class="document-type-card"
            :class="{ selected: selectedDocumentType?.id === docType.id }"
            @click="selectDocumentType(docType)"
          >
            <div class="card-header">
              <h3>{{ docType.name }}</h3>
              <span class="category-badge">{{ getCategoryName(docType.category) }}</span>
            </div>
            <p class="description">{{ docType.description }}</p>
            <div class="card-footer">
              <div class="languages">
                <span 
                  v-for="lang in docType.supportedLanguages" 
                  :key="lang"
                  class="lang-badge"
                >
                  {{ getLanguageName(lang) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="step-actions">
          <button 
            @click="nextStep" 
            :disabled="!selectedDocumentType"
            class="btn-primary"
          >
            Далее →
          </button>
        </div>
      </div>
      
      <!-- Шаг 2: Выбор шаблона -->
      <div class="step" :class="{ active: currentStep === 2 }">
        <div class="step-header">
          <div class="step-number">2</div>
          <h2>Выберите шаблон макета</h2>
          <p>Выберите шаблон для типа документа "{{ selectedDocumentType?.name }}"</p>
        </div>
        
        <div class="template-options">
          <div 
            class="template-option" 
            :class="{ selected: selectedTemplate === 'empty' }"
            @click="selectTemplate('empty')"
          >
            <div class="template-icon">📄</div>
            <h3>Пустой документ</h3>
            <p>Создать документ с базовой структурой</p>
          </div>
          
          <div 
            v-for="template in availableTemplates"
            :key="template.id"
            class="template-option"
            :class="{ selected: selectedTemplate === template.id }"
            @click="selectTemplate(template.id)"
          >
            <div class="template-icon">📋</div>
            <h3>{{ template.name }}</h3>
            <p>{{ template.description || 'Шаблон макета' }}</p>
            <div v-if="template.isDefault" class="default-badge">По умолчанию</div>
          </div>
          
          <div 
            class="template-option upload-option" 
            :class="{ selected: selectedTemplate === 'upload' }"
            @click="uploadTemplate"
          >
            <div class="template-icon">📁</div>
            <h3>Загрузить файл</h3>
            <p>Загрузить существующий документ или архив</p>
          </div>
        </div>
        
        <div class="step-actions">
          <button @click="prevStep" class="btn-secondary">← Назад</button>
          <button 
            @click="nextStep" 
            :disabled="!selectedTemplate"
            class="btn-primary"
          >
            Далее →
          </button>
        </div>
      </div>
      
      <!-- Шаг 3: Настройки -->
      <div class="step" :class="{ active: currentStep === 3 }">
        <div class="step-header">
          <div class="step-number">3</div>
          <h2>Настройки документа</h2>
          <p>Задайте основные параметры нового документа</p>
        </div>
        
        <div class="document-settings">
          <div class="setting-group">
            <label>Заголовок документа:</label>
            <input 
              v-model="articleSettings.title" 
              type="text" 
              placeholder="Введите заголовок"
              class="form-input"
            />
          </div>
          
          <div class="setting-group">
            <label>Язык по умолчанию:</label>
            <select v-model="articleSettings.defaultLanguage" class="form-select">
              <option 
                v-for="lang in selectedDocumentType?.supportedLanguages || ['ru']"
                :key="lang"
                :value="lang"
              >
                {{ getLanguageName(lang) }}
              </option>
            </select>
          </div>
          
          <div class="setting-group">
            <label>Журнал/Коллекция:</label>
            <select v-model="articleSettings.journalId" class="form-select">
              <option value="">Выберите журнал (опционально)</option>
              <!-- TODO: Загрузить список журналов -->
            </select>
          </div>
        </div>
        
        <div class="step-actions">
          <button @click="prevStep" class="btn-secondary">← Назад</button>
          <button 
            @click="createArticle" 
            :disabled="creating"
            class="btn-primary"
          >
            <span v-if="creating">Создание...</span>
            <span v-else>Создать статью</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDocumentTypesStore } from '@/stores/documentTypes'
import { useTemplatesStore } from '@/stores/templates'
import { useArticlesStore } from '@/stores/articles'

const router = useRouter()
const documentTypesStore = useDocumentTypesStore()
const templatesStore = useTemplatesStore()
const articlesStore = useArticlesStore()

const currentStep = ref(1)
const selectedDocumentType = ref(null)
const selectedTemplate = ref(null)
const availableTemplates = ref([])
const creating = ref(false)

const articleSettings = ref({
  title: '',
  defaultLanguage: 'ru',
  journalId: ''
})

onMounted(async () => {
  await documentTypesStore.loadDocumentTypes()
})

const selectDocumentType = (docType) => {
  selectedDocumentType.value = docType
  articleSettings.value.defaultLanguage = docType.defaultLanguage || 'ru'
}

const selectTemplate = (templateId) => {
  selectedTemplate.value = templateId
}

const uploadedFile = ref(null)
const uploadFileName = ref('')

const uploadTemplate = () => {
  // Trigger file input
  const fileInput = document.createElement('input')
  fileInput.type = 'file'
  fileInput.accept = '.html,.htm,.tex,.latex,.docx,.zip'
  fileInput.onchange = async (e) => {
    const file = e.target.files[0]
    if (file) {
      selectedTemplate.value = 'upload'
      uploadedFile.value = file
      uploadFileName.value = file.name
      alert(`Файл "${file.name}" выбран. Он будет импортирован при создании статьи.`)
    }
  }
  fileInput.click()
}

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const createArticle = async () => {
  creating.value = true
  try {
    // If user uploaded a file, import it first
    let importedParagraphs = null
    if (selectedTemplate.value === 'upload' && uploadedFile.value) {
      const importResult = await templatesStore.importDocument(
        uploadedFile.value,
        articleSettings.value.title,
        selectedDocumentType.value.id
      )
      
      if (importResult.success) {
        importedParagraphs = importResult.paragraphs
        alert(`Документ успешно импортирован! Создано параграфов: ${importResult.paragraphCount}`)
      } else {
        alert(`Ошибка импорта документа: ${importResult.error}`)
        creating.value = false
        return
      }
    }
    
    const articleData = {
      documentType: selectedDocumentType.value.id,
      templateId: selectedTemplate.value === 'empty' || selectedTemplate.value === 'upload' ? null : selectedTemplate.value,
      title: {
        [articleSettings.value.defaultLanguage]: articleSettings.value.title
      },
      journalId: articleSettings.value.journalId || null,
      defaultLanguage: articleSettings.value.defaultLanguage,
      createEmpty: selectedTemplate.value === 'empty',
      importedParagraphs: importedParagraphs // Include imported paragraphs if available
    }
    
    const result = await articlesStore.createArticle(articleData)
    if (result.success) {
      // Переходим к редактору созданной статьи
      router.push(`/editor/${result.data.id}`)
    } else {
      alert(`Ошибка создания статьи: ${result.error}`)
    }
  } catch (error) {
    console.error('Ошибка создания статьи:', error)
    alert('Произошла ошибка при создании статьи')
  } finally {
    creating.value = false
  }
}

const goBack = () => {
  router.push('/articles')
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

const getLanguageName = (lang) => {
  const languages = {
    'ru': 'Русский',
    'en': 'English',
    'zh': '中文'
  }
  return languages[lang] || lang.toUpperCase()
}

const getTemplateName = (templateId) => {
  const templates = {
    'standard-article': 'Стандартная статья',
    'review-article': 'Обзорная статья',
    'short-communication': 'Краткое сообщение',
    'conference-full': 'Полная статья конференции',
    'conference-short': 'Краткая статья конференции',
    'book-chapter': 'Глава книги',
    'monograph-chapter': 'Глава монографии',
    'phd-thesis': 'Докторская диссертация',
    'candidate-thesis': 'Кандидатская диссертация',
    'research-report': 'Научный отчет',
    'technical-report': 'Технический отчет'
  }
  return templates[templateId] || templateId
}
</script>

<style scoped>
.create-article-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #333;
  margin: 0;
}

.btn-back {
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-back:hover {
  background: #e5e7eb;
}

.creation-flow {
  display: flex;
  gap: 2rem;
}

.step {
  flex: 1;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.step.active {
  opacity: 1;
}

.step-header {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.step-number {
  background: #667eea;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.2rem;
}

.step-header h2 {
  color: #333;
  margin: 0;
}

.step-header p {
  color: #666;
  margin: 0.5rem 0 0 0;
}

.loading {
  text-align: center;
  padding: 2rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.document-types-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.document-type-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s;
}

.document-type-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.document-type-card.selected {
  border-color: #667eea;
  background: #f8faff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.card-header h3 {
  color: #333;
  margin: 0;
  font-size: 1.1rem;
}

.category-badge {
  background: #e5e7eb;
  color: #6b7280;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.description {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.languages {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.lang-badge {
  background: #f3f4f6;
  color: #6b7280;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
}

.template-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.template-option {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.template-option:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.template-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.template-option h3 {
  color: #333;
  margin: 0 0 0.5rem 0;
}

.template-option p {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

.template-option.selected {
  border-color: #667eea;
  background: #f8faff;
}

.default-badge {
  background: #10b981;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  margin-top: 0.5rem;
  display: inline-block;
}

.document-settings {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.setting-group {
  margin-bottom: 1.5rem;
}

.setting-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
}

.step-actions {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
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
  border: none;
}

.btn-secondary:hover {
  background: #e5e7eb;
}
</style>



