<template>
  <div class="references-manager">
    <div class="references-header">
      <h3>{{ t('editor.references') || 'References' }}</h3>
      <div class="references-actions">
        <button @click="showAddDialog = true" class="btn btn-primary">
          + {{ t('editor.addReference') || 'Add Reference' }}
        </button>
        <button @click="importBibTeX" class="btn btn-secondary">
          {{ t('editor.importBibTeX') || 'Import BibTeX' }}
        </button>
        <button @click="exportBibTeX" class="btn btn-secondary">
          {{ t('editor.exportBibTeX') || 'Export BibTeX' }}
        </button>
      </div>
    </div>

    <!-- Список ссылок -->
    <div class="references-list">
      <div 
        v-for="(ref, index) in references" 
        :key="ref.id || index"
        class="reference-item"
      >
        <div class="reference-number">[{{ index + 1 }}]</div>
        <div class="reference-content">
          <div class="reference-formatted" v-html="formatReference(ref)"></div>
          <div class="reference-meta">
            <span v-if="ref.DOI" class="reference-doi">DOI: {{ ref.DOI }}</span>
            <span v-if="ref.URL" class="reference-url">
              <a :href="ref.URL" target="_blank">{{ ref.URL }}</a>
            </span>
          </div>
        </div>
        <div class="reference-actions">
          <button @click="editReference(index)" class="btn-icon" title="Edit">
            ✏️
          </button>
          <button @click="deleteReference(index)" class="btn-icon" title="Delete">
            🗑️
          </button>
          <button @click="moveUp(index)" :disabled="index === 0" class="btn-icon" title="Move Up">
            ↑
          </button>
          <button @click="moveDown(index)" :disabled="index === references.length - 1" class="btn-icon" title="Move Down">
            ↓
          </button>
        </div>
      </div>

      <div v-if="references.length === 0" class="empty-state">
        <p>{{ t('editor.noReferences') || 'No references yet. Add your first reference.' }}</p>
      </div>
    </div>

    <!-- Диалог добавления ссылки -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingIndex !== null ? 'Edit Reference' : 'Add Reference' }}</h3>
          <button @click="closeDialog" class="btn-close">✕</button>
        </div>

        <div class="dialog-body">
          <!-- Вкладки -->
          <div class="tabs">
            <button 
              @click="addMode = 'doi'" 
              :class="{ active: addMode === 'doi' }"
              class="tab"
            >
              By DOI
            </button>
            <button 
              @click="addMode = 'manual'" 
              :class="{ active: addMode === 'manual' }"
              class="tab"
            >
              Manual Entry
            </button>
            <button 
              @click="addMode = 'bibtex'" 
              :class="{ active: addMode === 'bibtex' }"
              class="tab"
            >
              BibTeX
            </button>
          </div>

          <!-- By DOI -->
          <div v-if="addMode === 'doi'" class="add-form">
            <div class="form-group">
              <label>DOI</label>
              <input 
                v-model="doiInput" 
                type="text" 
                placeholder="10.1234/example"
                @keyup.enter="fetchDOI"
              />
            </div>
            <button @click="fetchDOI" :disabled="loading" class="btn btn-primary">
              {{ loading ? 'Loading...' : 'Fetch' }}
            </button>
            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          </div>

          <!-- Manual Entry -->
          <div v-if="addMode === 'manual'" class="add-form">
            <div class="form-group">
              <label>Type</label>
              <select v-model="manualRef.type">
                <option value="article-journal">Journal Article</option>
                <option value="book">Book</option>
                <option value="chapter">Book Chapter</option>
                <option value="paper-conference">Conference Paper</option>
                <option value="thesis">Thesis</option>
                <option value="webpage">Webpage</option>
              </select>
            </div>

            <div class="form-group">
              <label>Title *</label>
              <input v-model="manualRef.title" type="text" required />
            </div>

            <div class="form-group">
              <label>Authors (one per line) *</label>
              <textarea 
                v-model="manualRef.authorsText" 
                rows="3"
                placeholder="Ivanov, I.I.&#10;Petrov, P.P."
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Year *</label>
                <input v-model="manualRef.year" type="number" required />
              </div>
              <div class="form-group">
                <label>DOI</label>
                <input v-model="manualRef.doi" type="text" />
              </div>
            </div>

            <div class="form-group" v-if="manualRef.type === 'article-journal'">
              <label>Journal</label>
              <input v-model="manualRef.journal" type="text" />
            </div>

            <div class="form-row" v-if="manualRef.type === 'article-journal'">
              <div class="form-group">
                <label>Volume</label>
                <input v-model="manualRef.volume" type="text" />
              </div>
              <div class="form-group">
                <label>Issue</label>
                <input v-model="manualRef.issue" type="text" />
              </div>
              <div class="form-group">
                <label>Pages</label>
                <input v-model="manualRef.pages" type="text" placeholder="1-10" />
              </div>
            </div>

            <div class="form-group">
              <label>URL</label>
              <input v-model="manualRef.url" type="url" />
            </div>

            <button @click="addManualReference" class="btn btn-primary">
              {{ editingIndex !== null ? 'Update' : 'Add' }}
            </button>
          </div>

          <!-- BibTeX -->
          <div v-if="addMode === 'bibtex'" class="add-form">
            <div class="form-group">
              <label>BibTeX Entry</label>
              <textarea 
                v-model="bibtexInput" 
                rows="10"
                placeholder="@article{key,&#10;  author = {...},&#10;  title = {...},&#10;  ...&#10;}"
              ></textarea>
            </div>
            <button @click="parseBibTeX" class="btn btn-primary">
              Parse & Add
            </button>
            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { citationService, CITATION_STYLES } from '@/services/citationService'

