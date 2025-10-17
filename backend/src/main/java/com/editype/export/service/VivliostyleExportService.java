package com.editype.export.service;

import com.editype.article.entity.Article;
import com.editype.article.entity.ArticleMetadata;
import com.editype.article.entity.ArticleReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Service for exporting articles to PDF using Vivliostyle CLI
 * Provides high-quality typesetting with CSS Paged Media
 */
@Service
public class VivliostyleExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(VivliostyleExportService.class);
    
    private final ObjectMapper objectMapper;
    
    @Value("${vivliostyle.css.template:/app/templates/article-style.css}")
    private String cssTemplatePath;
    
    @Value("${vivliostyle.temp.dir:/app/temp}")
    private String tempDir;
    
    public VivliostyleExportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Export article to PDF using Vivliostyle
     */
    public byte[] exportToPDF(Article article, String language) throws IOException, InterruptedException {
        logger.info("Exporting article {} to PDF via Vivliostyle in language {}", article.getId(), language);
        
        Path workDir = Files.createTempDirectory(Path.of(tempDir), "vivlio-export-");
        
        try {
            // 1. Generate HTML from ProseMirror JSON
            String html = generateHTML(article, language);
            Path htmlFile = workDir.resolve("article.html");
            Files.writeString(htmlFile, html);
            
            // 2. Generate or copy CSS
            Path cssFile = workDir.resolve("style.css");
            String css = generateCSS(article);
            Files.writeString(cssFile, css);
            
            // 3. Run Vivliostyle CLI
            Path pdfFile = workDir.resolve("article.pdf");
            runVivliostyle(htmlFile, cssFile, pdfFile);
            
            // 4. Read PDF bytes
            byte[] pdfBytes = Files.readAllBytes(pdfFile);
            
            logger.info("Successfully exported article {} via Vivliostyle ({} bytes)", 
                article.getId(), pdfBytes.length);
            return pdfBytes;
            
        } finally {
            cleanupDirectory(workDir);
        }
    }
    
    /**
     * Generate HTML from article data
     */
    private String generateHTML(Article article, String language) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"").append(language).append("\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        
        // Title
        Map<String, ArticleMetadata> metadata = article.getMetadata();
        if (metadata != null && metadata.containsKey(language)) {
            ArticleMetadata langMetadata = metadata.get(language);
            if (langMetadata.getTitle() != null) {
                html.append("  <title>").append(escapeHtml(langMetadata.getTitle())).append("</title>\n");
            }
        }
        
        // CSS link
        html.append("  <link rel=\"stylesheet\" href=\"style.css\">\n");
        
        // KaTeX for math rendering
        html.append("  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css\">\n");
        html.append("  <script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js\"></script>\n");
        html.append("  <script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js\"></script>\n");
        
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Article header
        html.append("  <article>\n");
        html.append("    <header class=\"article-header\">\n");
        
        // Title
        if (metadata != null && metadata.containsKey(language)) {
            ArticleMetadata langMetadata = metadata.get(language);
            if (langMetadata.getTitle() != null) {
                html.append("      <h1 class=\"article-title\">")
                    .append(escapeHtml(langMetadata.getTitle()))
                    .append("</h1>\n");
            }
        }
        
        // Authors - Skip for now (only IDs available)
        
        // Metadata
        if (metadata != null && metadata.containsKey(language)) {
            ArticleMetadata langMetadata = metadata.get(language);
            
            // DOI
            if (article.getDoi() != null && !article.getDoi().isEmpty()) {
                html.append("      <div class=\"article-doi\">")
                    .append("DOI: <a href=\"https://doi.org/").append(article.getDoi()).append("\">")
                    .append(article.getDoi()).append("</a>")
                    .append("</div>\n");
            }
            
            // Abstract
            if (langMetadata.getAnnotation() != null && !langMetadata.getAnnotation().isEmpty()) {
                html.append("      <div class=\"article-abstract\">\n");
                html.append("        <h2>Abstract</h2>\n");
                html.append("        <p>").append(escapeHtml(langMetadata.getAnnotation())).append("</p>\n");
                html.append("      </div>\n");
            }
            
            // Keywords
            if (langMetadata.getKeywords() != null && !langMetadata.getKeywords().isEmpty()) {
                html.append("      <div class=\"article-keywords\">\n");
                html.append("        <strong>Keywords:</strong> ")
                    .append(escapeHtml(langMetadata.getKeywords()))
                    .append("\n");
                html.append("      </div>\n");
            }
        }
        
        html.append("    </header>\n\n");
        
        // Main content
        html.append("    <main class=\"article-content\">\n");
        String content = convertPmJsonToHTML(article.getPmJson());
        html.append(content);
        html.append("    </main>\n\n");
        
        // References
        if (article.getReferences() != null && !article.getReferences().isEmpty()) {
            html.append("    <section class=\"article-references\">\n");
            html.append("      <h2>References</h2>\n");
            html.append("      <ol class=\"references-list\">\n");
            
            for (ArticleReference ref : article.getReferences()) {
                html.append("        <li>").append(formatReference(ref, language)).append("</li>\n");
            }
            
            html.append("      </ol>\n");
            html.append("    </section>\n");
        }
        
        html.append("  </article>\n");
        
        // KaTeX auto-render script
        html.append("  <script>\n");
        html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
        html.append("      renderMathInElement(document.body, {\n");
        html.append("        delimiters: [\n");
        html.append("          {left: '$$', right: '$$', display: true},\n");
        html.append("          {left: '$', right: '$', display: false}\n");
        html.append("        ]\n");
        html.append("      });\n");
        html.append("    });\n");
        html.append("  </script>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    /**
     * Generate CSS for article styling
     */
    private String generateCSS(Article article) {
        return """
            /* Vivliostyle CSS for Scientific Articles */
            
            @page {
              size: A4;
              margin: 25mm 20mm;
              
              @top-left {
                content: string(journal-name);
                font-size: 9pt;
                color: #666;
              }
              
              @top-right {
                content: string(article-title, first);
                font-size: 9pt;
                color: #666;
              }
              
              @bottom-center {
                content: counter(page);
                font-size: 10pt;
              }
            }
            
            /* Page breaks */
            @page:first {
              @top-left { content: none; }
              @top-right { content: none; }
            }
            
            /* Typography */
            html {
              font-family: 'Times New Roman', 'Liberation Serif', serif;
              font-size: 12pt;
              line-height: 1.6;
              color: #000;
            }
            
            body {
              margin: 0;
              padding: 0;
            }
            
            article {
              hyphens: auto;
            }
            
            /* Headers */
            h1 {
              font-size: 18pt;
              font-weight: bold;
              margin: 24pt 0 12pt 0;
              line-height: 1.3;
              page-break-after: avoid;
            }
            
            h2 {
              font-size: 14pt;
              font-weight: bold;
              margin: 18pt 0 9pt 0;
              page-break-after: avoid;
            }
            
            h3 {
              font-size: 12pt;
              font-weight: bold;
              margin: 12pt 0 6pt 0;
              page-break-after: avoid;
            }
            
            /* Article header */
            .article-header {
              margin-bottom: 24pt;
              border-bottom: 2pt solid #333;
              padding-bottom: 12pt;
            }
            
            .article-title {
              string-set: article-title content();
              font-size: 20pt;
              text-align: center;
              margin-bottom: 12pt;
            }
            
            .article-authors {
              text-align: center;
              font-size: 11pt;
              margin-bottom: 12pt;
            }
            
            .author {
              display: inline-block;
              margin: 0 6pt;
            }
            
            .article-doi {
              text-align: center;
              font-size: 10pt;
              color: #666;
              margin-bottom: 12pt;
            }
            
            .article-abstract {
              margin: 18pt 0;
              padding: 12pt;
              background: #f5f5f5;
              border-left: 3pt solid #333;
            }
            
            .article-abstract h2 {
              font-size: 12pt;
              margin-top: 0;
            }
            
            .article-keywords {
              font-size: 10pt;
              margin-top: 12pt;
            }
            
            /* Paragraphs */
            p {
              margin: 0 0 12pt 0;
              text-align: justify;
              text-indent: 1.5em;
            }
            
            p:first-of-type,
            h1 + p,
            h2 + p,
            h3 + p {
              text-indent: 0;
            }
            
            /* Lists */
            ul, ol {
              margin: 12pt 0;
              padding-left: 24pt;
            }
            
            li {
              margin-bottom: 6pt;
            }
            
            /* Tables */
            table {
              width: 100%;
              border-collapse: collapse;
              margin: 18pt 0;
              page-break-inside: avoid;
            }
            
            th, td {
              border: 1pt solid #333;
              padding: 6pt;
              text-align: left;
            }
            
            th {
              background: #f0f0f0;
              font-weight: bold;
            }
            
            /* Figures */
            figure {
              margin: 18pt 0;
              page-break-inside: avoid;
            }
            
            figure img {
              max-width: 100%;
              height: auto;
            }
            
            figcaption {
              margin-top: 6pt;
              font-size: 10pt;
              font-style: italic;
              text-align: center;
            }
            
            /* Math */
            .katex-display {
              margin: 18pt 0;
              text-align: center;
            }
            
            /* Code */
            code {
              font-family: 'Courier New', monospace;
              font-size: 10pt;
              background: #f5f5f5;
              padding: 2pt 4pt;
              border-radius: 2pt;
            }
            
            pre {
              background: #f5f5f5;
              padding: 12pt;
              border-radius: 4pt;
              overflow-x: auto;
              margin: 12pt 0;
              page-break-inside: avoid;
            }
            
            pre code {
              background: none;
              padding: 0;
            }
            
            /* References */
            .article-references {
              margin-top: 24pt;
              page-break-before: auto;
            }
            
            .references-list {
              list-style-type: decimal;
              padding-left: 24pt;
            }
            
            .references-list li {
              margin-bottom: 9pt;
              font-size: 10pt;
              line-height: 1.4;
            }
            
            /* Links */
            a {
              color: #0066cc;
              text-decoration: none;
            }
            
            a:hover {
              text-decoration: underline;
            }
            
            /* Blockquotes */
            blockquote {
              margin: 18pt 24pt;
              padding-left: 12pt;
              border-left: 3pt solid #ccc;
              font-style: italic;
              color: #666;
            }
            
            /* Emphasis */
            strong {
              font-weight: bold;
            }
            
            em {
              font-style: italic;
            }
            
            /* Page breaks */
            .page-break {
              page-break-after: always;
            }
            
            h1, h2, h3 {
              page-break-after: avoid;
            }
            
            table, figure {
              page-break-inside: avoid;
            }
            """;
    }
    
    /**
     * Convert ProseMirror JSON to HTML
     */
    private String convertPmJsonToHTML(String pmJson) {
        if (pmJson == null || pmJson.isEmpty()) {
            return "<p>No content</p>";
        }
        
        try {
            Map<String, Object> doc = objectMapper.readValue(pmJson, Map.class);
            return proseMirrorToHTML(doc);
        } catch (IOException e) {
            logger.error("Failed to parse ProseMirror JSON", e);
            return "<p>Error rendering content</p>";
        }
    }
    
    /**
     * Recursive conversion of ProseMirror nodes to HTML
     */
    @SuppressWarnings("unchecked")
    private String proseMirrorToHTML(Map<String, Object> node) {
        StringBuilder html = new StringBuilder();
        String type = (String) node.get("type");
        
        if (type == null) return "";
        
        switch (type) {
            case "doc":
                List<Map<String, Object>> content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    for (Map<String, Object> child : content) {
                        html.append(proseMirrorToHTML(child));
                    }
                }
                break;
                
            case "heading":
                Map<String, Object> attrs = (Map<String, Object>) node.get("attrs");
                int level = attrs != null ? ((Number) attrs.get("level")).intValue() : 1;
                html.append("<h").append(level).append(">");
                html.append(getTextContent(node));
                html.append("</h").append(level).append(">\n");
                break;
                
            case "paragraph":
                html.append("<p>").append(getTextContent(node)).append("</p>\n");
                break;
                
            case "bulletList":
                html.append("<ul>\n");
                content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    for (Map<String, Object> item : content) {
                        html.append("  <li>").append(getTextContent(item)).append("</li>\n");
                    }
                }
                html.append("</ul>\n");
                break;
                
            case "orderedList":
                html.append("<ol>\n");
                content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    for (Map<String, Object> item : content) {
                        html.append("  <li>").append(getTextContent(item)).append("</li>\n");
                    }
                }
                html.append("</ol>\n");
                break;
                
            case "codeBlock":
                html.append("<pre><code>");
                html.append(escapeHtml(getTextContent(node)));
                html.append("</code></pre>\n");
                break;
                
            case "blockquote":
                html.append("<blockquote>");
                html.append(getTextContent(node));
                html.append("</blockquote>\n");
                break;
                
            case "math_inline":
                attrs = (Map<String, Object>) node.get("attrs");
                if (attrs != null) {
                    String latex = (String) attrs.get("latex");
                    html.append("$").append(latex).append("$");
                }
                break;
                
            case "math_display":
                attrs = (Map<String, Object>) node.get("attrs");
                if (attrs != null) {
                    String latex = (String) attrs.get("latex");
                    html.append("<div class=\"math-display\">$$").append(latex).append("$$</div>\n");
                }
                break;
                
            case "horizontalRule":
                html.append("<hr />\n");
                break;
        }
        
        return html.toString();
    }
    
    /**
     * Get text content from node with marks
     */
    @SuppressWarnings("unchecked")
    private String getTextContent(Map<String, Object> node) {
        StringBuilder text = new StringBuilder();
        
        List<Map<String, Object>> content = (List<Map<String, Object>>) node.get("content");
        if (content != null) {
            for (Map<String, Object> child : content) {
                String type = (String) child.get("type");
                
                if ("text".equals(type)) {
                    String textValue = escapeHtml((String) child.get("text"));
                    List<Map<String, Object>> marks = (List<Map<String, Object>>) child.get("marks");
                    
                    if (marks != null && !marks.isEmpty()) {
                        for (Map<String, Object> mark : marks) {
                            String markType = (String) mark.get("type");
                            switch (markType) {
                                case "bold":
                                    textValue = "<strong>" + textValue + "</strong>";
                                    break;
                                case "italic":
                                    textValue = "<em>" + textValue + "</em>";
                                    break;
                                case "code":
                                    textValue = "<code>" + textValue + "</code>";
                                    break;
                            }
                        }
                    }
                    
                    text.append(textValue);
                } else {
                    text.append(getTextContent(child));
                }
            }
        }
        
        return text.toString();
    }
    
    /**
     * Format reference
     */
    private String formatReference(ArticleReference ref, String language) {
        StringBuilder formatted = new StringBuilder();
        
        // Get text in specified language or first available
        if (ref.getText() != null && !ref.getText().isEmpty()) {
            String text = ref.getText().getOrDefault(language, 
                ref.getText().values().iterator().next());
            formatted.append(escapeHtml(text));
        }
        
        if (ref.getDoi() != null && !ref.getDoi().isEmpty()) {
            formatted.append(" DOI: <a href=\"https://doi.org/")
                .append(ref.getDoi()).append("\">")
                .append(ref.getDoi()).append("</a>");
        }
        
        if (ref.getUrl() != null && !ref.getUrl().isEmpty()) {
            formatted.append(" <a href=\"").append(ref.getUrl())
                .append("\">").append(ref.getUrl()).append("</a>");
        }
        
        return formatted.toString();
    }
    
    /**
     * Run Vivliostyle CLI
     */
    private void runVivliostyle(Path htmlFile, Path cssFile, Path pdfFile) 
            throws IOException, InterruptedException {
        List<String> command = List.of(
            "vivliostyle",
            "build",
            htmlFile.toString(),
            "--style", cssFile.toString(),
            "--output", pdfFile.toString(),
            "--size", "A4"
        );
        
        logger.debug("Executing Vivliostyle command: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(htmlFile.getParent().toFile());
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        // Read output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug(line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            logger.error("Vivliostyle failed with exit code {}: {}", exitCode, output);
            throw new IOException("Vivliostyle export failed");
        }
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
    
    /**
     * Cleanup temporary directory
     */
    private void cleanupDirectory(Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete temp file: {}", path, e);
                        }
                    });
            }
        } catch (IOException e) {
            logger.warn("Failed to cleanup directory: {}", dir, e);
        }
    }
}

