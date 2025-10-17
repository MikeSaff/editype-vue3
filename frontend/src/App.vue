<template>
  <div id="app">
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">Znan.io</router-link>
        <div class="nav-links">
          <template v-if="authStore.isAuthenticated()">
            <router-link to="/articles">Мои статьи</router-link>
            <router-link to="/editor/new">Создать</router-link>
            <div class="user-info">
              <span class="user-name">{{ getUserDisplayName() }}</span>
              <span v-if="authStore.isEditorumUser()" class="editorum-badge">Editorum</span>
            </div>
            <button @click="handleLogout" class="btn-logout">Выйти</button>
          </template>
          <template v-else>
            <router-link to="/login">Войти</router-link>
          </template>
          <div class="language-selector">
            <button @click="changeLanguage('en')" :class="{ active: locale === 'en' }">EN</button>
            <button @click="changeLanguage('ru')" :class="{ active: locale === 'ru' }">RU</button>
            <button @click="changeLanguage('cn')" :class="{ active: locale === 'cn' }">CN</button>
          </div>
        </div>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const { t, locale } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const changeLanguage = (lang) => {
  locale.value = lang
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const getUserDisplayName = () => {
  const user = authStore.user
  if (!user) return ''
  
  if (user.firstName && user.lastName) {
    return `${user.firstName} ${user.lastName}`
  } else if (user.firstName) {
    return user.firstName
  } else {
    return user.email
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

.navbar {
  background-color: #2c3e50;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.nav-links a {
  color: white;
  text-decoration: none;
  transition: opacity 0.3s;
}

.nav-links a:hover {
  opacity: 0.8;
}

.language-selector {
  display: flex;
  gap: 0.5rem;
}

.language-selector button {
  padding: 0.25rem 0.75rem;
  background-color: transparent;
  border: 1px solid white;
  color: white;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.language-selector button:hover,
.language-selector button.active {
  background-color: white;
  color: #2c3e50;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-name {
  color: white;
  opacity: 0.9;
  font-weight: 500;
}

.editorum-badge {
  background-color: #667eea;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
}

.btn-logout {
  padding: 0.25rem 0.75rem;
  background-color: #e74c3c;
  border: 1px solid #e74c3c;
  color: white;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.btn-logout:hover {
  background-color: #c0392b;
}

.main-content {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}
</style>

