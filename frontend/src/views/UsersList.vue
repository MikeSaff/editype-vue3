<template>
  <div class="users-page">
    <div class="page-header">
      <h1>{{ t('users.title') }}</h1>
      <button @click="showCreateForm = true" class="btn btn-primary">
        {{ t('users.createNew') }}
      </button>
    </div>

    <div v-if="error" class="error-message">{{ error }}</div>

    <table v-if="users.length > 0" class="users-table">
      <thead>
        <tr>
          <th>{{ t('users.email') }}</th>
          <th>{{ t('users.roles') }}</th>
          <th>{{ t('users.createdAt') }}</th>
          <th>{{ t('users.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.email }}</td>
          <td>{{ user.roles?.join(', ') || 'N/A' }}</td>
          <td>{{ formatDate(user.createdAt) }}</td>
          <td>
            <button @click="editUser(user)" class="btn btn-small">
              {{ t('users.edit') }}
            </button>
            <button @click="deleteUserConfirm(user.id)" class="btn btn-small btn-danger">
              {{ t('users.delete') }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else-if="!loading" class="empty-state">
      No users found
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateForm || editingUser" class="modal">
      <div class="modal-content">
        <h2>{{ editingUser ? t('users.edit') : t('users.createNew') }}</h2>
        <form @submit.prevent="saveUser">
          <div class="form-group">
            <label>{{ t('users.email') }}</label>
            <input v-model="formData.email" type="email" required />
          </div>
          <div class="form-group">
            <label>{{ t('users.password') }}</label>
            <input 
              v-model="formData.password" 
              type="password" 
              :required="!editingUser"
              :placeholder="editingUser ? 'Leave empty to keep current' : ''"
            />
          </div>
          <div class="form-group">
            <label>{{ t('users.roles') }}</label>
            <div class="checkbox-group">
              <label>
                <input type="checkbox" value="AUTHOR" v-model="formData.roles" />
                AUTHOR
              </label>
              <label>
                <input type="checkbox" value="EDITOR" v-model="formData.roles" />
                EDITOR
              </label>
              <label>
                <input type="checkbox" value="REVIEWER" v-model="formData.roles" />
                REVIEWER
              </label>
            </div>
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary">{{ t('users.save') }}</button>
            <button type="button" @click="cancelEdit" class="btn">{{ t('users.cancel') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/users'

const { t } = useI18n()

const users = ref([])
const loading = ref(false)
const error = ref(null)
const showCreateForm = ref(false)
const editingUser = ref(null)
const formData = ref({
  email: '',
  password: '',
  roles: []
})

const fetchUsers = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await userApi.getAllUsers()
    users.value = response.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to fetch users'
  } finally {
    loading.value = false
  }
}

const editUser = (user) => {
  editingUser.value = user
  formData.value = {
    email: user.email,
    password: '',
    roles: [...(user.roles || [])]
  }
}

const saveUser = async () => {
  try {
    if (editingUser.value) {
      await userApi.updateUser(editingUser.value.id, formData.value)
    } else {
      await userApi.createUser(formData.value)
    }
    await fetchUsers()
    cancelEdit()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to save user'
  }
}

const deleteUserConfirm = async (id) => {
  if (confirm(t('users.confirmDelete'))) {
    try {
      await userApi.deleteUser(id)
      await fetchUsers()
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to delete user'
    }
  }
}

const cancelEdit = () => {
  showCreateForm.value = false
  editingUser.value = null
  formData.value = {
    email: '',
    password: '',
    roles: []
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-page {
  padding: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.users-table {
  width: 100%;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.users-table th,
.users-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.users-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-small {
  padding: 0.25rem 0.75rem;
  margin-right: 0.5rem;
  background-color: #95a5a6;
  color: white;
}

.btn-small:hover {
  background-color: #7f8c8d;
}

.btn-danger {
  background-color: #e74c3c;
}

.btn-danger:hover {
  background-color: #c0392b;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input[type="email"],
.form-group input[type="password"] {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.checkbox-group {
  display: flex;
  gap: 1rem;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: normal;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}
</style>




