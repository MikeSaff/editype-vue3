import { defineStore } from 'pinia'
import { ref } from 'vue'
import { stylesApi } from '@/api/styles'

export const useStylesStore = defineStore('styles', () => {
  const styles = ref([])
  const loading = ref(false)
  const error = ref(null)

  /**
   * Load all paragraph styles
   */
  const loadStyles = async (params = {}) => {
    loading.value = true
    error.value = null

    try {
      const response = await stylesApi.getAllStyles(params)
      styles.value = response.data
      return { success: true }
    } catch (err) {
      console.error('Error loading styles:', err)
      error.value = err.response?.data?.message || 'Failed to load styles'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Get style by ID
   */
  const getStyleById = async (id) => {
    try {
      const response = await stylesApi.getStyleById(id)
      return { success: true, style: response.data }
    } catch (err) {
      console.error('Error loading style:', err)
      return { success: false, error: err.response?.data?.message || 'Failed to load style' }
    }
  }

  /**
   * Create new paragraph style
   */
  const createStyle = async (styleData) => {
    loading.value = true
    error.value = null

    try {
      const response = await stylesApi.createStyle(styleData)
      styles.value.push(response.data)
      return { success: true, style: response.data }
    } catch (err) {
      console.error('Error creating style:', err)
      error.value = err.response?.data?.message || 'Failed to create style'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Update paragraph style
   */
  const updateStyle = async (id, styleData) => {
    loading.value = true
    error.value = null

    try {
      const response = await stylesApi.updateStyle(id, styleData)
      const index = styles.value.findIndex(s => s.id === id)
      if (index !== -1) {
        styles.value[index] = response.data
      }
      return { success: true, style: response.data }
    } catch (err) {
      console.error('Error updating style:', err)
      error.value = err.response?.data?.message || 'Failed to update style'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete paragraph style
   */
  const deleteStyle = async (id) => {
    loading.value = true
    error.value = null

    try {
      await stylesApi.deleteStyle(id)
      styles.value = styles.value.filter(s => s.id !== id)
      return { success: true }
    } catch (err) {
      console.error('Error deleting style:', err)
      error.value = err.response?.data?.message || 'Failed to delete style'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Initialize default styles
   */
  const initializeDefaults = async () => {
    loading.value = true
    error.value = null

    try {
      await stylesApi.initializeDefaults()
      await loadStyles() // Reload styles after initialization
      return { success: true }
    } catch (err) {
      console.error('Error initializing default styles:', err)
      error.value = err.response?.data?.message || 'Failed to initialize default styles'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Get styles by category
   */
  const getStylesByCategory = (category) => {
    return styles.value.filter(s => s.category === category)
  }

  /**
   * Get user-selectable styles
   */
  const getUserSelectableStyles = () => {
    return styles.value.filter(s => s.isUserSelectable === true)
  }

  return {
    styles,
    loading,
    error,
    loadStyles,
    getStyleById,
    createStyle,
    updateStyle,
    deleteStyle,
    initializeDefaults,
    getStylesByCategory,
    getUserSelectableStyles
  }
})




