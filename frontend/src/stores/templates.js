import { defineStore } from 'pinia'
import { ref } from 'vue'
import { templatesApi } from '@/api/templates'

export const useTemplatesStore = defineStore('templates', () => {
  const templates = ref([])
  const currentTemplate = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Загрузка всех активных шаблонов
  const loadAllTemplates = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.getAllTemplates()
      templates.value = response.data || []
      return { success: true, data: templates.value }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки шаблонов'
      console.error('Ошибка загрузки шаблонов:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Загрузка шаблонов по типу документа
  const loadTemplatesByDocumentType = async (documentTypeId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.getTemplatesByDocumentType(documentTypeId)
      const documentTypeTemplates = response.data || []
      return { success: true, data: documentTypeTemplates }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки шаблонов'
      console.error('Ошибка загрузки шаблонов:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Загрузка шаблона по умолчанию
  const loadDefaultTemplate = async (documentTypeId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.getDefaultTemplate(documentTypeId)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки шаблона'
      console.error('Ошибка загрузки шаблона:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Загрузка конкретного шаблона
  const loadTemplate = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.getTemplate(id)
      currentTemplate.value = response.data
      return { success: true, data: currentTemplate.value }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки шаблона'
      console.error('Ошибка загрузки шаблона:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Создание нового шаблона (только админы)
  const createTemplate = async (templateData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.createTemplate(templateData)
      const newTemplate = response.data
      templates.value.unshift(newTemplate)
      return { success: true, data: newTemplate }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка создания шаблона'
      console.error('Ошибка создания шаблона:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Обновление шаблона (только админы)
  const updateTemplate = async (id, templateData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.updateTemplate(id, templateData)
      const updatedTemplate = response.data
      
      // Обновляем шаблон в списке
      const index = templates.value.findIndex(t => t.id === id)
      if (index !== -1) {
        templates.value[index] = updatedTemplate
      }
      
      return { success: true, data: updatedTemplate }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка обновления шаблона'
      console.error('Ошибка обновления шаблона:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Удаление шаблона (только админы)
  const deleteTemplate = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      await templatesApi.deleteTemplate(id)
      
      // Удаляем шаблон из списка
      templates.value = templates.value.filter(t => t.id !== id)
      
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка удаления шаблона'
      console.error('Ошибка удаления шаблона:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Поиск шаблонов по имени
  const searchTemplates = async (name) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.searchTemplates(name)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка поиска шаблонов'
      console.error('Ошибка поиска шаблонов:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Получение шаблона по ID из локального списка
  const getTemplateById = (id) => {
    return templates.value.find(t => t.id === id)
  }

  // Очистка данных
  const clearTemplates = () => {
    templates.value = []
    currentTemplate.value = null
    error.value = null
  }

  // Импорт документа как шаблона
  const importDocument = async (file, name, documentTypeId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await templatesApi.importDocument(file, name, documentTypeId)
      
      if (response.data.success) {
        return { 
          success: true, 
          paragraphs: response.data.paragraphs,
          paragraphCount: response.data.paragraphCount 
        }
      } else {
        error.value = response.data.error || 'Ошибка импорта документа'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err.response?.data?.message || err.response?.data?.error || 'Ошибка импорта документа'
      console.error('Ошибка импорта документа:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    templates,
    currentTemplate,
    loading,
    error,
    
    // Actions
    loadAllTemplates,
    loadTemplatesByDocumentType,
    loadDefaultTemplate,
    loadTemplate,
    createTemplate,
    updateTemplate,
    deleteTemplate,
    searchTemplates,
    getTemplateById,
    clearTemplates,
    importDocument
  }
})