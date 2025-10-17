import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const editorumAccessToken = ref(localStorage.getItem('editorumAccessToken') || null)
  const editorumRefreshToken = ref(localStorage.getItem('editorumRefreshToken') || null)

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials)
      token.value = response.data.token
      user.value = {
        email: response.data.email,
        roles: response.data.roles,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        isEditorumUser: response.data.isEditorumUser
      }
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(user.value))
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login failed' 
      }
    }
  }

  const register = async (userData) => {
    try {
      const response = await authApi.register(userData)
      token.value = response.data.token
      user.value = {
        email: response.data.email,
        roles: response.data.roles,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        isEditorumUser: response.data.isEditorumUser
      }
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(user.value))
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed' 
      }
    }
  }

  // OAuth2 авторизация через Editorum (единственная точка входа)
  const authenticateWithEditorum = async (code) => {
    try {
      const response = await authApi.oauth2Callback({ code })
      token.value = response.data.token
      editorumAccessToken.value = response.data.editorumAccessToken
      editorumRefreshToken.value = response.data.editorumRefreshToken
      
      user.value = {
        email: response.data.email,
        roles: response.data.roles,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        isEditorumUser: response.data.isEditorumUser
      }
      
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('editorumAccessToken', response.data.editorumAccessToken)
      localStorage.setItem('editorumRefreshToken', response.data.editorumRefreshToken)
      localStorage.setItem('user', JSON.stringify(user.value))
      
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'OAuth2 authentication failed' 
      }
    }
  }

  // Получение URL для авторизации в Editorum
  const getEditorumAuthUrl = async () => {
    try {
      const response = await authApi.getEditorumAuthUrl()
      return response.data.url
    } catch (error) {
      throw new Error('Failed to get authorization URL')
    }
  }

  // Обновление токена Editorum
  const refreshEditorumToken = async () => {
    try {
      if (!editorumRefreshToken.value) {
        throw new Error('No refresh token available')
      }
      
      const response = await authApi.refreshEditorumToken({
        refresh_token: editorumRefreshToken.value
      })
      
      editorumAccessToken.value = response.data.access_token
      if (response.data.refresh_token) {
        editorumRefreshToken.value = response.data.refresh_token
      }
      
      localStorage.setItem('editorumAccessToken', editorumAccessToken.value)
      localStorage.setItem('editorumRefreshToken', editorumRefreshToken.value)
      
      return { success: true }
    } catch (error) {
      // Если обновление токена не удалось, перенаправляем на авторизацию
      logout()
      return { 
        success: false, 
        error: 'Token refresh failed, please login again' 
      }
    }
  }

  const logout = () => {
    token.value = null
    editorumAccessToken.value = null
    editorumRefreshToken.value = null
    user.value = null
    
    localStorage.removeItem('token')
    localStorage.removeItem('editorumAccessToken')
    localStorage.removeItem('editorumRefreshToken')
    localStorage.removeItem('user')
  }

  const isAuthenticated = () => {
    return !!token.value
  }

  const isEditorumUser = () => {
    return user.value?.isEditorumUser === true
  }

  // Initialize user from localStorage
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }

  return {
    user,
    token,
    editorumAccessToken,
    editorumRefreshToken,
    login,
    register,
    authenticateWithEditorum,
    getEditorumAuthUrl,
    refreshEditorumToken,
    logout,
    isAuthenticated,
    isEditorumUser
  }
})

