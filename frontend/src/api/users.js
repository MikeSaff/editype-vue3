import apiClient from './axios'

export const userApi = {
  // Get all users
  getAllUsers() {
    return apiClient.get('/users')
  },

  // Get user by ID
  getUserById(id) {
    return apiClient.get(`/users/${id}`)
  },

  // Create new user
  createUser(userData) {
    return apiClient.post('/users', userData)
  },

  // Update user
  updateUser(id, userData) {
    return apiClient.put(`/users/${id}`, userData)
  },

  // Delete user
  deleteUser(id) {
    return apiClient.delete(`/users/${id}`)
  }
}




