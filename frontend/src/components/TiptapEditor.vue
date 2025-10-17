<template>
  <div class="tiptap-editor-container">
    <!-- Toolbar -->
    <div v-if="editor && !readonly" class="tiptap-toolbar">
      <!-- Форматирование текста -->
      <div class="toolbar-group">
        <button 
          @click="commands.toggleBold(editor)"
          :class="{ 'is-active': editor.isActive('bold') }"
          class="toolbar-btn"
          title="Bold (Ctrl+B)"
        >
          <strong>B</strong>
        </button>
        <button 
          @click="commands.toggleItalic(editor)"
          :class="{ 'is-active': editor.isActive('italic') }"
          class="toolbar-btn"
          title="Italic (Ctrl+I)"
        >
          <em>I</em>
        </button>
        <button 
          @click="commands.toggleStrike(editor)"
          :class="{ 'is-active': editor.isActive('strike') }"
          class="toolbar-btn"
          title="Strike"
        >
          <s>S</s>
        </button>
        <button 
          @click="commands.toggleCode(editor)"
          :class="{ 'is-active': editor.isActive('code') }"
          class="toolbar-btn"
          title="Code"
        >
          &lt;/&gt;
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Заголовки -->
      <div class="toolbar-group">
        <select 
          @change="onHeadingChange($event)" 
          class="toolbar-select"
          :value="getCurrentHeadingLevel()"
        >
          <option value="0">Paragraph</option>
          <option value="1">Heading 1</option>
          <option value="2">Heading 2</option>
          <option value="3">Heading 3</option>
          <option value="4">Heading 4</option>
        </select>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Списки -->
      <div class="toolbar-group">
        <button 
          @click="commands.toggleBulletList(editor)"
          :class="{ 'is-active': editor.isActive('bulletList') }"
          class="toolbar-btn"
          title="Bullet List"
        >
          •
        </button>
        <button 
          @click="commands.toggleOrderedList(editor)"
          :class="{ 'is-active': editor.isActive('orderedList') }"
          class="toolbar-btn"
          title="Ordered List"
        >
          1.
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Математика -->
      <div class="toolbar-group">
        <button 
          @click="insertInlineMath"
          class="toolbar-btn"
          title="Inline Formula"
        >
          ∑
        </button>
        <button 
          @click="insertDisplayMath"
          class="toolbar-btn"
          title="Display Formula"
        >
          ∫
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Таблицы -->
      <div class="toolbar-group">
        <button 
          @click="insertTable"
          class="toolbar-btn"
          title="Insert Table"
        >
          ⊞
        </button>
        <button 
          v-if="editor.isActive('table')"
          @click="commands.deleteTable(editor)"
          class="toolbar-btn"
          title="Delete Table"
        >
          ⊟
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Изображения и ссылки -->
      <div class="toolbar-group">
        <button 
          @click="insertImage"
          class="toolbar-btn"
          title="Insert Image"
        >
          🖼
        </button>
        <button 
          @click="insertLink"
          class="toolbar-btn"
          title="Insert Link"
        >
          🔗
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- Отмена/Повтор -->
      <div class="toolbar-group">
        <button 
          @click="commands.undo(editor)"
          :disabled="!editor.can().undo()"
          class="toolbar-btn"
          title="Undo (Ctrl+Z)"
        >
          ↶
        </button>
        <button 
          @click="commands.redo(editor)"
          :disabled="!editor.can().redo()"
          class="toolbar-btn"
          title="Redo (Ctrl+Shift+Z)"
        >
          ↷
        </button>
      </div>
    </div>

    <!-- Editor Content -->
    <div class="tiptap-editor-wrapper">
      <editor-content 
        :editor="editor" 
        class="tiptap-editor-content"
        :class="{ 'readonly': readonly }"
      />
    </div>

    <!-- Character count -->
    <div v-if="editor" class="editor-footer">
      <span class="character-count">
        {{ editor.storage.characterCount?.characters() || 0 }} characters, 
        {{ editor.storage.characterCount?.words() || 0 }} words
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import { getTiptapExtensions, editorCommands, defaultContent } from '@/editor/tiptapConfig'
import 'katex/dist/katex.min.css'

