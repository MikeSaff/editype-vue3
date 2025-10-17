import axios from './axios'

export const articlesApi = {
  // Получение списка статей пользователя
  getUserArticles: () => axios.get('/api/znan/articles'),
  
  // Получение статьи по ID
  getArticle: (id) => axios.get(`/api/znan/articles/${id}`),
  
  // Alias для getArticle
  getById: (id) => axios.get(`/api/znan/articles/${id}`),
  
  // Получение статьи с указанным языком
  getArticleByLanguage: (id, language) => axios.get(`/api/znan/articles/${id}?lang=${language}`),
  
  // Создание новой статьи
  createArticle: (articleData) => axios.post('/api/znan/articles', articleData),
  
  // Alias для createArticle
  create: (articleData) => axios.post('/api/znan/articles', articleData),
  
  // Обновление статьи
  updateArticle: (id, articleData) => axios.put(`/api/znan/articles/${id}`, articleData),
  
  // Alias для updateArticle
  update: (id, articleData) => axios.put(`/api/znan/articles/${id}`, articleData),
  
  // Удаление статьи
  deleteArticle: (id) => axios.delete(`/api/znan/articles/${id}`),
  
  // Получить замок на редактирование
  acquireLock: (id, email) => axios.post(`/api/znan/articles/${id}/lock`, { email }),
  
  // Освободить замок
  releaseLock: (id, email) => axios.post(`/api/znan/articles/${id}/unlock`, { email }),
  
  // Сохранение статьи (синоним update)
  saveArticle: (id, articleData) => axios.put(`/api/znan/articles/${id}`, articleData),
  
  // Экспорт статьи
  exportArticle: (id, format) => axios.post(`/api/znan/articles/${id}/export`, { type: format }),
  
  // Получение статуса экспорта
  getExportStatus: (jobId) => axios.get(`/api/znan/exports/${jobId}`),
  
  // Загрузка статьи из Editorum
  loadFromEditorum: (editorumId) => axios.post(`/api/znan/articles/load/${editorumId}`)
}

export default articlesApi
