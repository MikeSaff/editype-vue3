<template>
  <div class="article-editor-new">
    <!-- Header -->
    <div class="editor-header">
      <div class="header-left">
        <button @click="$router.back()" class="btn-back">← {{ t('common.back') || 'Back' }}</button>
        <h1>{{ currentTitle || 'Untitled Article' }}</h1>
      </div>
      <div class="header-actions">
        <span v-if="saving" class="status-indicator saving">
          💾 {{ t('editor.saving') || 'Saving...' }}
        </span>
        <span v-else class="status-indicator saved">
          ✓ {{ t('editor.saved') || 'Saved' }}
        </span>
        <button @click="saveArticle" class="btn btn-primary">
          {{ t('common.save') || 'Save' }}
        </button>
        <button @click="showExportMenu = !showExportMenu" class="btn btn-secondary">
          {{ t('common.export') || 'Export' }} ▾
        </button>
      </div>
    </div>

    <!-- Export Menu -->
    <div v-if="showExportMenu" class="export-menu">
      <button @click="exportAs('pdf-latex')" class="export-option">
        📄 PDF (LaTeX)
      </button>
      <button @click="exportAs('pdf-vivliostyle')" class="export-option">
        📘 PDF (Vivliostyle)
      </button>
      <button @click="exportAs('latex')" class="export-option">
        📝 LaTeX Source
      </button>
      <button @click="exportAs('html')" class="export-option">
        🌐 HTML
      </button>
      <button @click="exportAs('jats')" class="export-option">
        📋 JATS XML
      </button>
    </div>

    <!-- Language Tabs -->
    <div class="language-tabs">
      <button 
        v-for="lang in article.languages" 
        :key="lang"
        @click="currentLanguage = lang"
        :class="{ active: currentLanguage === lang }"
        class="tab-button"
      >
        {{ lang.toUpperCase() }}
      </button>
      <button @click="addLanguage" class="tab-button add-lang">
        + {{ t('editor.addLanguage') || 'Add Language' }}
      </button>
    </div>

    <!-- Metadata Section -->
    <div class="metadata-section">
      <div class="metadata-grid">
        <div class="form-group col-span-2">
          <label>{{ t('editor.title') || 'Title' }} ({{ currentLanguage.toUpperCase() }})</label>
          <input 
            v-model="article.metadata[currentLanguage].title" 
            type="text"
            @input="triggerAutoSave"
            :placeholder="t('editor.titlePlaceholder') || 'Enter article title'"
            class="input-large"
          />
        </div>

        <div class="form-group">
          <label>{{ t('editor.doi') || 'DOI' }}</label>
          <input 
            v-model="article.doi" 
            type="text" 
            placeholder="10.1234/example"
            @input="triggerAutoSave"
          />
        </div>

        <div class="form-group">
          <label>{{ t('editor.pages') || 'Pages' }}</label>
          <div class="pages-input">
            <input 
              v-model.number="article.firstPage" 
              type="number" 
              placeholder="From"
              @input="triggerAutoSave"
            />
            <span>—</span>
            <input 
              v-model.number="article.lastPage" 
              type="number" 
              placeholder="To"
              @input="triggerAutoSave"
            />
          </div>
        </div>

        <div class="form-group col-span-2">
          <label>{{ t('editor.abstract') || 'Abstract' }} ({{ currentLanguage.toUpperCase() }})</label>
          <textarea 
            v-model="article.metadata[currentLanguage].annotation"
            rows="4"
            @input="triggerAutoSave"
            :placeholder="t('editor.abstractPlaceholder') || 'Enter abstract'"
          ></textarea>
        </div>

        <div class="form-group col-span-2">
          <label>{{ t('editor.keywords') || 'Keywords' }} ({{ currentLanguage.toUpperCase() }})</label>
          <input 
            v-model="article.metadata[currentLanguage].keywords"
            type="text"
            @input="triggerAutoSave"
            :placeholder="t('editor.keywordsPlaceholder') || 'keyword1, keyword2, keyword3'"
          />
        </div>
      </div>
    </div>

    <!-- Tiptap Editor -->
    <div class="editor-section">
      <TiptapEditor 
        v-model="article.pmJson"
        @change="onEditorChange"
        :placeholder="t('editor.contentPlaceholder') || 'Start writing your article...'"
      />
    </div>

    <!-- References Manager -->
    <div class="references-section">
      <ReferencesManager 
        v-model="article.references"
        :citation-style="citationStyle"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import TiptapEditor from '@/components/TiptapEditor.vue'
import ReferencesManager from '@/components/ReferencesManager.vue'
import { articlesApi } from '@/api/articles'
import { exportApi } from '@/api/export'
import { useAuthStore } from '@/stores/auth'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// State
const article = ref({
  id: null,
  editorumId: null,
  titles: {
    ru: '',
    en: ''
  },
  metadata: {
    ru: { title: '', annotation: '', keywords: '' },
    en: { title: '', annotation: '', keywords: '' }
  },
  pmJson: null,
  text: '',
  references: [],
  doi: '',
  firstPage: null,
  lastPage: null,
  languages: ['en', 'ru'],
  authorIds: [],
  authorNames: []
})

const currentLanguage = ref('en')
const saving = ref(false)
const showExportMenu = ref(false)
const citationStyle = ref('gost-numeric')

let autoSaveTimer = null

// Computed
const currentTitle = computed(() => {
  return article.value.metadata[currentLanguage.value]?.title || ''
})

