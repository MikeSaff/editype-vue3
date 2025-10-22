<template>
  <div class="create-article-simple">
    <div class="container">
      <h1>Создать новую статью</h1>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="createArticle">
        <div class="form-group">
          <label>Название статьи (RU):</label>
          <input 
            v-model="articleData.titleRu" 
            type="text" 
            required
            placeholder="Введите название статьи"
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label>Название статьи (EN):</label>
          <input 
            v-model="articleData.titleEn" 
            type="text" 
            placeholder="Enter article title"
            class="form-input"
          />
        </div>
        
        <div class="form-actions">
          <button type="button" @click="goBack" class="btn btn-secondary">
            Отмена
          </button>
          <button type="submit" :disabled="creating" class="btn btn-primary">
            <span v-if="creating">Создание...</span>
            <span v-else>Создать статью</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { articlesApi } from '@/api/articles'

const router = useRouter()

const articleData = ref({
  titleRu: '',
  titleEn: ''
})

const creating = ref(false)
const error = ref(null)

const createArticle = async () => {
  creating.value = true
  error.value = null
  
  try {
    // Создаем простую структуру статьи
    const newArticle = {
      metadata: {
        ru: {
          title: articleData.value.titleRu,
          annotation: '',
          keywords: ''
        },
        en: {
          title: articleData.value.titleEn || articleData.value.titleRu,
          annotation: '',
          keywords: ''
        }
      },
      text: '',
      pmJson: null,
      languages: ['ru', 'en'],
      references: []
    }
    
    console.log('Creating article:', newArticle)
    
    const response = await articlesApi.create(newArticle)
    
    console.log('Article created:', response.data)
    
    // Переход к редактору
    router.push(`/editor/${response.data.id}`)
    
  } catch (err) {
    console.error('Ошибка создания статьи:', err)
    error.value = err.response?.data?.message || err.message || 'Ошибка создания статьи'
  } finally {
    creating.value = false
  }
}

const goBack = () => {
  router.push('/articles')
}
</script>

<style scoped>
.create-article-simple {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 2rem;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

h1 {
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
  border: 1px solid #fcc;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-secondary:hover {
  background: #e5e7eb;
}
</style>

