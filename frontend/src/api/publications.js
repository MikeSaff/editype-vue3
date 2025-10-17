import apiClient from './axios'

export const publicationApi = {
  // Get all publications
  getAllPublications() {
    return apiClient.get('/publications')
  },

  // Get publication by ID
  getPublicationById(id) {
    return apiClient.get(`/publications/${id}`)
  },

  // Create new publication
  createPublication(publicationData) {
    return apiClient.post('/publications', publicationData)
  },

  // Update publication
  updatePublication(id, publicationData) {
    return apiClient.put(`/publications/${id}`, publicationData)
  },

  // Delete publication
  deletePublication(id) {
    return apiClient.delete(`/publications/${id}`)
  }
}




