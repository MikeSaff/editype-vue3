<template>
  <div class="article-editor">
    <div class="editor-header">
      <h1>{{ currentTitle || 'Untitled Article' }}</h1>
      <div class="header-actions">
        <span v-if="lockedBy && lockedBy !== user?.email" class="lock-warning">
          🔒 Locked by {{ lockedBy }}
        </span>
        <span v-else-if="autoSaving" class="saving-indicator">
          💾 Saving...
        </span>
        <span v-else class="saved-indicator">
          ✓ Saved
        </span>
        <button @click="saveArticle" class="btn btn-primary">Save</button>
        <button @click="showExportDialog = true" class="btn">Export</button>
      </div>
    </div>

    <!-- Language tabs -->
    <div class="language-tabs">
      <button 
        v-for="lang in availableLanguages" 
        :key="lang"
        @click="currentLanguage = lang"
        :class="{ active: currentLanguage === lang }"
        class="tab-button"
      >
        {{ lang.toUpperCase() }}
      </button>
      <button @click="showAddLanguageDialog = true" class="tab-button add-lang">
        + Add Language
      </button>
    </div>

    <!-- Metadata section -->
    <div class="metadata-section">
      <div class="form-group">
        <label>{{ t('publications.titleField') }} ({{ currentLanguage.toUpperCase() }})</label>
        <input 
          v-model="article.metadata[currentLanguage].title" 
          type="text"
          @input="triggerAutoSave"
          placeholder="Article title"
        />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>{{ t('publications.doi') }}</label>
          <input v-model="article.doi" type="text" placeholder="10.1234/example" />
        </div>
        <div class="form-group">
          <label>First Page</label>
          <input v-model="article.firstPage" type="number" />
        </div>
        <div class="form-group">
          <label>Last Page</label>
          <input v-model="article.lastPage" type="number" />
        </div>
      </div>

      <div class="form-group">
        <label>Abstract ({{ currentLanguage.toUpperCase() }})</label>
        <textarea 
          v-model="article.metadata[currentLanguage].annotation"
          rows="4"
          @input="triggerAutoSave"
          placeholder="Article abstract"
        ></textarea>
      </div>

      <div class="form-group">
        <label>Keywords ({{ currentLanguage.toUpperCase() }})</label>
        <input 
          v-model="article.metadata[currentLanguage].keywords"
          type="text"
          @input="triggerAutoSave"
          placeholder="keyword1, keyword2, keyword3"
        />
      </div>
    </div>

    <!-- Rich text editor -->
    <div class="editor-section">
      <label>Article Text</label>
      <div class="editor-toolbar">
        <button @click="toggleBold" title="Bold"><strong>B</strong></button>
        <button @click="toggleItalic" title="Italic"><em>I</em></button>
        <button @click="insertFormula" title="Insert Formula">∑</button>
        <button @click="insertTable" title="Insert Table">⊞</button>
      </div>
      <div class="rich-editor" contenteditable="true" ref="editorEl" @input="onEditorInput">
        {{ article.text }}
      </div>
    </div>

    <!-- References section -->
    <div class="references-section">
      <h3>References</h3>
      <button @click="addReference" class="btn btn-small">+ Add Reference</button>
      <div v-for="(ref, index) in article.references" :key="index" class="reference-item">
        <textarea 
          v-model="ref.text[currentLanguage]"
          rows="2"
          :placeholder="`Reference ${index + 1} (${currentLanguage.toUpperCase()})`"
        ></textarea>
        <button @click="removeReference(index)" class="btn btn-small btn-danger">Remove</button>
      </div>
    </div>

    <!-- Preview panel -->
    <div class="preview-panel" v-if="showPreview">
      <h3>Preview ({{ currentLanguage.toUpperCase() }})</h3>
      <div class="preview-content" v-html="renderedPreview"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { articleApi } from '@/api/articles'
import { useAuthStore } from '@/stores/auth'

const { t, locale } = useI18n()
const route = useRoute()
const authStore = useAuthStore()

const currentLanguage = ref('en')
const availableLanguages = ref(['ru', 'en'])
const showPreview = ref(false)
const showExportDialog = ref(false)
const showAddLanguageDialog = ref(false)
const autoSaving = ref(false)
const lockedBy = ref(null)
const editorEl = ref(null)

let autoSaveTimer = null

