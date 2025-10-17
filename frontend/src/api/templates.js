import axios from './axios'

export const templatesApi = {
  // Получение всех активных шаблонов
  getAllTemplates: () => axios.get('/api/templates'),
  
  // Получение шаблонов по типу документа
  getTemplatesByDocumentType: (documentTypeId) => 
    axios.get(`/api/templates/by-document-type/${documentTypeId}`),
  
  // Получение шаблона по умолчанию для типа документа
  getDefaultTemplate: (documentTypeId) => 
    axios.get(`/api/templates/default/${documentTypeId}`),
  
  // Получение шаблона по ID
  getTemplate: (id) => axios.get(`/api/templates/${id}`),
  
  // Создание нового шаблона (только админы)
  createTemplate: (templateData) => {
    const formData = new FormData()
    Object.keys(templateData).forEach(key => {
      if (templateData[key] !== null && templateData[key] !== undefined) {
        if (key === 'file' && templateData[key] instanceof File) {
          formData.append('file', templateData[key])
        } else if (typeof templateData[key] === 'object') {
          formData.append(key, JSON.stringify(templateData[key]))
        } else {
          formData.append(key, templateData[key])
        }
      }
    })
    return axios.post('/api/templates', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // Обновление шаблона (только админы)
  updateTemplate: (id, templateData) => {
    const formData = new FormData()
    Object.keys(templateData).forEach(key => {
      if (templateData[key] !== null && templateData[key] !== undefined) {
        if (key === 'file' && templateData[key] instanceof File) {
          formData.append('file', templateData[key])
        } else if (typeof templateData[key] === 'object') {
          formData.append(key, JSON.stringify(templateData[key]))
        } else {
          formData.append(key, templateData[key])
        }
      }
    })
    return axios.put(`/api/templates/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // Удаление шаблона (только админы)
  deleteTemplate: (id) => axios.delete(`/api/templates/${id}`),
  
  // Поиск шаблонов по имени
  searchTemplates: (name) => axios.get(`/api/templates/search?name=${encodeURIComponent(name)}`),
  
  // Импорт документа (HTML, LaTeX, DOCX, ZIP) как шаблона
  importDocument: (file, name, documentTypeId) => {
    const formData = new FormData()
    formData.append('file', file)
    if (name) formData.append('name', name)
    if (documentTypeId) formData.append('documentTypeId', documentTypeId)
    
    return axios.post('/api/templates/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}