import axios from './axios'

export const articlesApi = {
  // Получение списка статей пользователя
  getUserArticles: () => axios.get('/znan/articles'),
  
  // Получение статьи по ID
  getArticle: (id) => axios.get(`/znan/articles/${id}`),
  
  // Alias для getArticle
  getById: (id) => axios.get(`/znan/articles/${id}`),
  
  // Получение статьи с указанным языком
  getArticleByLanguage: (id, language) => axios.get(`/znan/articles/${id}?lang=${language}`),
  
  // Создание новой статьи
  createArticle: (articleData) => axios.post('/znan/articles', articleData),
  
  // Alias для createArticle
  create: (articleData) => axios.post('/znan/articles', articleData),
  
  // Обновление статьи
  updateArticle: (id, articleData) => axios.put(`/znan/articles/${id}`, articleData),
  
  // Alias для updateArticle
  update: (id, articleData) => axios.put(`/znan/articles/${id}`, articleData),
  
  // Удаление статьи
  deleteArticle: (id) => axios.delete(`/znan/articles/${id}`),
  
  // Получить замок на редактирование
  acquireLock: (id, email) => axios.post(`/znan/articles/${id}/lock`, { email }),
  
  // Освободить замок
  releaseLock: (id, email) => axios.post(`/znan/articles/${id}/unlock`, { email }),
  
  // Сохранение статьи (синоним update)
  saveArticle: (id, articleData) => axios.put(`/znan/articles/${id}`, articleData),
  
  // Экспорт статьи
  exportArticle: (id, format) => axios.post(`/znan/articles/${id}/export`, { type: format }),
  
  // Получение статуса экспорта
  getExportStatus: (jobId) => axios.get(`/znan/exports/${jobId}`),
  
  // Загрузка статьи из Editorum
  loadFromEditorum: (editorumId) => axios.post(`/znan/articles/load/${editorumId}`)
}

export default articlesApi
