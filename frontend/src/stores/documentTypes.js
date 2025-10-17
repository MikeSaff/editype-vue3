import { defineStore } from 'pinia'
import { ref } from 'vue'
import { documentTypesApi } from '@/api/documentTypes'

export const useDocumentTypesStore = defineStore('documentTypes', () => {
  const documentTypes = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Загрузка списка типов документов
  const loadDocumentTypes = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await documentTypesApi.getDocumentTypes()
      documentTypes.value = response.data || []
      return { success: true, data: documentTypes.value }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки типов документов'
      console.error('Ошибка загрузки типов документов:', err)
      
      // Возвращаем заглушку в случае ошибки
      documentTypes.value = getDefaultDocumentTypes()
      
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Загрузка конкретного типа документа
  const loadDocumentType = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await documentTypesApi.getDocumentType(id)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки типа документа'
      console.error('Ошибка загрузки типа документа:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Поиск типа документа по ID в локальном списке
  const getDocumentTypeById = (id) => {
    return documentTypes.value.find(type => type.id === id)
  }

  // Получение типов документов по категории
  const getDocumentTypesByCategory = (category) => {
    return documentTypes.value.filter(type => type.category === category)
  }

  // Очистка данных
  const clearDocumentTypes = () => {
    documentTypes.value = []
    error.value = null
  }

  // Заглушка с типами документов по умолчанию
  const getDefaultDocumentTypes = () => {
    return [
      {
        id: 'scientific-article',
        name: 'Научная статья',
        description: 'Стандартная научная статья для публикации в журнале',
        category: 'publication',
        active: true,
        availableTemplates: ['standard-article', 'review-article', 'short-communication'],
        defaultTemplateId: 'standard-article',
        defaultLanguage: 'ru',
        supportedLanguages: ['ru', 'en', 'zh']
      },
      {
        id: 'conference-paper',
        name: 'Статья конференции',
        description: 'Статья для публикации в материалах конференции',
        category: 'publication',
        active: true,
        availableTemplates: ['conference-full', 'conference-short'],
        defaultTemplateId: 'conference-full',
        defaultLanguage: 'ru',
        supportedLanguages: ['ru', 'en']
      },
      {
        id: 'book-chapter',
        name: 'Глава книги',
        description: 'Глава для коллективной монографии или сборника',
        category: 'book',
        active: true,
        availableTemplates: ['book-chapter', 'monograph-chapter'],
        defaultTemplateId: 'book-chapter',
        defaultLanguage: 'ru',
        supportedLanguages: ['ru', 'en']
      },
      {
        id: 'thesis',
        name: 'Диссертация',
        description: 'Диссертация на соискание ученой степени',
        category: 'academic',
        active: true,
        availableTemplates: ['phd-thesis', 'candidate-thesis'],
        defaultTemplateId: 'phd-thesis',
        defaultLanguage: 'ru',
        supportedLanguages: ['ru']
      },
      {
        id: 'report',
        name: 'Отчет',
        description: 'Научно-технический отчет',
        category: 'document',
        active: true,
        availableTemplates: ['research-report', 'technical-report'],
        defaultTemplateId: 'research-report',
        defaultLanguage: 'ru',
        supportedLanguages: ['ru', 'en']
      }
    ]
  }

  return {
    // State
    documentTypes,
    loading,
    error,
    
    // Actions
    loadDocumentTypes,
    loadDocumentType,
    getDocumentTypeById,
    getDocumentTypesByCategory,
    clearDocumentTypes
  }
})



