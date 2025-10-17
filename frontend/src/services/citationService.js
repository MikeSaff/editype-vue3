// Citation Service - Full implementation with citation-js
import { Cite, plugins } from 'citation-js'

// Register plugins
import '@citation-js/plugin-bibtex'
import '@citation-js/plugin-csl'

export const CITATION_STYLES = {
  GOST_NUMERIC: 'gost-numeric',
  GOST_AUTHOR_DATE: 'gost-author-date',
  APA: 'apa',
  VANCOUVER: 'vancouver',
  HARVARD: 'harvard',
  IEEE: 'ieee',
  MLA: 'mla'
}

// CSL style mapping to citation-js templates
const STYLE_TEMPLATES = {
  'gost-numeric': 'vancouver', // fallback for GOST
  'gost-author-date': 'apa', // fallback for GOST author-date
  'apa': 'apa',
  'vancouver': 'vancouver',
  'harvard': 'harvard',
  'ieee': 'ieee',
  'mla': 'mla'
}

class CitationService {
  constructor() {
    this.cache = new Map()
    this.config = Cite.plugins.config.get('@csl')
  }

  /**
   * Fetch reference data by DOI using CrossRef API
   * @param {string} doi - Digital Object Identifier
   * @returns {Promise<Object>} CSL-JSON formatted reference
   */
  async fetchByDOI(doi) {
    try {
      // Check cache first
      if (this.cache.has(doi)) {
        return this.cache.get(doi)
      }

      // Fetch from CrossRef API via citation-js
      const cite = await Cite.async(doi)
      const data = cite.data[0]

      // Cache the result
      this.cache.set(doi, data)

      return data
    } catch (error) {
      console.error('Error fetching DOI:', error)
      throw new Error(`Failed to fetch DOI: ${doi}. ${error.message}`)
    }
  }

  /**
   * Fetch reference data by PubMed ID
   * @param {string} pmid - PubMed ID
   * @returns {Promise<Object>} CSL-JSON formatted reference
   */
  async fetchByPubMedID(pmid) {
    try {
      // Check cache first
      const cacheKey = `pmid:${pmid}`
      if (this.cache.has(cacheKey)) {
        return this.cache.get(cacheKey)
      }

      // Fetch from PubMed API via citation-js
      const cite = await Cite.async(pmid)
      const data = cite.data[0]

      // Cache the result
      this.cache.set(cacheKey, data)

      return data
    } catch (error) {
      console.error('Error fetching PubMed ID:', error)
      throw new Error(`Failed to fetch PubMed ID: ${pmid}. ${error.message}`)
    }
  }

  /**
   * Import references from BibTeX format
   * @param {string} bibtex - BibTeX formatted text
   * @returns {Array<Object>} Array of CSL-JSON references
   */
  importBibTeX(bibtex) {
    try {
      const cite = new Cite(bibtex, { forceType: '@bibtex/text' })
      return cite.data
    } catch (error) {
      console.error('Error parsing BibTeX:', error)
      throw new Error(`Failed to parse BibTeX: ${error.message}`)
    }
  }

  /**
   * Export references to BibTeX format
   * @param {Array<Object>} references - Array of CSL-JSON references
   * @returns {string} BibTeX formatted text
   */
  exportToBibTeX(references) {
    try {
      const cite = new Cite(references)
      return cite.format('bibtex')
    } catch (error) {
      console.error('Error exporting to BibTeX:', error)
      return '% Error exporting to BibTeX'
    }
  }

  /**
   * Format a single reference according to style
   * @param {Object} reference - CSL-JSON reference object
   * @param {string} style - Citation style (GOST, APA, etc.)
   * @param {string} lang - Language code (ru, en, cn)
   * @returns {string} Formatted reference
   */
  formatReference(reference, style = CITATION_STYLES.GOST_NUMERIC, lang = 'en') {
    try {
      const cite = new Cite(reference)
      const template = STYLE_TEMPLATES[style] || 'apa'
      
      // For GOST styles, we'll use a custom formatter
      if (style.startsWith('gost')) {
        return this.formatGOST(reference, lang)
      }

      const config = {
        format: 'text',
        template: template,
        lang: lang === 'ru' ? 'ru-RU' : lang === 'cn' ? 'zh-CN' : 'en-US'
      }

      return cite.format('bibliography', config)
    } catch (error) {
      console.error('Error formatting reference:', error)
      // Fallback to simple format
      return this.formatSimple(reference)
    }
  }

  /**
   * Format reference in GOST style (custom implementation)
   * @param {Object} reference - CSL-JSON reference
   * @param {string} lang - Language code
   * @returns {string} Formatted reference
   */
  formatGOST(reference, lang = 'ru') {
    const authors = reference.author || []
    const title = reference.title || 'Без названия'
    const year = reference.issued?.['date-parts']?.[0]?.[0] || 'б.г.'
    const journal = reference['container-title'] || ''
    const volume = reference.volume || ''
    const issue = reference.issue || ''
    const pages = reference.page || ''
    const doi = reference.DOI ? ` DOI: ${reference.DOI}` : ''

    // Format authors (up to 3, then et al.)
    let authorString = ''
    if (authors.length > 0) {
      const authorNames = authors.slice(0, 3).map(author => {
        const family = author.family || ''
        const given = author.given ? ` ${author.given.charAt(0)}.` : ''
        return `${family}${given}`
      })
      authorString = authorNames.join(', ')
      if (authors.length > 3) {
        authorString += lang === 'ru' ? ' и др.' : ' et al.'
      }
    }

    // Build citation
    let citation = authorString ? `${authorString} ` : ''
    citation += `${title}`
    
    if (journal) {
      citation += ` // ${journal}`
    }
    
    citation += `. ${year}`
    
    if (volume) {
      citation += `. ${lang === 'ru' ? 'Т.' : 'Vol.'} ${volume}`
    }
    
    if (issue) {
      citation += `. ${lang === 'ru' ? '№' : 'No.'} ${issue}`
    }
    
    if (pages) {
      citation += `. ${lang === 'ru' ? 'С.' : 'P.'} ${pages}`
    }
    
    citation += doi

    return citation
  }

