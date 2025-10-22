import axios from './axios'

/**
 * API client for paragraph styles management
 */
export const stylesApi = {
  /**
   * Get all paragraph styles
   * @param {Object} params - Query parameters (category, userSelectable)
   */
  getAllStyles: (params) => axios.get('/admin/styles', { params }),

  /**
   * Get style by ID
   */
  getStyleById: (id) => axios.get(`/admin/styles/${id}`),

  /**
   * Create new paragraph style
   */
  createStyle: (data) => axios.post('/admin/styles', data),

  /**
   * Update paragraph style
   */
  updateStyle: (id, data) => axios.put(`/admin/styles/${id}`, data),

  /**
   * Delete paragraph style
   */
  deleteStyle: (id) => axios.delete(`/admin/styles/${id}`),

  /**
   * Initialize default styles
   */
  initializeDefaults: () => axios.post('/admin/styles/initialize-defaults')
}




