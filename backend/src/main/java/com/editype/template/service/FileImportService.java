package com.editype.template.service;

import com.editype.article.entity.Article;
import com.editype.article.entity.ArticleParagraph;
import com.editype.template.entity.DocumentTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for importing documents and archives as templates.
 * Supports: HTML, LaTeX, DOCX, ZIP archives.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileImportService {

    /**
     * Import a document file and convert it to paragraphs
     */
    public ImportResult importDocument(MultipartFile file) {
        log.info("Importing document: {}", file.getOriginalFilename());

        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return ImportResult.error("Invalid filename");
            }

            String ext = getFileExtension(filename);

            switch (ext.toLowerCase()) {
                case "html":
                case "htm":
                    return importHtmlDocument(file);
                case "tex":
                case "latex":
                    return importLatexDocument(file);
                case "zip":
                    return importZipArchive(file);
                case "docx":
                    return importDocxDocument(file);
                default:
                    return ImportResult.error("Unsupported file format: " + ext);
            }

        } catch (Exception e) {
            log.error("Error importing document", e);
            return ImportResult.error("Error importing document: " + e.getMessage());
        }
    }

    /**
     * Import HTML document
     */
    private ImportResult importHtmlDocument(MultipartFile file) throws Exception {
        log.info("Importing HTML document");

        String content = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        List<ArticleParagraph> paragraphs = parseHtmlToParagraphs(content);

        return ImportResult.success(paragraphs);
    }

    /**
     * Import LaTeX document
     */
    private ImportResult importLatexDocument(MultipartFile file) throws Exception {
        log.info("Importing LaTeX document");

        String content = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        List<ArticleParagraph> paragraphs = parseLatexToParagraphs(content);

        return ImportResult.success(paragraphs);
    }

    /**
     * Import ZIP archive (may contain LaTeX project or multiple files)
     */
    private ImportResult importZipArchive(MultipartFile file) throws Exception {
        log.info("Importing ZIP archive");

        List<ArticleParagraph> paragraphs = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    log.info("Processing ZIP entry: {}", entryName);

                    if (entryName.endsWith(".tex") || entryName.endsWith(".latex")) {
                        String content = new BufferedReader(
                                new InputStreamReader(zis, StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));

                        paragraphs.addAll(parseLatexToParagraphs(content));
                        break; // Use first LaTeX file found
                    }
                }
                zis.closeEntry();
            }
        }

        if (paragraphs.isEmpty()) {
            return ImportResult.error("No valid content found in ZIP archive");
        }

        return ImportResult.success(paragraphs);
    }

    /**
     * Import DOCX document
     * TODO: Implement DOCX parsing with Apache POI or similar library
     */
    private ImportResult importDocxDocument(MultipartFile file) throws Exception {
        log.warn("DOCX import not yet fully implemented");
        
        // Placeholder implementation
        // In production, use Apache POI to parse DOCX:
        // XWPFDocument document = new XWPFDocument(file.getInputStream());
        // for (XWPFParagraph para : document.getParagraphs()) { ... }

        return ImportResult.error("DOCX import requires Apache POI library. Please install dependencies.");
    }

    /**
     * Parse HTML content into paragraphs
     * Basic implementation - can be enhanced with Jsoup for better parsing
     */
    private List<ArticleParagraph> parseHtmlToParagraphs(String html) {
        List<ArticleParagraph> paragraphs = new ArrayList<>();

        // Remove HTML tags and split by double newlines
        String plainText = html.replaceAll("<[^>]+>", "");
        String[] blocks = plainText.split("\n\n");

        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                ArticleParagraph paragraph = ArticleParagraph.builder()
                        .id(UUID.randomUUID().toString())
                        .type("paragraph")
                        .content("<p>" + block.trim() + "</p>")
                        .build();
                paragraphs.add(paragraph);
            }
        }

        return paragraphs;
    }

    /**
     * Parse LaTeX content into paragraphs
     * Basic implementation - recognizes sections, paragraphs, equations
     */
    private List<ArticleParagraph> parseLatexToParagraphs(String latex) {
        List<ArticleParagraph> paragraphs = new ArrayList<>();

        String[] lines = latex.split("\n");
        StringBuilder currentBlock = new StringBuilder();
        boolean inEquation = false;
        boolean inTable = false;

        for (String line : lines) {
            line = line.trim();

            // Skip comments and document structure commands
            if (line.startsWith("%") || 
                line.startsWith("\\documentclass") ||
                line.startsWith("\\usepackage") ||
                line.startsWith("\\begin{document}") ||
                line.startsWith("\\end{document}")) {
                continue;
            }

            // Handle sections/subsections as headings
            if (line.startsWith("\\section{") || line.startsWith("\\subsection{")) {
                if (currentBlock.length() > 0) {
                    addParagraph(paragraphs, currentBlock.toString(), "paragraph");
                    currentBlock.setLength(0);
                }

                String title = extractLatexCommand(line);
                String type = line.startsWith("\\section{") ? "heading1" : "heading2";
                addParagraph(paragraphs, title, type);
                continue;
            }

            // Handle equations
            if (line.startsWith("\\begin{equation}") || line.startsWith("\\[")) {
                if (currentBlock.length() > 0) {
                    addParagraph(paragraphs, currentBlock.toString(), "paragraph");
                    currentBlock.setLength(0);
                }
                inEquation = true;
                continue;
            }

            if (line.startsWith("\\end{equation}") || line.startsWith("\\]")) {
                if (currentBlock.length() > 0) {
                    addParagraph(paragraphs, currentBlock.toString(), "formula");
                    currentBlock.setLength(0);
                }
                inEquation = false;
                continue;
            }

            // Handle tables
            if (line.startsWith("\\begin{table}") || line.startsWith("\\begin{tabular}")) {
                if (currentBlock.length() > 0) {
                    addParagraph(paragraphs, currentBlock.toString(), "paragraph");
                    currentBlock.setLength(0);
                }
                inTable = true;
                continue;
            }

            if (line.startsWith("\\end{table}") || line.startsWith("\\end{tabular}")) {
                if (currentBlock.length() > 0) {
                    addParagraph(paragraphs, currentBlock.toString(), "table");
                    currentBlock.setLength(0);
                }
                inTable = false;
                continue;
            }

            // Accumulate content
            if (!line.isEmpty()) {
                if (currentBlock.length() > 0) {
                    currentBlock.append(" ");
                }
                currentBlock.append(line);
            } else if (currentBlock.length() > 0) {
                // Empty line - end of paragraph
                String type = inEquation ? "formula" : (inTable ? "table" : "paragraph");
                addParagraph(paragraphs, currentBlock.toString(), type);
                currentBlock.setLength(0);
            }
        }

        // Add remaining content
        if (currentBlock.length() > 0) {
            addParagraph(paragraphs, currentBlock.toString(), "paragraph");
        }

        return paragraphs;
    }

    private void addParagraph(List<ArticleParagraph> paragraphs, String content, String type) {
        ArticleParagraph paragraph = ArticleParagraph.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .content("<p>" + content + "</p>")
                .build();
        paragraphs.add(paragraph);
    }

    private String extractLatexCommand(String line) {
        int start = line.indexOf("{");
        int end = line.lastIndexOf("}");
        if (start != -1 && end != -1) {
            return line.substring(start + 1, end);
        }
        return line;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Result of import operation
     */
    public static class ImportResult {
        private boolean success;
        private String error;
        private List<ArticleParagraph> paragraphs;

        public static ImportResult success(List<ArticleParagraph> paragraphs) {
            ImportResult result = new ImportResult();
            result.success = true;
            result.paragraphs = paragraphs;
            return result;
        }

        public static ImportResult error(String error) {
            ImportResult result = new ImportResult();
            result.success = false;
            result.error = error;
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public List<ArticleParagraph> getParagraphs() {
            return paragraphs;
        }
    }
}

