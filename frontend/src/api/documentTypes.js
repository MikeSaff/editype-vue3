import axios from './axios'

export const documentTypesApi = {
  // Получение списка доступных типов документов
  getDocumentTypes: () => axios.get('/api/znan/document-types'),
  
  // Получение типа документа по ID
  getDocumentType: (id) => axios.get(`/api/znan/document-types/${id}`)
}



