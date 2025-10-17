import { defineStore } from 'pinia'
import { ref } from 'vue'
import { editorumApi } from '@/api/editorum'

export const useEditorumStore = defineStore('editorum', () => {
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('editorum_token') || null)
  const refreshToken = ref(localStorage.getItem('editorum_refresh_token') || null)
  const tokenExpiresAt = ref(null)

  // Start OAuth2 flow
  const startOAuth = async () => {
    try {
      const response = await editorumApi.getAuthorizationUrl()
      // Redirect to Editorum authorization
      window.location.href = response.data.url
    } catch (error) {
      console.error('Failed to start OAuth', error)
      throw error
    }
  }

  // Handle OAuth callback (exchange code for token)
  const handleCallback = async (code) => {
    try {
      const response = await editorumApi.exchangeCodeForToken(code)
      setTokens(response.data)
      await loadUser()
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'OAuth callback failed' 
      }
    }
  }

  // Refresh access token
  const refresh = async () => {
    if (!refreshToken.value) {
      return { success: false, error: 'No refresh token' }
    }

    try {
      const response = await editorumApi.refreshToken(refreshToken.value)
      setTokens(response.data)
      return { success: true }
    } catch (error) {
      logout()
      return { success: false, error: 'Token refresh failed' }
    }
  }

  // Load user profile from Editorum
  const loadUser = async () => {
    try {
      const response = await editorumApi.getCurrentUser()
      user.value = response.data
      localStorage.setItem('editorum_user', JSON.stringify(user.value))
    } catch (error) {
      console.error('Failed to load user', error)
      throw error
    }
  }

  // Set tokens and expiration
  const setTokens = (tokenResponse) => {
    accessToken.value = tokenResponse.access_token
    refreshToken.value = tokenResponse.refresh_token
    
    // Calculate expiration time
    const expiresIn = tokenResponse.expires_in || 3600
    tokenExpiresAt.value = Date.now() + (expiresIn * 1000)
    
    // Save to localStorage
    localStorage.setItem('editorum_token', accessToken.value)
    localStorage.setItem('editorum_refresh_token', refreshToken.value)
    localStorage.setItem('editorum_token_expires_at', tokenExpiresAt.value)
  }

  // Check if token is expired or will expire soon
  const isTokenExpired = () => {
    if (!tokenExpiresAt.value) return true
    // Consider expired if less than 5 minutes remaining
    return Date.now() > (tokenExpiresAt.value - 300000)
  }

  // Logout
  const logout = () => {
    accessToken.value = null
    refreshToken.value = null
    tokenExpiresAt.value = null
    user.value = null
    
    localStorage.removeItem('editorum_token')
    localStorage.removeItem('editorum_refresh_token')
    localStorage.removeItem('editorum_token_expires_at')
    localStorage.removeItem('editorum_user')
  }

  // Check if authenticated
  const isAuthenticated = () => {
    return !!accessToken.value && !isTokenExpired()
  }

  // Initialize from localStorage
  const storedUser = localStorage.getItem('editorum_user')
  const storedExpiry = localStorage.getItem('editorum_token_expires_at')
  
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
  if (storedExpiry) {
    tokenExpiresAt.value = parseInt(storedExpiry)
  }

  return {
    user,
    accessToken,
    startOAuth,
    handleCallback,
    refresh,
    loadUser,
    logout,
    isAuthenticated,
    isTokenExpired
  }
})




