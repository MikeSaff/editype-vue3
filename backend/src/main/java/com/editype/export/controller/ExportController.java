package com.editype.export.controller;

import com.editype.article.entity.Article;
import com.editype.article.service.ArticleService;
import com.editype.export.service.ExportService;
import com.editype.export.service.LatexExportService;
import com.editype.export.service.VivliostyleExportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for exporting publications
 * Supports: HTML, XML JATS, TXT, PDF (LaTeX/Vivliostyle), DOCX (Pandoc), LaTeX source
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    
    private final ExportService exportService;
    private final ArticleService articleService;
    private final LatexExportService latexExportService;
    private final VivliostyleExportService vivliostyleExportService;
    
    /**
     * Export publication to HTML
     */
    @GetMapping("/{id}/html")
    public ResponseEntity<String> exportToHtml(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        String html = exportService.exportToHtml(id, lang);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", "publication_" + id + ".html");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(html);
    }
    
    /**
     * Export publication to XML JATS
     */
    @GetMapping("/{id}/jats")
    public ResponseEntity<String> exportToJats(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        String xml = exportService.exportToJatsXml(id, lang);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment", "publication_" + id + ".xml");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(xml);
    }
    
    /**
     * Export publication to plain text
     */
    @GetMapping("/{id}/txt")
    public ResponseEntity<String> exportToText(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        String text = exportService.exportToText(id, lang);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "publication_" + id + ".txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(text);
    }
    
    /**
     * Export article to PDF via LaTeX/Pandoc (high quality, academic standard)
     */
    @GetMapping("/articles/{id}/pdf/latex")
    public ResponseEntity<byte[]> exportArticleToPdfLatex(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        try {
            logger.info("Exporting article {} to PDF via LaTeX in language {}", id, lang);
            
            Article article = articleService.getById(id);
            byte[] pdfBytes = latexExportService.exportToPDF(article, lang);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "article_" + id + "_latex.pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            logger.error("Failed to export article {} to PDF via LaTeX", id, e);
            return ResponseEntity.internalServerError()
                    .body(("Error exporting to PDF: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Export article to PDF via Vivliostyle (beautiful typography, web-based)
     */
    @GetMapping("/articles/{id}/pdf/vivliostyle")
    public ResponseEntity<byte[]> exportArticleToPdfVivliostyle(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        try {
            logger.info("Exporting article {} to PDF via Vivliostyle in language {}", id, lang);
            
            Article article = articleService.getById(id);
            byte[] pdfBytes = vivliostyleExportService.exportToPDF(article, lang);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "article_" + id + "_vivlio.pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            logger.error("Failed to export article {} to PDF via Vivliostyle", id, e);
            return ResponseEntity.internalServerError()
                    .body(("Error exporting to PDF: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Export article to LaTeX source code
     */
    @GetMapping("/articles/{id}/latex")
    public ResponseEntity<byte[]> exportArticleToLatex(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        try {
            logger.info("Exporting article {} to LaTeX source in language {}", id, lang);
            
            Article article = articleService.getById(id);
            byte[] latexBytes = latexExportService.exportToLatex(article, lang);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "article_" + id + ".tex");
            headers.setContentLength(latexBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(latexBytes);
                    
        } catch (Exception e) {
            logger.error("Failed to export article {} to LaTeX", id, e);
            return ResponseEntity.internalServerError()
                    .body(("Error exporting to LaTeX: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Export publication to PDF (default: uses Vivliostyle for best results)
     * Legacy endpoint - redirects to Vivliostyle
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue = "vivliostyle") String engine) {
        
        // Redirect to appropriate export method
        if ("latex".equalsIgnoreCase(engine)) {
            return exportArticleToPdfLatex(id, lang);
        } else {
            return exportArticleToPdfVivliostyle(id, lang);
        }
    }
    
    /**
     * Export publication to DOCX (via Pandoc)
     * Note: Requires Pandoc to be installed in the Docker container
     */
    @GetMapping("/{id}/docx")
    public ResponseEntity<String> exportToDocx(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        
        // TODO: Implement DOCX export via Pandoc
        // This will use similar pipeline as LaTeX but output to DOCX
        return ResponseEntity.ok()
                .body("DOCX export via Pandoc - coming soon. Will use: pandoc article.md -o article.docx");
    }
}

