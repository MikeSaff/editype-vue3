<template>
  <div class="callback-page">
    <div class="loading-container">
      <div class="spinner" v-if="!error"></div>
      <h2>{{ message }}</h2>
      <div v-if="error" class="error-message">{{ error }}</div>
      <div v-if="success" class="success-message">Успешно! Перенаправление...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const message = ref('Аутентификация через Editorum...')
const error = ref(null)
const success = ref(false)

onMounted(async () => {
  const code = route.query.code
  const errorParam = route.query.error
  
  // Проверяем наличие ошибки в URL
  if (errorParam) {
    error.value = `Ошибка авторизации: ${errorParam}`
    setTimeout(() => router.push('/login'), 3000)
    return
  }
  
  if (!code) {
    error.value = 'Код авторизации не получен'
    setTimeout(() => router.push('/login'), 3000)
    return
  }

  try {
    message.value = 'Обмен кода на токены...'
    
    // Вызываем аутентификацию через Editorum
    const result = await authStore.authenticateWithEditorum(code)
    
    if (result.success) {
      message.value = 'Аутентификация успешна!'
      success.value = true
      setTimeout(() => router.push('/'), 1500)
    } else {
      error.value = result.error || 'Ошибка аутентификации'
      setTimeout(() => router.push('/login'), 3000)
    }
  } catch (err) {
    console.error('Ошибка OAuth2 callback:', err)
    error.value = 'Произошла ошибка при обработке авторизации'
    setTimeout(() => router.push('/login'), 3000)
  }
})
</script>

<style scoped>
.callback-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loading-container {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  min-width: 300px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h2 {
  color: #333;
  margin: 0 0 10px 0;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  border: 1px solid #fcc;
}

.success-message {
  background-color: #efe;
  color: #3c3;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  border: 1px solid #cfc;
}
</style>

