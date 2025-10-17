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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for exporting articles to PDF using LaTeX/Pandoc pipeline
 */
@Service
public class LatexExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(LatexExportService.class);
    
    private final ObjectMapper objectMapper;
    
    @Value("${latex.engine:xelatex}")
    private String latexEngine;
    
    @Value("${latex.template.path:/app/templates/article-template.tex}")
    private String templatePath;
    
    @Value("${latex.temp.dir:/app/temp}")
    private String tempDir;
    
    public LatexExportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Export article to PDF using Pandoc + LaTeX
     */
    public byte[] exportToPDF(Article article, String language) throws IOException, InterruptedException {
        logger.info("Exporting article {} to PDF in language {}", article.getId(), language);
        
        // Create temp directory for this export
        Path workDir = Files.createTempDirectory(Path.of(tempDir), "latex-export-");
        
        try {
            // 1. Convert ProseMirror JSON to Markdown
            String markdown = convertPmJsonToMarkdown(article.getPmJson());
            
            // 2. Build YAML front matter
            String yaml = buildYamlFrontMatter(article, language);
            
            // 3. Combine YAML + Markdown
            String fullMarkdown = yaml + "\n\n" + markdown;
            
            // 4. Write markdown to file
            Path mdFile = workDir.resolve("article.md");
            Files.writeString(mdFile, fullMarkdown);
            
            // 5. Write bibliography to file if references exist
            Path bibFile = null;
            if (article.getReferences() != null && !article.getReferences().isEmpty()) {
                bibFile = workDir.resolve("references.bib");
                String bibtex = convertReferencesToBibTeX(article.getReferences());
                Files.writeString(bibFile, bibtex);
            }
            
            // 6. Convert Markdown to LaTeX using Pandoc
            Path texFile = workDir.resolve("article.tex");
            convertMarkdownToLatex(mdFile, texFile, bibFile);
            
            // 7. Compile LaTeX to PDF
            Path pdfFile = workDir.resolve("article.pdf");
            compileLatexToPdf(texFile, pdfFile, workDir);
            
            // 8. Read PDF bytes
            byte[] pdfBytes = Files.readAllBytes(pdfFile);
            
            logger.info("Successfully exported article {} to PDF ({} bytes)", article.getId(), pdfBytes.length);
            return pdfBytes;
            
        } finally {
            // Cleanup temp directory
            cleanupDirectory(workDir);
        }
    }
    
    /**
     * Export article to LaTeX source
     */
    public byte[] exportToLatex(Article article, String language) throws IOException, InterruptedException {
        logger.info("Exporting article {} to LaTeX source in language {}", article.getId(), language);
        
        Path workDir = Files.createTempDirectory(Path.of(tempDir), "latex-source-");
        
        try {
            String markdown = convertPmJsonToMarkdown(article.getPmJson());
            String yaml = buildYamlFrontMatter(article, language);
            String fullMarkdown = yaml + "\n\n" + markdown;
            
            Path mdFile = workDir.resolve("article.md");
            Files.writeString(mdFile, fullMarkdown);
            
            Path bibFile = null;
            if (article.getReferences() != null && !article.getReferences().isEmpty()) {
                bibFile = workDir.resolve("references.bib");
                String bibtex = convertReferencesToBibTeX(article.getReferences());
                Files.writeString(bibFile, bibtex);
            }
            
            Path texFile = workDir.resolve("article.tex");
            convertMarkdownToLatex(mdFile, texFile, bibFile);
            
            return Files.readAllBytes(texFile);
            
        } finally {
            cleanupDirectory(workDir);
        }
    }
    
    /**
     * Convert ProseMirror JSON to Markdown
     */
    private String convertPmJsonToMarkdown(String pmJson) {
        if (pmJson == null || pmJson.isEmpty()) {
            return "";
        }
        
        try {
            Map<String, Object> doc = objectMapper.readValue(pmJson, Map.class);
            return proseMirrorToMarkdown(doc);
        } catch (IOException e) {
            logger.error("Failed to parse ProseMirror JSON", e);
            return "";
        }
    }
    
    /**
     * Recursive conversion of ProseMirror nodes to Markdown
     */
    @SuppressWarnings("unchecked")
    private String proseMirrorToMarkdown(Map<String, Object> node) {
        StringBuilder markdown = new StringBuilder();
        String type = (String) node.get("type");
        
        if (type == null) return "";
        
        switch (type) {
            case "doc":
                List<Map<String, Object>> content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    for (Map<String, Object> child : content) {
                        markdown.append(proseMirrorToMarkdown(child));
                    }
                }
                break;
                
            case "heading":
                Map<String, Object> attrs = (Map<String, Object>) node.get("attrs");
                int level = attrs != null ? ((Number) attrs.get("level")).intValue() : 1;
                markdown.append("#".repeat(level)).append(" ");
                markdown.append(getTextContent(node)).append("\n\n");
                break;
                
            case "paragraph":
                markdown.append(getTextContent(node)).append("\n\n");
                break;
                
            case "bulletList":
                content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    for (Map<String, Object> item : content) {
                        markdown.append("- ").append(getTextContent(item)).append("\n");
                    }
                    markdown.append("\n");
                }
                break;
                
            case "orderedList":
                content = (List<Map<String, Object>>) node.get("content");
                if (content != null) {
                    int i = 1;
                    for (Map<String, Object> item : content) {
                        markdown.append(i++).append(". ").append(getTextContent(item)).append("\n");
                    }
                    markdown.append("\n");
                }
                break;
                
            case "codeBlock":
                markdown.append("```\n");
                markdown.append(getTextContent(node));
                markdown.append("\n```\n\n");
                break;
                
            case "blockquote":
                String quote = getTextContent(node);
                markdown.append("> ").append(quote.replace("\n", "\n> ")).append("\n\n");
                break;
                
            case "math_inline":
                attrs = (Map<String, Object>) node.get("attrs");
                if (attrs != null) {
                    String latex = (String) attrs.get("latex");
                    markdown.append("$").append(latex).append("$");
                }
                break;
                
            case "math_display":
                attrs = (Map<String, Object>) node.get("attrs");
                if (attrs != null) {
                    String latex = (String) attrs.get("latex");
                    markdown.append("\n$$\n").append(latex).append("\n$$\n\n");
                }
                break;
                
            case "table":
                markdown.append(convertTableToMarkdown((Map<String, Object>) node));
                break;
                
            case "horizontalRule":
                markdown.append("\n---\n\n");
                break;
        }
        
        return markdown.toString();
    }
    
    /**
     * Get text content from ProseMirror node
     */
    @SuppressWarnings("unchecked")
    private String getTextContent(Map<String, Object> node) {
        StringBuilder text = new StringBuilder();
        
        List<Map<String, Object>> content = (List<Map<String, Object>>) node.get("content");
        if (content != null) {
            for (Map<String, Object> child : content) {
                String type = (String) child.get("type");
                
                if ("text".equals(type)) {
                    String textValue = (String) child.get("text");
                    List<Map<String, Object>> marks = (List<Map<String, Object>>) child.get("marks");
                    
                    if (marks != null && !marks.isEmpty()) {
                        for (Map<String, Object> mark : marks) {
                            String markType = (String) mark.get("type");
                            switch (markType) {
                                case "bold":
                                    textValue = "**" + textValue + "**";
                                    break;
                                case "italic":
                                    textValue = "*" + textValue + "*";
                                    break;
                                case "code":
                                    textValue = "`" + textValue + "`";
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
     * Convert table to Markdown
     */
    private String convertTableToMarkdown(Map<String, Object> table) {
        // Simplified table conversion
        return "\n| Column 1 | Column 2 |\n|----------|----------|\n| Cell 1   | Cell 2   |\n\n";
    }
    
    /**
     * Build YAML front matter for Pandoc
     */
    private String buildYamlFrontMatter(Article article, String language) {
        StringBuilder yaml = new StringBuilder("---\n");
        
        // Title
        Map<String, ArticleMetadata> metadata = article.getMetadata();
        if (metadata != null && metadata.containsKey(language)) {
            ArticleMetadata langMetadata = metadata.get(language);
            if (langMetadata.getTitle() != null) {
                yaml.append("title: \"").append(escapeYaml(langMetadata.getTitle())).append("\"\n");
            }
        }
        
        // Authors
        // Note: Article has only authorIds, not names. Would need to fetch from User service
        // For now, skip authors
        
        // Abstract
        if (metadata != null && metadata.containsKey(language)) {
            ArticleMetadata langMetadata = metadata.get(language);
            
            if (langMetadata.getAnnotation() != null && !langMetadata.getAnnotation().isEmpty()) {
                yaml.append("abstract: |\n");
                String[] lines = langMetadata.getAnnotation().split("\n");
                for (String line : lines) {
                    yaml.append("  ").append(line).append("\n");
                }
            }
            
            // Keywords
            if (langMetadata.getKeywords() != null && !langMetadata.getKeywords().isEmpty()) {
                String[] keywords = langMetadata.getKeywords().split(",");
                yaml.append("keywords: [");
                yaml.append(String.join(", ", keywords));
                yaml.append("]\n");
            }
        }
        
        // DOI
        if (article.getDoi() != null && !article.getDoi().isEmpty()) {
            yaml.append("doi: \"").append(article.getDoi()).append("\"\n");
        }
        
        // Pages
        if (article.getFirstPage() != null) {
            yaml.append("first-page: ").append(article.getFirstPage()).append("\n");
        }
        if (article.getLastPage() != null) {
            yaml.append("last-page: ").append(article.getLastPage()).append("\n");
        }
        
        // Language
        yaml.append("lang: ").append(language.equals("ru") ? "russian" : "english").append("\n");
        
        // Document class
        yaml.append("documentclass: article\n");
        yaml.append("classoption: [12pt, a4paper]\n");
        yaml.append("geometry: margin=2.5cm\n");
        
        // Bibliography
        yaml.append("bibliography: references.bib\n");
        yaml.append("citation-style: gost-numeric\n");
        
        yaml.append("---\n");
        
        return yaml.toString();
    }
    
    /**
     * Escape special characters for YAML
     */
    private String escapeYaml(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"").replace("\n", " ");
    }
    
    /**
     * Convert references to BibTeX format
     */
    private String convertReferencesToBibTeX(List<ArticleReference> references) {
        StringBuilder bibtex = new StringBuilder();
        
        for (int i = 0; i < references.size(); i++) {
            ArticleReference ref = references.get(i);
            String id = ref.getId() != null ? ref.getId() : "ref" + (i + 1);
            
            bibtex.append("@article{").append(id).append(",\n");
            
            // Use first available language for title
            if (ref.getText() != null && !ref.getText().isEmpty()) {
                String firstText = ref.getText().values().iterator().next();
                bibtex.append("  note = {").append(firstText).append("},\n");
            }
            
            if (ref.getDoi() != null) {
                bibtex.append("  doi = {").append(ref.getDoi()).append("},\n");
            }
            
            if (ref.getUrl() != null) {
                bibtex.append("  url = {").append(ref.getUrl()).append("},\n");
            }
            
            bibtex.append("}\n\n");
        }
        
        return bibtex.toString();
    }
    
    /**
     * Convert Markdown to LaTeX using Pandoc
     */
    private void convertMarkdownToLatex(Path mdFile, Path texFile, Path bibFile) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("pandoc");
        command.add(mdFile.toString());
        command.add("-o");
        command.add(texFile.toString());
        command.add("--template=" + templatePath);
        command.add("--pdf-engine=" + latexEngine);
        
        if (bibFile != null && Files.exists(bibFile)) {
            command.add("--bibliography=" + bibFile.toString());
            command.add("--citeproc");
        }
        
        executeCommand(command, mdFile.getParent());
    }
    
    /**
     * Compile LaTeX to PDF
     */
    private void compileLatexToPdf(Path texFile, Path pdfFile, Path workDir) throws IOException, InterruptedException {
        // Use latexmk for automatic compilation with bibliography
        List<String> command = List.of(
            latexEngine,
            "-interaction=nonstopmode",
            "-output-directory=" + workDir.toString(),
            texFile.toString()
        );
        
        executeCommand(command, workDir);
        
        // Check if PDF was created
        if (!Files.exists(pdfFile)) {
            throw new IOException("PDF compilation failed - output file not created");
        }
    }
    
    /**
     * Execute system command
     */
    private void executeCommand(List<String> command, Path workingDir) throws IOException, InterruptedException {
        logger.debug("Executing command: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
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
            logger.error("Command failed with exit code {}: {}", exitCode, output);
            throw new IOException("Command execution failed: " + String.join(" ", command));
        }
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