const props = defineProps({
  modelValue: {
    type: [String, Object],
    default: null
  },
  placeholder: {
    type: String,
    default: 'Начните писать...'
  },
  readonly: {
    type: Boolean,
    default: false
  },
  autofocus: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const commands = editorCommands

// Initialize editor
const editor = useEditor({
  extensions: getTiptapExtensions(props.placeholder),
  content: props.modelValue || defaultContent,
  editable: !props.readonly,
  autofocus: props.autofocus,
  
  onUpdate: ({ editor }) => {
    const json = editor.getJSON()
    emit('update:modelValue', json)
    emit('change', json)
  }
})

// Watch for external changes
watch(() => props.modelValue, (newValue) => {
  if (editor.value && newValue) {
    const currentContent = editor.value.getJSON()
    if (JSON.stringify(currentContent) !== JSON.stringify(newValue)) {
      editor.value.commands.setContent(newValue, false)
    }
  }
})

// Watch readonly
watch(() => props.readonly, (newValue) => {
  if (editor.value) {
    editor.value.setEditable(!newValue)
  }
})

// Get current heading level
const getCurrentHeadingLevel = () => {
  if (!editor.value) return 0
  for (let level = 1; level <= 4; level++) {
    if (editor.value.isActive('heading', { level })) {
      return level
    }
  }
  return 0
}

// Heading change handler
const onHeadingChange = (event) => {
  const level = parseInt(event.target.value)
  if (level === 0) {
    commands.setParagraph(editor.value)
  } else {
    commands.setHeading(editor.value, level)
  }
}

// Insert inline math
const insertInlineMath = () => {
  const latex = prompt('Введите LaTeX формулу (inline):', 'E=mc^2')
  if (latex) {
    commands.insertInlineMath(editor.value, latex)
  }
}

// Insert display math
const insertDisplayMath = () => {
  const latex = prompt('Введите LaTeX формулу (display):', '\\int_{0}^{\\infty} e^{-x^2} dx')
  if (latex) {
    commands.insertDisplayMath(editor.value, latex)
  }
}

// Insert table
const insertTable = () => {
  const rows = parseInt(prompt('Количество строк:', '3'))
  const cols = parseInt(prompt('Количество столбцов:', '3'))
  if (rows && cols) {
    commands.insertTable(editor.value, rows, cols)
  }
}

// Insert image
const insertImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (event) => {
        const src = event.target.result
        commands.insertImage(editor.value, src)
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}

// Insert link
const insertLink = () => {
  const url = prompt('Введите URL:', 'https://')
  if (url) {
    commands.setLink(editor.value, url)
  }
}

// Cleanup
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
  }
})
</script>

<style scoped>
.tiptap-editor-container {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  overflow: hidden;
}

.tiptap-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  align-items: center;
}

.toolbar-group {
  display: flex;
  gap: 0.25rem;
}

.toolbar-btn {
  padding: 0.375rem 0.75rem;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-btn.is-active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.toolbar-select {
  padding: 0.375rem 0.5rem;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #d1d5db;
  margin: 0 0.25rem;
}

.tiptap-editor-wrapper {
  min-height: 400px;
  max-height: 800px;
  overflow-y: auto;
}

.tiptap-editor-content {
  padding: 1.5rem;
  outline: none;
}

.tiptap-editor-content.readonly {
  background: #f9fafb;
}

.editor-footer {
  padding: 0.5rem 1rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  font-size: 0.875rem;
  color: #6b7280;
}

/* Стили для контента редактора */
:deep(.ProseMirror) {
  outline: none;
  min-height: 400px;
}

:deep(.ProseMirror p) {
  margin: 0.75rem 0;
  line-height: 1.8;
}

:deep(.ProseMirror h1) {
  font-size: 2rem;
  font-weight: bold;
  margin: 1.5rem 0 1rem 0;
}

:deep(.ProseMirror h2) {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 1.25rem 0 0.75rem 0;
}

:deep(.ProseMirror h3) {
  font-size: 1.25rem;
  font-weight: bold;
  margin: 1rem 0 0.5rem 0;
}

:deep(.ProseMirror h4) {
  font-size: 1.1rem;
  font-weight: bold;
  margin: 0.75rem 0 0.5rem 0;
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  padding-left: 2rem;
  margin: 0.75rem 0;
}

:deep(.ProseMirror li) {
  margin: 0.25rem 0;
}

:deep(.ProseMirror code) {
  background: #f3f4f6;
  padding: 0.125rem 0.25rem;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

:deep(.ProseMirror pre) {
  background: #1f2937;
  color: #f9fafb;
  padding: 1rem;
  border-radius: 6px;
  overflow-x: auto;
  margin: 1rem 0;
}

:deep(.ProseMirror blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #6b7280;
  font-style: italic;
}

:deep(.ProseMirror hr) {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 2rem 0;
}

/* Таблицы */
:deep(.ProseMirror .scientific-table) {
  border-collapse: collapse;
  margin: 1rem 0;
  width: 100%;
  overflow: hidden;
}

:deep(.ProseMirror .scientific-table td),
:deep(.ProseMirror .scientific-table th) {
  border: 1px solid #d1d5db;
  padding: 0.5rem;
  min-width: 100px;
  text-align: left;
}

:deep(.ProseMirror .scientific-table th) {
  background: #f3f4f6;
  font-weight: bold;
}

:deep(.ProseMirror .selectedCell) {
  background: #e0e7ff;
}

/* Изображения */
:deep(.ProseMirror .article-image) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 1rem 0;
  display: block;
}

/* Ссылки */
:deep(.ProseMirror .article-link) {
  color: #667eea;
  text-decoration: underline;
  cursor: pointer;
}

:deep(.ProseMirror .article-link:hover) {
  color: #4c51bf;
}

/* Математика (KaTeX) */
:deep(.ProseMirror .math-node) {
  padding: 0.25rem 0.5rem;
  background: #f9fafb;
  border-radius: 4px;
  display: inline-block;
  margin: 0 0.25rem;
}

:deep(.ProseMirror .math-display) {
  display: block;
  margin: 1rem 0;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 6px;
  overflow-x: auto;
}

/* Placeholder */
:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: #9ca3af;
  pointer-events: none;
  height: 0;
}
</style>