const article = reactive({
  id: null,
  editorumId: null,
  metadata: {
    ru: { title: '', annotation: '', keywords: '' },
    en: { title: '', annotation: '', keywords: '' }
  },
  text: '',
  pmJson: null,
  references: [],
  doi: '',
  firstPage: null,
  lastPage: null,
  languages: ['ru', 'en']
})

const currentTitle = computed(() => {
  return article.metadata[currentLanguage.value]?.title || ''
})

const renderedPreview = computed(() => {
  // Simple HTML rendering (will be replaced with MathJax later)
  return article.text
})

const user = computed(() => authStore.user)

const loadArticle = async () => {
  const id = route.params.id
  if (!id) return

  try {
    const response = await articleApi.getArticleById(id)
    Object.assign(article, response.data)
    
    // Try to acquire lock
    if (user.value?.email) {
      const lockResponse = await articleApi.acquireLock(id, user.value.email)
      if (!lockResponse.data.success) {
        // Someone else is editing
        lockedBy.value = 'another user'
      }
    }
  } catch (err) {
    console.error('Failed to load article', err)
  }
}

const saveArticle = async () => {
  try {
    autoSaving.value = true
    await articleApi.saveArticle(article.id, article)
    autoSaving.value = false
  } catch (err) {
    console.error('Failed to save article', err)
    autoSaving.value = false
  }
}

const triggerAutoSave = () => {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  
  autoSaveTimer = setTimeout(() => {
    saveArticle()
  }, 3000) // Auto-save after 3 seconds of inactivity
}

const onEditorInput = (event) => {
  article.text = event.target.innerText
  triggerAutoSave()
}

const toggleBold = () => {
  document.execCommand('bold')
}

const toggleItalic = () => {
  document.execCommand('italic')
}

const insertFormula = () => {
  const formula = prompt('Enter LaTeX formula (e.g., E=mc^2):')
  if (formula) {
    document.execCommand('insertHTML', false, `<span class="math-inline">\\(${formula}\\)</span>`)
  }
}

const insertTable = () => {
  const rows = prompt('Number of rows:', '3')
  const cols = prompt('Number of columns:', '3')
  if (rows && cols) {
    let tableHTML = '<table border="1"><tbody>'
    for (let i = 0; i < rows; i++) {
      tableHTML += '<tr>'
      for (let j = 0; j < cols; j++) {
        tableHTML += '<td>Cell</td>'
      }
      tableHTML += '</tr>'
    }
    tableHTML += '</tbody></table>'
    document.execCommand('insertHTML', false, tableHTML)
  }
}

const addReference = () => {
  article.references.push({
    text: { ru: '', en: '', cn: '' },
    doi: ''
  })
}

const removeReference = (index) => {
  article.references.splice(index, 1)
}

onMounted(() => {
  loadArticle()
})

onBeforeUnmount(() => {
  // Release lock when leaving
  if (article.id && user.value?.email) {
    articleApi.releaseLock(article.id, user.value.email)
  }
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
})
</script>

<style scoped>
.article-editor {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #eee;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.lock-warning {
  color: #e74c3c;
  font-weight: bold;
}

.saving-indicator {
  color: #f39c12;
}

.saved-indicator {
  color: #27ae60;
}

.language-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #eee;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  background-color: transparent;
  border: none;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  font-weight: 500;
}

.tab-button:hover {
  background-color: #f0f0f0;
}

.tab-button.active {
  border-bottom-color: #3498db;
  color: #3498db;
}

.tab-button.add-lang {
  color: #27ae60;
  margin-left: auto;
}

.metadata-section {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
  font-size: 1rem;
}

.editor-section {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.editor-toolbar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.editor-toolbar button {
  padding: 0.5rem 1rem;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.editor-toolbar button:hover {
  background: #e8e8e8;
}

.rich-editor {
  min-height: 400px;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  outline: none;
  line-height: 1.8;
}

.rich-editor:focus {
  border-color: #3498db;
}

.references-section {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.reference-item {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  align-items: flex-start;
}

.reference-item textarea {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9rem;
}

.preview-panel {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.preview-content {
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 4px;
  background: #fafafa;
  line-height: 1.8;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-small {
  padding: 0.25rem 0.75rem;
  background-color: #95a5a6;
  color: white;
}

.btn-danger {
  background-color: #e74c3c;
}
</style>