  /**
   * Simple fallback formatter
   * @param {Object} reference - CSL-JSON reference
   * @returns {string} Simple formatted reference
   */
  formatSimple(reference) {
    const authors = reference.author || []
    const authorString = authors.length > 0
      ? authors[0].family || 'Anonymous'
      : 'Anonymous'
    const year = reference.issued?.['date-parts']?.[0]?.[0] || 'n.d.'
    const title = reference.title || 'Untitled'
    
    return `${authorString} (${year}). ${title}`
  }

  /**
   * Format multiple references
   * @param {Array<Object>} references - Array of CSL-JSON references
   * @param {string} style - Citation style
   * @param {string} lang - Language code
   * @returns {Array<string>} Array of formatted references
   */
  formatReferences(references, style = CITATION_STYLES.GOST_NUMERIC, lang = 'en') {
    return references.map(ref => this.formatReference(ref, style, lang))
  }

  /**
   * Create a manual reference from user input
   * @param {Object} data - Reference data
   * @returns {Object} CSL-JSON reference
   */
  createManualReference(data) {
    return {
      type: data.type || 'article-journal',
      id: data.id || this.generateId(),
      title: data.title,
      author: data.authors?.map(name => this.parseAuthorName(name)) || [],
      issued: { 'date-parts': [[data.year]] },
      'container-title': data.journal,
      volume: data.volume,
      issue: data.issue,
      page: data.pages,
      DOI: data.doi,
      URL: data.url,
      publisher: data.publisher
    }
  }

  /**
   * Parse author name into CSL format
   * @param {string} name - Author name
   * @returns {Object} CSL author object
   */
  parseAuthorName(name) {
    // Handle "Last, First" format
    if (name.includes(',')) {
      const parts = name.split(',').map(s => s.trim())
      return {
        family: parts[0],
        given: parts[1] || ''
      }
    }
    
    // Handle "First Last" format
    const parts = name.trim().split(/\s+/)
    if (parts.length === 1) {
      return { family: parts[0] }
    }
    
    return {
      given: parts.slice(0, -1).join(' '),
      family: parts[parts.length - 1]
    }
  }

  /**
   * Generate unique ID for reference
   * @returns {string} Unique ID
   */
  generateId() {
    return `ref_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * Validate DOI format
   * @param {string} doi - DOI to validate
   * @returns {boolean} True if valid
   */
  isValidDOI(doi) {
    const doiPattern = /^10\.\d{4,9}\/[-._;()/:A-Z0-9]+$/i
    return doiPattern.test(doi)
  }

  /**
   * Check if reference is duplicate
   * @param {Array<Object>} references - Existing references
   * @param {Object} newRef - New reference to check
   * @returns {boolean} True if duplicate
   */
  isDuplicate(references, newRef) {
    return references.some(ref => {
      // Check by DOI
      if (ref.DOI && newRef.DOI && ref.DOI === newRef.DOI) return true
      
      // Check by title (case-insensitive)
      if (ref.title && newRef.title && 
          ref.title.toLowerCase() === newRef.title.toLowerCase()) return true
      
      // Check by ID
      if (ref.id && newRef.id && ref.id === newRef.id) return true
      
      return false
    })
  }

  /**
   * Sort references by various criteria
   * @param {Array<Object>} references - References to sort
   * @param {string} sortBy - Sort criterion (author, year, title)
   * @returns {Array<Object>} Sorted references
   */
  sortReferences(references, sortBy = 'author') {
    return [...references].sort((a, b) => {
      switch (sortBy) {
        case 'author':
          return (a.author?.[0]?.family || '').localeCompare(b.author?.[0]?.family || '')
        case 'year':
          return (b.issued?.['date-parts']?.[0]?.[0] || 0) - (a.issued?.['date-parts']?.[0]?.[0] || 0)
        case 'title':
          return (a.title || '').localeCompare(b.title || '')
        default:
          return 0
      }
    })
  }

  /**
   * Get inline citation format
   * @param {Object} reference - Reference object
   * @param {string} style - Citation style (numeric or author-year)
   * @param {number} index - Reference index for numeric style
   * @returns {string} Inline citation
   */
  getInlineCitation(reference, style = 'numeric', index = 1) {
    if (style === 'numeric') {
      return `[${index}]`
    }
    
    const author = reference.author?.[0]?.family || 'Anonymous'
    const year = reference.issued?.['date-parts']?.[0]?.[0] || 'n.d.'
    return `(${author}, ${year})`
  }

  /**
   * Search for references by keyword
   * @param {Array<Object>} references - References to search
   * @param {string} keyword - Search keyword
   * @returns {Array<Object>} Matching references
   */
  searchReferences(references, keyword) {
    const lowerKeyword = keyword.toLowerCase()
    return references.filter(ref => {
      const title = (ref.title || '').toLowerCase()
      const authors = (ref.author || []).map(a => 
        `${a.family} ${a.given}`.toLowerCase()
      ).join(' ')
      const year = String(ref.issued?.['date-parts']?.[0]?.[0] || '')
      
      return title.includes(lowerKeyword) || 
             authors.includes(lowerKeyword) || 
             year.includes(lowerKeyword)
    })
  }

  /**
   * Clear cache
   */
  clearCache() {
    this.cache.clear()
  }
}

export const citationService = new CitationService()
export default CitationService
