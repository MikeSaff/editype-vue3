// Tiptap Editor Configuration
import StarterKit from '@tiptap/starter-kit'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableCell from '@tiptap/extension-table-cell'
import TableHeader from '@tiptap/extension-table-header'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import Mathematics from '@tiptap/extension-mathematics'

// LaTeX macros для научных публикаций
export const scientificMacros = {
  // Математические множества
  "\\RR": "\\mathbb{R}",
  "\\CC": "\\mathbb{C}",
  "\\NN": "\\mathbb{N}",
  "\\ZZ": "\\mathbb{Z}",
  "\\QQ": "\\mathbb{Q}",
  
  // Физика (квантовая механика)
  "\\ket": "\\left|#1\\right\\rangle",
  "\\bra": "\\left\\langle#1\\right|",
  "\\braket": "\\left\\langle#1\\middle|#2\\right\\rangle",
  
  // Производные
  "\\dd": "\\mathrm{d}",
  "\\diff": "\\frac{\\mathrm{d}#1}{\\mathrm{d}#2}",
  "\\pdiff": "\\frac{\\partial#1}{\\partial#2}",
  
  // Векторы
  "\\vec": "\\mathbf{#1}",
  
  // Операторы
  "\\grad": "\\nabla",
  "\\div": "\\nabla\\cdot",
  "\\curl": "\\nabla\\times",
  
  // Для кириллицы в формулах
  "\\text": "\\mathrm{#1}"
}

// Конфигурация KaTeX
export const katexConfig = {
  throwOnError: false,
  displayMode: false,
  macros: scientificMacros,
  trust: true, // Для \href, \includegraphics
  strict: false,
  output: 'html',
  fleqn: false,
  leqno: false,
  colorIsTextColor: true
}

// Конфигурация расширений Tiptap
export const getTiptapExtensions = (placeholder = 'Начните писать...') => {
  return [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3, 4, 5, 6]
      },
      bulletList: {
        keepMarks: true,
        keepAttributes: false
      },
      orderedList: {
        keepMarks: true,
        keepAttributes: false
      }
    }),
    
    // Таблицы
    Table.configure({
      resizable: true,
      HTMLAttributes: {
        class: 'scientific-table'
      }
    }),
    TableRow,
    TableHeader,
    TableCell,
    
    // Ссылки
    Link.configure({
      openOnClick: false,
      HTMLAttributes: {
        class: 'article-link',
        rel: 'noopener noreferrer'
      }
    }),
    
    // Изображения
    Image.configure({
      inline: false,
      allowBase64: true,
      HTMLAttributes: {
        class: 'article-image'
      }
    }),
    
    // Математика (LaTeX)
    Mathematics.configure({
      katexOptions: katexConfig,
      HTMLAttributes: {
        class: 'math-node'
      }
    }),
    
    // Placeholder
    Placeholder.configure({
      placeholder
    })
  ]
}

// Дефолтный контент для нового документа
export const defaultContent = {
  type: 'doc',
  content: [
    {
      type: 'heading',
      attrs: { level: 1 },
      content: [
        {
          type: 'text',
          text: 'Название статьи'
        }
      ]
    },
    {
      type: 'heading',
      attrs: { level: 2 },
      content: [
        {
          type: 'text',
          text: 'Введение'
        }
      ]
    },
    {
      type: 'paragraph',
      content: [
        {
          type: 'text',
          text: 'Начните писать вашу статью здесь...'
        }
      ]
    }
  ]
}

// Команды редактора
export const editorCommands = {
  // Форматирование текста
  toggleBold: (editor) => editor.chain().focus().toggleBold().run(),
  toggleItalic: (editor) => editor.chain().focus().toggleItalic().run(),
  toggleStrike: (editor) => editor.chain().focus().toggleStrike().run(),
  toggleCode: (editor) => editor.chain().focus().toggleCode().run(),
  
  // Заголовки
  setHeading: (editor, level) => editor.chain().focus().setHeading({ level }).run(),
  setParagraph: (editor) => editor.chain().focus().setParagraph().run(),
  
  // Списки
  toggleBulletList: (editor) => editor.chain().focus().toggleBulletList().run(),
  toggleOrderedList: (editor) => editor.chain().focus().toggleOrderedList().run(),
  
  // Таблицы
  insertTable: (editor, rows = 3, cols = 3) => {
    editor.chain().focus().insertTable({ rows, cols, withHeaderRow: true }).run()
  },
  deleteTable: (editor) => editor.chain().focus().deleteTable().run(),
  addRowBefore: (editor) => editor.chain().focus().addRowBefore().run(),
  addRowAfter: (editor) => editor.chain().focus().addRowAfter().run(),
  deleteRow: (editor) => editor.chain().focus().deleteRow().run(),
  addColumnBefore: (editor) => editor.chain().focus().addColumnBefore().run(),
  addColumnAfter: (editor) => editor.chain().focus().addColumnAfter().run(),
  deleteColumn: (editor) => editor.chain().focus().deleteColumn().run(),
  
  // Математика
  insertInlineMath: (editor, latex = 'E=mc^2') => {
    editor.chain().focus().insertContent({
      type: 'math_inline',
      attrs: { latex }
    }).run()
  },
  
  insertDisplayMath: (editor, latex = '\\int_{0}^{\\infty} e^{-x^2} dx = \\frac{\\sqrt{\\pi}}{2}') => {
    editor.chain().focus().insertContent({
      type: 'math_display',
      attrs: { latex }
    }).run()
  },
  
  // Изображения
  insertImage: (editor, src, alt = '', title = '') => {
    editor.chain().focus().setImage({ src, alt, title }).run()
  },
  
  // Ссылки
  setLink: (editor, href) => {
    editor.chain().focus().setLink({ href }).run()
  },
  unsetLink: (editor) => editor.chain().focus().unsetLink().run(),
  
  // Разделители
  setHorizontalRule: (editor) => editor.chain().focus().setHorizontalRule().run(),
  
  // Отмена/Повтор
  undo: (editor) => editor.chain().focus().undo().run(),
  redo: (editor) => editor.chain().focus().redo().run()
}


