import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/articles'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/auth/callback',
      name: 'oauth-callback',
      component: () => import('@/views/OAuth2Callback.vue')
    },
    {
      path: '/articles',
      name: 'articles',
      component: () => import('@/views/ArticlesList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editor/new',
      name: 'new-article',
      component: () => import('@/views/CreateArticle.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editor/:id',
      name: 'article-editor',
      component: () => import('@/views/ArticleEditorNew.vue'),
      meta: { requiresAuth: true }
    },
    // Admin routes
    {
      path: '/admin',
      name: 'admin',
      redirect: '/admin/dashboard'
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/templates',
      name: 'admin-templates',
      component: () => import('@/views/admin/TemplatesManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/paragraph-styles',
      name: 'admin-paragraph-styles',
      component: () => import('@/views/admin/ParagraphStylesManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    // Legacy routes for backward compatibility
    {
      path: '/publications',
      name: 'publications',
      redirect: '/articles'
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('@/views/UsersList.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // Проверяем, требуется ли аутентификация для маршрута
  if (to.meta.requiresAuth && !authStore.isAuthenticated()) {
    // Перенаправляем на страницу входа
    next('/login')
  } else if (to.meta.requiresAdmin) {
    // Проверяем права администратора
    const user = authStore.user
    const isAdmin = user?.roles?.includes('ADMIN')
    
    if (!isAdmin) {
      // Если не админ, перенаправляем на главную
      alert('Доступ запрещен. Требуются права администратора.')
      next('/')
    } else {
      next()
    }
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated()) {
    // Если пользователь уже авторизован, перенаправляем на главную
    next('/')
  } else {
    next()
  }
})

export default router

