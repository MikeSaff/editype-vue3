import axios from './axios'

/**
 * Export API
 * Handles article export to various formats
 */
export const exportApi = {
  /**
   * Export article to PDF via LaTeX
   */
  exportPdfLatex(articleId, lang = 'en') {
    return axios.get(`/export/articles/${articleId}/pdf/latex`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to PDF via Vivliostyle
   */
  exportPdfVivliostyle(articleId, lang = 'en') {
    return axios.get(`/export/articles/${articleId}/pdf/vivliostyle`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to LaTeX source
   */
  exportLatex(articleId, lang = 'en') {
    return axios.get(`/export/articles/${articleId}/latex`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to HTML
   */
  exportHtml(articleId, lang = 'en') {
    return axios.get(`/export/${articleId}/html`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to JATS XML
   */
  exportJats(articleId, lang = 'en') {
    return axios.get(`/export/${articleId}/jats`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to plain text
   */
  exportText(articleId, lang = 'en') {
    return axios.get(`/export/${articleId}/txt`, {
      params: { lang },
      responseType: 'blob'
    })
  },

  /**
   * Export article to DOCX (Pandoc)
   */
  exportDocx(articleId, lang = 'en') {
    return axios.get(`/export/${articleId}/docx`, {
      params: { lang },
      responseType: 'blob'
    })
  }
}

export default exportApi
