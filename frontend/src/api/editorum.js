import apiClient from './axios'

export const editorumApi = {
  // OAuth2 Authorization
  getAuthorizationUrl() {
    return apiClient.get('/oauth/authorize-url')
  },

  exchangeCodeForToken(code) {
    return apiClient.post('/oauth/token', { code })
  },

  refreshToken(refreshToken) {
    return apiClient.post('/oauth/refresh', { refresh_token: refreshToken })
  },

  // User profile from Editorum
  getCurrentUser() {
    return apiClient.get('/editorum/user')
  },

  // Journals
  getAllJournals() {
    return apiClient.get('/editorum/journals')
  },

  getJournal(id) {
    return apiClient.get(`/editorum/journals/${id}`)
  },

  // Files
  getFiles(type, id) {
    return apiClient.get(`/editorum/files/${type}/${id}`)
  }
}




