import { defineStore } from 'pinia'
import { ref } from 'vue'
import { articlesApi } from '@/api/articles'

export const useArticlesStore = defineStore('articles', () => {
  const articles = ref([])
  const currentArticle = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Загрузка списка статей пользователя
  const loadUserArticles = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await articlesApi.getUserArticles()
      articles.value = response.data || []
      return { success: true, data: articles.value }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки статей'
      console.error('Ошибка загрузки статей:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Загрузка конкретной статьи
  const loadArticle = async (id, language = 'ru') => {
    loading.value = true
    error.value = null
    
    try {
      const response = await articlesApi.getArticleByLanguage(id, language)
      currentArticle.value = response.data
      return { success: true, data: currentArticle.value }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки статьи'
      console.error('Ошибка загрузки статьи:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Создание новой статьи
  const createArticle = async (articleData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await articlesApi.createArticle(articleData)
      const newArticle = response.data
      articles.value.unshift(newArticle)
      return { success: true, data: newArticle }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка создания статьи'
      console.error('Ошибка создания статьи:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Обновление статьи
  const updateArticle = async (id, articleData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await articlesApi.updateArticle(id, articleData)
      const updatedArticle = response.data
      
      // Обновляем статью в списке
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value[index] = updatedArticle
      }
      
      // Обновляем текущую статью, если это она
      if (currentArticle.value?.id === id) {
        currentArticle.value = updatedArticle
      }
      
      return { success: true, data: updatedArticle }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка обновления статьи'
      console.error('Ошибка обновления статьи:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Удаление статьи
  const deleteArticle = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      await articlesApi.deleteArticle(id)
      
      // Удаляем статью из списка
      articles.value = articles.value.filter(article => article.id !== id)
      
      // Очищаем текущую статью, если это она
      if (currentArticle.value?.id === id) {
        currentArticle.value = null
      }
      
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка удаления статьи'
      console.error('Ошибка удаления статьи:', err)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // Экспорт статьи
  const exportArticle = async (id, format) => {
    try {
      const response = await articlesApi.exportArticle(id, format)
      return { success: true, jobId: response.data.jobId }
    } catch (err) {
      const error = err.response?.data?.message || 'Ошибка экспорта статьи'
      console.error('Ошибка экспорта статьи:', err)
      return { success: false, error }
    }
  }

  // Получение статуса экспорта
  const getExportStatus = async (jobId) => {
    try {
      const response = await articlesApi.getExportStatus(jobId)
      return { success: true, data: response.data }
    } catch (err) {
      const error = err.response?.data?.message || 'Ошибка получения статуса экспорта'
      console.error('Ошибка получения статуса экспорта:', err)
      return { success: false, error }
    }
  }

  // Поиск статьи по ID в локальном списке
  const getArticleById = (id) => {
    return articles.value.find(article => article.id === id)
  }

  // Очистка данных
  const clearArticles = () => {
    articles.value = []
    currentArticle.value = null
    error.value = null
  }

  // Очистка текущей статьи
  const clearCurrentArticle = () => {
    currentArticle.value = null
  }

  return {
    // State
    articles,
    currentArticle,
    loading,
    error,
    
    // Actions
    loadUserArticles,
    loadArticle,
    createArticle,
    updateArticle,
    deleteArticle,
    exportArticle,
    getExportStatus,
    getArticleById,
    clearArticles,
    clearCurrentArticle
  }
})



