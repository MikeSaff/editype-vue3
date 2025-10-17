<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>Znan.io</h1>
        <p>Научный редактор документов</p>
      </div>
      
      <div class="login-content">
        <div class="editorum-login">
          <h2>Вход в систему</h2>
          <p>Для входа в Znan.io используйте ваш аккаунт Editorum</p>
          
          <button 
            @click="loginWithEditorum" 
            class="btn-editorum"
            :disabled="loading"
          >
            <span v-if="loading">Вход...</span>
            <span v-else>Войти через Editorum</span>
          </button>
        </div>
        
        <!-- Скрытый раздел для разработки -->
        <div v-if="showDevLogin" class="dev-login">
          <hr>
          <h3>Разработка (только для тестирования)</h3>
          <form @submit.prevent="handleLocalLogin">
            <div class="form-group">
              <label>Email:</label>
              <input 
                v-model="localLogin.email" 
                type="email" 
                required 
                placeholder="test@example.com"
              />
            </div>
            <div class="form-group">
              <label>Пароль:</label>
              <input 
                v-model="localLogin.password" 
                type="password" 
                required 
                placeholder="password"
              />
            </div>
            <button type="submit" class="btn-dev">Войти (локально)</button>
          </form>
        </div>
      </div>
      
      <div class="login-footer">
        <p>Znan.io интегрирован с системой Editorum для единой авторизации</p>
        <div class="register-link">
          <p>Нет аккаунта? <router-link to="/register">Зарегистрироваться</router-link></p>
        </div>
        <div class="local-login-toggle">
          <button @click="showDevLogin = !showDevLogin" class="btn-toggle">
            {{ showDevLogin ? 'Скрыть локальный вход' : 'Локальный вход (для разработки)' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const showDevLogin = ref(false)
const localLogin = ref({
  email: '',
  password: ''
})

// Показать локальный вход только в режиме разработки
onMounted(() => {
  showDevLogin.value = import.meta.env.DEV
})

const loginWithEditorum = async () => {
  loading.value = true
  try {
    const authUrl = await authStore.getEditorumAuthUrl()
    // Перенаправляем пользователя на Editorum для авторизации
    window.location.href = authUrl
  } catch (error) {
    console.error('Ошибка получения URL авторизации:', error)
    alert('Ошибка входа в систему. Попробуйте позже.')
  } finally {
    loading.value = false
  }
}

const handleLocalLogin = async () => {
  loading.value = true
  try {
    const result = await authStore.login(localLogin.value)
    if (result.success) {
      router.push('/')
    } else {
      alert(result.error)
    }
  } catch (error) {
    console.error('Ошибка локального входа:', error)
    alert('Ошибка входа в систему')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 2.5rem;
  font-weight: 700;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 1.1rem;
}

.login-content h2 {
  color: #333;
  margin: 0 0 10px 0;
  text-align: center;
}

.login-content p {
  color: #666;
  text-align: center;
  margin-bottom: 30px;
}

.btn-editorum {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  padding: 15px;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-editorum:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-editorum:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.dev-login {
  margin-top: 30px;
  padding-top: 20px;
}

.dev-login hr {
  border: none;
  border-top: 1px solid #eee;
  margin-bottom: 20px;
}

.dev-login h3 {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 15px;
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-dev {
  width: 100%;
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-dev:hover {
  background: #e9ecef;
}

.login-footer {
  margin-top: 30px;
  text-align: center;
}

.login-footer p {
  color: #999;
  font-size: 0.9rem;
  margin: 0;
}

.register-link {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.register-link p {
  color: #666;
  font-size: 1rem;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}

.local-login-toggle {
  margin-top: 15px;
}

.btn-toggle {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: underline;
  padding: 5px;
}

.btn-toggle:hover {
  color: #5a6fd8;
}
</style>