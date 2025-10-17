import axios from './axios'

export const authApi = {
  // Локальная авторизация (deprecated - используется только для разработки)
  login: (credentials) => axios.post('/auth/login', credentials),
  register: (userData) => axios.post('/auth/register', userData),
  
  // OAuth2 интеграция с Editorum (единственная точка входа)
  getEditorumAuthUrl: () => axios.get('/oauth/authorize-url'),
  oauth2Callback: (data) => axios.post('/oauth/callback', data),
  refreshEditorumToken: (data) => axios.post('/oauth/refresh', data),
  
  // Проверка статуса авторизации
  getAuthStatus: () => axios.get('/oauth/status'),
  
  // Выход из системы
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('editorumAccessToken')
    localStorage.removeItem('editorumRefreshToken')
    localStorage.removeItem('user')
  }
}