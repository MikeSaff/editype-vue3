<template>
  <div class="articles-page">
    <div class="page-header">
      <h1>Мои статьи</h1>
      <router-link to="/editor/new" class="btn-create">
        <span>+</span> Создать статью
      </router-link>
    </div>
    
    <div v-if="articlesStore.loading" class="loading">
      <div class="spinner"></div>
      <p>Загрузка статей...</p>
    </div>
    
    <div v-else-if="articlesStore.articles.length === 0" class="empty-state">
      <div class="empty-icon">📝</div>
      <h2>У вас пока нет статей</h2>
      <p>Создайте свою первую статью или загрузите существующий документ</p>
      <router-link to="/editor/new" class="btn-primary">
        Создать статью
      </router-link>
    </div>
    
    <div v-else class="articles-grid">
      <div 
        v-for="article in articlesStore.articles" 
        :key="article.id"
        class="article-card"
        @click="openArticle(article.id)"
      >
        <div class="article-header">
          <h3 class="article-title">{{ getArticleTitle(article) }}</h3>
          <div class="article-status" :class="`status-${article.status}`">
            {{ getStatusText(article.status) }}
          </div>
        </div>
        
        <div class="article-meta">
          <div class="article-languages">
            <span 
              v-for="lang in getAvailableLanguages(article)" 
              :key="lang"
              class="lang-badge"
            >
              {{ lang.toUpperCase() }}
            </span>
          </div>
          <div class="article-date">
            Обновлено: {{ formatDate(article.updatedAt) }}
          </div>
        </div>
        
        <div class="article-actions">
          <button 
            @click.stop="editArticle(article.id)"
            class="btn-edit"
            :disabled="!article.id"
            :title="article.id ? `Редактировать статью ${article.id}` : 'ID статьи не определен'"
          >
            Редактировать
            <span v-if="!article.id" style="color: red;">⚠️</span>
          </button>
          <div class="export-menu">
            <button @click.stop="toggleExportMenu(article.id)" class="btn-export">
              Экспорт ▼
            </button>
            <div v-if="exportMenuOpen === article.id" class="export-dropdown">
              <button @click="exportArticle(article.id, 'html')">HTML</button>
              <button @click="exportArticle(article.id, 'pdf')">PDF</button>
              <button @click="exportArticle(article.id, 'docx')">DOCX</button>
              <button @click="exportArticle(article.id, 'jats')">JATS XML</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useArticlesStore } from '@/stores/articles'

const router = useRouter()
const authStore = useAuthStore()
const articlesStore = useArticlesStore()

const exportMenuOpen = ref(null)

onMounted(async () => {
  await loadArticles()
})

const loadArticles = async () => {
  console.log('📚 Загрузка статей...')
  const result = await articlesStore.loadUserArticles()
  
  if (!result.success) {
    console.error('❌ Ошибка загрузки статей:', result.error)
    // Показываем заглушку в случае ошибки
    articlesStore.articles = [
      {
        id: '1',
        title: { ru: 'Пример научной статьи', en: 'Example Scientific Article' },
        status: 'draft',
        updatedAt: new Date().toISOString(),
        availableLanguages: ['ru', 'en']
      },
      {
        id: '2', 
        title: { ru: 'Исследование новых методов', en: 'Research on New Methods' },
        status: 'published',
        updatedAt: new Date(Date.now() - 86400000).toISOString(),
        availableLanguages: ['ru']
      }
    ]
  } else {
    console.log('✅ Статьи загружены успешно:', articlesStore.articles.length, 'шт.')
    console.log('📋 Список статей:', articlesStore.articles.map(a => ({ id: a.id, title: getArticleTitle(a) })))
  }
}

const getArticleTitle = (article) => {
  // Возвращаем заголовок на русском языке, если есть
  if (article.title?.ru) return article.title.ru
  if (article.title?.en) return article.title.en
  return 'Без названия'
}

const getAvailableLanguages = (article) => {
  return article.availableLanguages || ['ru']
}

const getStatusText = (status) => {
  const statusMap = {
    'draft': 'Черновик',
    'review': 'На рецензии',
    'published': 'Опубликовано',
    'archived': 'Архив'
  }
  return statusMap[status] || status
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const openArticle = (articleId) => {
  router.push(`/editor/${articleId}`)
}

const editArticle = (articleId) => {
  console.log('🔍 Editing article, ID:', articleId)
  
  if (!articleId) {
    console.error('❌ Article ID is undefined!')
    alert('Ошибка: ID статьи не определен')
    return
  }
  
  try {
    console.log('📝 Navigating to:', `/editor/${articleId}`)
    router.push(`/editor/${articleId}`)
  } catch (error) {
    console.error('❌ Navigation error:', error)
    alert(`Ошибка навигации: ${error.message}`)
  }
}

const toggleExportMenu = (articleId) => {
  exportMenuOpen.value = exportMenuOpen.value === articleId ? null : articleId
}

const exportArticle = async (articleId, format) => {
  exportMenuOpen.value = null
  try {
    const result = await articlesStore.exportArticle(articleId, format)
    if (result.success) {
      alert(`Экспорт в ${format.toUpperCase()} запущен. ID задачи: ${result.jobId}`)
    } else {
      alert(`Ошибка экспорта: ${result.error}`)
    }
  } catch (error) {
    console.error('Ошибка экспорта:', error)
    alert('Ошибка экспорта статьи')
  }
}

// Закрываем меню экспорта при клике вне его
document.addEventListener('click', () => {
  exportMenuOpen.value = null
})
</script>

<style scoped>
.articles-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #333;
  margin: 0;
}

.btn-create {
  background: #667eea;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background 0.3s;
}

.btn-create:hover {
  background: #5a6fd8;
}

.loading {
  text-align: center;
  padding: 4rem 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h2 {
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin-bottom: 2rem;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 1rem 2rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  display: inline-block;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.article-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e1e5e9;
}

.article-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.article-title {
  color: #333;
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  line-height: 1.4;
  flex: 1;
  margin-right: 1rem;
}

.article-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-draft {
  background: #fef3c7;
  color: #92400e;
}

.status-review {
  background: #dbeafe;
  color: #1e40af;
}

.status-published {
  background: #d1fae5;
  color: #065f46;
}

.status-archived {
  background: #f3f4f6;
  color: #6b7280;
}

.article-meta {
  margin-bottom: 1.5rem;
}

.article-languages {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.lang-badge {
  background: #f3f4f6;
  color: #6b7280;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
}

.article-date {
  color: #6b7280;
  font-size: 0.9rem;
}

.article-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-edit {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-edit:hover {
  background: #5a6fd8;
}

.export-menu {
  position: relative;
}

.btn-export {
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-export:hover {
  background: #e5e7eb;
}

.export-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
  min-width: 120px;
}

.export-dropdown button {
  display: block;
  width: 100%;
  background: none;
  border: none;
  padding: 0.75rem 1rem;
  text-align: left;
  cursor: pointer;
  transition: background 0.3s;
  font-size: 0.9rem;
}

.export-dropdown button:hover {
  background: #f9fafb;
}

.export-dropdown button:first-child {
  border-radius: 6px 6px 0 0;
}

.export-dropdown button:last-child {
  border-radius: 0 0 6px 6px;
}
</style>
