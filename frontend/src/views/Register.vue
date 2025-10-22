<template>
  <div class="register-page">
    <div class="register-container">
      <h1>{{ t('auth.register') }}</h1>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>{{ t('auth.email') }}</label>
          <input v-model="userData.email" type="email" required />
        </div>
        
        <div class="form-group">
          <label>{{ t('auth.password') }}</label>
          <input v-model="userData.password" type="password" required minlength="6" />
        </div>
        
        <button type="submit" class="btn btn-primary btn-block">
          {{ t('auth.register') }}
        </button>
      </form>
      
      <p class="login-link">
        Already have an account? 
        <router-link to="/login">{{ t('auth.login') }}</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const userData = ref({
  email: '',
  password: ''
})

const error = ref(null)

const handleRegister = async () => {
  error.value = null
  try {
    const result = await authStore.register(userData.value)
    
    if (result.success) {
      console.log('Registration successful, redirecting...')
      // Force reload to ensure store is initialized
      window.location.href = '/articles'
    } else {
      error.value = result.error || 'Ошибка регистрации'
    }
  } catch (err) {
    console.error('Ошибка регистрации:', err)
    error.value = err.message || 'Произошла ошибка при регистрации'
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}

.register-container {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
}

.register-container h1 {
  margin-bottom: 1.5rem;
  text-align: center;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-block {
  width: 100%;
}

.login-link {
  margin-top: 1.5rem;
  text-align: center;
}

.login-link a {
  color: #3498db;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>




