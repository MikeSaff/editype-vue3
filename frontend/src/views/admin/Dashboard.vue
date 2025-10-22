<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1>Панель администратора</h1>
      <p>Статистика и управление системой Znan.io</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📄</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalArticles }}</div>
          <div class="stat-label">Всего статей</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalUsers }}</div>
          <div class="stat-label">Пользователей</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalTemplates }}</div>
          <div class="stat-label">Шаблонов</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🎨</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalStyles }}</div>
          <div class="stat-label">Стилей</div>
        </div>
      </div>
    </div>

    <div class="dashboard-sections">
      <div class="dashboard-section">
        <h2>Недавняя активность</h2>
        <div class="activity-list">
          <div v-for="(activity, index) in recentActivity" :key="index" class="activity-item">
            <div class="activity-icon">{{ activity.icon }}</div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.text }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="dashboard-section">
        <h2>Популярные шаблоны</h2>
        <div class="templates-list">
          <div v-for="template in popularTemplates" :key="template.id" class="template-item">
            <div class="template-name">{{ template.name }}</div>
            <div class="template-usage">
              <span class="usage-bar" :style="{ width: `${template.usagePercent}%` }"></span>
              <span class="usage-text">{{ template.usageCount }} использований</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>Быстрые действия</h2>
      <div class="actions-grid">
        <router-link to="/admin/templates" class="action-card">
          <div class="action-icon">📋</div>
          <div class="action-title">Управление шаблонами</div>
          <div class="action-description">Создание и редактирование шаблонов документов</div>
        </router-link>

        <router-link to="/admin/paragraph-styles" class="action-card">
          <div class="action-icon">🎨</div>
          <div class="action-title">Стили абзацев</div>
          <div class="action-description">Настройка стилей оформления текста</div>
        </router-link>

        <div class="action-card" @click="refreshStatistics">
          <div class="action-icon">🔄</div>
          <div class="action-title">Обновить статистику</div>
          <div class="action-description">Получить актуальные данные</div>
        </div>

        <div class="action-card" @click="viewLogs">
          <div class="action-icon">📊</div>
          <div class="action-title">Логи системы</div>
          <div class="action-description">Просмотр системных логов и событий</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useTemplatesStore } from '@/stores/templates'
import { useStylesStore } from '@/stores/styles'

const templatesStore = useTemplatesStore()
const stylesStore = useStylesStore()

const statistics = ref({
  totalArticles: 0,
  totalUsers: 0,
  totalTemplates: 0,
  totalStyles: 0
})

const recentActivity = ref([
  { icon: '📄', text: 'Создана новая статья "Исследование методов..."', time: '5 минут назад' },
  { icon: '👤', text: 'Новый пользователь зарегистрировался', time: '15 минут назад' },
  { icon: '📋', text: 'Обновлён шаблон "Научная статья"', time: '1 час назад' },
  { icon: '🎨', text: 'Создан новый стиль абзаца', time: '2 часа назад' },
  { icon: '📤', text: 'Экспортирован документ в PDF', time: '3 часа назад' }
])

const popularTemplates = ref([
  { id: 1, name: 'Научная статья (стандарт)', usageCount: 45, usagePercent: 100 },
  { id: 2, name: 'Конференционный доклад', usageCount: 32, usagePercent: 71 },
  { id: 3, name: 'Рецензия', usageCount: 18, usagePercent: 40 },
  { id: 4, name: 'Препринт', usageCount: 12, usagePercent: 27 }
])

onMounted(async () => {
  await loadStatistics()
})

const loadStatistics = async () => {
  try {
    // Load templates and styles
    await Promise.all([
      templatesStore.loadTemplates(),
      stylesStore.loadStyles()
    ])

    // Update statistics
    statistics.value.totalTemplates = templatesStore.templates.length
    statistics.value.totalStyles = stylesStore.styles.length

    // TODO: Load actual article and user counts from API
    statistics.value.totalArticles = 127 // Placeholder
    statistics.value.totalUsers = 48 // Placeholder
  } catch (error) {
    console.error('Error loading statistics:', error)
  }
}

const refreshStatistics = async () => {
  await loadStatistics()
  alert('Статистика обновлена!')
}

const viewLogs = () => {
  alert('Функция просмотра логов пока не реализована.')
  // TODO: Implement logs viewer
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 48px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.dashboard-sections {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.dashboard-section {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 24px;
}

.dashboard-section h2 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 4px;
}

.activity-icon {
  font-size: 24px;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.templates-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.template-item {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 4px;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.template-usage {
  position: relative;
  height: 24px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.usage-bar {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #3498db, #2980b9);
  transition: width 0.3s;
}

.usage-text {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  z-index: 1;
}

.quick-actions {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 24px;
}

.quick-actions h2 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.action-card {
  padding: 20px;
  background: #f9f9f9;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
}

.action-card:hover {
  background: white;
  border-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.action-description {
  font-size: 13px;
  color: #666;
}
</style>