// Load article
const loadArticle = async () => {
  const id = route.params.id
  if (!id || id === 'new') {
    // New article
    return
  }

  try {
    const response = await articlesApi.getById(id)
    article.value = {
      ...response.data,
      // Ensure all required fields exist
      titles: response.data.titles || { ru: '', en: '' },
      metadata: response.data.metadata || {
        ru: { title: '', annotation: '', keywords: '' },
        en: { title: '', annotation: '', keywords: '' }
      },
      references: response.data.references || [],
      languages: response.data.languages || ['en', 'ru']
    }
  } catch (error) {
    console.error('Failed to load article', error)
    alert('Failed to load article')
  }
}

// Save article
const saveArticle = async () => {
  if (saving.value) return
  
  saving.value = true
  
  try {
    // Sync titles with metadata
    article.value.languages.forEach(lang => {
      if (article.value.metadata[lang]) {
        article.value.titles[lang] = article.value.metadata[lang].title
      }
    })
    
    if (article.value.id) {
      await articlesApi.update(article.value.id, article.value)
    } else {
      const response = await articlesApi.create(article.value)
      article.value.id = response.data.id
      router.replace({ name: 'article-editor', params: { id: article.value.id } })
    }
    
    console.log('Article saved successfully')
  } catch (error) {
    console.error('Failed to save article', error)
    alert('Failed to save article')
  } finally {
    saving.value = false
  }
}

// Auto-save
const triggerAutoSave = () => {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  
  autoSaveTimer = setTimeout(() => {
    saveArticle()
  }, 3000)
}

// Editor change handler
const onEditorChange = (json) => {
  article.value.pmJson = JSON.stringify(json)
  triggerAutoSave()
}

// Add language
const addLanguage = () => {
  const lang = prompt('Enter language code (e.g., cn, de, fr):')
  if (lang && !article.value.languages.includes(lang)) {
    article.value.languages.push(lang)
    article.value.titles[lang] = ''
    article.value.metadata[lang] = { title: '', annotation: '', keywords: '' }
    currentLanguage.value = lang
  }
}

// Export article
const exportAs = async (format) => {
  if (!article.value.id) {
    alert('Please save the article first')
    return
  }
  
  showExportMenu.value = false
  
  try {
    let response
    const lang = currentLanguage.value
    
    switch (format) {
      case 'pdf-latex':
        response = await exportApi.exportPdfLatex(article.value.id, lang)
        downloadFile(response.data, `article_${article.value.id}_latex.pdf`, 'application/pdf')
        break
        
      case 'pdf-vivliostyle':
        response = await exportApi.exportPdfVivliostyle(article.value.id, lang)
        downloadFile(response.data, `article_${article.value.id}_vivlio.pdf`, 'application/pdf')
        break
        
      case 'latex':
        response = await exportApi.exportLatex(article.value.id, lang)
        downloadFile(response.data, `article_${article.value.id}.tex`, 'text/plain')
        break
        
      case 'html':
        response = await exportApi.exportHtml(article.value.id, lang)
        downloadFile(response.data, `article_${article.value.id}.html`, 'text/html')
        break
        
      case 'jats':
        response = await exportApi.exportJats(article.value.id, lang)
        downloadFile(response.data, `article_${article.value.id}.xml`, 'application/xml')
        break
    }
  } catch (error) {
    console.error('Export failed', error)
    alert('Export failed: ' + error.message)
  }
}

// Download file helper
const downloadFile = (data, filename, type) => {
  const blob = new Blob([data], { type })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
}

// Lifecycle
onMounted(() => {
  loadArticle()
})

onBeforeUnmount(() => {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
})

// Watch for language changes to update citation style
watch(currentLanguage, (newLang) => {
  citationStyle.value = newLang === 'ru' ? 'gost-numeric' : 'apa'
})
</script>

<style scoped>
.article-editor-new {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem;
  background: #f5f5f5;
}

/* Header */
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-left h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.btn-back {
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e5e7eb;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status-indicator {
  font-size: 0.9rem;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}

.status-indicator.saving {
  color: #f59e0b;
  background: #fef3c7;
}

.status-indicator.saved {
  color: #10b981;
  background: #d1fae5;
}

/* Export Menu */
.export-menu {
  position: absolute;
  right: 1rem;
  top: 5rem;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  z-index: 100;
  min-width: 200px;
}

.export-option {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  text-align: left;
  background: none;
  border: none;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.2s;
}

.export-option:hover {
  background: #f9fafb;
}

.export-option:last-child {
  border-bottom: none;
}

/* Language Tabs */
.language-tabs {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: white;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.tab-button {
  padding: 0.5rem 1.5rem;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.tab-button:hover {
  background: #e5e7eb;
}

.tab-button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.tab-button.add-lang {
  color: #10b981;
  margin-left: auto;
}

/* Metadata Section */
.metadata-section {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.metadata-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group.col-span-2 {
  grid-column: span 2;
}

.form-group label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #374151;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-family: inherit;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-large {
  font-size: 1.125rem;
  font-weight: 500;
}

.pages-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pages-input input {
  flex: 1;
}

/* Editor Section */
.editor-section {
  margin-bottom: 1rem;
}

/* References Section */
.references-section {
  margin-bottom: 2rem;
}

/* Buttons */
.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

/* Responsive */
@media (max-width: 768px) {
  .metadata-grid {
    grid-template-columns: 1fr;
  }
  
  .form-group.col-span-2 {
    grid-column: span 1;
  }
  
  .editor-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-left,
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
