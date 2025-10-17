<template>
  <div class="publications-page">
    <div class="page-header">
      <h1>{{ t('publications.title') }}</h1>
      <button @click="showCreateForm = true" class="btn btn-primary">
        {{ t('publications.createNew') }}
      </button>
    </div>

    <div v-if="error" class="error-message">{{ error }}</div>

    <table v-if="publications.length > 0" class="publications-table">
      <thead>
        <tr>
          <th>{{ t('publications.titleField') }}</th>
          <th>{{ t('publications.doi') }}</th>
          <th>{{ t('publications.createdAt') }}</th>
          <th>{{ t('publications.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="publication in publications" :key="publication.id">
          <td>{{ getTitle(publication.titles) }}</td>
          <td>{{ publication.doi || 'N/A' }}</td>
          <td>{{ formatDate(publication.createdAt) }}</td>
          <td>
            <button @click="editPublication(publication)" class="btn btn-small">
              {{ t('publications.edit') }}
            </button>
            <button @click="showExportMenu(publication)" class="btn btn-small btn-success">
              {{ t('publications.export') }}
            </button>
            <button @click="deletePublicationConfirm(publication.id)" class="btn btn-small btn-danger">
              {{ t('publications.delete') }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else-if="!loading" class="empty-state">
      No publications found
    </div>

    <!-- Export Menu Modal -->
    <div v-if="exportingPublication" class="modal">
      <div class="modal-content modal-small">
        <h2>{{ t('publications.export') }}</h2>
        <p>Export publication: {{ getTitle(exportingPublication.titles) }}</p>
        
        <div class="form-group">
          <label>Language</label>
          <select v-model="exportLanguage">
            <option value="ru">Russian</option>
            <option value="en">English</option>
            <option value="cn">Chinese</option>
          </select>
        </div>

        <div class="export-buttons">
          <button @click="handleExport('html')" class="btn btn-primary">HTML</button>
          <button @click="handleExport('jats')" class="btn btn-primary">XML JATS</button>
          <button @click="handleExport('txt')" class="btn btn-primary">TXT</button>
        </div>

        <button @click="exportingPublication = null" class="btn btn-block" style="margin-top: 1rem;">
          {{ t('users.cancel') }}
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateForm || editingPublication" class="modal">
      <div class="modal-content modal-large">
        <h2>{{ editingPublication ? t('publications.edit') : t('publications.createNew') }}</h2>
        <form @submit.prevent="savePublication">
          <!-- Language Tabs -->
          <div class="language-tabs">
            <button 
              v-for="lang in languages" 
              :key="lang" 
              type="button"
              @click="currentLang = lang"
              :class="{ active: currentLang === lang }"
              class="tab-button"
            >
              {{ lang.toUpperCase() }}
            </button>
          </div>

          <!-- Content for current language -->
          <div class="tab-content">
            <div class="form-group">
              <label>{{ t('publications.titleField') }} ({{ currentLang.toUpperCase() }})</label>
              <input v-model="formData.titles[currentLang]" type="text" />
            </div>

            <div class="form-group">
              <label>{{ t('publications.metadata') }} ({{ currentLang.toUpperCase() }})</label>
              <textarea 
                v-model="metadataTexts[currentLang]" 
                rows="3"
                placeholder='{"author": "Name", "keywords": "keyword1, keyword2"}'
              ></textarea>
            </div>

            <div class="form-group">
              <label>{{ t('publications.text') }} ({{ currentLang.toUpperCase() }})</label>
              <textarea v-model="formData.texts[currentLang]" rows="10"></textarea>
            </div>

            <div class="form-group">
              <label>{{ t('publications.preview') }}</label>
              <div class="preview-box" v-html="formData.texts[currentLang] || 'No content'"></div>
            </div>
          </div>

          <!-- DOI field (language-independent) -->
          <div class="form-group">
            <label>{{ t('publications.doi') }}</label>
            <input v-model="formData.doi" type="text" placeholder="10.1000/xyz123" />
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn btn-primary">{{ t('users.save') }}</button>
            <button type="button" @click="cancelEdit" class="btn">{{ t('users.cancel') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { publicationApi } from '@/api/publications'
import { exportApi } from '@/api/export'

const { t, locale } = useI18n()

const publications = ref([])
const loading = ref(false)
const error = ref(null)
const showCreateForm = ref(false)
const editingPublication = ref(null)
const exportingPublication = ref(null)
const exportLanguage = ref('en')
const currentLang = ref('en')
const languages = ['ru', 'en', 'cn']

const formData = reactive({
  titles: {},
  metadata: {},
  texts: {},
  doi: ''
})

const metadataTexts = reactive({})

const fetchPublications = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await publicationApi.getAllPublications()
    publications.value = response.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to fetch publications'
  } finally {
    loading.value = false
  }
}

const editPublication = (publication) => {
  editingPublication.value = publication
  formData.titles = { ...publication.titles }
  formData.metadata = { ...publication.metadata }
  formData.texts = { ...publication.texts }
  formData.doi = publication.doi || ''
  
  // Convert metadata objects to JSON strings for editing
  languages.forEach(lang => {
    if (formData.metadata[lang]) {
      metadataTexts[lang] = JSON.stringify(formData.metadata[lang], null, 2)
    }
  })
}

const savePublication = async () => {
  try {
    // Parse metadata JSON strings
    languages.forEach(lang => {
      if (metadataTexts[lang]) {
        try {
          formData.metadata[lang] = JSON.parse(metadataTexts[lang])
        } catch (e) {
          formData.metadata[lang] = {}
        }
      }
    })

    if (editingPublication.value) {
      await publicationApi.updatePublication(editingPublication.value.id, formData)
    } else {
      await publicationApi.createPublication(formData)
    }
    await fetchPublications()
    cancelEdit()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to save publication'
  }
}

const deletePublicationConfirm = async (id) => {
  if (confirm('Are you sure you want to delete this publication?')) {
    try {
      await publicationApi.deletePublication(id)
      await fetchPublications()
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to delete publication'
    }
  }
}

const cancelEdit = () => {
  showCreateForm.value = false
  editingPublication.value = null
  formData.titles = {}
  formData.metadata = {}
  formData.texts = {}
  formData.doi = ''
  Object.keys(metadataTexts).forEach(key => delete metadataTexts[key])
}

const getTitle = (titles) => {
  if (!titles) return 'Untitled'
  return titles[locale.value] || titles['en'] || titles[Object.keys(titles)[0]] || 'Untitled'
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

const showExportMenu = (publication) => {
  exportingPublication.value = publication
  exportLanguage.value = 'en'
}

const handleExport = async (format) => {
  try {
    let response
    const id = exportingPublication.value.id
    const lang = exportLanguage.value

    if (format === 'html') {
      response = await exportApi.exportToHtml(id, lang)
      downloadFile(response.data, `publication_${id}.html`, 'text/html')
    } else if (format === 'jats') {
      response = await exportApi.exportToJats(id, lang)
      downloadFile(response.data, `publication_${id}.xml`, 'application/xml')
    } else if (format === 'txt') {
      response = await exportApi.exportToText(id, lang)
      downloadFile(response.data, `publication_${id}.txt`, 'text/plain')
    }

    exportingPublication.value = null
  } catch (err) {
    error.value = err.response?.data?.message || 'Export failed'
  }
}

const downloadFile = (data, filename, mimeType) => {
  const blob = new Blob([data], { type: mimeType })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchPublications()
})
</script>

<style scoped>
.publications-page {
  padding: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.publications-table {
  width: 100%;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.publications-table th,
.publications-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.publications-table th {
  background-color: #f8f9fa;
  font-weight: 600;
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
  margin-right: 0.5rem;
  background-color: #95a5a6;
  color: white;
}

.btn-small:hover {
  background-color: #7f8c8d;
}

.btn-danger {
  background-color: #e74c3c;
}

.btn-danger:hover {
  background-color: #c0392b;
}

.btn-success {
  background-color: #27ae60;
}

.btn-success:hover {
  background-color: #229954;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  overflow-y: auto;
}

.modal-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  margin: 2rem;
}

.modal-large {
  max-width: 900px;
}

.modal-small {
  max-width: 400px;
}

.export-buttons {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.export-buttons .btn {
  flex: 1;
}

.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
}

.language-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #eee;
}

.tab-button {
  padding: 0.5rem 1.5rem;
  background-color: transparent;
  border: none;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab-button:hover {
  background-color: #f0f0f0;
}

.tab-button.active {
  border-bottom-color: #3498db;
  color: #3498db;
  font-weight: 600;
}

.tab-content {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
}

.preview-box {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 1rem;
  background-color: #f9f9f9;
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}
</style>