const { t, locale } = useI18n()

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  citationStyle: {
    type: String,
    default: CITATION_STYLES.GOST_NUMERIC
  }
})

const emit = defineEmits(['update:modelValue'])

const references = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const showAddDialog = ref(false)
const addMode = ref('doi')
const doiInput = ref('')
const bibtexInput = ref('')
const loading = ref(false)
const errorMessage = ref('')
const editingIndex = ref(null)

const manualRef = ref({
  type: 'article-journal',
  title: '',
  authorsText: '',
  year: new Date().getFullYear(),
  journal: '',
  volume: '',
  issue: '',
  pages: '',
  doi: '',
  url: ''
})

// Форматирование ссылки
const formatReference = (ref) => {
  const lang = locale.value
  return citationService.formatReference(ref, props.citationStyle, lang)
}

// Fetch DOI
const fetchDOI = async () => {
  if (!doiInput.value) return
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const refData = await citationService.fetchByDOI(doiInput.value)
    
    // Проверка дубликатов
    if (citationService.isDuplicate(references.value, refData)) {
      errorMessage.value = 'This reference is already in the list'
      loading.value = false
      return
    }
    
    if (editingIndex.value !== null) {
      references.value[editingIndex.value] = refData
    } else {
      references.value = [...references.value, refData]
    }
    
    closeDialog()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

// Add manual reference
const addManualReference = () => {
  if (!manualRef.value.title || !manualRef.value.authorsText || !manualRef.value.year) {
    errorMessage.value = 'Please fill in required fields'
    return
  }

  const authors = manualRef.value.authorsText
    .split('\n')
    .filter(line => line.trim())
    .map(name => name.trim())

  const refData = citationService.createManualReference({
    ...manualRef.value,
    authors
  })

  if (editingIndex.value !== null) {
    references.value[editingIndex.value] = refData
  } else {
    references.value = [...references.value, refData]
  }

  closeDialog()
}

// Parse BibTeX
const parseBibTeX = () => {
  if (!bibtexInput.value) return

  try {
    const parsed = citationService.importBibTeX(bibtexInput.value)
    
    parsed.forEach(ref => {
      if (!citationService.isDuplicate(references.value, ref)) {
        references.value = [...references.value, ref]
      }
    })
    
    closeDialog()
  } catch (error) {
    errorMessage.value = 'Failed to parse BibTeX: ' + error.message
  }
}

// Import BibTeX file
const importBibTeX = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.bib'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (event) => {
        bibtexInput.value = event.target.result
        addMode.value = 'bibtex'
        showAddDialog.value = true
      }
      reader.readAsText(file)
    }
  }
  input.click()
}

// Export BibTeX
const exportBibTeX = () => {
  try {
    const bibtex = citationService.exportToBibTeX(references.value)
    
    const blob = new Blob([bibtex], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'references.bib'
    a.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    alert('Failed to export BibTeX')
  }
}

// Edit reference
const editReference = (index) => {
  editingIndex.value = index
  const ref = references.value[index]
  
  // Populate manual form
  manualRef.value = {
    type: ref.type || 'article-journal',
    title: ref.title || '',
    authorsText: ref.author?.map(a => `${a.family}, ${a.given}`).join('\n') || '',
    year: ref.issued?.['date-parts']?.[0]?.[0] || new Date().getFullYear(),
    journal: ref['container-title'] || '',
    volume: ref.volume || '',
    issue: ref.issue || '',
    pages: ref.page || '',
    doi: ref.DOI || '',
    url: ref.URL || ''
  }
  
  addMode.value = 'manual'
  showAddDialog.value = true
}

// Delete reference
const deleteReference = (index) => {
  if (confirm('Delete this reference?')) {
    references.value = references.value.filter((_, i) => i !== index)
  }
}

// Move up
const moveUp = (index) => {
  if (index === 0) return
  const newRefs = [...references.value]
  ;[newRefs[index - 1], newRefs[index]] = [newRefs[index], newRefs[index - 1]]
  references.value = newRefs
}

// Move down
const moveDown = (index) => {
  if (index === references.value.length - 1) return
  const newRefs = [...references.value]
  ;[newRefs[index], newRefs[index + 1]] = [newRefs[index + 1], newRefs[index]]
  references.value = newRefs
}

// Close dialog
const closeDialog = () => {
  showAddDialog.value = false
  addMode.value = 'doi'
  doiInput.value = ''
  bibtexInput.value = ''
  errorMessage.value = ''
  editingIndex.value = null
  manualRef.value = {
    type: 'article-journal',
    title: '',
    authorsText: '',
    year: new Date().getFullYear(),
    journal: '',
    volume: '',
    issue: '',
    pages: '',
    doi: '',
    url: ''
  }
}
</script>

<style scoped>
.references-manager {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.references-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.references-actions {
  display: flex;
  gap: 0.5rem;
}

.references-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.reference-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.reference-number {
  font-weight: bold;
  color: #667eea;
  min-width: 40px;
}

.reference-content {
  flex: 1;
}

.reference-formatted {
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.reference-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.reference-doi {
  font-family: monospace;
}

.reference-url a {
  color: #667eea;
  text-decoration: none;
}

.reference-actions {
  display: flex;
  gap: 0.25rem;
}

.btn-icon {
  padding: 0.25rem 0.5rem;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

/* Dialog styles */
.dialog-overlay {
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

.dialog {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
}

.dialog-body {
  padding: 1.5rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.tab:hover {
  background: #f9fafb;
}

.tab.active {
  border-bottom-color: #667eea;
  color: #667eea;
}

.add-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: inherit;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.error-message {
  padding: 0.75rem;
  background: #fee2e2;
  color: #dc2626;
  border-radius: 4px;
  font-size: 0.875rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5a67d8;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background: #d1d5db;
}
</style>


