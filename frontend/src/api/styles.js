import axios from './axios'

/**
 * API client for paragraph styles management
 */
export const stylesApi = {
  /**
   * Get all paragraph styles
   * @param {Object} params - Query parameters (category, userSelectable)
   */
  getAllStyles: (params) => axios.get('/api/admin/styles', { params }),

  /**
   * Get style by ID
   */
  getStyleById: (id) => axios.get(`/api/admin/styles/${id}`),

  /**
   * Create new paragraph style
   */
  createStyle: (data) => axios.post('/api/admin/styles', data),

  /**
   * Update paragraph style
   */
  updateStyle: (id, data) => axios.put(`/api/admin/styles/${id}`, data),

  /**
   * Delete paragraph style
   */
  deleteStyle: (id) => axios.delete(`/api/admin/styles/${id}`),

  /**
   * Initialize default styles
   */
  initializeDefaults: () => axios.post('/api/admin/styles/initialize-defaults')
}


